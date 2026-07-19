package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.PotOilAccess;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopecookery.client.render.block.PotBlockEntityRender", remap = false)
public abstract class PotBlockEntityRenderMixin {
    private static final float OIL_Y = 1.2F / 16.0F;
    private static final float MIN = 3.0F / 16.0F;
    private static final float SIZE = 10.0F / 16.0F;

    @Inject(method = "render", at = @At("TAIL"))
    private void grilling$renderOil(PotBlockEntity pot, float partialTick, PoseStack pose, MultiBufferSource buffer,
                                    int light, int overlay, CallbackInfo ci) {
        if (!(pot instanceof PotOilAccess access)) return;
        if (!pot.getBlockState().getValue(PotBlock.SHOW_OIL)) return;
        String oilType = access.grilling$getOilType();
        ResourceLocation texture = grilling$texture(oilType.isEmpty() ? "default" : oilType);
        if (texture == null) return;

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
        float u0 = Mth.lerp(2.0F / 16.0F, sprite.getU0(), sprite.getU1());
        float u1 = Mth.lerp(12.0F / 16.0F, sprite.getU0(), sprite.getU1());
        float v0 = Mth.lerp(3.0F / 16.0F, sprite.getV0(), sprite.getV1());
        float v1 = Mth.lerp(13.0F / 16.0F, sprite.getV0(), sprite.getV1());
        if ("premium_chili".equals(access.grilling$getOilType())) light = 0xF000F0;

        pose.pushPose();
        pose.translate(0.5F, OIL_Y, 0.5F);
        int rotation = switch (pot.getBlockState().getValue(PotBlock.FACING)) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        pose.translate(MIN - 0.5F, 0, MIN - 0.5F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        var p = pose.last();
        consumer.addVertex(p, 0, 0, SIZE).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(p, 0, 1, 0);
        consumer.addVertex(p, SIZE, 0, SIZE).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(p, 0, 1, 0);
        consumer.addVertex(p, SIZE, 0, 0).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(p, 0, 1, 0);
        consumer.addVertex(p, 0, 0, 0).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(p, 0, 1, 0);
        pose.popPose();
    }

    private static ResourceLocation grilling$texture(String type) {
        String path = switch (type) {
            case "default" -> "block/pot_oil_default";
            case "canola" -> "block/pot_oil_canola";
            case "secret_chili" -> "block/pot_oil_secret_chili";
            case "premium_chili" -> "block/pot_oil_premium_chili";
            default -> null;
        };
        return path == null ? null : ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, path);
    }
}
