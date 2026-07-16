package cn.breezeth.kaleidoscope_grilling;

import cn.breezeth.kaleidoscope_grilling.mixin.SpriteContentsAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SkewerColorProvider {
    private static final int COLORS_PER_FOOD = 6;
    private static final int FALLBACK = 0xB86B45;
    private static final Map<PaletteKey, int[]> CACHE = new ConcurrentHashMap<>();

    public static int color(ItemStack skewer, int tintIndex) {
        if (tintIndex < 0) return -1;
        int slot = tintIndex / 8;
        int colorIndex = tintIndex % 8;
        if (slot >= 3 || colorIndex >= COLORS_PER_FOOD) return -1;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return opaque(stageColor(skewer, FALLBACK));
        List<ItemStack> ingredients = SkeweringHandler.readIngredientStacks(skewer, minecraft.level.registryAccess());
        if (slot >= ingredients.size()) return -1;
        ItemStack ingredient = ingredients.get(slot);
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(ingredient.getItem());
        PaletteKey key = new PaletteKey(id, ingredient.getComponents().hashCode());
        if (CACHE.size() > 512) CACHE.clear();
        int[] palette = CACHE.computeIfAbsent(key, ignored -> sample(minecraft, ingredient));
        return opaque(stageColor(skewer, palette[colorIndex]));
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static int[] sample(Minecraft minecraft, ItemStack ingredient) {
        try {
            BakedModel model = minecraft.getItemRenderer().getModel(ingredient, minecraft.level, null, 0);
            TextureAtlasSprite sprite = model.getParticleIcon();
            NativeImage image = ((SpriteContentsAccessor) (Object) sprite.contents()).grilling$getOriginalImage();
            int width = sprite.contents().width();
            int height = sprite.contents().height();
            List<Integer> colors = collect(image, width, height, true);
            if (colors.isEmpty()) colors = collect(image, width, height, false);
            int itemTint = minecraft.getItemColors().getColor(ingredient, 0);
            return spread(colors, itemTint);
        } catch (RuntimeException ignored) {
            return spread(List.of(FALLBACK), -1);
        }
    }

    private static List<Integer> collect(NativeImage image, int width, int height, boolean middleOnly) {
        int minX = middleOnly ? width / 4 : 0;
        int maxX = middleOnly ? Math.max(minX + 1, width * 3 / 4) : width;
        int minY = middleOnly ? height / 4 : 0;
        int maxY = middleOnly ? Math.max(minY + 1, height * 3 / 4) : height;
        LinkedHashSet<Integer> unique = new LinkedHashSet<>();
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                int abgr = image.getPixelRGBA(x, y);
                if ((abgr >>> 24) < 48) continue;
                int rgb = (abgr & 0xFF) << 16 | (abgr >>> 8 & 0xFF) << 8 | (abgr >>> 16 & 0xFF);
                unique.add(rgb);
            }
        }
        return new ArrayList<>(unique);
    }

    private static int[] spread(List<Integer> colors, int itemTint) {
        if (colors.isEmpty()) colors = List.of(FALLBACK);
        int[] result = new int[COLORS_PER_FOOD];
        for (int i = 0; i < result.length; i++) {
            int index = result.length == 1 ? 0 : Math.round((colors.size() - 1) * i / (float) (result.length - 1));
            int color = colors.get(index);
            if (itemTint >= 0) color = multiply(color, itemTint);
            result[i] = shade(color, 1.08F - i * 0.055F);
        }
        return result;
    }

    private static int stageColor(ItemStack stack, int rgb) {
        int stage = SecretSkewerItem.getVisualStage(stack);
        if (stage == 0 && stack.is(ModItems.SECRET_SKEWER.get()) && SecretSkewerItem.isCooked(stack)) stage = 4;
        if (stage == 0) return rgb;
        if (stage == 1) return blend(rgb, 0xFFD06A, 0.16F, 1.08F);
        if (stage == 2) return blend(rgb, 0xB96A32, 0.22F, 0.98F);
        if (stage == 3) return blend(rgb, 0x9D4825, 0.34F, 0.90F);
        if (stage >= 5) return blend(rgb, 0x17110E, 0.82F, 0.48F);
        int r = Mth.clamp(Math.round(((rgb >>> 16) & 0xFF) * 0.78F + 42), 0, 255);
        int g = Mth.clamp(Math.round(((rgb >>> 8) & 0xFF) * 0.65F + 20), 0, 255);
        int b = Mth.clamp(Math.round((rgb & 0xFF) * 0.48F + 8), 0, 255);
        return r << 16 | g << 8 | b;
    }

    private static int blend(int rgb, int target, float amount, float brightness) {
        int r = Mth.clamp(Math.round((((rgb >>> 16) & 0xFF) * (1 - amount) + ((target >>> 16) & 0xFF) * amount) * brightness), 0, 255);
        int g = Mth.clamp(Math.round((((rgb >>> 8) & 0xFF) * (1 - amount) + ((target >>> 8) & 0xFF) * amount) * brightness), 0, 255);
        int b = Mth.clamp(Math.round(((rgb & 0xFF) * (1 - amount) + (target & 0xFF) * amount) * brightness), 0, 255);
        return r << 16 | g << 8 | b;
    }

    private static int multiply(int first, int second) {
        int r = ((first >>> 16) & 0xFF) * ((second >>> 16) & 0xFF) / 255;
        int g = ((first >>> 8) & 0xFF) * ((second >>> 8) & 0xFF) / 255;
        int b = (first & 0xFF) * (second & 0xFF) / 255;
        return r << 16 | g << 8 | b;
    }

    private static int shade(int rgb, float factor) {
        int r = Mth.clamp(Math.round(((rgb >>> 16) & 0xFF) * factor), 0, 255);
        int g = Mth.clamp(Math.round(((rgb >>> 8) & 0xFF) * factor), 0, 255);
        int b = Mth.clamp(Math.round((rgb & 0xFF) * factor), 0, 255);
        return r << 16 | g << 8 | b;
    }

    private static int opaque(int rgb) {
        return 0xFF000000 | rgb;
    }

    private record PaletteKey(ResourceLocation item, int componentsHash) {}

    private SkewerColorProvider() {}
}
