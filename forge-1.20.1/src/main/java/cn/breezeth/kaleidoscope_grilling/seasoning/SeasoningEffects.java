package cn.breezeth.kaleidoscope_grilling.seasoning;

import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;


import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

public final class SeasoningEffects {
  private static final List<String> KINDS =
      List.of("speed", "strength", "duration", "totem", "vitality");

  public static List<Component> describe(List<String> values) {
    ArrayList<Component> result = new ArrayList<>();
    for (String kind : KINDS) {
      long count = GrillingDataManager.seasoningCount(values, kind);
      if (count > 0)
        result.add(Component.translatable("hud.kaleidoscope_grilling.seasoning." + kind, count));
    }
    long pepper = GrillingDataManager.seasoningCount(values, "numbness");
    if (pepper > 0)
      result.add(
          Component.translatable(
              "hud.kaleidoscope_grilling.seasoning."
                  + (pepper >= 4 ? "numbness" : "numbness_pending"),
              pepper));
    return result;
  }

  private SeasoningEffects() {}
}
