package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.entity.data.TaskDataRegister;

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

}
