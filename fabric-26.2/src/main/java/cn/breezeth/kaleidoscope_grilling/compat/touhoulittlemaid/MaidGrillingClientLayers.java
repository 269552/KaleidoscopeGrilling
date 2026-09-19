package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.GeckoEntityMaidRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.Mob;

public final class MaidGrillingClientLayers {
  public static void addVanilla(EntityMaidRenderer renderer, Context context) {
    renderer.addLayer(new MaidGrillingHeldItemLayer(renderer, context.getItemInHandRenderer()));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static void addGecko(GeckoEntityMaidRenderer<? extends Mob> renderer, Context context) {
    renderer.addGeoLayerRenderer(
        new MaidGrillingGeckoHeldItemLayer(renderer, context.getItemInHandRenderer()));
  }

  private MaidGrillingClientLayers() {}
}
