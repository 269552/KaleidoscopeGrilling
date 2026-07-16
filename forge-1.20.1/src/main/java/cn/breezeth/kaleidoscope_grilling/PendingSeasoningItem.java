package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PendingSeasoningItem extends BlockItem {
    public PendingSeasoningItem(Block block, Properties properties) { super(block, properties); }
    @Override public int getUseDuration(ItemStack stack) { return 80; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        return player != null && player.isShiftKeyDown() ? super.useOn(context) : InteractionResult.PASS;
    }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + 1, entity.getZ(),
                    12, 0.25, 0.35, 0.25, 0.05);
            level.playSound(null, entity.blockPosition(), ModSounds.ACTION_SUCCESS.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        ItemStack result = new ItemStack(ModItems.SPECIAL_SEASONING.get());
        SeasoningData.set(result, SeasoningData.get(stack));
        SeasoningData.setRandomVariant(result);
        return result;
    }
    @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        SeasoningTooltip.append(stack, tooltip);
        tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.shake_required").withStyle(ChatFormatting.YELLOW));
    }
}
