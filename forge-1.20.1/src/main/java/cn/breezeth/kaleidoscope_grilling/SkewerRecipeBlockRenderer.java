package cn.breezeth.kaleidoscope_grilling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class SkewerRecipeBlockRenderer implements BlockEntityRenderer<SkewerRecipeBlockEntity> {
    private final BlockEntityRendererProvider.Context context;

    public SkewerRecipeBlockRenderer(BlockEntityRendererProvider.Context context) { this.context = context; }

    @Override public void render(SkewerRecipeBlockEntity recipe, float partialTick, PoseStack pose,
                                 MultiBufferSource buffers, int light, int overlay) {
        if (recipe.recipeResult().isEmpty()) return;
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(recipe.recipeResult()));
        if (item == null) return;
        ItemStack result = new ItemStack(item);
        Direction facing = recipe.getBlockState().getValue(SkewerRecipeBlock.FACING);
        pose.pushPose();
        pose.translate(0.5D, 0.5D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.get2DDataValue() * 90.0F));
        pose.translate(-0.5D, -0.5D, -0.5D);
        pose.scale(0.5F, 0.5F, 0.5F);
        pose.translate(1.0D, 1.25D, 0.0D);
        context.getItemRenderer().renderStatic(result, ItemDisplayContext.FIXED, light, overlay,
                pose, buffers, recipe.getLevel(), 0);
        pose.popPose();
    }
}
