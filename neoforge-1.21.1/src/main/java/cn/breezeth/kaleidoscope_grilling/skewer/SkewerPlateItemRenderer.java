package cn.breezeth.kaleidoscope_grilling.skewer;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;

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
  private static final float[][] SLOTS = {
    {8F, 3F, 6.25F, 0F},
    {12F, 3F, 6.25F, 0F},
    {4F, 3F, 6.25F, 0F},
    {6F, 5.5F, 6.25F, -22.5F},
    {10F, 5.5F, 6.25F, -22.5F}
  };
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
    for (int i = 0; i < skewers.size() && i < SLOTS.length; i++) {
      ItemStack skewer = skewers.get(i);
      float[] slot = SLOTS[i];
      pose.pushPose();
      pose.translate(slot[0] / 16F, slot[1] / 16F, slot[2] / 16F);
      pose.mulPose(Axis.YP.rotationDegrees(slot[3]));
      pose.mulPose(Axis.XP.rotationDegrees(90F));
      if (skewer.is(ModItems.SECRET_SKEWER.get())) pose.scale(2F / 3F, 2F / 3F, 2F / 3F);
      minecraft
          .getItemRenderer()
          .renderStatic(
              skewer, ItemDisplayContext.FIXED, light, overlay, pose, buffers, minecraft.level, i);
      pose.popPose();
    }
  }
}
