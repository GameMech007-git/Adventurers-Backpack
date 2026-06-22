package com.anantaya.adventurersbackpack.client.mixin;

import com.anantaya.adventurersbackpack.client.render.BackpackRenderStateAccess;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements BackpackRenderStateAccess {

    @Unique
    private final ItemStackRenderState adventurersbackpack$backpackRenderState = new ItemStackRenderState();

    @Override
    public ItemStackRenderState adventurersbackpack$getBackpackRenderState() {
        return this.adventurersbackpack$backpackRenderState;
    }
}