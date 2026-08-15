package cn.breezeth.kaleidoscope_grilling.skewer;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModAdvancements;
import cn.breezeth.kaleidoscope_grilling.registry.ModEffects;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class CursedSkewerItem extends MultiBiteSkewerItem {
  private static final ResourceKey<DamageType> ORDINARY_SKEWER_DAMAGE = ResourceKey.create(
      Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "ordinary_skewer"));

  public CursedSkewerItem(Properties properties) {
    super(properties, null, null, 0, AnimationProfile.THREE_RANDOM);
  }

  @Override
  public Component getName(ItemStack stack) {
    return super.getName(stack).copy().withStyle(ChatFormatting.DARK_RED);
  }

  @Override
  protected float burpVolume() {
    return 0.0F;
  }

  @Override
  protected void afterFoodCommitted(ItemStack stack, Level level, LivingEntity entity) {
    boolean challenged = entity.hasEffect(ModEffects.INVINCIBLE);
    if (!(level instanceof ServerLevel server)) return;
    if (challenged) entity.removeEffect(ModEffects.INVINCIBLE);
    boolean blocked = challenged && entity.getRandom().nextBoolean();
    if (blocked) {
      server.playSound(null, entity.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.8F);
      server.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.35F);
      server.sendParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getY() + 1.0, entity.getZ(), 28, 0.55, 0.7, 0.55, 0.12);
      ModAdvancements.strongestShield(entity);
      return;
    }
    server.playSound(null, entity.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 0.65F);
    server.playSound(null, entity.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.7F, 0.55F);
    server.sendParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getY() + 1.0, entity.getZ(), 24, 0.45, 0.65, 0.45, 0.12);
    server.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY() + 0.8, entity.getZ(), 18, 0.4, 0.6, 0.4, 0.04);
    if (challenged) ModAdvancements.strongestSpear(entity);
    DamageSource source = new DamageSource(server.registryAccess()
        .registryOrThrow(Registries.DAMAGE_TYPE)
        .getHolderOrThrow(ORDINARY_SKEWER_DAMAGE));
    entity.hurt(source, Float.MAX_VALUE);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.ordinary_skewer.maxim").withStyle(ChatFormatting.DARK_RED));
  }
}
