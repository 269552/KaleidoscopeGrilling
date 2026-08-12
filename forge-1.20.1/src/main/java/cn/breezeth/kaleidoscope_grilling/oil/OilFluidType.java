package cn.breezeth.kaleidoscope_grilling.oil;

import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

public final class OilFluidType extends FluidType {
  private final int tint;
  private final boolean lavaTextures;

  public OilFluidType(Properties properties, int tint) {
    this(properties, tint, false);
  }

  public OilFluidType(Properties properties, int tint, boolean lavaTextures) {
    super(properties);
    this.tint = tint;
    this.lavaTextures = lavaTextures;
  }

  @Override
  public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
    consumer.accept(
        new IClientFluidTypeExtensions() {
          private final ResourceLocation still =
              new ResourceLocation(
                  "minecraft", lavaTextures ? "block/lava_still" : "block/water_still");
          private final ResourceLocation flowing =
              new ResourceLocation(
                  "minecraft", lavaTextures ? "block/lava_flow" : "block/water_flow");

          @Override
          public ResourceLocation getStillTexture() {
            return still;
          }

          @Override
          public ResourceLocation getFlowingTexture() {
            return flowing;
          }

          @Override
          public int getTintColor() {
            return tint;
          }
        });
  }
}
