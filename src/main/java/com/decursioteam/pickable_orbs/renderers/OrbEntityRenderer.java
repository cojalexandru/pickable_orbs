package com.decursioteam.pickable_orbs.renderers;

import com.decursioteam.pickable_orbs.codec.ExtraOptions;
import com.decursioteam.pickable_orbs.codec.OrbData;
import com.decursioteam.pickable_orbs.entities.OrbEntity;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.Color;

@OnlyIn(Dist.CLIENT)
public class OrbEntityRenderer extends EntityRenderer<OrbEntity> {

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

    public OrbEntityRenderer(EntityRendererProvider.Context renderManager, OrbData orbData, ExtraOptions extraData) {
        super(renderManager);
        this.shadowRadius = SHADOW_RADIUS;
        this.shadowStrength = SHADOW_STRENGTH;

        this.halfHeartTexture = orbData.getTexture();
        this.renderType = RenderType.entityTranslucent(halfHeartTexture);
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
    protected int getBlockLightLevel(OrbEntity entity, BlockPos pos) {
        return Mth.clamp(super.getBlockLightLevel(entity, pos) + LIGHT_ADJUSTMENT, 0, 15);
    }

    @Override
    public void render(OrbEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack,
            MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        setupTransformation(matrixStack);

        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        PoseStack.Pose pose = matrixStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        float cameraYaw = this.entityRenderDispatcher.camera.getYRot();
        float cameraPitch = this.entityRenderDispatcher.camera.getXRot();
        Vector3f normal = new Vector3f(0.0F, 0.0F, 1.0F);
        normal.rotate(Axis.YP.rotationDegrees(-cameraYaw));
        normal.rotate(Axis.XP.rotationDegrees(-cameraPitch));

        float animationProgress = ((float) entity.tickCount + partialTicks) / 2.0F;
        Color renderColor = animation ? getAnimatedColor(animationProgress) : color;

        int spriteIndex = (entity.tickCount / 2) % 16;
        renderQuad(vertexConsumer, matrix4f, new Matrix3f().rotationXYZ((float) Math.toRadians(cameraPitch),
                (float) Math.toRadians(cameraYaw), 0.0F), renderColor, packedLight, spriteIndex);

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
                animation ? 155 : 255);
    }

    private void renderQuad(VertexConsumer buffer, Matrix4f pose, Matrix3f normal, Color color, int packedLight,
            int spriteIndex) {
        float minU = (spriteIndex % SPRITES_PER_ROW) * SPRITE_SIZE / (float) SPRITE_SHEET_SIZE;
        float maxU = minU + SPRITE_SIZE / (float) SPRITE_SHEET_SIZE;
        float minV = (spriteIndex / SPRITES_PER_ROW) * SPRITE_SIZE / (float) SPRITE_SHEET_SIZE;
        float maxV = minV + SPRITE_SIZE / (float) SPRITE_SHEET_SIZE;

        vertex(buffer, pose, normal, -0.5F, -0.25F, color, minU, maxV, packedLight);
        vertex(buffer, pose, normal, 0.5F, -0.25F, color, maxU, maxV, packedLight);
        vertex(buffer, pose, normal, 0.5F, 0.75F, color, maxU, minV, packedLight);
        vertex(buffer, pose, normal, -0.5F, 0.75F, color, minU, minV, packedLight);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f pose, Matrix3f normal, float x, float y, Color color,
            float texU, float texV, int packedLight) {

        Vector3f transformedNormal = new Vector3f(0.0F, 1.0F, 0.0F);
        normal.transform(transformedNormal);

        buffer.addVertex(pose, x, y, 0.0F)
                .setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .setUv(texU, texV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(transformedNormal.x(), transformedNormal.y(), transformedNormal.z());
    }

    @Override
    public ResourceLocation getTextureLocation(OrbEntity entity) {
        return halfHeartTexture;
    }
}