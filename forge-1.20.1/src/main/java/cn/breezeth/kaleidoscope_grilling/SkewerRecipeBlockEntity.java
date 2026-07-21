package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SkewerRecipeBlockEntity extends BlockEntity {
  private static final String RESULT = "RecipeResult";
  private static final String BOOK = "RecipeBook";
  private String recipeResult = "";
  private ItemStack recipeBook = ItemStack.EMPTY;

  public SkewerRecipeBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.SKEWER_RECIPE.get(), pos, state);
  }

  public String recipeResult() {
    return recipeResult;
  }

  public ItemStack recipeBook() {
    if (!recipeBook.isEmpty()) return recipeBook.copyWithCount(1);
    ItemStack fallback = new ItemStack(ModItems.SKEWER_RECIPE_BOOK.get());
    if (!recipeResult.isEmpty()) SkewerRecipeBookItem.setRecipeResult(fallback, recipeResult);
    return fallback;
  }

  public void setRecipeBook(ItemStack book) {
    recipeBook = book.copyWithCount(1);
    recipeResult = SkewerRecipeBookItem.readRecipeResult(recipeBook);
    sync();
  }

  public void setRecipeResult(String recipeResult) {
    this.recipeResult = recipeResult == null ? "" : recipeResult;
    recipeBook = new ItemStack(ModItems.SKEWER_RECIPE_BOOK.get());
    if (!this.recipeResult.isEmpty())
      SkewerRecipeBookItem.setRecipeResult(recipeBook, this.recipeResult);
    sync();
  }

  private void sync() {
    setChanged();
    if (level != null && !level.isClientSide) {
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putString(RESULT, recipeResult);
    if (!recipeBook.isEmpty()) tag.put(BOOK, recipeBook.save(new CompoundTag()));
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    recipeResult = tag.getString(RESULT);
    recipeBook = tag.contains(BOOK) ? ItemStack.of(tag.getCompound(BOOK)) : ItemStack.EMPTY;
  }

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag tag = super.getUpdateTag();
    saveAdditional(tag);
    return tag;
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
    CompoundTag tag = packet.getTag();
    if (tag != null) load(tag);
  }
}
