package cn.breezeth.kaleidoscope_grilling;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import org.slf4j.Logger;

public final class SkewerGuiIconCache {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final int BASE_SIZE = 32;
  private static final int CACHE_SIZE = 64;
  private static final int MAX_CUSTOM_ICONS = 64;
  private static final int FIXED_BAKES_PER_FRAME = 2;
  private static final int CUSTOM_BAKES_PER_FRAME = 1;
  // JVM fallback switch: -Dkaleidoscope_grilling.disableGuiIconCache=true
  private static final boolean JVM_DISABLED =
      Boolean.getBoolean("kaleidoscope_grilling.disableGuiIconCache");
  private static final Map<String, ResourceLocation> FIXED_CACHE = new HashMap<>();
  private static final LinkedHashMap<String, ResourceLocation> CUSTOM_CACHE =
      new LinkedHashMap<>(MAX_CUSTOM_ICONS, 0.75F, true);
  private static final Set<String> PENDING_FIXED = new LinkedHashSet<>();
  private static final Set<String> PENDING_CUSTOM = new LinkedHashSet<>();
  private static final Set<String> FAILED = new HashSet<>();
  private static long customTextureSequence;
  private static long budgetFrame = Long.MIN_VALUE;
  private static int fixedBudget;
  private static int customBudget;

  public static boolean render(GuiGraphics graphics, ItemStack stack, int x, int y) {
    if (!isEnabled() || SkewerItemRenderContext.isCapturing() || SkewerOutlineRender.isActive())
      return false;
    Minecraft minecraft = Minecraft.getInstance();
    boolean custom = isCompletedCustom(minecraft, stack);
    boolean fixed = SkewerRecipes.isRawSkewer(stack) || SkewerRecipes.isCookedSkewer(stack);
    if (!custom && !fixed) return false;
    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
    boolean cooked =
        SkewerRecipes.isCookedSkewer(stack)
            || stack.is(ModItems.SECRET_SKEWER.get()) && SecretSkewerItem.isCooked(stack);
    boolean hot = cooked && minecraft.level != null && FoodState.isHot(stack, minecraft.level);
    String state = cooked ? hot ? "hot" : "cooked" : "raw";
    int size = CACHE_SIZE;
    String key = custom ? customKey(minecraft, stack, itemId, state) : itemId + "/" + state;
    if (FAILED.contains(key)) return false;
    Map<String, ResourceLocation> cache = custom ? CUSTOM_CACHE : FIXED_CACHE;
    ResourceLocation texture = cache.get(key);
    if (texture == null) {
      Set<String> pending = custom ? PENDING_CUSTOM : PENDING_FIXED;
      if (pending.add(key)) {
        if (custom) trimPendingCustom();
        return false;
      }
      refreshBudgets(minecraft);
      if (custom ? customBudget <= 0 : fixedBudget <= 0) return false;
      if (custom) customBudget--;
      else fixedBudget--;
      pending.remove(key);
      ResourceLocation textureId =
          custom
              ? new ResourceLocation(
                  KaleidoscopeGrilling.MOD_ID, "skewer_gui_cache/custom_" + customTextureSequence++)
              : new ResourceLocation(
                  KaleidoscopeGrilling.MOD_ID,
                  "skewer_gui_cache/" + itemId.getPath() + "_" + state);
      texture = bake(graphics, stack, itemId, state, textureId, outlineColor(cooked, hot), size);
      if (texture == null) {
        FAILED.add(key);
        return false;
      }
      cache.put(key, texture);
      if (custom) trimCustomCache(minecraft);
    }
    graphics.blit(texture, x, y, 16, 16, 0.0F, 0.0F, size, size, size, size);
    return true;
  }

  public static boolean hasCachedIcon(ItemStack stack) {
    if (!isEnabled() || SkewerItemRenderContext.isCapturing()) return false;
    Minecraft minecraft = Minecraft.getInstance();
    if (!isCompletedCustom(minecraft, stack)) return false;
    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
    boolean cooked = stack.is(ModItems.SECRET_SKEWER.get()) && SecretSkewerItem.isCooked(stack);
    boolean hot = cooked && minecraft.level != null && FoodState.isHot(stack, minecraft.level);
    String state = cooked ? hot ? "hot" : "cooked" : "raw";
    return CUSTOM_CACHE.containsKey(customKey(minecraft, stack, itemId, state));
  }

  public static void clear() {
    Minecraft minecraft = Minecraft.getInstance();
    FIXED_CACHE.values().forEach(minecraft.getTextureManager()::release);
    CUSTOM_CACHE.values().forEach(minecraft.getTextureManager()::release);
    FIXED_CACHE.clear();
    CUSTOM_CACHE.clear();
    PENDING_FIXED.clear();
    PENDING_CUSTOM.clear();
    FAILED.clear();
    customTextureSequence = 0;
    budgetFrame = Long.MIN_VALUE;
    fixedBudget = 0;
    customBudget = 0;
  }

  private static ResourceLocation bake(
      GuiGraphics current,
      ItemStack stack,
      ResourceLocation itemId,
      String state,
      ResourceLocation textureId,
      int outlineRgb,
      int size) {
    Minecraft minecraft = Minecraft.getInstance();
    RenderTarget main = minecraft.getMainRenderTarget();
    TextureTarget target = null;
    NativeImage blackCapture = null;
    NativeImage whiteCapture = null;
    boolean projectionBackedUp = false;
    try {
      current.flush();
      target = new TextureTarget(size, size, true, Minecraft.ON_OSX);
      RenderSystem.backupProjectionMatrix();
      projectionBackedUp = true;
      RenderSystem.setProjectionMatrix(
          new Matrix4f()
              .setOrtho(0.0F, 16.0F, 16.0F, 0.0F, 1000.0F, ForgeHooksClient.getGuiFarPlane()),
          VertexSorting.ORTHOGRAPHIC_Z);

      blackCapture = capture(minecraft, target, stack, 0.0F);
      whiteCapture = capture(minecraft, target, stack, 1.0F);
      NativeImage transparent = recoverTransparency(blackCapture, whiteCapture, size);
      int visiblePixels = countVisiblePixels(transparent);
      if (visiblePixels == 0) {
        LOGGER.warn("Skewer GUI cache captured a blank icon for {} ({})", itemId, state);
        transparent.close();
        return null;
      }
      NativeImage outlined = outline(transparent, outlineRgb, Math.max(1, size / BASE_SIZE));
      transparent.close();
      blackCapture.close();
      blackCapture = null;
      whiteCapture.close();
      whiteCapture = null;

      DynamicTexture texture = new DynamicTexture(outlined);
      texture.setFilter(true, false);
      minecraft.getTextureManager().register(textureId, texture);
      LOGGER.info(
          "Cached skewer GUI icon {} ({}, {}x{}, {} visible pixels)",
          itemId,
          state,
          size,
          size,
          visiblePixels);
      return textureId;
    } catch (RuntimeException exception) {
      LOGGER.warn("Unable to cache skewer GUI icon {} ({})", itemId, state, exception);
      return null;
    } finally {
      if (blackCapture != null) blackCapture.close();
      if (whiteCapture != null) whiteCapture.close();
      if (projectionBackedUp) RenderSystem.restoreProjectionMatrix();
      if (target != null) target.destroyBuffers();
      main.bindWrite(true);
    }
  }

  private static NativeImage capture(
      Minecraft minecraft, TextureTarget target, ItemStack stack, float background) {
    target.setClearColor(background, background, background, 1.0F);
    target.clear(Minecraft.ON_OSX);
    target.bindWrite(true);
    SkewerItemRenderContext.pushCapture();
    try {
      GuiGraphics offscreen = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
      offscreen.renderItem(stack, 0, 0);
      offscreen.flush();
    } finally {
      SkewerItemRenderContext.popCapture();
    }
    return Screenshot.takeScreenshot(target);
  }

  private static NativeImage recoverTransparency(NativeImage black, NativeImage white, int size) {
    NativeImage result = new NativeImage(size, size, true);
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        int overBlack = black.getPixelRGBA(x, y);
        int overWhite = white.getPixelRGBA(x, y);
        int red = overBlack & 0xFF;
        int green = overBlack >>> 8 & 0xFF;
        int blue = overBlack >>> 16 & 0xFF;
        int redDifference = clamp((overWhite & 0xFF) - red);
        int greenDifference = clamp((overWhite >>> 8 & 0xFF) - green);
        int blueDifference = clamp((overWhite >>> 16 & 0xFF) - blue);
        int alpha = 255 - Math.max(redDifference, Math.max(greenDifference, blueDifference));
        if (alpha <= 2) {
          result.setPixelRGBA(x, y, 0);
          continue;
        }
        red = clamp((red * 255 + alpha / 2) / alpha);
        green = clamp((green * 255 + alpha / 2) / alpha);
        blue = clamp((blue * 255 + alpha / 2) / alpha);
        result.setPixelRGBA(x, y, alpha << 24 | blue << 16 | green << 8 | red);
      }
    }
    return result;
  }

  private static int clamp(int value) {
    return Math.max(0, Math.min(255, value));
  }

  private static NativeImage outline(NativeImage source, int rgb, int radius) {
    int width = source.getWidth();
    int height = source.getHeight();
    NativeImage result = new NativeImage(width, height, true);
    result.copyFrom(source);
    int abgr = 0xFF000000 | (rgb & 0xFF) << 16 | (rgb & 0xFF00) | (rgb >>> 16 & 0xFF);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (alpha(source, x, y) > 16) continue;
        if (hasVisibleNeighbor(source, x, y, radius)) {
          result.setPixelRGBA(x, y, abgr);
        }
      }
    }
    return result;
  }

  private static boolean hasVisibleNeighbor(NativeImage image, int x, int y, int radius) {
    for (int offsetY = -radius; offsetY <= radius; offsetY++) {
      for (int offsetX = -radius; offsetX <= radius; offsetX++) {
        if (Math.abs(offsetX) + Math.abs(offsetY) > radius) continue;
        if (alpha(image, x + offsetX, y + offsetY) > 16) return true;
      }
    }
    return false;
  }

  private static int alpha(NativeImage image, int x, int y) {
    if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) return 0;
    return image.getPixelRGBA(x, y) >>> 24;
  }

  private static int countVisiblePixels(NativeImage image) {
    int count = 0;
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        if (alpha(image, x, y) > 16) count++;
      }
    }
    return count;
  }

  private static int outlineColor(boolean cooked, boolean hot) {
    return hot ? 0xFF3B30 : cooked ? 0xFFFF33 : 0xFFFFFF;
  }

  private static String customKey(
      Minecraft minecraft, ItemStack stack, ResourceLocation itemId, String state) {
    StringBuilder key =
        new StringBuilder(itemId.toString())
            .append('/')
            .append(state)
            .append('/')
            .append(SkeweringHandler.modelState(stack))
            .append('/')
            .append(SecretSkewerItem.getVisualStage(stack));
    List<ItemStack> ingredients =
        minecraft.level == null
            ? SkewerRecipes.displayIngredients(stack)
            : SkeweringHandler.readIngredientStacks(stack);
    for (ItemStack ingredient : ingredients) {
      key.append('|')
          .append(ForgeRegistries.ITEMS.getKey(ingredient.getItem()))
          .append('@')
          .append(ingredient.hasTag() ? ingredient.getTag().hashCode() : 0);
    }
    return key.toString();
  }

  private static void trimCustomCache(Minecraft minecraft) {
    while (CUSTOM_CACHE.size() > MAX_CUSTOM_ICONS) {
      Map.Entry<String, ResourceLocation> eldest = CUSTOM_CACHE.entrySet().iterator().next();
      CUSTOM_CACHE.remove(eldest.getKey());
      minecraft.getTextureManager().release(eldest.getValue());
    }
  }

  private static boolean isCompletedCustom(Minecraft minecraft, ItemStack stack) {
    return stack.is(ModItems.SECRET_SKEWER.get())
        && minecraft.level != null
        && SkeweringHandler.readIngredientStacks(stack).size() >= 3;
  }

  private static void refreshBudgets(Minecraft minecraft) {
    long frame = minecraft.getFrameTimeNs();
    if (frame == budgetFrame) return;
    budgetFrame = frame;
    fixedBudget = FIXED_BAKES_PER_FRAME;
    customBudget = CUSTOM_BAKES_PER_FRAME;
  }

  private static void trimPendingCustom() {
    while (PENDING_CUSTOM.size() > MAX_CUSTOM_ICONS) {
      PENDING_CUSTOM.remove(PENDING_CUSTOM.iterator().next());
    }
  }

  private static boolean isEnabled() {
    return !JVM_DISABLED && HotFoodConfig.ENABLE_SKEWER_GUI_CACHE.get();
  }

  private SkewerGuiIconCache() {}
}
