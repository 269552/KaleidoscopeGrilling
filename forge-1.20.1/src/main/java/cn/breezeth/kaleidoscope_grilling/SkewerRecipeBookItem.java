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
import java.util.Map;
import java.util.HashMap;

public final class SkewerRecipeBookItem extends Item {
    private static final String RECIPE_RESULT_TAG = "RecipeResult";

    public SkewerRecipeBookItem(Properties properties) {
        super(properties);
    }

    public static String readRecipeResult(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(RECIPE_RESULT_TAG)) {
            return stack.getTag().getString(RECIPE_RESULT_TAG);
        }
        return "";
    }

    public static void setRecipeResult(ItemStack stack, String resultId) {
        stack.getOrCreateTag().putString(RECIPE_RESULT_TAG, resultId);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String resultId = readRecipeResult(stack);
        if (!resultId.isEmpty()) {
            Item resultItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(resultId));
            if (resultItem != null) {
                tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.recipe_book_records",
                        resultItem.getDescription()).withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.recipe_book_empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack book = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.is(Items.STICK)) return InteractionResultHolder.pass(book);

        String resultId = readRecipeResult(book);
        if (resultId.isEmpty()) return InteractionResultHolder.pass(book);

        List<List<String>> ingredients = SkewerRecipes.getIngredients(resultId);
        if (ingredients == null) return InteractionResultHolder.pass(book);

        if (level.isClientSide) return InteractionResultHolder.success(book);

        // Scan inventory for ingredients
        Map<Integer, Integer> slotMap = new HashMap<>();
        for (int slot = 0; slot < ingredients.size(); slot++) {
            List<String> acceptable = ingredients.get(slot);
            boolean found = false;
            for (int invSlot = 0; invSlot < player.getInventory().getContainerSize(); invSlot++) {
                if (invSlot == player.getInventory().selected && hand == InteractionHand.MAIN_HAND) continue;
                if (invSlot == 40) continue; // offhand slot
                ItemStack invStack = player.getInventory().getItem(invSlot);
                if (invStack.isEmpty()) continue;
                ResourceLocation invId = ForgeRegistries.ITEMS.getKey(invStack.getItem());
                if (invId != null && acceptable.contains(invId.toString())) {
                    slotMap.put(slot, invSlot);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Build missing ingredient message
                StringBuilder missing = new StringBuilder();
                for (String acc : acceptable) {
                    Item accItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(acc));
                    if (accItem != null) {
                        if (missing.length() > 0) missing.append("/");
                        missing.append(accItem.getDescription().getString());
                    }
                }
                player.displayClientMessage(Component.translatable(
                        "message.kaleidoscope_grilling.skewer_book_missing", missing.toString(), 1), true);
                return InteractionResultHolder.fail(book);
            }
        }

        // All ingredients found, consume and produce
        if (!player.getAbilities().instabuild) {
            for (int slot : slotMap.values()) {
                player.getInventory().getItem(slot).shrink(1);
            }
            offhand.shrink(1);
        }

        Item resultItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(resultId));
        if (resultItem != null) {
            if (!player.getInventory().add(new ItemStack(resultItem))) {
                player.drop(new ItemStack(resultItem), false);
            }
        }

        return InteractionResultHolder.success(book);
    }
}
