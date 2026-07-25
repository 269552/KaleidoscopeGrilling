package cn.breezeth.kaleidoscope_grilling;

/** Shared protocol constants for the explicit hot-food merge inventory action. */
public final class HotFoodMerge {
  public static final int MENU_BUTTON_BASE = 0x4B470000;

  public static int menuButtonForSlot(int slot) {
    return MENU_BUTTON_BASE + slot;
  }

  public static int slotFromMenuButton(int button) {
    return button - MENU_BUTTON_BASE;
  }

  public static boolean isMergeButton(int button) {
    return button >= MENU_BUTTON_BASE && button < MENU_BUTTON_BASE + 1000;
  }

  private HotFoodMerge() {}
}
