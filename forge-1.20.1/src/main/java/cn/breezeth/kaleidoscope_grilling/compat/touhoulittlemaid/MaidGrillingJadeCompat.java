package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.config.IPluginConfig;

public final class MaidGrillingJadeCompat {
  private static final ResourceLocation UID =
      new ResourceLocation("kaleidoscope_grilling", "maid_grilling");

  public static void register(IWailaClientRegistration registration) {
    registration.registerEntityComponent(Provider.INSTANCE, EntityMaid.class);
  }

  private enum Provider implements IEntityComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(
        ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
      if (!(accessor.getEntity() instanceof EntityMaid maid)
          || MaidGrillingData.KEY == null
          || maid.getTask() == null
          || !maid.getTask().getUid().equals(MaidGrillingTask.UID)) return;
      MaidGrillingData data =
          maid.getOrCreateData(MaidGrillingData.KEY, MaidGrillingData.DEFAULT);
      String statusKey;
      if (data.action() != MaidGrillingData.Action.NONE)
        statusKey = "gui.kaleidoscope_grilling.maid_grilling.action." + data.action().serializedName();
      else if (data.waitReason() != MaidGrillingData.WaitReason.NONE)
        statusKey =
            "gui.kaleidoscope_grilling.maid_grilling.wait." + data.waitReason().serializedName();
      else
        statusKey = "gui.kaleidoscope_grilling.maid_grilling.stage." + data.stage().serializedName();
      tooltip.add(
          Component.translatable(
              "jade.kaleidoscope_grilling.maid_grilling.stage",
              Component.translatable(statusKey)));
      tooltip.add(
          Component.translatable(
              "jade.kaleidoscope_grilling.maid_grilling.wireless_io",
              MaidGrillingWirelessIO.hasEndpoint(maid) ? "\u2713" : "\u274c"));
    }

    @Override
    public ResourceLocation getUid() {
      return UID;
    }
  }

  private MaidGrillingJadeCompat() {}
}
