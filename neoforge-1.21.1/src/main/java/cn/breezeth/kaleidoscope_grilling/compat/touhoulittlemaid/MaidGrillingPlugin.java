package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.entity.data.TaskDataRegister;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.GeckoEntityMaidRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.Mob;

@LittleMaidExtension
public final class MaidGrillingPlugin implements ILittleMaid {
  @Override
  public void addMaidTask(TaskManager manager) {
    manager.add(new MaidGrillingTask());
  }

  @Override
  public void registerTaskData(TaskDataRegister register) {
    MaidGrillingData.KEY = register.register(MaidGrillingTask.UID, MaidGrillingData.CODEC);
  }

  @Override
  public void addAdditionMaidLayer(EntityMaidRenderer renderer, Context context) {
    renderer.addLayer(new MaidGrillingHeldItemLayer(renderer, context.getItemInHandRenderer()));
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void addAdditionGeckoMaidLayer(
      GeckoEntityMaidRenderer<? extends Mob> renderer, Context context) {
    renderer.addGeoLayerRenderer(
        new MaidGrillingGeckoHeldItemLayer(renderer, context.getItemInHandRenderer()));
  }

}
