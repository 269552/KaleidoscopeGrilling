package cn.breezeth.kaleidoscope_grilling.oil;

import cn.breezeth.kaleidoscope_grilling.registry.ModFluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public final class BigVatRenderer implements BlockEntityRenderer<BigVatBlockEntity> {
  private static final float MIN = 2.0F / 16.0F;
  private static final float SIZE = 12.0F / 16.0F;

  public BigVatRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public void render(
      BigVatBlockEntity vat,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int packedLight,
      int overlay) {
    FluidStack stack = vat.fluid();
    if (stack.isEmpty()) return;
    int level =
        Math.min(
            4,
            Math.max(
                1,
                (vat.amount() * 4 + BigVatBlockEntity.CAPACITY - 1) / BigVatBlockEntity.CAPACITY));
    float y = (2.0F + level * 3.0F) / 16.0F + 0.001F;
    IClientFluidTypeExtensions properties = IClientFluidTypeExtensions.of(stack.getFluid());
    ResourceLocation texture = properties.getStillTexture(stack);
    if (texture == null) return;
    TextureAtlasSprite sprite =
        Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
    int argb = properties.getTintColor(stack);
    int a = 255;
    int r = argb >> 16 & 255, g = argb >> 8 & 255, b = argb & 255;
    int light = stack.getFluid() == ModFluids.PREMIUM_SOURCE.get() ? 0xF000F0 : packedLight;
    float u0 = net.minecraft.util.Mth.lerp(1.0F / 16.0F, sprite.getU0(), sprite.getU1());
    float u1 = net.minecraft.util.Mth.lerp(15.0F / 16.0F, sprite.getU0(), sprite.getU1());
    float v0 = net.minecraft.util.Mth.lerp(1.0F / 16.0F, sprite.getV0(), sprite.getV1());
    float v1 = net.minecraft.util.Mth.lerp(15.0F / 16.0F, sprite.getV0(), sprite.getV1());

    pose.pushPose();
    pose.translate(MIN, y, MIN);
    VertexConsumer consumer =
        buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
    var p = pose.last();
    consumer
        .addVertex(p, 0, 0, SIZE)
        .setColor(r, g, b, a)
        .setUv(u0, v1)
        .setOverlay(overlay)
        .setLight(light)
        .setNormal(p, 0, 1, 0);
    consumer
        .addVertex(p, SIZE, 0, SIZE)
        .setColor(r, g, b, a)
        .setUv(u1, v1)
        .setOverlay(overlay)
        .setLight(light)
        .setNormal(p, 0, 1, 0);
    consumer
        .addVertex(p, SIZE, 0, 0)
        .setColor(r, g, b, a)
        .setUv(u1, v0)
        .setOverlay(overlay)
        .setLight(light)
        .setNormal(p, 0, 1, 0);
    consumer
        .addVertex(p, 0, 0, 0)
        .setColor(r, g, b, a)
        .setUv(u0, v0)
        .setOverlay(overlay)
        .setLight(light)
        .setNormal(p, 0, 1, 0);
    pose.popPose();
  }
}
