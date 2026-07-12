package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class UnfinishedSkewerItem extends Item {
    public UnfinishedSkewerItem(Properties properties) { super(properties); }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        List<String> ingredients = SkeweringHandler.readIngredients(stack);
        tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.skewer_progress",
                ingredients.size(), SkewerRecipes.expectedSize(ingredients)).withStyle(ChatFormatting.YELLOW));
        for (String id : ingredients) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (item != null) tooltip.add(Component.literal("- ").append(item.getDescription()).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.skewer_cancel").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide) {
            for (String id : SkeweringHandler.readIngredients(stack)) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
                if (item != null) player.getInventory().placeItemBackInInventory(new ItemStack(item));
            }
            player.getInventory().placeItemBackInInventory(new ItemStack(Items.STICK));
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        return InteractionResultHolder.sidedSuccess(level.isClientSide ? stack : ItemStack.EMPTY, level.isClientSide);
    }
}
