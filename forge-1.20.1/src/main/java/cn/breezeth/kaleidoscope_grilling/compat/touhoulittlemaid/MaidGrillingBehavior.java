package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import cn.breezeth.kaleidoscope_grilling.AdvancedRackAutomationApi;
import cn.breezeth.kaleidoscope_grilling.rack.AdvancedRackBlockEntity;
import cn.breezeth.kaleidoscope_grilling.GrillAutomationApi;
import cn.breezeth.kaleidoscope_grilling.grill.GrillBlockEntity;
import cn.breezeth.kaleidoscope_grilling.food.HotFoodConfig;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.registry.ModSounds;
import cn.breezeth.kaleidoscope_grilling.oil.OilPotCompat;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningData;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

final class MaidGrillingBehavior extends Behavior<EntityMaid> {
  private static final int OIL_SLOT = 0;
  private static final int SEASONING_SLOT = 1;
  private static final int FLINT_SLOT = 2;
  private static final int RAW_START = 3;
  private static final int RAW_END = 6;
  private static final int OUTPUT_START = 6;
  private static final int OUTPUT_END = 9;
  private static final int ADVANCED_RACK_SEARCH_RANGE = 24;

  private int actionCooldown;
  private int searchCooldown;
  private int actionTailTicks;
  private MaidGrillingData.Action pendingAction = MaidGrillingData.Action.NONE;
  private BlockPos openContainerPos;
  private long closeContainerAt;
  private boolean animatedContainerOpen;

  MaidGrillingBehavior() {
    super(ImmutableMap.of(), 12000);
  }

  @Override
  protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
    return MaidGrillingData.KEY != null && HotFoodConfig.ENABLE_MAID_GRILLING_TASK.get();
  }

  @Override
  protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
    return maid.isAlive()
        && maid.getTask() != null
        && maid.getTask().getUid().equals(MaidGrillingTask.UID)
        && HotFoodConfig.ENABLE_MAID_GRILLING_TASK.get();
  }

  @Override
  protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
    updateOpenContainer(level, gameTime);
    MaidGrillingSpeech.tick(maid);
    if (!migrateLegacyTools(level, maid)) return;
    if (actionCooldown-- > 0) return;
    if (pendingAction != MaidGrillingData.Action.NONE) {
      if (!canCommitPendingAction(level, maid, pendingAction)) {
        pendingAction = MaidGrillingData.Action.NONE;
        actionTailTicks = 0;
        clearAction(maid);
        setWaitReason(maid, MaidGrillingData.WaitReason.INTERRUPTED);
        return;
      }
      commitPendingAction(level, maid, pendingAction);
      pendingAction = MaidGrillingData.Action.NONE;
      actionCooldown = actionTailTicks;
      actionTailTicks = 0;
      return;
    }
    MaidGrillingData data = data(maid);
    if (data.action() != MaidGrillingData.Action.NONE && data.actionUntil() <= gameTime)
      clearAction(maid);
    data = data(maid);
    if (data.grill().isPresent()) {
      BlockPos grillPos = localPos(level, data.grill().get());
      if (grillPos == null || !(level.getBlockEntity(grillPos) instanceof GrillBlockEntity)) {
        boolean returning =
            data.stage() == MaidGrillingData.WorkStage.RETURN_TOOLS
                || data.stage() == MaidGrillingData.WorkStage.STORE_OUTPUT;
        if (!returning) {
          setData(
              maid,
              data.withWork(data.grill(), MaidGrillingData.WorkStage.RETURN_TOOLS));
          data = data(maid);
        }
        setWaitReason(maid, MaidGrillingData.WaitReason.GRILL_MISSING);
      }
      boolean needsLease =
          data.stage() != MaidGrillingData.WorkStage.RETURN_TOOLS
              && data.stage() != MaidGrillingData.WorkStage.STORE_OUTPUT
              && data.stage() != MaidGrillingData.WorkStage.WAIT_CHAIR;
      if (needsLease
          && !GrillAutomationApi.heartbeat(level, grillPos, maid.getUUID())
          && !GrillAutomationApi.acquire(level, grillPos, maid.getUUID())) {
        setData(
            maid,
            data.withWork(data.grill(), MaidGrillingData.WorkStage.RETURN_TOOLS)
                .withWaitReason(MaidGrillingData.WaitReason.GRILL_BUSY));
        return;
      }
    }

    switch (data.stage()) {
      case FIND_GRILL -> findGrill(level, maid, data);
      case WAIT_CHAIR -> waitChair(level, maid, data);
      case WAIT_INPUT -> waitInput(level, maid, data);
      case GATHER_INPUT -> gatherInput(level, maid, data);
      case GATHER_TOOLS -> gatherTools(level, maid, data);
      case WORK_GRILL -> workGrill(level, maid, data);
      case RETURN_TOOLS -> returnTools(level, maid, data);
      case STORE_OUTPUT -> storeOutput(level, maid, data);
    }
  }

  private void findGrill(ServerLevel level, EntityMaid maid, MaidGrillingData data) {
    if (searchCooldown-- > 0) return;
    searchCooldown = 100;
    BlockPos center = maid.blockPosition();
    int range = Math.min(32, Math.max(4, (int) maid.getRestrictRadius()));
    BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    BlockPos grillMissingChair = null;
    double missingChairDistance = Double.MAX_VALUE;
    for (int radius = 0; radius <= range; radius++) {
      BlockPos best = null;
      double bestDistance = Double.MAX_VALUE;
      for (int y = -4; y <= 4; y++) {
        for (int x = -radius; x <= radius; x++) {
          for (int z = -radius; z <= radius; z++) {
            if (radius > 0 && Math.abs(x) != radius && Math.abs(z) != radius) continue;
            cursor.setWithOffset(center, x, y, z);
            if (!(level.getBlockEntity(cursor) instanceof GrillBlockEntity)) continue;
            GrillAutomationApi.Snapshot snapshot = GrillAutomationApi.snapshot(level, cursor);
            if (snapshot == null
                || (snapshot.automationOwner() != null
                    && !snapshot.automationOwner().equals(maid.getUUID()))
                || !maid.isWithinRestriction(cursor)) continue;
            double distance = cursor.distSqr(center);
            if (findWorkChair(level, maid, cursor) == null) {
              if (distance < missingChairDistance) {
                missingChairDistance = distance;
                grillMissingChair = cursor.immutable();
              }
              continue;
            }
            if (distance < bestDistance) {
              bestDistance = distance;
              best = cursor.immutable();
            }
          }
        }
      }
      if (best == null || !GrillAutomationApi.acquire(level, best, maid.getUUID())) continue;
      MaidGrillingData.BoundContainer grill =
          new MaidGrillingData.BoundContainer(level.dimension().location(), best);
      setData(maid, data.withWork(Optional.of(grill), MaidGrillingData.WorkStage.GATHER_INPUT));
      return;
    }
    if (grillMissingChair != null) {
      MaidGrillingData.BoundContainer grill =
          new MaidGrillingData.BoundContainer(level.dimension().location(), grillMissingChair);
      setData(
          maid,
          data.withWork(Optional.of(grill), MaidGrillingData.WorkStage.WAIT_CHAIR)
              .withWaitReason(MaidGrillingData.WaitReason.MISSING_CHAIR));
      MaidGrillingSpeech.say(
          maid, "bubble.kaleidoscope_grilling.maid_grilling.missing_chair", true);
    } else {
      setWaitReason(maid, MaidGrillingData.WaitReason.NO_GRILL);
    }
  }

  private void waitChair(ServerLevel level, EntityMaid maid, MaidGrillingData data) {
    if (searchCooldown-- > 0) return;
    searchCooldown = 40;
    BlockPos grill = grillPos(level, data);
    if (grill == null || !(level.getBlockEntity(grill) instanceof GrillBlockEntity)) {
      setData(maid, data.resetBatch());
      return;
    }
    if (findWorkChair(level, maid, grill) != null) setData(maid, data.resetBatch());
    else setWaitReason(maid, MaidGrillingData.WaitReason.MISSING_CHAIR);
  }

  private void waitInput(ServerLevel level, EntityMaid maid, MaidGrillingData data) {
    if (searchCooldown-- > 0) return;
    searchCooldown = 40;
    if (hasAvailableRawInput(level, maid, data)) setData(maid, data.resetBatch());
    else {
      setWaitReason(maid, MaidGrillingData.WaitReason.MISSING_RAW_SKEWER);
      MaidGrillingSpeech.say(
          maid, "bubble.kaleidoscope_grilling.maid_grilling.missing_skewer", true);
    }
  }

  private void gatherInput(ServerLevel level, EntityMaid maid, MaidGrillingData data) {
    BlockPos grillPos = grillPos(level, data);
    if (grillPos == null) return;
    GrillAutomationApi.Snapshot snapshot = GrillAutomationApi.snapshot(level, grillPos);
    if (snapshot == null) return;
    if (snapshot.occupied() > 0 || rawCount(maid) >= 3) {
      setData(maid, data.withWork(data.grill(), MaidGrillingData.WorkStage.GATHER_TOOLS));
      return;
    }

    int destination = emptyTaskSlot(maid, RAW_START, RAW_END);
    if (destination < 0) {
      setData(maid, data.withWork(data.grill(), MaidGrillingData.WorkStage.GATHER_TOOLS));
      return;
    }

    WirelessCandidate wireless =
        findWirelessSource(level, maid, GrillAutomationApi::acceptsRawSkewer);
    if (wireless != null) {
      if (!arriveContainer(maid, wireless.container().pos())) {
        setWaitReason(maid, MaidGrillingData.WaitReason.SUPPLY_UNREACHABLE);
        return;
      }
      openContainer(level, maid, wireless.container().pos());
      ItemStack pulled = wireless.handler().extractItem(wireless.slot(), 1, false);
      if (!pulled.isEmpty()) {
        maid.getTaskInv().setStackInSlot(destination, pulled);
        showAndSwing(maid, pulled, MaidGrillingData.Action.PICKUP, 10);
        actionCooldown = scaledDelay(10);
        return;
      }
    }

    IItemHandlerModifiable own = maid.getAvailableInv(false);
    int ownSlot = findSlot(own, GrillAutomationApi::acceptsRawSkewer);
    if (ownSlot >= 0) {
      ItemStack pulled = own.extractItem(ownSlot, 1, false);
      maid.getTaskInv().setStackInSlot(destination, pulled);
      showAndSwing(maid, pulled, MaidGrillingData.Action.PICKUP, 10);
      actionCooldown = scaledDelay(10);
      return;
    }

    if (rawCount(maid) > 0)
      setData(maid, data.withWork(data.grill(), MaidGrillingData.WorkStage.GATHER_TOOLS));
    else {
      MaidGrillingSpeech.say(maid, "bubble.kaleidoscope_grilling.maid_grilling.missing_skewer", true);
      GrillAutomationApi.release(level, grillPos, maid.getUUID());
      setData(maid, data.withWork(Optional.empty(), MaidGrillingData.WorkStage.WAIT_INPUT));
      setWaitReason(maid, MaidGrillingData.WaitReason.MISSING_RAW_SKEWER);
    }
  }

  private void gatherTools(ServerLevel level, EntityMaid maid, MaidGrillingData data) {
    BlockPos grillPos = grillPos(level, data);
    if (grillPos == null) return;
    GrillAutomationApi.Snapshot snapshot = GrillAutomationApi.snapshot(level, grillPos);
    if (snapshot == null) return;
    int batch = Math.max(snapshot.occupied(), rawCount(maid));
    if (batch <= 0) {
      reset(level, maid, data);
      return;
    }

    if (!snapshot.lit() && toolStack(maid, data, FLINT_SLOT).isEmpty()) {
      if (borrowTool(level, maid, data, grillPos, FLINT_SLOT, stack -> stack.is(Items.FLINT_AND_STEEL))) return;
      MaidGrillingSpeech.say(maid, "bubble.kaleidoscope_grilling.maid_grilling.missing_flint", true);
      setWaitReason(maid, MaidGrillingData.WaitReason.MISSING_FLINT);
      abortUnstarted(level, maid, data, snapshot);
      return;
    }
    data = data(maid);
    if (snapshot.phase() == 0
        && OilPotCompat.getCount(toolStack(maid, data, OIL_SLOT))
            < Math.max(1, snapshot.occupied())) {
      int required = Math.max(1, snapshot.occupied());
      if (borrowTool(level, maid, data, grillPos, OIL_SLOT, stack -> OilPotCompat.isOilPot(stack) && OilPotCompat.getCount(stack) >= required)) return;
      MaidGrillingSpeech.say(maid, "bubble.kaleidoscope_grilling.maid_grilling.missing_oil", true);
      setWaitReason(maid, MaidGrillingData.WaitReason.MISSING_OIL);
      abortUnstarted(level, maid, data, snapshot);
      return;
    }
    if (snapshot.phase() == 2
        && !snapshot.seasoned()
        && !usableSeasoning(
            toolStack(maid, data(maid), SEASONING_SLOT), Math.max(1, snapshot.occupied()))) {
      int required = Math.max(1, snapshot.occupied());
      if (borrowTool(level, maid, data, grillPos, SEASONING_SLOT, stack -> usableSeasoning(stack, required))) return;
      MaidGrillingSpeech.say(maid, "bubble.kaleidoscope_grilling.maid_grilling.missing_seasoning", true);
      setWaitReason(maid, MaidGrillingData.WaitReason.MISSING_SEASONING);
      abortUnstarted(level, maid, data, snapshot);
      return;
    }
    if (snapshot.phase() < 2
        && !usableSeasoning(
            toolStack(maid, data(maid), SEASONING_SLOT), Math.max(1, snapshot.occupied()))) {
      int required = Math.max(1, snapshot.occupied());
      if (borrowTool(level, maid, data, grillPos, SEASONING_SLOT, stack -> usableSeasoning(stack, required))) return;
      MaidGrillingSpeech.say(maid, "bubble.kaleidoscope_grilling.maid_grilling.missing_seasoning", true);
      setWaitReason(maid, MaidGrillingData.WaitReason.MISSING_SEASONING);
      abortUnstarted(level, maid, data, snapshot);
      return;
    }
    if (snapshot.occupied() == 0) {
      int capacity = batch;
      if (snapshot.phase() == 0)
        capacity = Math.min(capacity, OilPotCompat.getCount(toolStack(maid, data(maid), OIL_SLOT)));
      if (snapshot.phase() < 2)
        capacity =
            Math.min(
                capacity,
                remainingSeasoning(toolStack(maid, data(maid), SEASONING_SLOT)));
      if (rawCount(maid) > capacity) {
        returnRawInput(level, maid, data);
        return;
      }
    }
    MaidGrillingSpeech.say(maid, "bubble.kaleidoscope_grilling.maid_grilling.start", true);
    MaidGrillingData ready = data(maid);
    setData(maid, ready.withWork(ready.grill(), MaidGrillingData.WorkStage.WORK_GRILL));
  }

  private boolean borrowTool(
      ServerLevel level,
      EntityMaid maid,
      MaidGrillingData data,
      BlockPos grillPos,
      int taskSlot,
      Predicate<ItemStack> predicate) {
    if (source(data, taskSlot).isPresent()) {
      setData(
          maid,
          data.withWork(data.grill(), MaidGrillingData.WorkStage.RETURN_TOOLS));
      return true;
    }
    int ownSlot = findSlot(maid.getMaidInv(), predicate);
    RackCandidate rack = findRack(level, maid, predicate);
    if (rack != null) {
      int maidSlot = emptySlot(maid.getMaidInv());
      if (maidSlot < 0) {
        if (ownSlot >= 0) return trackOwnedTool(level, maid, taskSlot, ownSlot);
        reportInventoryFull(maid);
        return true;
      }
      if (!rack.reachable()) {
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        setWaitReason(maid, MaidGrillingData.WaitReason.TOOL_SOURCE_UNREACHABLE);
        return true;
      }
      if (!arriveAdvancedRack(maid, rack)) {
        setWaitReason(maid, MaidGrillingData.WaitReason.APPROACHING_TOOL_SOURCE);
        return true;
      }
      AdvancedRackAutomationApi.BorrowResult result =
          AdvancedRackAutomationApi.borrow(level, rack.pos, predicate, false);
      if (result.success()) {
        maid.getMaidInv().setStackInSlot(maidSlot, result.stack());
        MaidGrillingData.BorrowSource source =
            new MaidGrillingData.BorrowSource(
                new MaidGrillingData.BoundContainer(level.dimension().location(), rack.pos),
                result.receipt().slot(),
                maidSlot,
                true,
                false,
                false);
        setSource(maid, data(maid), taskSlot, Optional.of(source));
        showAndSwing(maid, result.stack(), MaidGrillingData.Action.PICKUP, 10);
        actionCooldown = scaledDelay(10);
      }
      return true;
    }

    WirelessCandidate wireless = findWirelessSource(level, maid, predicate);
    if (wireless != null) {
      int maidSlot = emptySlot(maid.getMaidInv());
      if (maidSlot < 0) {
        if (ownSlot >= 0) return trackOwnedTool(level, maid, taskSlot, ownSlot);
        reportInventoryFull(maid);
        return true;
      }
      if (!arriveContainer(maid, wireless.container().pos())) {
        setWaitReason(maid, MaidGrillingData.WaitReason.APPROACHING_TOOL_SOURCE);
        return true;
      }
      openContainer(level, maid, wireless.container().pos());
      ItemStack borrowed = wireless.handler().extractItem(wireless.slot(), 1, false);
      if (borrowed.isEmpty()) return true;
      maid.getMaidInv().setStackInSlot(maidSlot, borrowed);
      setSource(
          maid,
          data(maid),
          taskSlot,
          Optional.of(
              new MaidGrillingData.BorrowSource(
                  wireless.container(), wireless.slot(), maidSlot, false, true, false)));
      showAndSwing(maid, borrowed, MaidGrillingData.Action.PICKUP, 10);
      actionCooldown = scaledDelay(10);
      return true;
    }

    if (ownSlot >= 0) return trackOwnedTool(level, maid, taskSlot, ownSlot);

    return false;
  }

  private boolean trackOwnedTool(
      ServerLevel level, EntityMaid maid, int taskSlot, int maidSlot) {
    ItemStack owned = maid.getMaidInv().getStackInSlot(maidSlot);
    MaidGrillingData.BorrowSource source =
        new MaidGrillingData.BorrowSource(
            new MaidGrillingData.BoundContainer(level.dimension().location(), maid.blockPosition()),
            -1,
            maidSlot,
            false,
            false,
            true);
    setSource(maid, data(maid), taskSlot, Optional.of(source));
    showAndSwing(maid, owned, MaidGrillingData.Action.PICKUP, 10);
    actionCooldown = scaledDelay(10);
    return true;
  }

  private void workGrill(ServerLevel level, EntityMaid maid, MaidGrillingData data) {
    BlockPos pos = grillPos(level, data);
    if (pos == null) return;
    EntityChair workChair = findWorkChair(level, maid, pos);
    if (workChair == null) {
      GrillAutomationApi.release(level, pos, maid.getUUID());
      setData(
          maid,
          data.withWork(data.grill(), MaidGrillingData.WorkStage.WAIT_CHAIR)
              .withWaitReason(MaidGrillingData.WaitReason.MISSING_CHAIR));
      MaidGrillingSpeech.say(
          maid, "bubble.kaleidoscope_grilling.maid_grilling.missing_chair", true);
      return;
    }
    if (!sitAtGrill(maid, workChair, pos)) {
      setWaitReason(maid, MaidGrillingData.WaitReason.CHAIR_UNREACHABLE);
      return;
    }
    GrillAutomationApi.Snapshot snapshot = GrillAutomationApi.snapshot(level, pos);
    if (snapshot == null) return;

    if (!snapshot.lit()) {
      ItemStack flint = toolStack(maid, data, FLINT_SLOT);
      if (flint.isEmpty()) {
        setData(maid, data.withWork(data.grill(), MaidGrillingData.WorkStage.GATHER_TOOLS));
        return;
      }
      beginWorkAction(maid, flint, MaidGrillingData.Action.IGNITE, 20);
      return;
    }

    if (snapshot.phase() == 0) {
      int rawSlot = firstTaskSlot(maid, RAW_START, RAW_END);
      if (snapshot.occupied() < 3 && rawSlot >= 0) {
        ItemStack raw = maid.getTaskInv().getStackInSlot(rawSlot);
        beginWorkAction(maid, raw, MaidGrillingData.Action.INSERT, 10);
        return;
      }
      if (snapshot.occupied() == 0) {
        setData(maid, data.withWork(data.grill(), MaidGrillingData.WorkStage.RETURN_TOOLS));
        return;
      }
      ItemStack oil = toolStack(maid, data, OIL_SLOT);
      if (oil.isEmpty() || OilPotCompat.getCount(oil) < snapshot.occupied()) {
        beginWorkAction(maid, ItemStack.EMPTY, MaidGrillingData.Action.EXTINGUISH, 20);
        return;
      }
      beginWorkAction(maid, oil, MaidGrillingData.Action.BRUSH, 20);
      return;
    }

    if (snapshot.phase() == 1) {
      if (snapshot.flipCooldown() > 0) {
        setWaitReason(maid, MaidGrillingData.WaitReason.WAITING_FLIP);
        return;
      }
      beginWorkAction(maid, ItemStack.EMPTY, MaidGrillingData.Action.FLIP, 40);
      return;
    }

    if (snapshot.phase() == 2 && !snapshot.seasoned()) {
      ItemStack seasoning = toolStack(maid, data, SEASONING_SLOT);
      if (!usableSeasoning(seasoning, snapshot.occupied())) {
        beginWorkAction(maid, ItemStack.EMPTY, MaidGrillingData.Action.EXTINGUISH, 20);
        return;
      }
      beginWorkAction(maid, seasoning, MaidGrillingData.Action.SEASON, 20);
      return;
    }

    if ((snapshot.phase() == 2 && snapshot.seasoned()) || snapshot.phase() == 3) {
      int outputSlot = emptyTaskSlot(maid, OUTPUT_START, OUTPUT_END);
      if (outputSlot < 0) return;
      beginWorkAction(maid, ItemStack.EMPTY, MaidGrillingData.Action.EXTRACT, 10);
    }
  }

  private void beginWorkAction(
      EntityMaid maid, ItemStack stack, MaidGrillingData.Action action, int duration) {
    int total = scaledDelay(duration);
    int keyframe = Math.max(1, Math.round(total * 0.65F));
    showAction(maid, stack, action, total);
    maid.swing(InteractionHand.MAIN_HAND);
    pendingAction = action;
    actionTailTicks = Math.max(0, total - keyframe);
    actionCooldown = keyframe;
  }

  private void commitPendingAction(
      ServerLevel level, EntityMaid maid, MaidGrillingData.Action action) {
    MaidGrillingData data = data(maid);
    BlockPos pos = grillPos(level, data);
    if (pos == null || !(level.getBlockEntity(pos) instanceof GrillBlockEntity)) return;
    switch (action) {
      case IGNITE -> {
        ItemStack flint = toolStack(maid, data, FLINT_SLOT);
        if (!flint.isEmpty()) GrillAutomationApi.ignite(level, pos, flint, maid, false);
      }
      case INSERT -> {
        int slot = firstTaskSlot(maid, RAW_START, RAW_END);
        if (slot < 0) return;
        ItemStack raw = maid.getTaskInv().getStackInSlot(slot);
        if (GrillAutomationApi.insertSkewer(level, pos, raw, false).success()) {
          level.playSound(null, pos, ModSounds.ACTION_SUCCESS.get(), SoundSource.BLOCKS, 0.55F, 1.0F);
          if (raw.isEmpty()) maid.getTaskInv().setStackInSlot(slot, ItemStack.EMPTY);
        }
      }
      case BRUSH -> {
        if (GrillAutomationApi.brushOil(
                level, pos, toolStack(maid, data, OIL_SLOT), false)
            .success())
          level.playSound(null, pos, ModSounds.GRILL_FLIP.get(), SoundSource.BLOCKS, 0.65F, 1.0F);
      }
      case FLIP -> {
        if (GrillAutomationApi.flip(level, pos, false).success())
          level.playSound(null, pos, ModSounds.GRILL_FLIP.get(), SoundSource.BLOCKS, 0.75F, 1.0F);
      }
      case SEASON -> {
        GrillAutomationApi.Result result =
            GrillAutomationApi.season(
                level, pos, toolStack(maid, data, SEASONING_SLOT), false);
        if (result.success()) {
          level.playSound(null, pos, ModSounds.SEASON.get(), SoundSource.BLOCKS, 0.85F, 1.0F);
          if (result.shouldReplaceHeld()) {
            int seasoningSlot = toolSlot(maid, data, SEASONING_SLOT);
            if (seasoningSlot >= 0)
              maid.getMaidInv().setStackInSlot(seasoningSlot, result.heldReplacement());
          }
        }
      }
      case EXTRACT -> {
        int outputSlot = emptyTaskSlot(maid, OUTPUT_START, OUTPUT_END);
        if (outputSlot < 0) return;
        GrillAutomationApi.Result result = GrillAutomationApi.extract(level, pos, false);
        if (!result.success()) return;
        maid.getTaskInv().setStackInSlot(outputSlot, result.output());
        level.playSound(null, pos, ModSounds.PICKUP_ITEM.get(), SoundSource.BLOCKS, 0.7F, 1.0F);
        setData(
            maid,
            data(maid)
                .withAction(
                    MaidGrillingData.Action.EXTRACT,
                    data(maid).actionUntil(),
                    result.output())
                .withCompletedBatch());
        GrillAutomationApi.Snapshot after = GrillAutomationApi.snapshot(level, pos);
        if (after == null || after.occupied() == 0)
          setData(
              maid,
              data(maid)
                  .withWork(data.grill(), MaidGrillingData.WorkStage.RETURN_TOOLS));
      }
      case EXTINGUISH -> {
        GrillAutomationApi.extinguish(level, pos, false);
        setData(maid, data(maid).withWork(data.grill(), MaidGrillingData.WorkStage.GATHER_TOOLS));
      }
      case NONE, PICKUP -> {}
    }
  }

  private boolean canCommitPendingAction(
      ServerLevel level, EntityMaid maid, MaidGrillingData.Action action) {
    MaidGrillingData data = data(maid);
    BlockPos pos = grillPos(level, data);
    GrillAutomationApi.Snapshot snapshot =
        pos == null ? null : GrillAutomationApi.snapshot(level, pos);
    if (snapshot == null) return false;
    return switch (action) {
      case IGNITE -> !snapshot.lit();
      case INSERT ->
          snapshot.phase() == 0
              && snapshot.occupied() < 3
              && firstTaskSlot(maid, RAW_START, RAW_END) >= 0;
      case BRUSH -> snapshot.phase() == 0 && snapshot.occupied() > 0;
      case FLIP ->
          snapshot.phase() == 1 && snapshot.occupied() > 0 && snapshot.flipCooldown() <= 0;
      case SEASON -> snapshot.phase() == 2 && snapshot.occupied() > 0 && !snapshot.seasoned();
      case EXTRACT ->
          snapshot.occupied() > 0
              && ((snapshot.phase() == 2 && snapshot.seasoned()) || snapshot.phase() == 3);
      case EXTINGUISH -> snapshot.lit() && snapshot.occupied() > 0;
      case NONE, PICKUP -> true;
    };
  }

  private void returnTools(ServerLevel level, EntityMaid maid, MaidGrillingData data) {
    if (returnRawInput(level, maid, data)) return;
    if (returnOne(level, maid, data, OIL_SLOT, data.oilSource())) return;
    data = data(maid);
    if (returnOne(level, maid, data, SEASONING_SLOT, data.seasoningSource())) return;
    data = data(maid);
    if (returnOne(level, maid, data, FLINT_SLOT, data.flintSource())) return;
    setData(maid, data(maid).withWork(data.grill(), MaidGrillingData.WorkStage.STORE_OUTPUT));
  }

  private boolean returnRawInput(ServerLevel level, EntityMaid maid, MaidGrillingData data) {
    int slot = firstTaskSlot(maid, RAW_START, RAW_END);
    if (slot < 0) return false;
    ItemStack raw = maid.getTaskInv().getStackInSlot(slot);
    WirelessDestination destination = findWirelessDestination(level, maid, false, raw);
    if (destination != null) {
      if (!arriveContainer(maid, destination.container().pos())) {
        setWaitReason(maid, MaidGrillingData.WaitReason.OUTPUT_UNREACHABLE);
        return true;
      }
      openContainer(level, maid, destination.container().pos());
      ItemStack remainder =
          ItemHandlerHelper.insertItemStacked(destination.handler(), raw.copy(), false);
      maid.getTaskInv().setStackInSlot(slot, remainder);
      actionCooldown = scaledDelay(10);
      return true;
    }
    ItemStack remainder = ItemHandlerHelper.insertItemStacked(maid.getAvailableInv(false), raw.copy(), false);
    maid.getTaskInv().setStackInSlot(slot, remainder);
    return true;
  }

  private boolean returnOne(
      ServerLevel level,
      EntityMaid maid,
      MaidGrillingData data,
      int taskSlot,
      Optional<MaidGrillingData.BorrowSource> source) {
    if (source.isEmpty()) return false;
    MaidGrillingData.BorrowSource receipt = source.get();
    if (receipt.owned()) {
      setSource(maid, data, taskSlot, Optional.empty());
      return false;
    }
    int maidSlot = receipt.maidSlot();
    if (maidSlot < 0 || maidSlot >= maid.getMaidInv().getSlots()) {
      setSource(maid, data, taskSlot, Optional.empty());
      return false;
    }
    ItemStack returned = maid.getMaidInv().getStackInSlot(maidSlot);
    if (returned.isEmpty()) {
      setSource(maid, data, taskSlot, Optional.empty());
      return false;
    }
    BlockPos pos = localPos(level, receipt.container());
    if (pos == null) return true;
    RackCandidate returnRack =
        receipt.advancedRack() ? rackCandidate(level, maid, pos) : null;
    if (returnRack != null && !returnRack.reachable()) {
      maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      setWaitReason(maid, MaidGrillingData.WaitReason.TOOL_SOURCE_UNREACHABLE);
      return true;
    }
    if (!(receipt.advancedRack()
        ? returnRack != null && arriveAdvancedRack(maid, returnRack)
        : arriveContainer(maid, pos))) {
      setWaitReason(maid, MaidGrillingData.WaitReason.APPROACHING_TOOL_SOURCE);
      return true;
    }
    if (receipt.wireless()) openContainer(level, maid, pos);
    ItemStack remainder;
    if (receipt.advancedRack()) {
      AdvancedRackAutomationApi.ReturnResult result =
          AdvancedRackAutomationApi.returnBorrowed(
              level,
              new AdvancedRackAutomationApi.BorrowReceipt(
                  receipt.container().dimension(), pos, receipt.slot()),
              returned,
              false);
      remainder = result.remainder();
    } else {
      remainder = insertAtSource(itemHandler(level, pos), receipt.slot(), returned);
    }
    maid.getMaidInv().setStackInSlot(maidSlot, remainder);
    if (remainder.isEmpty()) {
      setSource(maid, data(maid), taskSlot, Optional.empty());
      actionCooldown = scaledDelay(10);
    }
    return true;
  }

  private void storeOutput(ServerLevel level, EntityMaid maid, MaidGrillingData data) {
    int slot = firstTaskSlot(maid, OUTPUT_START, OUTPUT_END);
    if (slot < 0) {
      if (data.completedBatch())
        MaidGrillingSpeech.say(maid, "bubble.kaleidoscope_grilling.maid_grilling.done", false);
      reset(level, maid, data);
      return;
    }
    ItemStack output = maid.getTaskInv().getStackInSlot(slot);
    WirelessDestination destination = findWirelessDestination(level, maid, true, output);
    if (destination != null) {
      if (!arriveContainer(maid, destination.container().pos())) return;
      openContainer(level, maid, destination.container().pos());
      ItemStack remainder =
          ItemHandlerHelper.insertItemStacked(destination.handler(), output.copy(), false);
      maid.getTaskInv().setStackInSlot(slot, remainder);
      if (remainder.isEmpty()) {
        actionCooldown = scaledDelay(10);
        return;
      }
      output = remainder;
    }
    ItemStack remainder = ItemHandlerHelper.insertItemStacked(maid.getAvailableInv(false), output.copy(), false);
    maid.getTaskInv().setStackInSlot(slot, remainder);
    if (!remainder.isEmpty())
      MaidGrillingSpeech.say(maid, "bubble.kaleidoscope_grilling.maid_grilling.output_full", true);
    if (!remainder.isEmpty()) setWaitReason(maid, MaidGrillingData.WaitReason.OUTPUT_FULL);
  }

  @Override
  protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
    closeOpenContainer(level);
    pendingAction = MaidGrillingData.Action.NONE;
    actionTailTicks = 0;
    maid.getHandItemsForAnimation()[InteractionHand.MAIN_HAND.ordinal()] = ItemStack.EMPTY;
    if (maid.getVehicle() instanceof EntityChair) maid.stopRiding();
    if (MaidGrillingData.KEY != null)
      setData(
          maid,
          data(maid).withAction(MaidGrillingData.Action.NONE, 0L, ItemStack.EMPTY));
    maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
  }

  private static boolean arriveContainer(EntityMaid maid, BlockPos pos) {
    if (maid.getVehicle() instanceof EntityChair) maid.stopRiding();
    if (maid.distanceToSqr(pos.getCenter()) <= 2.25D) {
      maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      maid.getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
      return true;
    }
    BehaviorUtils.setWalkAndLookTargetMemories(maid, pos, 0.55F, 1);
    return false;
  }

  private static boolean arriveAdvancedRack(EntityMaid maid, RackCandidate rack) {
    if (maid.getVehicle() instanceof EntityChair) maid.stopRiding();
    BlockPos approach = rack.approach();
    if (maid.distanceToSqr(approach.getCenter()) <= 2.25D) {
      maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      BlockPos pos = rack.pos();
      maid.getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
      return true;
    }
    BehaviorUtils.setWalkAndLookTargetMemories(maid, approach, 0.55F, 0);
    return false;
  }

  private static EntityChair findWorkChair(ServerLevel level, EntityMaid maid, BlockPos grill) {
    AABB searchArea = new AABB(grill).inflate(2.5D, 1.5D, 2.5D);
    EntityChair nearest = null;
    double nearestDistance = Double.MAX_VALUE;
    for (EntityChair chair : level.getEntitiesOfClass(EntityChair.class, searchArea)) {
      if (!chair.isAlive()
          || (chair.hasPassenger() && !chair.getPassengers().contains(maid))) continue;
      double distance = maid.distanceToSqr(chair);
      if (distance < nearestDistance) {
        nearestDistance = distance;
        nearest = chair;
      }
    }
    return nearest;
  }

  private static boolean sitAtGrill(EntityMaid maid, EntityChair workChair, BlockPos grill) {
    if (maid.getVehicle() == workChair) {
      maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      faceGrill(maid, workChair, grill);
      return true;
    }
    if (maid.isPassenger()) maid.stopRiding();
    if (maid.distanceToSqr(workChair) <= 2.25D && maid.startRiding(workChair, true)) {
      maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      faceGrill(maid, workChair, grill);
      return true;
    }
    BehaviorUtils.setWalkAndLookTargetMemories(maid, workChair.blockPosition(), 0.55F, 0);
    return false;
  }

  private static void faceGrill(EntityMaid maid, EntityChair chair, BlockPos grill) {
    double dx = grill.getX() + 0.5D - chair.getX();
    double dz = grill.getZ() + 0.5D - chair.getZ();
    if (dx * dx + dz * dz < 1.0E-6D) return;
    float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
    chair.setYRot(yaw);
    chair.yRotO = yaw;
    maid.setYRot(yaw);
    maid.yRotO = yaw;
    maid.setYBodyRot(yaw);
    maid.yBodyRotO = yaw;
    maid.setYHeadRot(yaw);
    maid.yHeadRotO = yaw;
    maid.getLookControl().setLookAt(grill.getX() + 0.5D, grill.getY() + 0.5D, grill.getZ() + 0.5D);
  }

  private static boolean hasAvailableRawInput(
      ServerLevel level, EntityMaid maid, MaidGrillingData data) {
    if (findSlot(maid.getAvailableInv(false), GrillAutomationApi::acceptsRawSkewer) >= 0) return true;
    return findWirelessSource(level, maid, GrillAutomationApi::acceptsRawSkewer) != null;
  }

  private static RackCandidate findRack(
      ServerLevel level,
      EntityMaid maid,
      Predicate<ItemStack> predicate) {
    List<BlockPos> matching = new ArrayList<>();
    BlockPos center = maid.blockPosition();
    int minChunkX = (center.getX() - ADVANCED_RACK_SEARCH_RANGE) >> 4;
    int maxChunkX = (center.getX() + ADVANCED_RACK_SEARCH_RANGE) >> 4;
    int minChunkZ = (center.getZ() - ADVANCED_RACK_SEARCH_RANGE) >> 4;
    int maxChunkZ = (center.getZ() + ADVANCED_RACK_SEARCH_RANGE) >> 4;
    double maxDistance = ADVANCED_RACK_SEARCH_RANGE * ADVANCED_RACK_SEARCH_RANGE;
    for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
      for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
        var chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk == null) continue;
        for (var entry : chunk.getBlockEntities().entrySet()) {
          BlockPos pos = entry.getKey();
          if (!(entry.getValue() instanceof AdvancedRackBlockEntity rack)
              || !maid.isWithinRestriction(pos)) continue;
          if (pos.distSqr(center) > maxDistance) continue;
          for (int slot = 0; slot < rack.getContainerSize(); slot++) {
            if (!predicate.test(rack.getItem(slot))) continue;
            matching.add(pos.immutable());
            break;
          }
        }
      }
    }
    matching.sort(
        Comparator.comparingDouble((BlockPos pos) -> pos.distSqr(center))
            .thenComparingInt(BlockPos::getY)
            .thenComparingInt(BlockPos::getX)
            .thenComparingInt(BlockPos::getZ));
    RackCandidate nearestUnreachable = null;
    for (BlockPos pos : matching) {
      RackCandidate candidate = rackCandidate(level, maid, pos);
      if (candidate.reachable()) return candidate;
      if (nearestUnreachable == null) nearestUnreachable = candidate;
    }
    return nearestUnreachable;
  }

  private static RackCandidate rackCandidate(ServerLevel level, EntityMaid maid, BlockPos rackPos) {
    List<BlockPos> approaches = new ArrayList<>();
    for (int y = rackPos.getY() - 3; y <= rackPos.getY() + 3; y++) {
      for (int dx = -1; dx <= 1; dx++) {
        for (int dz = -1; dz <= 1; dz++) {
          if (dx == 0 && dz == 0) continue;
          BlockPos candidate = new BlockPos(rackPos.getX() + dx, y, rackPos.getZ() + dz);
          if (canStandAt(level, maid, candidate)) approaches.add(candidate);
        }
      }
    }
    approaches.sort(Comparator.comparingDouble(pos -> pos.distSqr(maid.blockPosition())));
    for (BlockPos approach : approaches) {
      if (maid.distanceToSqr(approach.getCenter()) <= 2.25D) {
        return new RackCandidate(rackPos, approach, true);
      }
      var path = maid.getNavigation().createPath(approach, 0);
      if (path != null && path.canReach()) return new RackCandidate(rackPos, approach, true);
    }
    BlockPos fallback =
        approaches.isEmpty()
            ? rackPos
              : approaches.get(0);
    return new RackCandidate(rackPos, fallback, false);
  }

  private static boolean canStandAt(ServerLevel level, EntityMaid maid, BlockPos pos) {
    if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
      return false;
    }
    AABB moved =
        maid.getBoundingBox()
            .move(
                pos.getX() + 0.5D - maid.getX(),
                pos.getY() - maid.getY(),
                pos.getZ() + 0.5D - maid.getZ());
    return level.noCollision(maid, moved);
  }

  private static int comparePos(BlockPos a, BlockPos b) {
    if (b == null) return -1;
    int x = Integer.compare(a.getX(), b.getX());
    if (x != 0) return x;
    int y = Integer.compare(a.getY(), b.getY());
    return y != 0 ? y : Integer.compare(a.getZ(), b.getZ());
  }

  private static WirelessCandidate findWirelessSource(
      ServerLevel level, EntityMaid maid, Predicate<ItemStack> predicate) {
    for (MaidGrillingData.BoundContainer container :
        MaidGrillingWirelessIO.supplyEndpoints(maid)) {
      if (!validWirelessEndpoint(level, maid, container)) continue;
      IItemHandler handler = itemHandler(level, container.pos());
      int slot = findSlot(handler, predicate);
      if (slot >= 0) return new WirelessCandidate(container, handler, slot);
    }
    return null;
  }

  private static WirelessDestination findWirelessDestination(
      ServerLevel level, EntityMaid maid, boolean output, ItemStack stack) {
    var endpoints =
        output
            ? MaidGrillingWirelessIO.outputEndpoints(maid)
            : MaidGrillingWirelessIO.supplyEndpoints(maid);
    for (MaidGrillingData.BoundContainer container : endpoints) {
      if (!validWirelessEndpoint(level, maid, container)) continue;
      IItemHandler handler = itemHandler(level, container.pos());
      ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, stack.copy(), true);
      if (remainder.getCount() < stack.getCount()) {
        return new WirelessDestination(container, handler);
      }
    }
    return null;
  }

  private static boolean validWirelessEndpoint(
      ServerLevel level, EntityMaid maid, MaidGrillingData.BoundContainer container) {
    BlockPos pos = localPos(level, container);
    return pos != null && itemHandler(level, pos) != null && maid.isWithinRestriction(pos);
  }

  private static IItemHandler itemHandler(ServerLevel level, BlockPos pos) {
    return pos == null || !level.hasChunkAt(pos)
        ? null
        : level.getBlockEntity(pos).getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
  }

  private static int findSlot(IItemHandler handler, Predicate<ItemStack> predicate) {
    if (handler == null) return -1;
    for (int slot = 0; slot < handler.getSlots(); slot++) {
      ItemStack stack = handler.getStackInSlot(slot);
      if (!stack.isEmpty() && predicate.test(stack)) return slot;
    }
    return -1;
  }

  private static int emptySlot(IItemHandler handler) {
    if (handler == null) return -1;
    for (int slot = 0; slot < handler.getSlots(); slot++)
      if (handler.getStackInSlot(slot).isEmpty()) return slot;
    return -1;
  }

  private static boolean migrateLegacyTools(ServerLevel level, EntityMaid maid) {
    for (int tool = OIL_SLOT; tool <= FLINT_SLOT; tool++) {
      ItemStack legacy = maid.getTaskInv().getStackInSlot(tool);
      if (legacy.isEmpty()) continue;
      int maidSlot = emptySlot(maid.getMaidInv());
      if (maidSlot < 0) {
        reportInventoryFull(maid);
        return false;
      }
      maid.getMaidInv().setStackInSlot(maidSlot, legacy);
      maid.getTaskInv().setStackInSlot(tool, ItemStack.EMPTY);
      MaidGrillingData current = data(maid);
      Optional<MaidGrillingData.BorrowSource> old = source(current, tool);
      MaidGrillingData.BorrowSource migrated =
          old.map(
                  receipt ->
                      new MaidGrillingData.BorrowSource(
                          receipt.container(),
                          receipt.slot(),
                          maidSlot,
                          receipt.advancedRack(),
                          receipt.wireless(),
                          receipt.owned()))
              .orElseGet(
                  () ->
                      new MaidGrillingData.BorrowSource(
                          new MaidGrillingData.BoundContainer(
                              level.dimension().location(), maid.blockPosition()),
                          -1,
                          maidSlot,
                          false,
                          false,
                          true));
      setSource(maid, current, tool, Optional.of(migrated));
    }
    return true;
  }

  private static int toolSlot(EntityMaid maid, MaidGrillingData data, int tool) {
    Optional<MaidGrillingData.BorrowSource> source = source(data, tool);
    if (source.isPresent()) {
      int slot = source.get().maidSlot();
      return slot >= 0 && slot < maid.getMaidInv().getSlots() ? slot : -1;
    }
    return -1;
  }

  private static ItemStack toolStack(EntityMaid maid, MaidGrillingData data, int tool) {
    int slot = toolSlot(maid, data, tool);
    if (slot < 0) return ItemStack.EMPTY;
    ItemStack stack = maid.getMaidInv().getStackInSlot(slot);
    if (source(data, tool).isEmpty() && !toolPredicate(tool).test(stack)) return ItemStack.EMPTY;
    return stack;
  }

  private static Predicate<ItemStack> toolPredicate(int tool) {
    return switch (tool) {
      case OIL_SLOT -> stack -> OilPotCompat.isOilPot(stack) && OilPotCompat.getCount(stack) > 0;
      case SEASONING_SLOT -> stack -> usableSeasoning(stack, 1);
      case FLINT_SLOT -> stack -> stack.is(Items.FLINT_AND_STEEL);
      default -> stack -> false;
    };
  }

  private static Optional<MaidGrillingData.BorrowSource> source(
      MaidGrillingData data, int tool) {
    return switch (tool) {
      case OIL_SLOT -> data.oilSource();
      case SEASONING_SLOT -> data.seasoningSource();
      case FLINT_SLOT -> data.flintSource();
      default -> Optional.empty();
    };
  }

  private static void reportInventoryFull(EntityMaid maid) {
    setWaitReason(maid, MaidGrillingData.WaitReason.INVENTORY_FULL);
    MaidGrillingSpeech.say(
        maid, "bubble.kaleidoscope_grilling.maid_grilling.inventory_full", true);
  }

  private static ItemStack insertAtSource(
      IItemHandler handler, int preferredSlot, ItemStack stack) {
    if (handler == null || stack.isEmpty()) return stack.copy();
    ItemStack remainder = stack.copy();
    if (preferredSlot >= 0 && preferredSlot < handler.getSlots())
      remainder = handler.insertItem(preferredSlot, remainder, false);
    if (!remainder.isEmpty()) remainder = ItemHandlerHelper.insertItemStacked(handler, remainder, false);
    return remainder;
  }

  private static int rawCount(EntityMaid maid) {
    int count = 0;
    for (int slot = RAW_START; slot < RAW_END; slot++)
      if (!maid.getTaskInv().getStackInSlot(slot).isEmpty()) count++;
    return count;
  }

  private static int emptyTaskSlot(EntityMaid maid, int start, int end) {
    for (int slot = start; slot < end; slot++)
      if (maid.getTaskInv().getStackInSlot(slot).isEmpty()) return slot;
    return -1;
  }

  private static int firstTaskSlot(EntityMaid maid, int start, int end) {
    for (int slot = start; slot < end; slot++)
      if (!maid.getTaskInv().getStackInSlot(slot).isEmpty()) return slot;
    return -1;
  }

  private static boolean usableSeasoning(ItemStack stack, int amount) {
    return stack.is(ModItems.SPECIAL_SEASONING.get()) && remainingSeasoning(stack) >= amount;
  }

  private static int remainingSeasoning(ItemStack stack) {
    return stack.is(ModItems.SPECIAL_SEASONING.get())
        ? SeasoningData.MAX_USES - SeasoningData.getUses(stack)
        : 0;
  }

  private static void abortUnstarted(
      ServerLevel level,
      EntityMaid maid,
      MaidGrillingData data,
      GrillAutomationApi.Snapshot snapshot) {
    if (snapshot.occupied() > 0) return;
    BlockPos grill = grillPos(level, data);
    if (grill != null) GrillAutomationApi.release(level, grill, maid.getUUID());
    MaidGrillingData.WaitReason reason = data(maid).waitReason();
    setData(
        maid,
        data.withWork(data.grill(), MaidGrillingData.WorkStage.RETURN_TOOLS)
            .withWaitReason(reason));
  }

  private static void showAndSwing(
      EntityMaid maid, ItemStack stack, MaidGrillingData.Action action, int duration) {
    maid.getHandItemsForAnimation()[InteractionHand.MAIN_HAND.ordinal()] = copyOne(stack);
    showAction(maid, stack, action, scaledDelay(duration));
    maid.swing(InteractionHand.MAIN_HAND);
  }

  private static void showAction(
      EntityMaid maid, ItemStack stack, MaidGrillingData.Action action, int duration) {
    ItemStack handItem =
        switch (action) {
          case PICKUP, IGNITE, INSERT, BRUSH, SEASON -> copyOne(stack);
          case FLIP, EXTRACT, EXTINGUISH, NONE -> ItemStack.EMPTY;
        };
    maid.getHandItemsForAnimation()[InteractionHand.MAIN_HAND.ordinal()] = handItem;
    setData(
        maid,
        data(maid)
            .withAction(
                action,
                maid.level().getGameTime() + duration,
                handItem));
  }

  private static void clearAction(EntityMaid maid) {
    maid.getHandItemsForAnimation()[InteractionHand.MAIN_HAND.ordinal()] = ItemStack.EMPTY;
    setData(maid, data(maid).withAction(MaidGrillingData.Action.NONE, 0L, ItemStack.EMPTY));
  }

  private void openContainer(ServerLevel level, EntityMaid maid, BlockPos pos) {
    maid.swing(InteractionHand.MAIN_HAND);
    if (pos.equals(openContainerPos)) {
      closeContainerAt = level.getGameTime() + 12L;
      return;
    }
    closeOpenContainer(level);
    var blockEntity = level.getBlockEntity(pos);
    var state = level.getBlockState(pos);
    animatedContainerOpen = false;
    if (blockEntity instanceof ChestBlockEntity
        && ChestBlockEntity.getOpenCount(level, pos) == 0) {
      level.blockEvent(pos, state.getBlock(), 1, 1);
      animatedContainerOpen = true;
    } else if (blockEntity instanceof BarrelBlockEntity
        && state.hasProperty(BlockStateProperties.OPEN)
        && !state.getValue(BlockStateProperties.OPEN)) {
      level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, true), 3);
      animatedContainerOpen = true;
    } else if (!(blockEntity instanceof ChestBlockEntity)
        && !(blockEntity instanceof BarrelBlockEntity)) {
      animatedContainerOpen = true;
    }
    if (animatedContainerOpen)
      level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, 1.0F);
    openContainerPos = pos.immutable();
    closeContainerAt = level.getGameTime() + 12L;
  }

  private void updateOpenContainer(ServerLevel level, long gameTime) {
    if (openContainerPos != null && gameTime >= closeContainerAt) closeOpenContainer(level);
  }

  private void closeOpenContainer(ServerLevel level) {
    if (openContainerPos == null) return;
    var blockEntity = level.getBlockEntity(openContainerPos);
    var state = level.getBlockState(openContainerPos);
    boolean closed = false;
    if (animatedContainerOpen
        && blockEntity instanceof ChestBlockEntity
        && ChestBlockEntity.getOpenCount(level, openContainerPos) == 0) {
      level.blockEvent(openContainerPos, state.getBlock(), 1, 0);
      closed = true;
    } else if (animatedContainerOpen
        && blockEntity instanceof BarrelBlockEntity
        && state.hasProperty(BlockStateProperties.OPEN)) {
      level.setBlock(openContainerPos, state.setValue(BlockStateProperties.OPEN, false), 3);
      closed = true;
    } else if (animatedContainerOpen
        && !(blockEntity instanceof ChestBlockEntity)
        && !(blockEntity instanceof BarrelBlockEntity)) {
      closed = true;
    }
    if (closed)
      level.playSound(
          null, openContainerPos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, 1.0F);
    openContainerPos = null;
    closeContainerAt = 0L;
    animatedContainerOpen = false;
  }

  private static int scaledDelay(int ticks) {
    return Math.max(1, (int) Math.round(ticks / HotFoodConfig.MAID_GRILL_ACTION_SPEED_MULTIPLIER.get()));
  }

  private static MaidGrillingData data(EntityMaid maid) {
    return maid.getOrCreateData(MaidGrillingData.KEY, MaidGrillingData.DEFAULT);
  }

  private static void setData(EntityMaid maid, MaidGrillingData data) {
    maid.setAndSyncData(MaidGrillingData.KEY, data);
  }

  private static void setWaitReason(EntityMaid maid, MaidGrillingData.WaitReason reason) {
    MaidGrillingData current = data(maid);
    if (current.waitReason() != reason) setData(maid, current.withWaitReason(reason));
  }

  private static void setSource(
      EntityMaid maid,
      MaidGrillingData data,
      int taskSlot,
      Optional<MaidGrillingData.BorrowSource> source) {
    setData(
        maid,
        switch (taskSlot) {
          case OIL_SLOT -> data.withSources(source, data.seasoningSource(), data.flintSource());
          case SEASONING_SLOT -> data.withSources(data.oilSource(), source, data.flintSource());
          case FLINT_SLOT -> data.withSources(data.oilSource(), data.seasoningSource(), source);
          default -> data;
        });
  }

  private static BlockPos grillPos(ServerLevel level, MaidGrillingData data) {
    return data.grill().map(bound -> localPos(level, bound)).orElse(null);
  }

  private static BlockPos localPos(ServerLevel level, MaidGrillingData.BoundContainer bound) {
    if (!level.dimension().location().equals(bound.dimension()) || !level.hasChunkAt(bound.pos())) return null;
    return bound.pos();
  }

  private static void reset(ServerLevel level, EntityMaid maid, MaidGrillingData data) {
    BlockPos grill = grillPos(level, data);
    if (grill != null) GrillAutomationApi.release(level, grill, maid.getUUID());
    setData(maid, data.resetBatch());
  }

  private record WirelessCandidate(
      MaidGrillingData.BoundContainer container, IItemHandler handler, int slot) {}

  private record WirelessDestination(
      MaidGrillingData.BoundContainer container, IItemHandler handler) {}

  private record RackCandidate(BlockPos pos, BlockPos approach, boolean reachable) {}

  private static ItemStack copyOne(ItemStack stack) {
    ItemStack copy = stack.copy();
    copy.setCount(Math.min(1, copy.getCount()));
    return copy;
  }
}
