package com.anantaya.adventurersbackpack.upgrade.foodpouch;

import com.anantaya.adventurersbackpack.backpack.BackpackInventory;
import com.anantaya.adventurersbackpack.backpack.BackpackItem;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeFinder;
import com.anantaya.adventurersbackpack.upgrade.BackpackUpgradeItem;
import com.anantaya.adventurersbackpack.upgrade.config.FoodPouchConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BackpackFoodPouchHelper {

    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final int EMPTY_WARNING_INTERVAL_TICKS = 600;
    private static final int EMPTY_WARNING_FOOD_LEVEL = 8;
    private static final float EMERGENCY_HEALTH = 8.0F;

    private BackpackFoodPouchHelper() {
    }

    public static void tick(ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }

        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        if (!player.getFoodData().needsFood()) {
            return;
        }

        RegistryAccess registryAccess = player.level().registryAccess();

        ItemStack backpackStack = BackpackUpgradeFinder.findBackpackWithUpgrade(
                player,
                BackpackUpgradeItem.Type.FOOD_POUCH,
                registryAccess
        );

        if (backpackStack.isEmpty()) {
            return;
        }

        if (!(backpackStack.getItem() instanceof BackpackItem backpackItem)) {
            return;
        }

        BackpackTier tier = backpackItem.getTier();

        BackpackInventory backpackInventory = new BackpackInventory(
                backpackStack,
                tier.totalSlots,
                registryAccess
        );

        ItemStack foodPouchUpgrade = BackpackUpgradeFinder.findUpgradeStack(
                backpackInventory,
                tier,
                BackpackUpgradeItem.Type.FOOD_POUCH
        );

        if (foodPouchUpgrade.isEmpty()) {
            return;
        }

        FoodPouchMode mode = FoodPouchConfig.getMode(foodPouchUpgrade);

        if (mode == FoodPouchMode.OFF) {
            return;
        }

        boolean ate = tryEatFromNormalStorage(
                player,
                backpackInventory,
                tier,
                mode
        );

        if (ate) {
            backpackInventory.saveToData();
            return;
        }

        boolean warningEnabled = FoodPouchConfig.shouldWarnWhenEmpty(foodPouchUpgrade);

        if (warningEnabled) {
            maybeWarnFoodPouchEmpty(player, backpackInventory, tier);
        }
    }

    private static boolean tryEatFromNormalStorage(
            ServerPlayer player,
            BackpackInventory backpackInventory,
            BackpackTier tier,
            FoodPouchMode mode
    ) {
        int foodLevel = player.getFoodData().getFoodLevel();
        int missingFood = 20 - foodLevel;

        if (missingFood <= 0) {
            return false;
        }

        boolean emergency = mode == FoodPouchMode.EMERGENCY
                && player.getHealth() <= EMERGENCY_HEALTH;

        FoodChoice bestChoice = null;

        for (int i = 0; i < tier.normalSlots; i++) {
            int slotIndex = tier.normalStart() + i;
            ItemStack foodStack = backpackInventory.getItem(slotIndex);

            if (!isAllowedFood(foodStack)) {
                continue;
            }

            FoodProperties food = foodStack.get(DataComponents.FOOD);

            if (food == null) {
                continue;
            }

            int nutrition = food.nutrition();

            if (!emergency && nutrition > missingFood) {
                continue;
            }

            FoodChoice choice = new FoodChoice(
                    slotIndex,
                    foodStack.copy(),
                    food,
                    Math.max(0, missingFood - nutrition),
                    nutrition,
                    foodStack.getCount()
            );

            if (isBetterChoice(choice, bestChoice, mode, emergency)) {
                bestChoice = choice;
            }
        }

        if (bestChoice == null) {
            return false;
        }

        player.getFoodData().eat(bestChoice.food());

        playFoodPouchEatingFeedback(player, bestChoice.stack());

        ItemStack updated = bestChoice.stack().copy();
        updated.consume(1, player);

        backpackInventory.setItem(bestChoice.slotIndex(), updated);

        return true;
    }

    private static void playFoodPouchEatingFeedback(
            ServerPlayer player,
            ItemStack foodStack
    ) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (player.isSilent()) {
            return;
        }

        serverLevel.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.GENERIC_EAT,
                SoundSource.PLAYERS,
                0.65F,
                0.9F + player.getRandom().nextFloat() * 0.2F
        );

        serverLevel.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.PLAYER_BURP,
                SoundSource.PLAYERS,
                0.35F,
                0.9F + player.getRandom().nextFloat() * 0.2F
        );

        serverLevel.sendParticles(
                new ItemParticleOption(
                        ParticleTypes.ITEM,
                        foodStack.getItem()
                ),
                player.getX(),
                player.getY() + 1.35D,
                player.getZ(),
                8,
                0.18D,
                0.12D,
                0.18D,
                0.03D
        );

        player.sendSystemMessage(
                Component.literal("Food Pouch ate ")
                        .append(foodStack.getHoverName()),
                true
        );
    }

    private static boolean isBetterChoice(
            FoodChoice choice,
            FoodChoice bestChoice,
            FoodPouchMode mode,
            boolean emergency
    ) {
        if (bestChoice == null) {
            return true;
        }

        if (mode == FoodPouchMode.LOW_VALUE || emergency) {
            if (choice.nutrition() != bestChoice.nutrition()) {
                return choice.nutrition() < bestChoice.nutrition();
            }

            return choice.stackCount() < bestChoice.stackCount();
        }

        if (choice.waste() != bestChoice.waste()) {
            return choice.waste() < bestChoice.waste();
        }

        if (choice.nutrition() != bestChoice.nutrition()) {
            return choice.nutrition() < bestChoice.nutrition();
        }

        return choice.stackCount() < bestChoice.stackCount();
    }

    private static void maybeWarnFoodPouchEmpty(
            ServerPlayer player,
            BackpackInventory backpackInventory,
            BackpackTier tier
    ) {
        if (player.getFoodData().getFoodLevel() > EMPTY_WARNING_FOOD_LEVEL) {
            return;
        }

        if (player.tickCount % EMPTY_WARNING_INTERVAL_TICKS != 0) {
            return;
        }

        if (hasAnyAllowedFood(backpackInventory, tier)) {
            return;
        }

        player.sendSystemMessage(
                Component.literal("Food Pouch is empty!")
                        .withStyle(ChatFormatting.YELLOW),
                true
        );
    }

    private static boolean hasAnyAllowedFood(
            BackpackInventory backpackInventory,
            BackpackTier tier
    ) {
        for (int i = 0; i < tier.normalSlots; i++) {
            int slotIndex = tier.normalStart() + i;

            if (isAllowedFood(backpackInventory.getItem(slotIndex))) {
                return true;
            }
        }

        return false;
    }

    private static boolean isAllowedFood(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        FoodProperties food = stack.get(DataComponents.FOOD);

        if (food == null) {
            return false;
        }

        if (stack.is(Items.ROTTEN_FLESH)) {
            return false;
        }

        if (stack.is(Items.SPIDER_EYE)) {
            return false;
        }

        if (stack.is(Items.PUFFERFISH)) {
            return false;
        }

        if (stack.is(Items.POISONOUS_POTATO)) {
            return false;
        }

        if (stack.is(Items.SUSPICIOUS_STEW)) {
            return false;
        }

        if (stack.is(Items.CHORUS_FRUIT)) {
            return false;
        }

        if (stack.is(Items.GOLDEN_APPLE)) {
            return false;
        }

        return !stack.is(Items.ENCHANTED_GOLDEN_APPLE);
    }

    private record FoodChoice(
            int slotIndex,
            ItemStack stack,
            FoodProperties food,
            int waste,
            int nutrition,
            int stackCount
    ) {
    }
}