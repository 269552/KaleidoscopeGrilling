package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import cn.breezeth.kaleidoscope_grilling.food.HotFoodConfig;
import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class MaidGrillingTask implements IMaidTask {
  public static final ResourceLocation UID =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "grilling");

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public ItemStack getIcon() {
    return new ItemStack(ModBlocks.GRILL_ITEM.get());
  }

  @Nullable
  @Override
  public SoundEvent getAmbientSound(EntityMaid maid) {
    return null;
  }

  @Override
  public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(
      EntityMaid maid) {
    List<Pair<Integer, BehaviorControl<? super EntityMaid>>> tasks = new ArrayList<>();
    if (!maid.level().isClientSide) tasks.add(Pair.of(5, new MaidGrillingBehavior()));
    return tasks;
  }

  @Override
  public boolean isEnable(EntityMaid maid) {
    return HotFoodConfig.ENABLE_MAID_GRILLING_TASK.get();
  }

  @Override
  public boolean enableLookAndRandomWalk(EntityMaid maid) {
    return false;
  }

  @Override
  public String getMaidActionSummary() {
    return "Grill skewers with oil and seasoning";
  }
}
