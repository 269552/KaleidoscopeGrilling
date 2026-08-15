package cn.breezeth.kaleidoscope_grilling.registry;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;

import cn.breezeth.kaleidoscope_grilling.food.FoodState;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ModAdvancements {
  private static final String EATEN_KEY = KaleidoscopeGrilling.MOD_ID + ":advancement_foods";
  private static final Set<String> FEAST_FOODS =
      Set.of(
          "grilled_beef_skewer",
          "grilled_pork_belly_skewer",
          "grilled_chicken_skin_skewer",
          "grilled_mid_wing_skewer",
          "grilled_squid_tentacle_skewer",
          "grilled_fish_skewer",
          "grilled_sweet_potato_sheet_skewer",
          "grilled_potato_slice_skewer",
          "grilled_caterpillar_skewer",
          "grilled_mushroom_skewer",
          "grilled_bun_slice_skewer",
          "grilled_ender_pearl_skewer",
          "grilled_meatball_skewer",
          "grilled_slime_skewer",
          "grilled_meat_and_bone_skewer",
          "grilled_fried_egg_skewer",
          "grilled_lamb_skewer",
          "grilled_golden_skewer",
          "ordinary_skewer",
          "cold_houttuynia",
          "sugared_tomato",
          "pepper_honey",
          "houttuynia_stir_fried_pork",
          "green_pepper_squid_tentacles",
          "braised_chicken_wings",
          "potato_beef_stew",
          "red_sweet_potato_porridge",
          "sour_spicy_noodles");

  public static void onPlayerTick(PlayerTickEvent.Post event) {
    Player player = event.getEntity();
    if (!(player instanceof ServerPlayer serverPlayer) || player.tickCount % 20 != 0) return;
    if (has(player, ModBlocks.GRILL_ITEM.get())) award(serverPlayer, "human_fireworks");
    if (ModItems.RAW_SKEWERS.stream().anyMatch(item -> has(player, item.get())))
      award(serverPlayer, "looking_the_part");
    if (has(player, ModItems.SKEWER_RECIPE_BOOK.get())) award(serverPlayer, "better_write_it_down");
    if (has(player, ModItems.CANOLA_SEEDS.get())) award(serverPlayer, "a_handful_of_canola");
    if (has(player, ModItems.OIL_RESIDUE.get())) award(serverPlayer, "strength_makes_oil");
    if (has(player, ModItems.SWEET_POTATO.get())) award(serverPlayer, "sweet_potato");
    if (player.level().dimension() == Level.NETHER && has(player, ModItems.HOUTTUYNIA.get()))
      award(serverPlayer, "nether_taste");
  }

  public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
    if (event.getEntity() instanceof ServerPlayer player
        && event.getPlacedBlock().is(ModBlocks.ADVANCED_RACK.get()))
      award(player, "neat_and_orderly");
  }

  public static void onPlayerClone(PlayerEvent.Clone event) {
    String eaten = event.getOriginal().getPersistentData().getString(EATEN_KEY);
    if (!eaten.isEmpty()) event.getEntity().getPersistentData().putString(EATEN_KEY, eaten);
  }

  public static void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
    recordFoodFinished(event.getEntity(), event.getItem());
  }

  public static void recordFoodFinished(LivingEntity entity, ItemStack stack) {
    if (!(entity instanceof ServerPlayer player)) return;
    ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
    if (!KaleidoscopeGrilling.MOD_ID.equals(id.getNamespace())) return;
    String path = id.getPath();
    if (FoodState.isHot(stack, player.level())
        && (path.startsWith("grilled_") || path.equals("secret_skewer")))
      award(player, "eat_it_hot");
    if (path.equals("raw_caterpillar_skewer")) award(player, "mental_preparation_failed");
    if (FEAST_FOODS.contains(path)) {
      Set<String> eaten = readEaten(player);
      if (eaten.add(path)) player.getPersistentData().putString(EATEN_KEY, String.join(",", eaten));
      if (eaten.containsAll(FEAST_FOODS)) award(player, "fireworks_feast");
    }
  }

  public static void skewerCompleted(Player player) {
    if (player instanceof ServerPlayer p) award(p, "looking_the_part");
  }

  public static void oiled(Player player) {
    if (player instanceof ServerPlayer p) award(p, "gleaming_with_oil");
  }

  public static void pepperPicked(Player player) {
    if (player instanceof ServerPlayer p) award(p, "mountain_fragrance");
  }

  public static void seasoningAdded(Player player, List<String> ingredients) {
    if (!(player instanceof ServerPlayer p)) return;
    if (ingredients.contains("kaleidoscope_grilling:green_chili_powder")
        && ingredients.contains("kaleidoscope_grilling:sichuan_pepper")
        && ingredients.contains("kaleidoscope_grilling:onion_powder"))
      award(p, "three_flavors_base");
    if (ingredients.size() >= 8) award(p, "world_in_a_bottle");
  }

  public static void seasoningFinished(LivingEntity entity, List<String> ingredients) {
    if (!(entity instanceof ServerPlayer player)) return;
    if (ingredients.contains("kaleidoscope_grilling:totem_powder")) award(player, "metallic_taste");
    if (ingredients.contains("kaleidoscope_grilling:dragon_egg_powder"))
      award(player, "taste_of_dragon");
  }

  public static void heavyMetalBlocked(LivingEntity entity) {
    if (entity instanceof ServerPlayer player) award(player, "metal_tolerance_failed");
  }

  public static void strongestShield(LivingEntity entity) {
    if (entity instanceof ServerPlayer player) award(player, "strongest_shield");
  }

  public static void strongestSpear(LivingEntity entity) {
    if (entity instanceof ServerPlayer player) award(player, "strongest_spear");
  }

  public static void weddingCandy(ServerPlayer player) {
    award(player, "wedding_candy");
  }

  private static boolean has(Player player, Item item) {
    return player.getInventory().contains(new ItemStack(item));
  }

  private static Set<String> readEaten(ServerPlayer player) {
    String value = player.getPersistentData().getString(EATEN_KEY);
    return value.isEmpty() ? new HashSet<>() : new HashSet<>(Arrays.asList(value.split(",")));
  }

  private static void award(ServerPlayer player, String path) {
    var advancement =
        player
            .server
            .getAdvancements()
            .get(ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, path));
    if (advancement != null) player.getAdvancements().award(advancement, "event");
  }

  private ModAdvancements() {}
}
