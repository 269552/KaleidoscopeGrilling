package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class OilResidueItem extends BoneMealItem {
  public OilResidueItem(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    ItemStack stack = context.getItemInHand();
    if (context.getLevel().isClientSide) {
      InteractionResult first = super.useOn(context);
      InteractionResult second = super.useOn(context);
      return first.consumesAction() || second.consumesAction()
          ? InteractionResult.sidedSuccess(true)
          : InteractionResult.PASS;
    }

    int originalCount = stack.getCount();
    stack.grow(1);
    InteractionResult first = super.useOn(context);
    InteractionResult second = super.useOn(context);
    boolean succeeded = first.consumesAction() || second.consumesAction();
    boolean creative = context.getPlayer() != null && context.getPlayer().getAbilities().instabuild;
    stack.setCount(succeeded && !creative ? originalCount - 1 : originalCount);
    return succeeded ? InteractionResult.sidedSuccess(false) : InteractionResult.PASS;
  }
}
