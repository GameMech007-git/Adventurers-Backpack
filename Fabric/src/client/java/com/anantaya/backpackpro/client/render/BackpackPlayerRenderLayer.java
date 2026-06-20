package com.anantaya.backpackpro.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class BackpackPlayerRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    public BackpackPlayerRenderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            AvatarRenderState state,
            float yRot,
            float xRot
    ) {
        ItemStackRenderState backpackRenderState =
                ((BackpackRenderStateAccess) state).backpackpro$getBackpackRenderState();

        if (backpackRenderState.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        PlayerModel model = this.getParentModel();

        model.root().translateAndRotate(poseStack);
        model.body.translateAndRotate(poseStack);

        poseStack.translate(0F, 0.3F, 0.22F);
        poseStack.mulPose(Axis.YP.rotationDegrees(0F));
        poseStack.scale(0.82F, -0.82F, -0.82F);

        backpackRenderState.submit(
                poseStack,
                submitNodeCollector,
                lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor
        );

        poseStack.popPose();
    }
}