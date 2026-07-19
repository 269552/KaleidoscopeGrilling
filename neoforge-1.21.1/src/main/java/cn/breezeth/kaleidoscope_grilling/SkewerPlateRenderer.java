package cn.breezeth.kaleidoscope_grilling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class SkewerPlateRenderer implements BlockEntityRenderer<SkewerPlateBlockEntity> {
    private static final float[][] SLOTS = {
            {8F, 3F, 6.25F, 0F},
            {12F, 3F, 6.25F, 0F},
            {4F, 3F, 6.25F, 0F},
            {6F, 5.5F, 6.25F, -22.5F},
            {10F, 5.5F, 6.25F, -22.5F}
    };

    public SkewerPlateRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(SkewerPlateBlockEntity plate, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        int facing = plate.getBlockState().getValue(SkewerPlateBlock.FACING).get2DDataValue();
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-facing * 90F));
        pose.translate(-0.5, 0, -0.5);
        var skewers = plate.copySkewers();
        for (int i = 0; i < skewers.size() && i < SLOTS.length; i++) {
            ItemStack stack = skewers.get(i);
            float[] slot = SLOTS[i];
            pose.pushPose();
            pose.translate(slot[0] / 16F, slot[1] / 16F, slot[2] / 16F);
            pose.mulPose(Axis.YP.rotationDegrees(slot[3]));
            pose.mulPose(Axis.XP.rotationDegrees(90F));
            if (stack.is(ModItems.SECRET_SKEWER.get())) pose.scale(2F / 3F, 2F / 3F, 2F / 3F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED,
                    light, overlay, pose, buffers, plate.getLevel(), i);
            pose.popPose();
        }
        pose.popPose();
    }
}
