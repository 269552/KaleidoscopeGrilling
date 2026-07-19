package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public final class SkewerOutlineRender {
    public static final int OUTLINE_TINT = 384;
    private static final double OUTLINE_DEPTH = -50.0D;
    private static final ThreadLocal<Integer> COLOR = new ThreadLocal<>();
    private static final int[][] OFFSETS = {
            {-1, -1}, {0, -1}, {1, -1},
            {-1, 0},           {1, 0},
            {-1, 1},  {0, 1},  {1, 1}
    };

    public static boolean isActive() {
        return COLOR.get() != null;
    }

    public static int color() {
        Integer color = COLOR.get();
        return color == null ? 0xFFFFFF : color;
    }

    public static void renderItem(GuiGraphics graphics, ItemStack stack, int x, int y, int color) {
        if (!isCustomSkewer(stack)) {
            graphics.renderItem(stack, x, y);
            return;
        }
        double guiScale = Math.max(1.0D, Minecraft.getInstance().getWindow().getGuiScale());
        double physicalPixel = 2.0D / guiScale;
        COLOR.set(color & 0xFFFFFF);
        try {
            for (int[] offset : OFFSETS) {
                graphics.pose().pushPose();
                try {
                    graphics.pose().translate(offset[0] * physicalPixel, offset[1] * physicalPixel, OUTLINE_DEPTH);
                    graphics.renderItem(stack, x, y);
                } finally {
                    graphics.pose().popPose();
                }
            }
        } finally {
            COLOR.remove();
        }
        graphics.renderItem(stack, x, y);
    }

    public static boolean isCustomSkewer(ItemStack stack) {
        return stack.is(ModItems.UNFINISHED_SKEWER.get()) || stack.is(ModItems.SECRET_SKEWER.get())
                || SkewerRecipes.isRawSkewer(stack) || SkewerRecipes.isCookedSkewer(stack);
    }

    private SkewerOutlineRender() {}
}
