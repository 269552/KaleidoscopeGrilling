package cn.breezeth.kaleidoscope_grilling.skewer;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class SkewerPlateItemRenderer extends BlockEntityWithoutLevelRenderer {
  private static final ResourceLocation PLATE_MODEL =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "item/skewer_plate_base");
  private static SkewerPlateItemRenderer instance;

  private SkewerPlateItemRenderer() {
    super(
        Minecraft.getInstance().getBlockEntityRenderDispatcher(),
        Minecraft.getInstance().getEntityModels());
  }

  public static SkewerPlateItemRenderer instance() {
    if (instance == null) instance = new SkewerPlateItemRenderer();
    return instance;
  }

  @Override
  public void renderByItem(
      ItemStack plate,
      ItemDisplayContext context,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    Minecraft minecraft = Minecraft.getInstance();
    BakedModel plateModel =
        minecraft.getModelManager().getModel(ModelResourceLocation.standalone(PLATE_MODEL));
    pose.pushPose();
    pose.translate(0.5F, 0.5F, 0.5F);
    minecraft
        .getItemRenderer()
        .render(plate, ItemDisplayContext.NONE, false, pose, buffers, light, overlay, plateModel);
    pose.popPose();

    var skewers = SkewerPlateItem.read(plate);
    float[][] slots = SkewerPlateRenderer.slotsFor(skewers.size());
    for (int i = 0; i < skewers.size() && i < slots.length; i++) {
      ItemStack skewer = skewers.get(i);
      float[] slot = slots[i];
      pose.pushPose();
      pose.translate(slot[0] / 16F, slot[1] / 16F, slot[2] / 16F);
      pose.mulPose(Axis.YP.rotationDegrees(slot[3]));
      pose.mulPose(Axis.XP.rotationDegrees(90F));
      pose.scale(4F / 5F, 4F / 5F, 4F / 5F);
      minecraft
          .getItemRenderer()
          .renderStatic(
              skewer, ItemDisplayContext.FIXED, light, overlay, pose, buffers, minecraft.level, i);
      pose.popPose();
    }
  }
}
