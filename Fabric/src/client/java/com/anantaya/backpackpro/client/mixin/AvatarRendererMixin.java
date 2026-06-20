package com.anantaya.backpackpro.client.mixin;

import com.anantaya.backpackpro.backpack.BackpackItem;
import com.anantaya.backpackpro.client.render.BackpackPlayerRenderLayer;
import com.anantaya.backpackpro.client.render.BackpackRenderStateAccess;

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
    private ItemModelResolver backpackpro$itemModelResolver;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void backpackpro$addBackpackLayer(
            EntityRendererProvider.Context context,
            boolean slimSteve,
            CallbackInfo ci
    ) {
        this.backpackpro$itemModelResolver = context.getItemModelResolver();

        ((LivingEntityRendererAccessor) (Object) this).backpackpro$addLayer(
                new BackpackPlayerRenderLayer((AvatarRenderer<?>) (Object) this)
        );
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void backpackpro$extractBackpackRenderState(
            AvatarlikeEntity entity,
            AvatarRenderState state,
            float partialTicks,
            CallbackInfo ci
    ) {
        BackpackRenderStateAccess access = (BackpackRenderStateAccess) state;

        access.backpackpro$getBackpackRenderState().clear();

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

        this.backpackpro$itemModelResolver.updateForLiving(
                access.backpackpro$getBackpackRenderState(),
                backpackStack,
                ItemDisplayContext.FIXED,
                entity
        );
    }
}