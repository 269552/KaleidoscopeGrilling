package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class RackSelectionMenu extends AbstractContainerMenu {
    public static final int DEPOSIT_ALL_BUTTON = 9;
    private final List<BlockPos> positions;
    private final ContainerData state;

    public RackSelectionMenu(int id, Inventory inventory) {
        this(id, inventory, List.of(), new SimpleContainerData(1));
    }

    public RackSelectionMenu(int id, Inventory inventory, List<BlockPos> positions) {
        this(id, inventory, List.copyOf(positions), new ContainerData() {
            @Override public int get(int index) { return positions.size(); }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 1; }
        });
    }

    private RackSelectionMenu(int id, Inventory inventory, List<BlockPos> positions, ContainerData state) {
        super(ModMenus.RACK_SELECTION.get(), id);
        this.positions = positions;
        this.state = state;
        SimpleContainer icons = new SimpleContainer(9);
        if (!positions.isEmpty()) {
            for (int i = 0; i < positions.size() && i < 9; i++) icons.setItem(i, ModBlocks.ADVANCED_RACK_ITEM.get().getDefaultInstance());
        }
        addDataSlots(state);
        for (int i = 0; i < 5; i++) addSlot(readOnly(icons, i, 43 + i * 18, 25));
        for (int i = 0; i < 4; i++) addSlot(readOnly(icons, 5 + i, 52 + i * 18, 49));
    }

    private static Slot readOnly(SimpleContainer container, int slot, int x, int y) {
        return new Slot(container, slot, x, y) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return false; }
        };
    }

    public int rackCount() { return state.get(0); }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        if (id >= 0 && id < positions.size()) {
            BlockPos pos = positions.get(id);
            AdvancedRackBlockEntity rack = validRack(serverPlayer, pos);
            if (rack != null) serverPlayer.openMenu(rack.shortcutMenu());
            else serverPlayer.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.rack_not_found"), true);
            return true;
        }
        if (id == DEPOSIT_ALL_BUTTON) {
            boolean moved = false;
            for (BlockPos pos : positions) {
                AdvancedRackBlockEntity rack = validRack(serverPlayer, pos);
                if (rack != null) moved |= rack.depositMatching(serverPlayer);
            }
            if (!moved) serverPlayer.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.rack_nothing_to_deposit"), true);
            return true;
        }
        return false;
    }

    private static AdvancedRackBlockEntity validRack(ServerPlayer player, BlockPos pos) {
        if (!player.level().hasChunkAt(pos) || player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) > 64) return null;
        return player.level().getBlockEntity(pos) instanceof AdvancedRackBlockEntity rack ? rack : null;
    }

    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override
    public boolean stillValid(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return rackCount() > 0;
        for (BlockPos pos : positions) if (validRack(serverPlayer, pos) != null) return true;
        return false;
    }

    public static MenuProvider provider(List<BlockPos> positions) {
        return new MenuProvider() {
            @Override public Component getDisplayName() { return Component.translatable("container.kaleidoscope_grilling.rack_selection"); }
            @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new RackSelectionMenu(id, inventory, positions); }
        };
    }
}
