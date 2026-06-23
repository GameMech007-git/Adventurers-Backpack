package com.anantaya.adventurersbackpack.client.mixin;

import com.anantaya.adventurersbackpack.backpack.BackpackItem;
import com.anantaya.adventurersbackpack.client.render.BackpackPlayerRenderLayer;
import com.anantaya.adventurersbackpack.client.render.BackpackRenderStateAccess;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;

import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity> {

    @Unique
    private ItemModelResolver adventurersbackpack$itemModelResolver;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void adventurersbackpack$addBackpackLayer(
            EntityRendererProvider.Context context,
            boolean slimSteve,
            CallbackInfo ci
    ) {
        this.adventurersbackpack$itemModelResolver = context.getItemModelResolver();

        ((LivingEntityRendererAccessor) (Object) this).adventurersbackpack$addLayer(
                new BackpackPlayerRenderLayer((AvatarRenderer<?>) (Object) this)
        );
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void adventurersbackpack$extractBackpackRenderState(
            AvatarlikeEntity entity,
            AvatarRenderState state,
            float partialTicks,
            CallbackInfo ci
    ) {
        BackpackRenderStateAccess access = (BackpackRenderStateAccess) state;

        access.adventurersbackpack$getBackpackRenderState().clear();

        if (!(entity instanceof Player player)) {
            return;
        }

        ItemStack backpackStack = ItemStack.EMPTY;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (!stack.isEmpty() && stack.getItem() instanceof BackpackItem) {
                backpackStack = stack;
                break;
            }
        }

        if (backpackStack.isEmpty()) {
            return;
        }

        this.adventurersbackpack$itemModelResolver.updateForLiving(
                access.adventurersbackpack$getBackpackRenderState(),
                backpackStack,
                ItemDisplayContext.FIXED,
                entity
        );
    }
}