package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.InteractionHand;

public interface AnvilPressAnimationAccess {
    void grilling$startAnvilPress();
    float grilling$getAnvilPressProgress(float partialTick);
    void grilling$startSeasoning();
    float grilling$getSeasoningProgress(float partialTick);
    void grilling$startOilBrush(InteractionHand hand, int brushType);
    float grilling$getOilBrushProgress(float partialTick);
    InteractionHand grilling$getOilBrushHand();
    int grilling$getOilBrushType();
}
