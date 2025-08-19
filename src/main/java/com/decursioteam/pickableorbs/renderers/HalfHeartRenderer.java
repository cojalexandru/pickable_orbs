package com.decursioteam.pickableorbs.renderers;

import com.decursioteam.pickableorbs.codec.ExtraOptions;
import com.decursioteam.pickableorbs.codec.OrbData;
import com.decursioteam.pickableorbs.entities.HalfHeartEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.Color;

@OnlyIn(Dist.CLIENT)
public class HalfHeartRenderer extends EntityRenderer<HalfHeartEntity> {

    private static final float SCALE = 0.3F;
    private static final float Y_OFFSET = 0.1F;
    private static final int LIGHT_ADJUSTMENT = 7;
    private static final float SHADOW_RADIUS = 0.15F;
    private static final float SHADOW_STRENGTH = 0.75F;
    private static final int SPRITE_SHEET_SIZE = 64;
    private static final int SPRITE_SIZE = 16;
    private static final int SPRITES_PER_ROW = 4;

    private final ResourceLocation halfHeartTexture;
    private final RenderType renderType;
    private final Color color;
    private final boolean animation;

    public HalfHeartRenderer(EntityRendererProvider.Context renderManager, OrbData orbData, ExtraOptions extraData) {
        super(renderManager);
        this.shadowRadius = SHADOW_RADIUS;
        this.shadowStrength = SHADOW_STRENGTH;

        this.halfHeartTexture = orbData.getTexture();
        this.renderType = RenderType.entityCutout(halfHeartTexture);
        this.color = parseColor(orbData.getColor());
        this.animation = extraData.getAnimation();
    }

    private static Color parseColor(String colorString) {
        if (!colorString.startsWith("#")) {
            colorString = "#" + colorString;
        }
        return Color.decode(colorString);
    }

    @Override
    protected int getBlockLightLevel(HalfHeartEntity entity, BlockPos pos) {
        return Mth.clamp(super.getBlockLightLevel(entity, pos) + LIGHT_ADJUSTMENT, 0, 15);
    }

    @Override
    public void render(HalfHeartEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        setupTransformation(matrixStack);

        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        PoseStack.Pose pose = matrixStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        // Calculate the normal based on camera orientation
        float cameraYaw = this.entityRenderDispatcher.camera.getYRot();
        float cameraPitch = this.entityRenderDispatcher.camera.getXRot();
        Vector3f normal = new Vector3f(0.0F, 0.0F, 1.0F);
        normal.rotate(Axis.YP.rotationDegrees(-cameraYaw));
        normal.rotate(Axis.XP.rotationDegrees(-cameraPitch));

        float animationProgress = ((float) entity.tickCount + partialTicks) / 2.0F;
        Color renderColor = animation ? getAnimatedColor(animationProgress) : color;

        int spriteIndex = (entity.tickCount / 2) % 16; // Change sprite every 2 ticks, cycle through all 16 sprites
        renderQuad(vertexConsumer, matrix4f, new Matrix3f().rotationXYZ((float) Math.toRadians(cameraPitch), (float) Math.toRadians(cameraYaw), 0.0F), renderColor, packedLight, spriteIndex);

        matrixStack.popPose();
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    private void setupTransformation(PoseStack matrixStack) {
        matrixStack.translate(0.0D, Y_OFFSET, 0.0D);
        matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        matrixStack.scale(SCALE, SCALE, SCALE);
    }

    private Color getAnimatedColor(float progress) {
        float factor = (Mth.sin(progress) + 1.0F) * 0.5F;
        return new Color(
                Math.round(factor * color.getRed()),
                Math.round(factor * color.getGreen()),
                Math.round(factor * color.getBlue()),
                animation ? 155 : 255
        );
    }

    private void renderQuad(VertexConsumer buffer, Matrix4f pose, Matrix3f normal, Color color, int packedLight, int spriteIndex) {
        float minU = (spriteIndex % SPRITES_PER_ROW) * SPRITE_SIZE / (float)SPRITE_SHEET_SIZE;
        float maxU = minU + SPRITE_SIZE / (float)SPRITE_SHEET_SIZE;
        float minV = (spriteIndex / SPRITES_PER_ROW) * SPRITE_SIZE / (float)SPRITE_SHEET_SIZE;
        float maxV = minV + SPRITE_SIZE / (float)SPRITE_SHEET_SIZE;

        vertex(buffer, pose, normal, -0.5F, -0.25F, color, minU, maxV, packedLight);
        vertex(buffer, pose, normal, 0.5F, -0.25F, color, maxU, maxV, packedLight);
        vertex(buffer, pose, normal, 0.5F, 0.75F, color, maxU, minV, packedLight);
        vertex(buffer, pose, normal, -0.5F, 0.75F, color, minU, minV, packedLight);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f pose, Matrix3f normal, float x, float y, Color color, float texU, float texV, int packedLight) {
        buffer.vertex(pose, x, y, 0.0F)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .uv(texU, texV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(HalfHeartEntity entity) {
        return halfHeartTexture;
    }
}