package cn.breezeth.kaleidoscope_grilling.oil;

import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;

public final class BigVatBlock extends BaseEntityBlock {
  public BigVatBlock(Properties properties) {
    super(properties);
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BigVatBlockEntity(pos, state);
  }

  @Override
  public InteractionResult use(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (!(level.getBlockEntity(pos) instanceof BigVatBlockEntity vat))
      return InteractionResult.PASS;
    ItemStack held = player.getItemInHand(hand);
    String vatOilType = vat.oilType();
    if (OilPotCompat.isOilPot(held) && !vatOilType.isEmpty())
      return fillOilPot(level, player, held, vat, vatOilType);
    String oilType = OilFillingHandler.type(held);
    if (oilType != null) return fillFromOilBucket(level, pos, player, hand, vat, oilType);
    if (FluidUtil.interactWithFluidHandler(player, hand, vat.fluidHandler()))
      return InteractionResult.sidedSuccess(level.isClientSide);
    if (!level.isClientSide)
      player.displayClientMessage(
          Component.translatable(
              "message.kaleidoscope_grilling.big_vat_status",
              vat.amount() / (float) BigVatBlockEntity.BUCKET_VOLUME,
              BigVatBlockEntity.CAPACITY_BUCKETS),
          true);
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  private static InteractionResult fillFromOilBucket(
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
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  private static InteractionResult fillOilPot(
      Level level, Player player, ItemStack pot, BigVatBlockEntity vat, String oilType) {
    if (!level.isClientSide) {
      String current = OilPotCompat.getType(pot);
      int count = OilPotCompat.getCount(pot);
      int missing = OilPotCompat.FLUID_CAPACITY - count;
      int bucketsNeeded = Math.min(vat.buckets(), missing / 8);
      if ((current.isEmpty() ? count > 0 : !current.equals(oilType)) || bucketsNeeded <= 0)
        player.displayClientMessage(
            Component.translatable("message.kaleidoscope_grilling.big_vat_reject"), true);
      else if (vat.extract(oilType, bucketsNeeded)) {
        OilPotCompat.fill(pot, oilType, bucketsNeeded * 8);
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
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  public java.util.List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    ItemStack out = new ItemStack(ModBlocks.BIG_VAT_ITEM.get());
    BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
    if (be instanceof BigVatBlockEntity vat) {
      CompoundTag data = new CompoundTag();
      vat.saveAdditional(data);
      out.addTagElement("BlockEntityTag", data);
    }
    return java.util.List.of(out);
  }
}
