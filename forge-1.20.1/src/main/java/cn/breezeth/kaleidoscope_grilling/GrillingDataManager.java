package cn.breezeth.kaleidoscope_grilling;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

public final class GrillingDataManager extends SimpleJsonResourceReloadListener {
  private static final Gson GSON = new Gson();

  public record Skewer(
      List<List<String>> ingredients, String cookedResult, String effect, int effectSeconds) {}

  private static volatile Map<String, Skewer> SKEWERS = Map.of();
  private static volatile Map<String, String> KINDS = Map.of();

  public GrillingDataManager() {
    super(GSON, "grilling");
  }

  public static void register(AddReloadListenerEvent event) {
    event.addListener(new GrillingDataManager());
  }

  public static Skewer skewer(String id) {
    return SKEWERS.get(id);
  }

  public static Map<String, Skewer> skewers() {
    return SKEWERS;
  }

  public static long seasoningCount(List<String> values, String kind) {
    return values.stream().filter(id -> kind.equals(KINDS.get(id))).count();
  }

  @Override
  protected void apply(
      Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
    Map<String, Skewer> skewers = new HashMap<>();
    Map<String, String> kinds = new HashMap<>();
    files
        .values()
        .forEach(
            json -> {
              JsonObject root = json.getAsJsonObject();
              if (root.has("skewers"))
                for (JsonElement element : root.getAsJsonArray("skewers")) {
                  JsonObject object = element.getAsJsonObject();
                  List<List<String>> slots = new ArrayList<>();
                  for (JsonElement slot : object.getAsJsonArray("ingredients")) {
                    List<String> selectors = new ArrayList<>();
                    for (JsonElement selector : slot.getAsJsonArray())
                      selectors.add(selector.getAsString());
                    slots.add(List.copyOf(selectors));
                  }
                  skewers.put(
                      object.get("id").getAsString(),
                      new Skewer(
                          List.copyOf(slots),
                          object.has("cooked_result")
                              ? object.get("cooked_result").getAsString()
                              : "",
                          object.has("effect") ? object.get("effect").getAsString() : "",
                          object.has("effect_seconds")
                              ? object.get("effect_seconds").getAsInt()
                              : 0));
                }
              if (root.has("seasoning_effects"))
                for (JsonElement element : root.getAsJsonArray("seasoning_effects")) {
                  JsonObject object = element.getAsJsonObject();
                  kinds.put(
                      object.get("ingredient").getAsString(), object.get("kind").getAsString());
                }
            });
    SKEWERS = Map.copyOf(skewers);
    KINDS = Map.copyOf(kinds);
  }
}
