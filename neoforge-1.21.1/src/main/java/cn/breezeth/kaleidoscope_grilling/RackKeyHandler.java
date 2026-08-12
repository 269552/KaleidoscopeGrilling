package cn.breezeth.kaleidoscope_grilling;


import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

public final class RackKeyHandler {
  public static final KeyMapping KEY =
      new KeyMapping(
          "key.kaleidoscope_grilling.rack_swap",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_CAPS_LOCK,
          "key.categories.kaleidoscope_grilling");
  private static boolean held;
  private static boolean capsHeld;
  private static boolean mergeClickConsumed;

  @EventBusSubscriber(
      modid = KaleidoscopeGrilling.MOD_ID,
      bus = EventBusSubscriber.Bus.MOD,
      value = Dist.CLIENT)
  public static final class Registration {
    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
      event.register(KEY);
    }
  }

  @EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
  public static final class Input {
    @SubscribeEvent
    public static void key(InputEvent.Key event) {
      if (!KEY.matches(event.getKey(), event.getScanCode())) return;
      if (event.getAction() == GLFW.GLFW_PRESS) {
        capsHeld = true;
        beginHold();
      } else if (event.getAction() == GLFW.GLFW_RELEASE) {
        capsHeld = false;
        finishHold();
      }
    }

    @SubscribeEvent
    public static void mousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
      if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT
          || !capsHeld
          || !(event.getScreen() instanceof AbstractContainerScreen<?> screen)
          || screen.getMenu() instanceof AdvancedRackMenu) return;
      Minecraft minecraft = Minecraft.getInstance();
      Slot slot = screen.getSlotUnderMouse();
      boolean allowedTarget =
          minecraft.player != null
              && slot != null
              && (HotFoodMerge.isAllowedClientTarget(
                      minecraft.player, screen.getMenu(), slot, screen.getTitle())
                  || (screen instanceof CreativeModeInventoryScreen
                      && HotFoodMerge.isAllowedCreativeInventoryTarget(
                          minecraft.player, screen.getMenu(), slot)));
      if (minecraft.level == null
          || slot == null
          || minecraft.player == null
          || !allowedTarget
          || !slot.hasItem()
          || screen.getMenu().getCarried().isEmpty()
          || !FoodState.canMergeHot(slot.getItem(), screen.getMenu().getCarried(), minecraft.level)) return;
      int menuSlot = screen.getMenu().slots.indexOf(slot);
      if (menuSlot < 0) return;
      if (minecraft.player.isCreative()
          && HotFoodMerge.isPlayerInventorySlot(minecraft.player, slot)) {
        ItemStack target = slot.getItem().copy();
        ItemStack carried = screen.getMenu().getCarried().copy();
        if (FoodState.mergeHot(target, carried, minecraft.level) <= 0) return;
        Slot inventorySlot =
            minecraft.player.inventoryMenu.slots.stream()
                .filter(
                    candidate ->
                        candidate.container == slot.container
                            && candidate.getSlotIndex() == slot.getSlotIndex())
                .findFirst()
                .orElse(null);
        if (inventorySlot == null || minecraft.gameMode == null) return;
        slot.set(target);
        screen.getMenu().setCarried(carried);
        minecraft.gameMode.handleCreativeModeItemAdd(target.copy(), inventorySlot.index);
      } else {
        if (minecraft.gameMode == null) return;
        minecraft.gameMode.handleInventoryButtonClick(
            screen.getMenu().containerId, HotFoodMerge.menuButtonForSlot(menuSlot));
      }
      mergeClickConsumed = true;
      event.setCanceled(true);
    }

    @SubscribeEvent
    public static void mouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
      if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT || !mergeClickConsumed) return;
      mergeClickConsumed = false;
      event.setCanceled(true);
    }
  }

  public static boolean isHeld() {
    return held;
  }

  public static boolean isCapsHeld() {
    return capsHeld;
  }

  public static void finishHold() {
    held = false;
  }

  private static void beginHold() {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.screen != null
        || minecraft.player == null
        || minecraft.player.isSpectator()
        || minecraft.getConnection() == null) return;
    held = true;
    minecraft.getConnection().sendCommand("kgrack");
  }

  private RackKeyHandler() {}
}
