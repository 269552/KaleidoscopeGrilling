package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

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
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 16;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            if (effectId != null) {
                BuiltInRegistries.MOB_EFFECT.getHolder(ResourceKey.create(Registries.MOB_EFFECT, effectId))
                        .ifPresent(effect -> entity.addEffect(new MobEffectInstance(effect, FoodState.isHot(stack, level) ? effectDuration * 2 : effectDuration)));
            }
            FoodState.applySeasoning(stack, level, entity);
            level.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_BURP,
                    SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        FoodTooltip.appendMaxim(tooltip,tooltipKey);
        FoodTooltip.appendEffect(tooltip,context,effectId,effectDuration);
    }
}
