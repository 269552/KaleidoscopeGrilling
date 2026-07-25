package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public final class KnifeDropHandler {
  private static final TagKey<Item> KNIVES =
      TagKey.create(
          Registries.ITEM,
          ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "kitchen_knife"));

  public static void onLivingDrops(LivingDropsEvent event) {
    if (!(event.getSource().getEntity() instanceof Player player)) return;
    ItemStack weapon = player.getMainHandItem();
    if (!weapon.is(KNIVES)) return;
    int looting =
        weapon.getEnchantmentLevel(
            player
                .level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING));
    if (event.getEntity().getType() == EntityType.CHICKEN) {
      drop(
          event,
          ModItems.CHICKEN_WING.get(),
          1 + player.getRandom().nextInt(2) + randomBonus(player, looting),
          1.0F);
    } else if (event.getEntity().getType() == EntityType.COW) {
      Item rawOffal =
          BuiltInRegistries.ITEM.get(
              ResourceLocation.fromNamespaceAndPath(
                  "kaleidoscope_cookery", "raw_cow_offal"));
      if (rawOffal != null)
        drop(
            event,
            rawOffal,
            1 + player.getRandom().nextInt(2) + randomBonus(player, looting),
            1.0F);
    } else if (event.getEntity().getType() == EntityType.SQUID) {
      drop(
          event,
          ModItems.SQUID_TENTACLE.get(),
          2 + player.getRandom().nextInt(2) + randomBonus(player, looting),
          0.5F);
    }
  }

  private static int randomBonus(Player p, int l) {
    return l <= 0 ? 0 : p.getRandom().nextInt(l + 1);
  }

  private static void drop(LivingDropsEvent e, Item item, int count, float chance) {
    if (e.getEntity().getRandom().nextFloat() < chance)
      e.getDrops()
          .add(
              new ItemEntity(
                  e.getEntity().level(),
                  e.getEntity().getX(),
                  e.getEntity().getY(),
                  e.getEntity().getZ(),
                  new ItemStack(item, count)));
  }

  private KnifeDropHandler() {}
}
