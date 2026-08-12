package cn.breezeth.kaleidoscope_grilling.food;

import cn.breezeth.kaleidoscope_grilling.registry.ModItems;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class SweetPotatoPowderItem extends Item {
  public SweetPotatoPowderItem(Properties p) {
    super(p);
  }

  @Override
  public int getUseDuration(ItemStack s) {
    return 30;
  }

  @Override
  public UseAnim getUseAnimation(ItemStack s) {
    return UseAnim.BOW;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level l, Player p, InteractionHand h) {
    p.startUsingItem(h);
    return InteractionResultHolder.consume(p.getItemInHand(h));
  }

  @Override
  public ItemStack finishUsingItem(ItemStack s, Level l, LivingEntity e) {
    l.playSound(
        null, e.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.8F, 1.1F);
    return new ItemStack(ModItems.RAW_SWEET_POTATO_SHEET.get(), s.getCount());
  }

  @Override
  public void appendHoverText(ItemStack s, @Nullable Level l, List<Component> t, TooltipFlag f) {
    t.add(
        Component.translatable("tooltip.kaleidoscope_grilling.sweet_potato_powder.knead")
            .withStyle(ChatFormatting.GRAY));
  }
}
