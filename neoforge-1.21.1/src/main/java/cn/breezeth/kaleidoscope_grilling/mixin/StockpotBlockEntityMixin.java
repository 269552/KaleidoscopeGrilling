package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity")
public abstract class StockpotBlockEntityMixin implements SeasonedPotAccess {
  @Shadow private ItemStack result;
  @Unique private List<String> grilling$seasoning = new ArrayList<>();

  @Override
  public void grilling$setSeasoning(List<String> values) {
    grilling$seasoning = new ArrayList<>(values);
  }

  @Override
  public List<String> grilling$getSeasoning() {
    return List.copyOf(grilling$seasoning);
  }

  @Inject(method = "addIngredient", at = @At("HEAD"), cancellable = true)
  private void grilling$acceptSeasoning(
      Level level, LivingEntity user, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
    if (!stack.is(ModItems.SPECIAL_SEASONING.get())) return;
    if (user instanceof Player player) SeasoningAnimation.start(player);
    grilling$seasoning = new ArrayList<>(SeasoningData.get(stack));
    if (user instanceof Player player && !player.getAbilities().instabuild) {
      int next = stack.getDamageValue() + 1;
      if (next >= stack.getMaxDamage()) {
        stack.setCount(0);
        player.setItemInHand(
            InteractionHand.MAIN_HAND, new ItemStack(ModItems.EMPTY_SEASONING_BOTTLE.get()));
      } else {
        stack.setDamageValue(next);
      }
    }
    if (!result.isEmpty()) {
      SeasoningData.set(result, grilling$seasoning);
      HotFoodApi.makeHot(result, level, 60);
    }
    cir.setReturnValue(true);
  }

  @Inject(method = "setRecipe", at = @At("TAIL"))
  private void grilling$seasonResult(Level level, CallbackInfo ci) {
    if (!grilling$seasoning.isEmpty() && !result.isEmpty()) {
      SeasoningData.set(result, grilling$seasoning);
      HotFoodApi.makeHot(result, level, 60);
    }
  }

  @Inject(method = "takeOutProduct", at = @At("HEAD"))
  private void grilling$refreshHotOnTakeout(
      Level level, LivingEntity user, ItemStack carrier, CallbackInfoReturnable<Boolean> cir) {
    if (result != null && !result.isEmpty()) {
      HotFoodApi.makeHot(result, level, 60);
      if (!grilling$seasoning.isEmpty()) SeasoningData.set(result, grilling$seasoning);
    }
  }

  @Inject(method = "saveAdditional", at = @At("TAIL"))
  private void grilling$save(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
    if (!grilling$seasoning.isEmpty()) {
      var list = new net.minecraft.nbt.ListTag();
      for (String s : grilling$seasoning) list.add(net.minecraft.nbt.StringTag.valueOf(s));
      tag.put("GrillingSeasoning", list);
    }
  }

  @Inject(method = "loadAdditional", at = @At("TAIL"))
  private void grilling$load(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
    grilling$seasoning.clear();
    if (tag.contains("GrillingSeasoning")) {
      var list = tag.getList("GrillingSeasoning", net.minecraft.nbt.Tag.TAG_STRING);
      for (int i = 0; i < list.size(); i++) grilling$seasoning.add(list.getString(i));
    }
  }
}
