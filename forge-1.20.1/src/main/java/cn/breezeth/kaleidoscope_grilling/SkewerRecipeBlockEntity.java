package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SkewerRecipeBlockEntity extends BlockEntity {
    private static final String RESULT = "RecipeResult";
    private String recipeResult = "";

    public SkewerRecipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SKEWER_RECIPE.get(), pos, state);
    }

    public String recipeResult() { return recipeResult; }

    public void setRecipeResult(String recipeResult) {
        this.recipeResult = recipeResult == null ? "" : recipeResult;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(RESULT, recipeResult);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        recipeResult = tag.getString(RESULT);
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) load(tag);
    }
}
