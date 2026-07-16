package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;

public final class SeasoningUse {
    public static boolean apply(Player player, InteractionHand hand, ItemStack seasoning, SeasonedPotAccess target) {
        var ingredients = SeasoningData.get(seasoning);
        if (ingredients.isEmpty()) {
            player.sendSystemMessage(Component.literal("[KG] 撒料失败：特制调料中没有材料。")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        target.grilling$setSeasoning(ingredients);
        SeasoningAnimation.start(player);
        player.level().playSound(null, player.blockPosition(), ModSounds.SEASON.get(), SoundSource.PLAYERS, 0.85F, 1.0F);
        player.sendSystemMessage(Component.literal("[KG] 撒料成功：调料效果已记录到当前厨具。")
                .withStyle(ChatFormatting.GREEN));
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
