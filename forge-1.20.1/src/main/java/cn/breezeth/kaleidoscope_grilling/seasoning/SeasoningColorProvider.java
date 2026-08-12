package cn.breezeth.kaleidoscope_grilling.seasoning;

import cn.breezeth.kaleidoscope_grilling.mixin.SpriteContentsAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class SeasoningColorProvider {
  private static final int[] FALLBACK = {0xB86B45, 0xE0A56A};
  private static final Map<ResourceLocation, int[]> CACHE = new ConcurrentHashMap<>();

  public static int color(ItemStack bottle, int tintIndex) {
    if (tintIndex < 0) return -1;
    int layer = tintIndex / 2;
    List<String> ingredients = SeasoningData.get(bottle);
    if (layer >= ingredients.size()) return -1;
    ResourceLocation id = new ResourceLocation(ingredients.get(layer));
    Item item = ForgeRegistries.ITEMS.getValue(id);
    if (item == null) return 0xFF000000 | FALLBACK[tintIndex & 1];
    int[] colors = CACHE.computeIfAbsent(id, ignored -> sample(new ItemStack(item)));
    return 0xFF000000 | colors[tintIndex & 1];
  }

  public static void clearCache() {
    CACHE.clear();
  }

  private static int[] sample(ItemStack ingredient) {
    try {
      Minecraft minecraft = Minecraft.getInstance();
      BakedModel model = minecraft.getItemRenderer().getModel(ingredient, minecraft.level, null, 0);
      TextureAtlasSprite sprite = model.getParticleIcon();
      NativeImage image =
          ((SpriteContentsAccessor) (Object) sprite.contents()).grilling$getOriginalImage();
      Map<Integer, Integer> counts = new HashMap<>();
      int width = sprite.contents().width(), height = sprite.contents().height();
      for (int y = height / 4; y < height * 3 / 4; y++)
        for (int x = width / 4; x < width * 3 / 4; x++) {
          int abgr = image.getPixelRGBA(x, y);
          if ((abgr >>> 24) < 48) continue;
          int rgb = (abgr & 0xFF) << 16 | (abgr >>> 8 & 0xFF) << 8 | (abgr >>> 16 & 0xFF);
          counts.merge(rgb, 1, Integer::sum);
        }
      int tint = minecraft.getItemColors().getColor(ingredient, 0);
      int[] result =
          counts.entrySet().stream()
              .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
              .mapToInt(Map.Entry::getKey)
              .limit(2)
              .toArray();
      if (result.length == 0) return FALLBACK.clone();
      if (result.length == 1) result = new int[] {result[0], shade(result[0], 0.78F)};
      if (tint >= 0) {
        result[0] = multiply(result[0], tint);
        result[1] = multiply(result[1], tint);
      }
      return result;
    } catch (RuntimeException ignored) {
      return FALLBACK.clone();
    }
  }

  private static int multiply(int a, int b) {
    return ((a >> 16 & 255) * (b >> 16 & 255) / 255) << 16
        | ((a >> 8 & 255) * (b >> 8 & 255) / 255) << 8
        | (a & 255) * (b & 255) / 255;
  }

  private static int shade(int rgb, float f) {
    return (int) ((rgb >> 16 & 255) * f) << 16
        | (int) ((rgb >> 8 & 255) * f) << 8
        | (int) ((rgb & 255) * f);
  }

  private SeasoningColorProvider() {}
}
