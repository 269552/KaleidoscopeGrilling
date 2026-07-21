package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.OilPotVisualState;
import cn.breezeth.kaleidoscope_grilling.TypedOilPotAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.OilPotBlockEntity")
public abstract class OilPotBlockEntityMixin implements TypedOilPotAccess {
  @Unique private String grilling$oilType = "";

  @Shadow
  public abstract int getOilCount();

  @Shadow
  public abstract void setOilCount(int count);

  @Override
  public String grilling$getOilType() {
    return grilling$oilType;
  }

  @Override
  public void grilling$setOilType(String type) {
    grilling$oilType = type;
    grilling$updateVisualState();
    grilling$sync();
  }

  @Override
  public int grilling$getOilCount() {
    return getOilCount();
  }

  @Override
  public void grilling$setOilCount(int count) {
    setOilCount(count);
    grilling$sync();
  }

  @Unique
  private void grilling$updateVisualState() {
    BlockEntity be = (BlockEntity) (Object) this;
    if (be.getLevel() == null || !be.getBlockState().hasProperty(OilPotVisualState.OIL_TYPE))
      return;
    var visual = OilPotVisualState.fromId(grilling$oilType);
    if (be.getBlockState().getValue(OilPotVisualState.OIL_TYPE) != visual) {
      be.getLevel()
          .setBlock(
              be.getBlockPos(), be.getBlockState().setValue(OilPotVisualState.OIL_TYPE, visual), 3);
    }
  }

  @Unique
  private void grilling$sync() {
    BlockEntity be = (BlockEntity) (Object) this;
    be.setChanged();
    if (be.getLevel() != null && !be.getLevel().isClientSide) {
      be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
    }
  }

  @Inject(method = "saveAdditional", at = @At("TAIL"))
  private void grilling$save(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
    tag.putString("GrillingOilType", grilling$oilType);
  }

  @Inject(method = "loadAdditional", at = @At("TAIL"))
  private void grilling$load(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
    grilling$oilType = tag.getString("GrillingOilType");
  }
}
