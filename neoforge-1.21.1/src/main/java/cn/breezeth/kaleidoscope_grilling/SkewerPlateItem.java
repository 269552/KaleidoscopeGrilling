package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class SkewerPlateItem extends Item {
    public SkewerPlateItem(Properties properties) { super(properties); }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return SkewerPlateItemRenderer.instance();
            }
        });
    }

    public static boolean isSkewer(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof SkewerItem
                || stack.is(ModItems.SECRET_SKEWER.get())
                || SkewerRecipes.isRawSkewer(stack)
                || stack.is(SkewerCompatApi.GRILLED_SKEWERS));
    }

    public static ItemStack create(List<ItemStack> skewers) {
        ItemStack result = new ItemStack(ModItems.SKEWER_PLATE.get());
        write(result, skewers);
        return result;
    }

    public static List<ItemStack> read(ItemStack plate) {
        NonNullList<ItemStack> stored = NonNullList.withSize(SkewerPlateBlockEntity.CAPACITY, ItemStack.EMPTY);
        plate.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(stored);
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : stored)
            if (isSkewer(stack) && result.size() < SkewerPlateBlockEntity.CAPACITY) result.add(stack.copyWithCount(1));
        return result;
    }

    private static void write(ItemStack plate, List<ItemStack> skewers) {
        List<ItemStack> stored = new ArrayList<>();
        for (int i = 0; i < Math.min(SkewerPlateBlockEntity.CAPACITY, skewers.size()); i++) {
            ItemStack stack = skewers.get(i);
            if (isSkewer(stack)) stored.add(stack.copyWithCount(1));
        }
        plate.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stored));
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() || read(stack).isEmpty()) return InteractionResultHolder.pass(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) return InteractionResult.PASS;
        return SkewerPlatePlacement.placePacked(context, read(context.getItemInHand()));
    }

    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 16; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.EAT; }

    @Override
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        ItemStack selected = highestNutrition(stack, entity);
        FoodProperties food = selected.isEmpty() ? null : selected.getItem().getFoodProperties(selected, entity);
        return food == null ? new FoodProperties.Builder().nutrition(1).saturationModifier(0).build() : food;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack plate, Level level, LivingEntity entity) {
        List<ItemStack> skewers = read(plate);
        int selected = highestNutritionIndex(skewers, entity);
        if (selected < 0) return plate;
        ItemStack eaten = skewers.remove(selected);
        ItemStack consumed = eaten.copy();
        ItemStack remainder = HotFoodHandler.finishNested(eaten, entity);
        if (entity instanceof Player player && !remainder.isEmpty()
                && !ItemStack.isSameItemSameComponents(remainder, consumed))
            player.getInventory().placeItemBackInInventory(remainder);
        ModAdvancements.recordFoodFinished(entity, consumed);
        if (skewers.isEmpty()) return ItemStack.EMPTY;
        write(plate, skewers);
        return plate;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        List<ItemStack> skewers = read(stack);
        tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.skewer_plate.count",
                skewers.size(), SkewerPlateBlockEntity.CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.skewer_plate.use")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.skewer_plate.place")
                .withStyle(ChatFormatting.DARK_GRAY));
        if (!skewers.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.skewer_plate.contents")
                    .withStyle(ChatFormatting.DARK_GRAY));
            for (ItemStack skewer : skewers)
                tooltip.add(Component.literal("- ").append(skewer.getHoverName()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static ItemStack highestNutrition(ItemStack plate, @Nullable LivingEntity entity) {
        List<ItemStack> skewers = read(plate);
        int index = highestNutritionIndex(skewers, entity);
        return index < 0 ? ItemStack.EMPTY : skewers.get(index);
    }

    public static ItemStack particleStack(ItemStack plate, @Nullable LivingEntity entity) {
        return new ItemStack(Items.COOKED_BEEF);
    }

    private static int highestNutritionIndex(List<ItemStack> skewers, @Nullable LivingEntity entity) {
        int selected = -1;
        int nutrition = Integer.MIN_VALUE;
        for (int i = 0; i < skewers.size(); i++) {
            ItemStack stack = skewers.get(i);
            FoodProperties food = stack.getItem().getFoodProperties(stack, entity);
            int value = food == null ? Integer.MIN_VALUE : food.nutrition();
            if (value > nutrition) {
                nutrition = value;
                selected = i;
            }
        }
        return selected;
    }
}
