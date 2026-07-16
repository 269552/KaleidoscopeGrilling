package cn.breezeth.kaleidoscope_grilling;

import com.github.ysbbbbbb.kaleidoscopecookery.inventory.tooltip.RecipeItemTooltip;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;

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
    public Component getName(ItemStack stack) {
        ItemStack result = resultStack(stack);
        return result.isEmpty() ? super.getName(stack)
                : Component.translatable("item.kaleidoscope_grilling.skewer_recipe_book.recorded", result.getHoverName());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String resultId = readRecipeResult(stack);
        if (!resultId.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.recipe_book.usage")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.recipe_book.wall_usage")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.recipe_book_empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.recipe_book.recording")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        String resultId = readRecipeResult(stack);
        List<List<String>> recipe = SkewerRecipes.getIngredients(resultId);
        ItemStack output = resultStack(stack);
        if (recipe == null || recipe.isEmpty() || output.isEmpty()) return Optional.empty();
        List<ItemStack> inputs = new ArrayList<>();
        for (List<String> selectors : recipe) {
            ItemStack input = ForgeRegistries.ITEMS.getValues().stream().map(ItemStack::new)
                    .filter(candidate -> selectors.stream().anyMatch(selector -> SkewerRecipes.matchesSelector(candidate, selector)))
                    .findFirst().orElse(ItemStack.EMPTY);
            if (input.isEmpty()) return Optional.empty();
            inputs.add(input);
        }
        return Optional.of(new RecipeItemTooltip(
                new RecipeItem.RecipeRecord(inputs, output, RecipeItem.POT, false), null));
    }

    private static ItemStack resultStack(ItemStack stack) {
        String value = readRecipeResult(stack);
        if (value.isEmpty()) return ItemStack.EMPTY;
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(value));
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack book = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.is(Items.STICK)) return InteractionResultHolder.pass(book);

        String resultId = readRecipeResult(book);
        if (resultId.isEmpty()) return InteractionResultHolder.pass(book);

        InteractionResult result = craft(level, player, offhand, resultId);
        return result == InteractionResult.FAIL ? InteractionResultHolder.fail(book)
                : result.consumesAction() ? InteractionResultHolder.success(book) : InteractionResultHolder.pass(book);
    }

    public static InteractionResult craft(Level level, Player player, ItemStack stick, String resultId) {
        if (!stick.is(Items.STICK)) return InteractionResult.PASS;

        List<List<String>> ingredients = SkewerRecipes.getIngredients(resultId);
        if (ingredients == null) return InteractionResult.PASS;

        if (level.isClientSide) return InteractionResult.SUCCESS;

        // Scan inventory for ingredients
        Map<Integer, Integer> consumption = new HashMap<>();
        for (int slot = 0; slot < ingredients.size(); slot++) {
            List<String> acceptable = ingredients.get(slot);
            boolean found = false;
            for (int invSlot = 0; invSlot < player.getInventory().getContainerSize(); invSlot++) {
                if (invSlot == player.getInventory().selected) continue;
                if (invSlot == 40) continue; // offhand slot
                ItemStack invStack = player.getInventory().getItem(invSlot);
                if (invStack.isEmpty()) continue;
                int reserved = consumption.getOrDefault(invSlot, 0);
                if (acceptable.stream().anyMatch(selector -> SkewerRecipes.matchesSelector(invStack, selector))
                        && invStack.getCount() > reserved) {
                    consumption.put(invSlot, reserved + 1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Build missing ingredient message
                StringBuilder missing = new StringBuilder();
                for (String acc : acceptable) {
                    if (acc.startsWith("#")) {
                        if (missing.length() > 0) missing.append("/");
                        missing.append(acc);
                        continue;
                    }
                    Item accItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(acc));
                    if (accItem != null) {
                        if (missing.length() > 0) missing.append("/");
                        missing.append(accItem.getDescription().getString());
                    }
                }
                player.displayClientMessage(Component.translatable(
                        "message.kaleidoscope_grilling.skewer_book_missing", missing.toString(), 1), true);
                return InteractionResult.FAIL;
            }
        }

        // All ingredients found, consume and produce
        if (!player.getAbilities().instabuild) {
            consumption.forEach((slot, count) -> player.getInventory().getItem(slot).shrink(count));
            stick.shrink(1);
        }

        Item resultItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(resultId));
        if (resultItem != null) {
            if (!player.getInventory().add(new ItemStack(resultItem))) {
                player.drop(new ItemStack(resultItem), false);
            }
        }
        level.playSound(null, player.blockPosition(), ModSounds.ACTION_SUCCESS.get(), SoundSource.PLAYERS, 0.7F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction face = context.getClickedFace();
        if (!face.getAxis().isHorizontal()) return InteractionResult.PASS;
        ItemStack book = context.getItemInHand();
        String id = readRecipeResult(book);
        if (id.isEmpty() || ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)) == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos target = context.getClickedPos().relative(face);
        if (!level.getBlockState(target).canBeReplaced()) return InteractionResult.FAIL;
        BlockState state = ModBlocks.SKEWER_RECIPE.get().defaultBlockState().setValue(SkewerRecipeBlock.FACING, face);
        if (!state.canSurvive(level, target)) return InteractionResult.FAIL;
        if (!level.isClientSide) {
            level.setBlock(target, state, 3);
            if (level.getBlockEntity(target) instanceof SkewerRecipeBlockEntity recipe) recipe.setRecipeResult(id);
            level.playSound(null, target, SoundEvents.ITEM_FRAME_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) book.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
