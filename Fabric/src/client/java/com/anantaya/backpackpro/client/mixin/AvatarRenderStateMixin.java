package com.anantaya.backpackpro.client.mixin;

import com.anantaya.backpackpro.client.render.BackpackRenderStateAccess;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements BackpackRenderStateAccess {

    @Unique
    private final ItemStackRenderState backpackpro$backpackRenderState = new ItemStackRenderState();

    @Override
    public ItemStackRenderState backpackpro$getBackpackRenderState() {
        return this.backpackpro$backpackRenderState;
    }
}