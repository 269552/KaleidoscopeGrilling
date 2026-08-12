package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

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

  public static boolean isAllowedTarget(
      Player player, AbstractContainerMenu menu, Slot slot) {
    boolean supportedMenu =
        menu == player.inventoryMenu || isSupportedStorageMenu(menu);
    if (!supportedMenu
        || !slot.isActive()
        || !slot.mayPickup(player)
        || !slot.mayPlace(menu.getCarried())) return false;
    int index = slot.getSlotIndex();
    if (slot.container == player.getInventory()) return index >= 0 && index < 36;
    return isSupportedStorageMenu(menu)
        && isSupportedStorage(slot.container)
        && index >= 0
        && index < slot.container.getContainerSize();
  }

  public static boolean isAllowedCreativeInventoryTarget(
      Player player, AbstractContainerMenu menu, Slot slot) {
    return player.isCreative()
        && isPlayerInventorySlot(player, slot)
        && slot.isActive()
        && slot.mayPickup(player)
        && slot.mayPlace(menu.getCarried());
  }

  public static boolean isAllowedClientTarget(
      Player player, AbstractContainerMenu menu, Slot slot, Component title) {
    boolean storageMenu = isClientStorageMenu(menu, title);
    if (!(menu == player.inventoryMenu || storageMenu)
        || !slot.isActive()
        || !slot.mayPickup(player)
        || !slot.mayPlace(menu.getCarried())) return false;
    int index = slot.getSlotIndex();
    if (slot.container == player.getInventory()) return index >= 0 && index < 36;
    return storageMenu && index >= 0 && index < slot.container.getContainerSize();
  }

  public static boolean isPlayerInventorySlot(Player player, Slot slot) {
    int index = slot.getSlotIndex();
    return slot.container == player.getInventory() && index >= 0 && index < 36;
  }

  private static boolean isSupportedStorageMenu(AbstractContainerMenu menu) {
    return menu instanceof ChestMenu
        && !menu.slots.isEmpty()
        && isSupportedStorage(menu.slots.getFirst().container);
  }

  private static boolean isClientStorageMenu(AbstractContainerMenu menu, Component title) {
    return menu instanceof ChestMenu
        && !(title.getContents() instanceof TranslatableContents contents
            && "container.enderchest".equals(contents.getKey()));
  }

  private static boolean isSupportedStorage(Object container) {
    return container instanceof ChestBlockEntity
        || container instanceof BarrelBlockEntity
        || container instanceof CompoundContainer;
  }

  private HotFoodMerge() {}
}
