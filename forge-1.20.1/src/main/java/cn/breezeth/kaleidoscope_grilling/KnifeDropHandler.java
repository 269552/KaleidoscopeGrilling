package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class KnifeDropHandler {
  private static final TagKey<Item> KNIVES =
      TagKey.create(Registries.ITEM, new ResourceLocation("kaleidoscope_cookery", "kitchen_knife"));

  public static void onLivingDrops(LivingDropsEvent event) {
    if (!(event.getSource().getEntity() instanceof Player player)) return;
    ItemStack weapon = player.getMainHandItem();
    if (!weapon.is(KNIVES)) return;
    int looting = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, weapon);
    if (event.getEntity().getType() == EntityType.CHICKEN) {
      drop(
          event,
          ModItems.CHICKEN_WING.get(),
          1 + player.getRandom().nextInt(2) + randomBonus(player, looting),
          1.0F);
    } else if (event.getEntity().getType() == EntityType.COW) {
      Item rawOffal =
          ForgeRegistries.ITEMS.getValue(
              new ResourceLocation("kaleidoscope_cookery", "raw_cow_offal"));
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

  private static int randomBonus(Player player, int looting) {
    return looting <= 0 ? 0 : player.getRandom().nextInt(looting + 1);
  }

  private static void drop(LivingDropsEvent event, Item item, int count, float chance) {
    if (event.getEntity().getRandom().nextFloat() < chance)
      event
          .getDrops()
          .add(
              new ItemEntity(
                  event.getEntity().level(),
                  event.getEntity().getX(),
                  event.getEntity().getY(),
                  event.getEntity().getZ(),
                  new ItemStack(item, count)));
  }

  private KnifeDropHandler() {}
}
