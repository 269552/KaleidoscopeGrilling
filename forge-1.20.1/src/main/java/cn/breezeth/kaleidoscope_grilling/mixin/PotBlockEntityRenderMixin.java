package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.PotOilAccess;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
    private void grilling$renderOil(PotBlockEntity pot, float partialTick, PoseStack pose, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        if (!(pot instanceof PotOilAccess access)) return;
        String oilType = access.grilling$getOilType();
        if (oilType.isEmpty()) return;

        int argb = switch (oilType) {
            case "canola" -> 0x80C08A24;
            case "secret_chili" -> 0x80FF4500;
            case "premium_chili" -> 0x808B0000;
            default -> 0x80FFD700;
        };
        int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF, a = (argb >> 24) & 0xFF;

        pose.pushPose();
        pose.translate(MIN, OIL_Y, MIN);

        VertexConsumer consumer = buffer.getBuffer(RenderType.translucent());
        var p = pose.last();
        consumer.vertex(p.pose(), 0, 0, SIZE).color(r, g, b, a).uv(0, 0).uv2(light).normal(p.normal(), 0, 1, 0).endVertex();
        consumer.vertex(p.pose(), SIZE, 0, SIZE).color(r, g, b, a).uv(1, 0).uv2(light).normal(p.normal(), 0, 1, 0).endVertex();
        consumer.vertex(p.pose(), SIZE, 0, 0).color(r, g, b, a).uv(1, 1).uv2(light).normal(p.normal(), 0, 1, 0).endVertex();
        consumer.vertex(p.pose(), 0, 0, 0).color(r, g, b, a).uv(0, 1).uv2(light).normal(p.normal(), 0, 1, 0).endVertex();

        pose.popPose();
    }
}
