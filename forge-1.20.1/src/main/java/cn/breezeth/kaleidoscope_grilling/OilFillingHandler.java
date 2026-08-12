package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class OilFillingHandler {
  public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
    if (event.getHand() != InteractionHand.MAIN_HAND) return;
    ItemStack bucket = event.getEntity().getMainHandItem();
    ItemStack pot = event.getEntity().getOffhandItem();
    String type = type(bucket);
    if (type == null || !OilPotCompat.isOilPot(pot)) return;
    event.setCanceled(true);
    event.setCancellationResult(InteractionResult.SUCCESS);
    if (event.getLevel().isClientSide) return;
    String current = OilPotCompat.getType(pot);
    int count = OilPotCompat.getCount(pot);
    if (count > OilPotCompat.FLUID_CAPACITY - 8
        || (current.isEmpty() ? count > 0 : !current.equals(type))) {
      event
          .getEntity()
          .displayClientMessage(
              Component.translatable("message.kaleidoscope_grilling.oil_type_mismatch"), true);
      return;
    }
    OilPotCompat.fill(pot, type, 8);
    playPour(event, event.getEntity().blockPosition(), type, OilPotCompat.getCount(pot));
    consumeBucket(event);
  }

  public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    if (event.getHand() != InteractionHand.MAIN_HAND) return;
    ItemStack bucket = event.getItemStack();
    String type = type(bucket);
    var id =
        net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(
            event.getLevel().getBlockState(event.getPos()).getBlock());
    if (type == null
        || id == null
        || !id.toString().equals("kaleidoscope_cookery:oil_pot")
        || !(event.getLevel().getBlockEntity(event.getPos()) instanceof TypedOilPotAccess pot))
      return;
    event.setCanceled(true);
    event.setCancellationResult(InteractionResult.SUCCESS);
    if (event.getLevel().isClientSide) return;
    String current = pot.grilling$getOilType();
    int count = pot.grilling$getOilCount();
    if (count > OilPotCompat.FLUID_CAPACITY - 8
        || (current.isEmpty() ? count > 0 : !current.equals(type))) {
      event
          .getEntity()
          .displayClientMessage(
              Component.translatable("message.kaleidoscope_grilling.oil_type_mismatch"), true);
      return;
    }
    pot.grilling$setOilType(type);
    pot.grilling$setOilCount(pot.grilling$getOilCount() + 8);
    playPour(event, event.getPos(), type, pot.grilling$getOilCount());
    consumeBucket(event);
  }

  private static void playPour(
      PlayerInteractEvent event, net.minecraft.core.BlockPos pos, String type, int amount) {
    event
        .getLevel()
        .playSound(
            null,
            pos,
            "premium_chili".equals(type) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY,
            SoundSource.PLAYERS,
            0.9F,
            0.8F
                + 0.5F
                    * Math.min(OilPotCompat.FLUID_CAPACITY, amount)
                    / OilPotCompat.FLUID_CAPACITY);
  }

  private static void consumeBucket(PlayerInteractEvent event) {
    if (!event.getEntity().getAbilities().instabuild)
      event.getEntity().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
  }

  static String type(ItemStack stack) {
    if (stack.is(ModItems.CANOLA_OIL_BUCKET.get())) return "canola";
    if (stack.is(ModItems.SECRET_CHILI_OIL_BUCKET.get())) return "secret_chili";
    if (stack.is(ModItems.PREMIUM_CHILI_OIL_BUCKET.get())) return "premium_chili";
    return null;
  }

  private OilFillingHandler() {}
}
