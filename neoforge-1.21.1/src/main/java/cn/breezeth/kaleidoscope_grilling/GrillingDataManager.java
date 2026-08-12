package cn.breezeth.kaleidoscope_grilling;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

public final class GrillingDataManager extends SimpleJsonResourceReloadListener {
  private static final Gson GSON = new Gson();
  private static final Map<String, Skewer> BUILT_IN_SKEWERS = loadBuiltInSkewers();
  private static final Map<String, String> BUILT_IN_KINDS = loadBuiltInKinds();

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

  static Skewer skewerForDisplay(String id) {
    Skewer loaded = SKEWERS.get(id);
    return loaded != null ? loaded : BUILT_IN_SKEWERS.get(id);
  }

  public static Map<String, Skewer> skewers() {
    return SKEWERS;
  }

  static Map<String, Skewer> skewersForDisplay() {
    Map<String, Skewer> loaded = SKEWERS;
    return loaded.isEmpty() ? BUILT_IN_SKEWERS : loaded;
  }

  public static long seasoningCount(List<String> values, String kind) {
    Map<String, String> loaded = KINDS;
    Map<String, String> kinds = loaded.isEmpty() ? BUILT_IN_KINDS : loaded;
    return values.stream().filter(id -> kind.equals(kinds.get(id))).count();
  }

  public static boolean isSeasoningIngredient(String id) {
    Map<String, String> loaded = KINDS;
    Map<String, String> kinds = loaded.isEmpty() ? BUILT_IN_KINDS : loaded;
    return kinds.containsKey(id);
  }

  @Override
  protected void apply(
      Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
    Map<String, Skewer> skewers = new HashMap<>();
    Map<String, String> kinds = new HashMap<>();
    files.values().forEach(json -> readRoot(json.getAsJsonObject(), skewers, kinds));
    SKEWERS = Map.copyOf(skewers);
    KINDS = Map.copyOf(kinds);
  }

  private static Map<String, Skewer> loadBuiltInSkewers() {
    var stream =
        GrillingDataManager.class.getResourceAsStream(
            "/data/kaleidoscope_grilling/grilling/skewers.json");
    if (stream == null) return Map.of();
    try (stream;
        var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      Map<String, Skewer> skewers = new HashMap<>();
      readRoot(GSON.fromJson(reader, JsonObject.class), skewers, new HashMap<>());
      return Map.copyOf(skewers);
    } catch (Exception ignored) {
      return Map.of();
    }
  }

  private static Map<String, String> loadBuiltInKinds() {
    var stream =
        GrillingDataManager.class.getResourceAsStream(
            "/data/kaleidoscope_grilling/grilling/skewers.json");
    if (stream == null) return Map.of();
    try (stream;
        var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      Map<String, String> kinds = new HashMap<>();
      readRoot(GSON.fromJson(reader, JsonObject.class), new HashMap<>(), kinds);
      return Map.copyOf(kinds);
    } catch (Exception ignored) {
      return Map.of();
    }
  }

  private static void readRoot(
      JsonObject root, Map<String, Skewer> skewers, Map<String, String> kinds) {
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
                object.has("cooked_result") ? object.get("cooked_result").getAsString() : "",
                object.has("effect") ? object.get("effect").getAsString() : "",
                object.has("effect_seconds") ? object.get("effect_seconds").getAsInt() : 0));
      }
    if (root.has("seasoning_effects"))
      for (JsonElement element : root.getAsJsonArray("seasoning_effects")) {
        JsonObject object = element.getAsJsonObject();
        kinds.put(object.get("ingredient").getAsString(), object.get("kind").getAsString());
      }
  }
}
