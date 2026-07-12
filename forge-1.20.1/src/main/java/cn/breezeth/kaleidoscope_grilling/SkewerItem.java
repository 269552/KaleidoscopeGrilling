package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SkewerItem extends Item {
    private final String tooltipKey;
    private final ResourceLocation effectId;
    private final int effectDuration;

    public SkewerItem(Properties properties, @Nullable String tooltipKey,
                      @Nullable ResourceLocation effectId, int effectDuration) {
        super(properties);
        this.tooltipKey = tooltipKey;
        this.effectId = effectId;
        this.effectDuration = effectDuration;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 16;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            MobEffect effect = effectId == null ? null : ForgeRegistries.MOB_EFFECTS.getValue(effectId);
            if (effect != null) {
                entity.addEffect(new MobEffectInstance(effect, FoodState.isHot(stack, level) ? effectDuration * 2 : effectDuration));
            }
            FoodState.applySeasoning(stack, level, entity);
            level.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_BURP,
                    SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        FoodTooltip.appendMaxim(tooltip,tooltipKey);
        if (level != null && FoodState.isHot(stack, level)) tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.hot").withStyle(ChatFormatting.GOLD));
        FoodTooltip.appendEffect(tooltip,effectId,effectDuration);
    }
}
