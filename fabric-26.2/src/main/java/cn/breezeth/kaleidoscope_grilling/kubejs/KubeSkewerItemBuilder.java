package cn.breezeth.kaleidoscope_grilling.kubejs;

import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/** Item builder used by StartupEvents.registry('item', ...). */
public final class KubeSkewerItemBuilder extends ItemBuilder {
  private final boolean raw;
  private ResourceLocation effect;
  private int effectSeconds;
  private MultiBiteSkewerItem.AnimationProfile animation =
      MultiBiteSkewerItem.AnimationProfile.THREE;
  private boolean generatedModel = true;

  public KubeSkewerItemBuilder(ResourceLocation id, boolean raw) {
    super(id);
    this.raw = raw;
    useGeneratedSkewerModel();
  }

  public KubeSkewerItemBuilder effect(String id, int seconds) {
    ResourceLocation parsed = ResourceLocation.tryParse(id);
    if (parsed == null) throw new IllegalArgumentException("Invalid effect ID: " + id);
    effect = parsed;
    effectSeconds = Math.max(0, seconds);
    return this;
  }

  public KubeSkewerItemBuilder animation(String value) {
    try {
      animation =
          MultiBiteSkewerItem.AnimationProfile.valueOf(
              value.trim().toUpperCase(Locale.ROOT));
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException(
          "Animation must be one of ONE, TWO, THREE, THREE_ALT, THREE_RANDOM, or FOUR",
          exception);
    }
    return this;
  }

  public KubeSkewerItemBuilder modelSource(String value) {
    String normalized = value == null ? "generated" : value.trim().toLowerCase(Locale.ROOT);
    if (normalized.equals("auto") || normalized.equals("generated")) {
      useGeneratedSkewerModel();
    } else if (normalized.equals("provided")) {
      generatedModel = false;
      modelGenerator = null;
      parentModel = null;
    } else {
      throw new IllegalArgumentException(
          "modelSource must be 'auto', 'generated', or 'provided'");
    }
    return this;
  }

  private void useGeneratedSkewerModel() {
    generatedModel = true;
    parentModel = null;
    modelGenerator =
        model ->
            model.parent(
                ResourceLocation.fromNamespaceAndPath(
                    "kaleidoscope_grilling", "item/secret_skewer"));
  }

  @Override
  public Item createObject() {
    Item.Properties properties = createItemProperties();
    if (foodBuilder == null) {
      properties.food(
          new FoodProperties.Builder()
              .nutrition(raw ? 2 : 6)
              .saturationModifier(raw ? 0.2F : 0.6F)
              .build());
    }
    return new KubeSkewerItem(properties, effect, effectSeconds * 20, animation, generatedModel);
  }
}
