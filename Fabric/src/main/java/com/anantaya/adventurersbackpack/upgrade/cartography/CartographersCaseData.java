package com.anantaya.adventurersbackpack.upgrade.cartography;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public final class CartographersCaseData {

    private static final String ROOT_KEY = "CartographersCase";

    private static final String ACTIVE_SLOT_KEY = "ActiveSlot";

    private static final String DEATH_KEY = "Death";
    private static final String DEATH_DIMENSION_KEY = "Dimension";
    private static final String DEATH_X_KEY = "X";
    private static final String DEATH_Y_KEY = "Y";
    private static final String DEATH_Z_KEY = "Z";

    private static final String COMPASSES_KEY = "Compasses";
    private static final String SLOT_PREFIX = "Slot";

    private CartographersCaseData() {
    }

    public static int getActiveSlot(ItemStack backpackStack) {
        CompoundTag root = getCartographersCaseRoot(backpackStack);
        return root.getInt(ACTIVE_SLOT_KEY).orElse(CartographersCaseHelper.NO_ACTIVE_SLOT);
    }

    public static void setActiveSlot(ItemStack backpackStack, int activeSlot) {
        if (activeSlot != CartographersCaseHelper.NO_ACTIVE_SLOT
                && (activeSlot < CartographersCaseHelper.DEATH_SIGNAL_SLOT
                || activeSlot > CartographersCaseHelper.LAST_COMPASS_SLOT)) {
            activeSlot = CartographersCaseHelper.NO_ACTIVE_SLOT;
        }

        CompoundTag fullTag = getFullCustomTag(backpackStack);
        CompoundTag root = getCartographersCaseRoot(fullTag);

        root.putInt(ACTIVE_SLOT_KEY, activeSlot);
        fullTag.put(ROOT_KEY, root);

        saveFullCustomTag(backpackStack, fullTag);
    }

    public static void cycleActiveSlot(ItemStack backpackStack, int direction) {
        int activeSlot = getActiveSlot(backpackStack);


        int nextSlot = activeSlot + direction;

        if (nextSlot > CartographersCaseHelper.LAST_COMPASS_SLOT) {
            nextSlot = CartographersCaseHelper.NO_ACTIVE_SLOT;
        }

        if (nextSlot < CartographersCaseHelper.NO_ACTIVE_SLOT) {
            nextSlot = CartographersCaseHelper.LAST_COMPASS_SLOT;
        }

        setActiveSlot(backpackStack, nextSlot);
    }

    public static void saveDeathTarget(ServerPlayer player, ItemStack backpackStack) {
        BlockPos pos = player.blockPosition();
        String dimension = player.level().dimension().identifier().toString();

        CompoundTag fullTag = getFullCustomTag(backpackStack);
        CompoundTag root = getCartographersCaseRoot(fullTag);

        CompoundTag deathTag = new CompoundTag();
        deathTag.putString(DEATH_DIMENSION_KEY, dimension);
        deathTag.putInt(DEATH_X_KEY, pos.getX());
        deathTag.putInt(DEATH_Y_KEY, pos.getY());
        deathTag.putInt(DEATH_Z_KEY, pos.getZ());

        root.put(DEATH_KEY, deathTag);
        fullTag.put(ROOT_KEY, root);

        saveFullCustomTag(backpackStack, fullTag);
    }

    public static Optional<DeathTarget> getDeathTarget(ItemStack backpackStack) {
        CompoundTag root = getCartographersCaseRoot(backpackStack);

        Optional<CompoundTag> deathTagOptional = root.getCompound(DEATH_KEY);
        if (deathTagOptional.isEmpty()) {
            return Optional.empty();
        }

        CompoundTag deathTag = deathTagOptional.get();

        Optional<String> dimensionOptional = deathTag.getString(DEATH_DIMENSION_KEY);
        Optional<Integer> xOptional = deathTag.getInt(DEATH_X_KEY);
        Optional<Integer> yOptional = deathTag.getInt(DEATH_Y_KEY);
        Optional<Integer> zOptional = deathTag.getInt(DEATH_Z_KEY);

        if (dimensionOptional.isEmpty()
                || xOptional.isEmpty()
                || yOptional.isEmpty()
                || zOptional.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                new DeathTarget(
                        dimensionOptional.get(),
                        new BlockPos(
                                xOptional.get(),
                                yOptional.get(),
                                zOptional.get()
                        )
                )
        );
    }

    public static void clearDeathTarget(ItemStack backpackStack) {
        CompoundTag fullTag = getFullCustomTag(backpackStack);
        CompoundTag root = getCartographersCaseRoot(fullTag);

        root.remove(DEATH_KEY);
        fullTag.put(ROOT_KEY, root);

        saveFullCustomTag(backpackStack, fullTag);
    }

    public static ItemStack getCompass(ItemStack backpackStack, int compassInventoryIndex, HolderLookup.Provider registries) {
        if (compassInventoryIndex < 0 || compassInventoryIndex >= CartographersCaseHelper.LODESTONE_COMPASS_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }

        CompoundTag root = getCartographersCaseRoot(backpackStack);
        CompoundTag compassesTag = root.getCompound(COMPASSES_KEY).orElse(new CompoundTag());

        String key = SLOT_PREFIX + compassInventoryIndex;
        Optional<CompoundTag> itemTagOptional = compassesTag.getCompound(key);

        if (itemTagOptional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return ItemStack.CODEC
                .parse(
                        registries.createSerializationContext(NbtOps.INSTANCE),
                        itemTagOptional.get()
                )
                .result()
                .orElse(ItemStack.EMPTY);
    }

    public static boolean setCompass(
            ItemStack backpackStack,
            int compassInventoryIndex,
            ItemStack compassStack,
            HolderLookup.Provider registries
    ) {
        if (compassInventoryIndex < 0 || compassInventoryIndex >= CartographersCaseHelper.LODESTONE_COMPASS_SLOT_COUNT) {
            return false;
        }

        if (!compassStack.isEmpty() && !CartographersCaseHelper.isValidLodestoneCompass(compassStack)) {
            return false;
        }

        CompoundTag fullTag = getFullCustomTag(backpackStack);
        CompoundTag root = getCartographersCaseRoot(fullTag);
        CompoundTag compassesTag = root.getCompound(COMPASSES_KEY).orElse(new CompoundTag());

        String key = SLOT_PREFIX + compassInventoryIndex;

        if (compassStack.isEmpty()) {
            compassesTag.remove(key);
        } else {
            ItemStack stored = compassStack.copy();
            stored.setCount(1);

            ItemStack.CODEC
                    .encodeStart(
                            registries.createSerializationContext(NbtOps.INSTANCE),
                            stored
                    )
                    .result()
                    .ifPresent(tagData -> compassesTag.put(key, tagData));
        }

        root.put(COMPASSES_KEY, compassesTag);
        fullTag.put(ROOT_KEY, root);

        saveFullCustomTag(backpackStack, fullTag);
        return true;
    }

    public static Optional<Target> getTargetForActiveSlot(
            ItemStack backpackStack,
            HolderLookup.Provider registries
    ) {
        int activeSlot = getActiveSlot(backpackStack);

        if (activeSlot == CartographersCaseHelper.NO_ACTIVE_SLOT) {
            return Optional.empty();
        }

        if (activeSlot == CartographersCaseHelper.DEATH_SIGNAL_SLOT) {
            Optional<DeathTarget> deathTarget = getDeathTarget(backpackStack);
            return deathTarget.map(target -> new Target(
                    "Last Death",
                    target.dimension(),
                    target.pos()
            ));
        }

        if (!CartographersCaseHelper.isCompassSlotIndex(activeSlot)) {
            return Optional.empty();
        }

        int compassInventoryIndex = CartographersCaseHelper.toCompassInventoryIndex(activeSlot);
        ItemStack compass = getCompass(backpackStack, compassInventoryIndex, registries);

        return CartographersCaseHelper.getLodestoneTarget(compass)
                .map(globalPos -> new Target(
                        CartographersCaseHelper.getCompassDisplayName(compass, "Lodestone"),
                        globalPos.dimension().identifier().toString(),
                        globalPos.pos()
                ));
    }

    private static CompoundTag getFullCustomTag(ItemStack backpackStack) {
        CustomData customData = backpackStack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        );

        return customData.copyTag();
    }

    private static void saveFullCustomTag(ItemStack backpackStack, CompoundTag fullTag) {
        backpackStack.set(DataComponents.CUSTOM_DATA, CustomData.of(fullTag));
    }

    private static CompoundTag getCartographersCaseRoot(ItemStack backpackStack) {
        return getCartographersCaseRoot(getFullCustomTag(backpackStack));
    }

    private static CompoundTag getCartographersCaseRoot(CompoundTag fullTag) {
        return fullTag.getCompound(ROOT_KEY).orElse(new CompoundTag());
    }

    public record DeathTarget(
            String dimension,
            BlockPos pos
    ) {
    }

    public record Target(
            String name,
            String dimension,
            BlockPos pos
    ) {
    }
}