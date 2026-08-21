package com.decursioteam.pickable_orbs.renderers;

import com.decursioteam.pickable_orbs.codec.ExtraOptions;
import com.decursioteam.pickable_orbs.codec.OrbData;
import com.decursioteam.pickable_orbs.datagen.Orbs;
import com.decursioteam.pickable_orbs.entities.OrbEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import java.awt.*;

public class OrbEntityRenderer extends EntityRenderer<OrbEntity, OrbEntityRenderer.OrbRenderState> {

    private static final float SCALE = 0.3F;
    private static final float Y_OFFSET = 0.1F;
    private static final int LIGHT_ADJUSTMENT = 7;
    private static final float SHADOW_RADIUS = 0.15F;
    private static final float SHADOW_STRENGTH = 0.75F;
    private static final int SPRITE_SHEET_SIZE = 64;
    private static final int SPRITE_SIZE = 16;
    private static final int SPRITES_PER_ROW = 4;

    public OrbEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        this.shadowRadius = SHADOW_RADIUS;
        this.shadowStrength = SHADOW_STRENGTH;
    }

    private static Color parseColor(String colorString) {
        if (colorString == null)
            return Color.WHITE;
        if (!colorString.startsWith("#")) {
            colorString = "#" + colorString;
        }
        return Color.decode(colorString);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, Color color,
            float texU, float texV, int packedLight) {

        buffer.addVertex(pose, x, y, 0.0F)
                .setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .setUv(texU, texV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public OrbRenderState createRenderState() {
        return new OrbRenderState();
    }

    @Override
    public void extractRenderState(OrbEntity entity, OrbRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tickCount = entity.tickCount;

        Orbs orbsData = entity.getOrbData();
        if (orbsData != null) {
            OrbData orbData = orbsData.getData();
            ExtraOptions extraData = orbsData.getExtraData();

            if (orbData != null && orbData.getTexture() != null) {
                state.renderType = RenderTypes.entityTranslucent(orbData.getTexture());
                state.color = parseColor(orbData.getColor());
            } else {
                state.renderType = null;
                state.color = Color.WHITE;
            }
            if (extraData != null) {
                state.animation = extraData.getAnimation();
            } else {
                state.animation = false;
            }
        } else {
            state.renderType = null;
            state.color = Color.WHITE;
            state.animation = false;
        }
    }

    @Override
    protected int getBlockLightLevel(OrbEntity entity, BlockPos pos) {
        return Mth.clamp(super.getBlockLightLevel(entity, pos) + LIGHT_ADJUSTMENT, 0, 15);
    }

    @Override
    public void submit(OrbRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        if (state.renderType == null)
            return;

        poseStack.pushPose();

        poseStack.translate(0.0F, Y_OFFSET, 0.0F);
        poseStack.mulPose(camera.orientation);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(SCALE, SCALE, SCALE);

        float animationProgress = state.ageInTicks / 3.0F;
        Color renderColor = state.animation ? getAnimatedColor(animationProgress, state.color) : state.color;
        int spriteIndex = (state.tickCount / 2) % 16;

        submitNodeCollector.submitCustomGeometry(poseStack, state.renderType, (pose, buffer) -> {
            renderQuad(buffer, pose, renderColor, 15728880, spriteIndex);
        });

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    private Color getAnimatedColor(float progress, Color baseColor) {
        float factor = (Mth.sin(progress) + 1.0F) * 0.5F;
        int alpha = (int) (100 + factor * 155);
        return new Color(
                baseColor.getRed(),
                baseColor.getGreen(),
                baseColor.getBlue(),
                alpha);
    }

    private void renderQuad(VertexConsumer buffer, PoseStack.Pose pose, Color color, int packedLight, int spriteIndex) {
        float minU = (spriteIndex % SPRITES_PER_ROW) * SPRITE_SIZE / (float) SPRITE_SHEET_SIZE;
        float maxU = minU + SPRITE_SIZE / (float) SPRITE_SHEET_SIZE;
        float minV = (spriteIndex / SPRITES_PER_ROW) * SPRITE_SIZE / (float) SPRITE_SHEET_SIZE;
        float maxV = minV + SPRITE_SIZE / (float) SPRITE_SHEET_SIZE;

        vertex(buffer, pose, -0.5F, -0.25F, color, minU, maxV, packedLight);
        vertex(buffer, pose, 0.5F, -0.25F, color, maxU, maxV, packedLight);
        vertex(buffer, pose, 0.5F, 0.75F, color, maxU, minV, packedLight);
        vertex(buffer, pose, -0.5F, 0.75F, color, minU, minV, packedLight);
    }

    public static class OrbRenderState extends EntityRenderState {
        public int tickCount;
        public RenderType renderType;
        public Color color;
        public boolean animation;
    }
}