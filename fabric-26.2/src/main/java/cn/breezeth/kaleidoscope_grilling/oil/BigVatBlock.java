package cn.breezeth.kaleidoscope_grilling.oil;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;

public final class BigVatBlock extends BaseEntityBlock {
  public static final MapCodec<BigVatBlock> CODEC = simpleCodec(BigVatBlock::new);

  public BigVatBlock(Properties properties) {
    super(properties);
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BigVatBlockEntity(pos, state);
  }

  @Override
  protected ItemInteractionResult useItemOn(
      ItemStack held,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (!(level.getBlockEntity(pos) instanceof BigVatBlockEntity vat))
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    String vatOilType = vat.oilType();
    if (OilPotCompat.isOilPot(held) && !vatOilType.isEmpty())
      return fillOilPot(level, player, held, vat, vatOilType);
    String oilType = OilFillingHandler.type(held);
    if (oilType != null) return fillFromOilBucket(level, pos, player, hand, vat, oilType);
    if (FluidUtil.interactWithFluidHandler(player, hand, vat.fluidHandler()))
      return ItemInteractionResult.SUCCESS;
    if (!level.isClientSide)
      player.displayClientMessage(
          Component.translatable(
              "message.kaleidoscope_grilling.big_vat_status",
              vat.amount() / (float) BigVatBlockEntity.BUCKET_VOLUME,
              BigVatBlockEntity.CAPACITY_BUCKETS),
          true);
    return ItemInteractionResult.SUCCESS;
  }

  private static ItemInteractionResult fillFromOilBucket(
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BigVatBlockEntity vat,
      String oilType) {
    if (!level.isClientSide) {
      if (!vat.insert(oilType, 1)) {
        player.displayClientMessage(
            Component.translatable("message.kaleidoscope_grilling.big_vat_reject"), true);
      } else {
        level.playSound(
            null,
            pos,
            "premium_chili".equals(oilType)
                ? SoundEvents.BUCKET_EMPTY_LAVA
                : SoundEvents.BUCKET_EMPTY,
            SoundSource.BLOCKS,
            0.9F,
            0.8F + 0.5F * vat.amount() / BigVatBlockEntity.CAPACITY);
        if (!player.getAbilities().instabuild)
          player.setItemInHand(hand, new ItemStack(Items.BUCKET));
      }
    }
    return ItemInteractionResult.SUCCESS;
  }

  private static ItemInteractionResult fillOilPot(
      Level level, Player player, ItemStack pot, BigVatBlockEntity vat, String oilType) {
    if (!level.isClientSide) {
      String current = OilPotCompat.getType(pot);
      int count = OilPotCompat.getCount(pot);
      int needed =
          Math.min(vat.buckets(), (OilPotCompat.FLUID_CAPACITY - count) / 8);
      if ((current.isEmpty() ? count > 0 : !current.equals(oilType)) || needed <= 0)
        player.displayClientMessage(
            Component.translatable("message.kaleidoscope_grilling.big_vat_reject"), true);
      else if (vat.extract(oilType, needed)) {
        OilPotCompat.fill(pot, oilType, needed * 8);
        level.playSound(
            null,
            player.blockPosition(),
            "premium_chili".equals(oilType)
                ? SoundEvents.BUCKET_EMPTY_LAVA
                : SoundEvents.BUCKET_EMPTY,
            SoundSource.PLAYERS,
            0.9F,
            0.8F
                + 0.5F
                    * OilPotCompat.getCount(pot)
                    / OilPotCompat.FLUID_CAPACITY);
      }
    }
    return ItemInteractionResult.SUCCESS;
  }

  @Override
  protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    ItemStack out = new ItemStack(ModBlocks.BIG_VAT_ITEM.get());
    BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
    if (be instanceof BigVatBlockEntity vat) {
      CompoundTag data = new CompoundTag();
      vat.saveAdditional(data, params.getLevel().registryAccess());
      data.putString("id", KaleidoscopeGrilling.MOD_ID + ":big_vat");
      out.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(data));
    }
    return List.of(out);
  }
}
