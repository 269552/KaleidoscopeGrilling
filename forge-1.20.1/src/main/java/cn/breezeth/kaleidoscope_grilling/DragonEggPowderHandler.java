package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class DragonEggPowderHandler {
    private static final TagKey<Item> KNIVES = TagKey.create(Registries.ITEM, new ResourceLocation("kaleidoscope_cookery", "kitchen_knife"));
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack knife = event.getItemStack();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (level.isClientSide || !knife.is(KNIVES) || !level.getBlockState(pos).is(Blocks.DRAGON_EGG)) return;
        int looting = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, knife);
        int count = 1 + (looting == 0 ? 0 : level.random.nextInt(looting + 1));
        event.getEntity().addItem(new ItemStack(ModItems.DRAGON_EGG_POWDER.get(), count));
        knife.hurtAndBreak(1, event.getEntity(), p -> p.broadcastBreakEvent(event.getHand()));
    }

    private DragonEggPowderHandler() {}
}
