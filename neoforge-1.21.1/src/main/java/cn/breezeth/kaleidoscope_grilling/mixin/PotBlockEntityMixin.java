package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.List;

@Mixin(targets="com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity")
public abstract class PotBlockEntityMixin implements SeasonedPotAccess, PotOilAccess, PotHudAccess {
    @Shadow private ItemStack result;
    @Shadow private net.minecraft.core.NonNullList<ItemStack> inputs;
    @Shadow private int status;
    @Shadow public abstract boolean hasHeatSource(Level level);
    @Unique private List<String> grilling$seasoning = new ArrayList<>();
    @Unique private String grilling$oilType = "";
    @Unique private String grilling$pendingOilType = "default";
    @Override public void grilling$setSeasoning(List<String> values){grilling$seasoning=new ArrayList<>(values);}
    @Override public List<String> grilling$getSeasoning(){return List.copyOf(grilling$seasoning);}
    @Override public String grilling$getOilType(){return grilling$oilType;}
    @Override public int grilling$getStatus(){return status;}
    @Override public List<ItemStack> grilling$getInputs(){return inputs.stream().map(ItemStack::copy).toList();}
    @Override public boolean grilling$hasHeatSource(Level level){return hasHeatSource(level);}
    @Inject(method="addIngredient",at=@At("HEAD"),cancellable=true)
    private void grilling$accept(Level level, LivingEntity user, ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        if(!stack.is(ModItems.SPECIAL_SEASONING.get()))return;
        if(user instanceof Player player)SeasoningAnimation.start(player);
        grilling$setSeasoning(SeasoningData.get(stack));
        if(!(user instanceof Player p)||!p.getAbilities().instabuild){int next=stack.getDamageValue()+1;if(next>=stack.getMaxDamage()){stack.setCount(0);if(user instanceof Player player)player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(ModItems.EMPTY_SEASONING_BOTTLE.get()));}else stack.setDamageValue(next);}
        cir.setReturnValue(true);
    }
    @Inject(method="onPlaceOil",at=@At("HEAD"))
    private void grilling$captureOilBeforeConsumption(Level level,LivingEntity entity,ItemStack stack,CallbackInfoReturnable<Boolean> cir){
        String stored=OilPotCompat.getType(stack);
        if(!stored.isEmpty())grilling$pendingOilType=stored;
        else if(stack.is(ModItems.CANOLA_OIL_BUCKET.get()))grilling$pendingOilType="canola";
        else if(stack.is(ModItems.SECRET_CHILI_OIL_BUCKET.get()))grilling$pendingOilType="secret_chili";
        else if(stack.is(ModItems.PREMIUM_CHILI_OIL_BUCKET.get()))grilling$pendingOilType="premium_chili";
        else grilling$pendingOilType="default";
    }
    @Inject(method="onPlaceOil",at=@At("RETURN"))
    private void grilling$oil(Level level,LivingEntity entity,ItemStack stack,CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValueZ()){
            grilling$oilType=grilling$pendingOilType;
        }
        grilling$pendingOilType="default";
    }
    @Inject(method="startCooking",at=@At("TAIL")) private void grilling$season(Level level,CallbackInfo ci){
        if(!grilling$seasoning.isEmpty()&&!result.isEmpty())SeasoningData.set(result,grilling$seasoning);
        if(!grilling$oilType.isEmpty()&&!result.isEmpty()){
            int seconds=switch(grilling$oilType){case"secret_chili"->600;case"premium_chili"->1200;default->60;};
            FoodState.setHot(result,level.getGameTime()+seconds*20L);
        }
    }

    @Inject(method="takeOutProduct",at=@At("HEAD"))
    private void grilling$hotOnTakeout(Level level,LivingEntity user,ItemStack stack,CallbackInfoReturnable<Boolean> cir){
        if(!result.isEmpty()){
            int seconds=switch(grilling$oilType){case"secret_chili"->600;case"premium_chili"->1200;default->60;};
            HotFoodApi.makeHot(result,level,seconds);
            if(!grilling$seasoning.isEmpty())SeasoningData.set(result,grilling$seasoning);
        }
    }
    @Inject(method="reset",at=@At("TAIL")) private void grilling$reset(CallbackInfo ci){grilling$seasoning.clear();grilling$oilType="";grilling$pendingOilType="default";}
    @Inject(method="saveAdditional",at=@At("TAIL")) private void grilling$save(CompoundTag tag,HolderLookup.Provider provider,CallbackInfo ci){tag.putString("GrillingSeasoning",String.join("\n",grilling$seasoning));tag.putString("GrillingOilType",grilling$oilType);}
    @Inject(method="loadAdditional",at=@At("TAIL")) private void grilling$load(CompoundTag tag,HolderLookup.Provider provider,CallbackInfo ci){grilling$seasoning=tag.getString("GrillingSeasoning").isEmpty()?new ArrayList<>():new ArrayList<>(List.of(tag.getString("GrillingSeasoning").split("\n")));grilling$oilType=tag.getString("GrillingOilType");}
}
