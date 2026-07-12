package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.ArrayList;
import java.util.List;

public final class GrillBlockEntity extends BlockEntity implements Container {
    public static final int SLOT_COUNT = 4;
    private static final int COOK_TICKS = 1200;
    private static final int FINISHED_TICKS = 800;
    private static final int BURNT_TICKS = 400;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int phase;
    private int phaseTicks;
    private int flips;
    private int flipCooldown;
    private boolean seasoned;
    private boolean failed;
    private int heatDurationTicks;
    private final List<String> seasoningIngredients = new ArrayList<>();

    public GrillBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.GRILL.get(), pos, state); }

    public static void tick(Level level, BlockPos pos, BlockState state, GrillBlockEntity grill) {
        if (grill.flipCooldown > 0) grill.flipCooldown--;
        if (grill.phase == 0 || grill.isEmpty()) return;
        grill.phaseTicks++;
        if (grill.phase == 1 && grill.phaseTicks >= COOK_TICKS) {
            grill.failed = grill.flips < 4;
            grill.phase = 2;
            grill.phaseTicks = 0;
            grill.sync();
        } else if (grill.phase == 2 && grill.phaseTicks >= FINISHED_TICKS) {
            grill.phase = 3;
            grill.phaseTicks = 0;
            grill.sync();
        } else if (grill.phase == 3 && grill.phaseTicks >= BURNT_TICKS) {
            grill.clearContent();
            grill.resetProcess();
            grill.sync();
        }
    }

    public boolean canAccept(ItemStack stack) { return phase == 0 && items.stream().anyMatch(ItemStack::isEmpty) && (ModItems.RAW_SKEWERS.stream().anyMatch(i -> stack.is(i.get())) || (stack.is(ModItems.SECRET_SKEWER.get()) && !SecretSkewerItem.isCooked(stack))); }
    public void insert(ItemStack held, Player player) {
        for (int i = 0; i < items.size(); i++) if (items.get(i).isEmpty()) {
            items.set(i, held.copyWithCount(1));
            if (!player.getAbilities().instabuild) held.shrink(1);
            sync();
            return;
        }
    }
    public int brushOil(int heatTicks) { if (phase != 0 || isEmpty()) return 0; phase = 1; phaseTicks = 0; heatDurationTicks = heatTicks; sync(); return occupiedSlots(); }
    public boolean canFlip() { return phase == 1 && flipCooldown == 0; }
    public void flip() { if (!canFlip()) return; flips++; flipCooldown = 20; sync(); }
    public int season(ItemStack seasoning) { if (phase != 2 || seasoned) return 0; seasoned = true; seasoningIngredients.clear(); seasoningIngredients.addAll(SeasoningData.get(seasoning)); sync(); return occupiedSlots(); }
    public int seasonableCount() { return phase == 2 && !seasoned ? occupiedSlots() : 0; }
    public boolean canExtractNormally() { return phase == 2 && seasoned && !failed; }
    public boolean canExtract() { return (phase == 2 && seasoned) || phase == 3; }
    public boolean extractOne(Player player) {
        if (!canExtract()) return false;
        for (int i = 0; i < items.size(); i++) {
            ItemStack input = items.get(i);
            if (input.isEmpty()) continue;
            ItemStack output;
            if (phase == 3) output = new ItemStack(ModItems.DARK_GRILLING.get());
            else if (failed) output = new ItemStack(ModItems.MYSTERIOUS_SKEWER.get());
            else if (input.is(ModItems.SECRET_SKEWER.get())) {
                output = input.copy();
                SecretSkewerItem.setCooked(output, true);
                CustomData data = input.get(DataComponents.CUSTOM_DATA);
                if (data != null) output.set(DataComponents.CUSTOM_DATA, data);
                SeasoningData.set(output, seasoningIngredients);
                if (level != null) FoodState.setHot(output, level.getGameTime() + heatDurationTicks);
            } else {
                ResourceLocation rawId = BuiltInRegistries.ITEM.getKey(input.getItem());
                String cookedPath = rawId.getPath().replaceFirst("^raw_", "grilled_");
                output = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, cookedPath)));
                CustomData data = input.get(DataComponents.CUSTOM_DATA);
                if (data != null) output.set(DataComponents.CUSTOM_DATA, data);
                SeasoningData.set(output, seasoningIngredients);
                if (level != null) FoodState.setHot(output, level.getGameTime() + heatDurationTicks);
            }
            items.set(i, ItemStack.EMPTY);
            player.getInventory().placeItemBackInInventory(output);
            if (isEmpty()) resetProcess();
            sync();
            return true;
        }
        return false;
    }
    public boolean isFailed() { return failed; }
    public int getPhase() { return phase; }
    public int getFlips() { return flips; }
    private int occupiedSlots() { return (int) items.stream().filter(s -> !s.isEmpty()).count(); }
    private void resetProcess() { phase = phaseTicks = flips = flipCooldown = heatDurationTicks = 0; seasoned = failed = false; seasoningIngredients.clear(); }
    private void sync() { setChanged(); if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); ContainerHelper.saveAllItems(tag, items, registries); tag.putInt("Phase", phase); tag.putInt("PhaseTicks", phaseTicks); tag.putInt("Flips", flips); tag.putInt("FlipCooldown", flipCooldown); tag.putInt("HeatDuration",heatDurationTicks); tag.putBoolean("Seasoned", seasoned); tag.putBoolean("Failed", failed); ListTag list=new ListTag();seasoningIngredients.forEach(v->list.add(StringTag.valueOf(v)));tag.put("SeasoningIngredients",list); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY); ContainerHelper.loadAllItems(tag, items, registries); phase = tag.getInt("Phase"); phaseTicks = tag.getInt("PhaseTicks"); flips = tag.getInt("Flips"); flipCooldown = tag.getInt("FlipCooldown"); heatDurationTicks=tag.getInt("HeatDuration"); seasoned = tag.getBoolean("Seasoned"); failed = tag.getBoolean("Failed"); seasoningIngredients.clear();ListTag list=tag.getList("SeasoningIngredients",8);for(int i=0;i<list.size();i++)seasoningIngredients.add(list.getString(i)); }
    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack result = ContainerHelper.removeItem(items, slot, amount); if (!result.isEmpty()) sync(); return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); stack.limitSize(1); sync(); }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent() { items.clear(); }
}
