package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.api.entity.data.TaskDataKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record MaidGrillingData(
    Optional<BoundContainer> grill,
    WorkStage stage,
    Optional<BorrowSource> oilSource,
    Optional<BorrowSource> seasoningSource,
    Optional<BorrowSource> flintSource,
    WaitReason waitReason,
    Action action,
    long actionUntil,
    ItemStack displayItem,
    boolean completedBatch) {
  public static final MaidGrillingData DEFAULT =
      new MaidGrillingData(
          Optional.empty(),
          WorkStage.FIND_GRILL,
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          WaitReason.NONE,
          Action.NONE,
          0L,
          ItemStack.EMPTY,
          false);

  public static final Codec<MaidGrillingData> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      BoundContainer.CODEC.optionalFieldOf("Grill").forGetter(MaidGrillingData::grill),
                      Codec.STRING
                          .optionalFieldOf("Stage", WorkStage.FIND_GRILL.serializedName)
                          .xmap(WorkStage::byName, stage -> stage.serializedName)
                          .forGetter(MaidGrillingData::stage),
                      BorrowSource.CODEC.optionalFieldOf("OilSource").forGetter(MaidGrillingData::oilSource),
                      BorrowSource.CODEC.optionalFieldOf("SeasoningSource").forGetter(MaidGrillingData::seasoningSource),
                      BorrowSource.CODEC.optionalFieldOf("FlintSource").forGetter(MaidGrillingData::flintSource),
                      Codec.STRING
                          .optionalFieldOf("WaitReason", WaitReason.NONE.serializedName)
                          .xmap(WaitReason::byName, reason -> reason.serializedName)
                          .forGetter(MaidGrillingData::waitReason),
                      Codec.STRING
                          .optionalFieldOf("Action", Action.NONE.serializedName)
                          .xmap(Action::byName, action -> action.serializedName)
                          .forGetter(MaidGrillingData::action),
                      Codec.LONG.optionalFieldOf("ActionUntil", 0L).forGetter(MaidGrillingData::actionUntil),
                      ItemStack.OPTIONAL_CODEC.optionalFieldOf("DisplayItem", ItemStack.EMPTY).forGetter(MaidGrillingData::displayItem),
                      Codec.BOOL.optionalFieldOf("CompletedBatch", false).forGetter(MaidGrillingData::completedBatch))
                  .apply(instance, MaidGrillingData::new));

  public static TaskDataKey<MaidGrillingData> KEY;

  public MaidGrillingData withWork(Optional<BoundContainer> grill, WorkStage stage) {
    return new MaidGrillingData(
        grill,
        stage,
        oilSource,
        seasoningSource,
        flintSource,
        WaitReason.NONE,
        action,
        actionUntil,
        displayItem,
        completedBatch);
  }

  public MaidGrillingData withSources(
      Optional<BorrowSource> oil,
      Optional<BorrowSource> seasoning,
      Optional<BorrowSource> flint) {
    return new MaidGrillingData(
        grill, stage, oil, seasoning, flint, waitReason, action, actionUntil, displayItem, completedBatch);
  }

  public MaidGrillingData withWaitReason(WaitReason reason) {
    return new MaidGrillingData(
        grill,
        stage,
        oilSource,
        seasoningSource,
        flintSource,
        reason,
        action,
        actionUntil,
        displayItem,
        completedBatch);
  }

  public MaidGrillingData withAction(Action action, long actionUntil, ItemStack displayItem) {
    return new MaidGrillingData(
        grill,
        stage,
        oilSource,
        seasoningSource,
        flintSource,
        waitReason,
        action,
        actionUntil,
        displayItem.copyWithCount(1),
        completedBatch);
  }

  public MaidGrillingData withCompletedBatch() {
    return new MaidGrillingData(
        grill,
        stage,
        oilSource,
        seasoningSource,
        flintSource,
        waitReason,
        action,
        actionUntil,
        displayItem,
        true);
  }

  public MaidGrillingData resetBatch() {
    return new MaidGrillingData(
        Optional.empty(),
        WorkStage.FIND_GRILL,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        WaitReason.NONE,
        Action.NONE,
        0L,
        ItemStack.EMPTY,
        false);
  }

  public record BoundContainer(ResourceLocation dimension, BlockPos pos) {
    public static final Codec<BoundContainer> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        ResourceLocation.CODEC.fieldOf("Dimension").forGetter(BoundContainer::dimension),
                        BlockPos.CODEC.fieldOf("Pos").forGetter(BoundContainer::pos))
                    .apply(instance, BoundContainer::new));
  }

  public enum WorkStage {
    FIND_GRILL("find_grill"),
    WAIT_CHAIR("wait_chair"),
    WAIT_INPUT("wait_input"),
    GATHER_INPUT("gather_input"),
    GATHER_TOOLS("gather_tools"),
    WORK_GRILL("work_grill"),
    RETURN_TOOLS("return_tools"),
    STORE_OUTPUT("store_output");

    private final String serializedName;

    WorkStage(String serializedName) {
      this.serializedName = serializedName;
    }

    static WorkStage byName(String name) {
      for (WorkStage value : values()) if (value.serializedName.equals(name)) return value;
      return FIND_GRILL;
    }

    public String serializedName() {
      return serializedName;
    }
  }

  public enum WaitReason {
    NONE("none"),
    NO_GRILL("no_grill"),
    GRILL_BUSY("grill_busy"),
    GRILL_MISSING("grill_missing"),
    MISSING_CHAIR("missing_chair"),
    CHAIR_UNREACHABLE("chair_unreachable"),
    MISSING_RAW_SKEWER("missing_raw_skewer"),
    SUPPLY_UNREACHABLE("supply_unreachable"),
    MISSING_FLINT("missing_flint"),
    MISSING_OIL("missing_oil"),
    MISSING_SEASONING("missing_seasoning"),
    INVENTORY_FULL("inventory_full"),
    APPROACHING_TOOL_SOURCE("approaching_tool_source"),
    TOOL_SOURCE_UNREACHABLE("tool_source_unreachable"),
    WAITING_FLIP("waiting_flip"),
    OUTPUT_UNREACHABLE("output_unreachable"),
    OUTPUT_FULL("output_full"),
    INTERRUPTED("interrupted");

    private final String serializedName;

    WaitReason(String serializedName) {
      this.serializedName = serializedName;
    }

    static WaitReason byName(String name) {
      for (WaitReason value : values())
        if (value.serializedName.equals(name)) return value;
      return NONE;
    }

    public String serializedName() {
      return serializedName;
    }
  }

  public enum Action {
    NONE("none", 1),
    PICKUP("pickup", 10),
    IGNITE("ignite", 20),
    INSERT("insert", 10),
    BRUSH("brush", 20),
    FLIP("flip", 40),
    SEASON("season", 20),
    EXTRACT("extract", 10),
    EXTINGUISH("extinguish", 20);

    private final String serializedName;
    private final int duration;

    Action(String serializedName, int duration) {
      this.serializedName = serializedName;
      this.duration = duration;
    }

    public int duration() {
      return duration;
    }

    public String serializedName() {
      return serializedName;
    }

    static Action byName(String name) {
      for (Action value : values()) if (value.serializedName.equals(name)) return value;
      return NONE;
    }
  }

  public record BorrowSource(
      BoundContainer container,
      int slot,
      int maidSlot,
      boolean advancedRack,
      boolean wireless,
      boolean owned) {
    public static final Codec<BorrowSource> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        BoundContainer.CODEC.fieldOf("Container").forGetter(BorrowSource::container),
                        Codec.INT.fieldOf("Slot").forGetter(BorrowSource::slot),
                        Codec.INT.optionalFieldOf("MaidSlot", -1).forGetter(BorrowSource::maidSlot),
                        Codec.BOOL.optionalFieldOf("AdvancedRack", false).forGetter(BorrowSource::advancedRack),
                        Codec.BOOL.optionalFieldOf("Wireless", false).forGetter(BorrowSource::wireless),
                        Codec.BOOL.optionalFieldOf("Owned", false).forGetter(BorrowSource::owned))
                    .apply(instance, BorrowSource::new));
  }
}
