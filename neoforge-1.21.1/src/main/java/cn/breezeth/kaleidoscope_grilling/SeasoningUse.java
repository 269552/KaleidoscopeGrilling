package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SeasoningUse {
  public static boolean apply(
      Player player, InteractionHand hand, ItemStack seasoning, SeasonedPotAccess target) {
    var ingredients = SeasoningData.get(seasoning);
    if (ingredients.isEmpty()) return false;
    target.grilling$setSeasoning(ingredients);
    SeasoningAnimation.start(player);
    player
        .level()
        .playSound(
            null, player.blockPosition(), ModSounds.SEASON.get(), SoundSource.PLAYERS, 0.85F, 1.0F);
    if (player.getAbilities().instabuild) return true;
    int next = seasoning.getDamageValue() + 1;
    if (next >= seasoning.getMaxDamage()) {
      player.setItemInHand(hand, new ItemStack(ModItems.EMPTY_SEASONING_BOTTLE.get()));
    } else {
      seasoning.setDamageValue(next);
    }
    return true;
  }

  private SeasoningUse() {}
}
