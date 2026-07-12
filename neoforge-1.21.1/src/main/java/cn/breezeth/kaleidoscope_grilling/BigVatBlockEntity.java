package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BigVatBlockEntity extends BlockEntity {
    public static final int CAPACITY_BUCKETS = 64;
    private String content = "";
    private int buckets;

    public BigVatBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.BIG_VAT.get(), pos, state); }
    public String content() { return content; }
    public int buckets() { return buckets; }
    public boolean canAccept(String type) { return buckets < CAPACITY_BUCKETS && (content.isEmpty() || content.equals(type)); }
    public boolean insert(String type, int amount) { if (amount <= 0 || !canAccept(type) || buckets + amount > CAPACITY_BUCKETS) return false; content=type; buckets+=amount; sync(); return true; }
    public boolean extract(String type, int amount) { if (amount <= 0 || !content.equals(type) || buckets < amount) return false; buckets-=amount; if(buckets==0)content=""; sync(); return true; }
    private void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries){super.saveAdditional(tag,registries);tag.putString("Content",content);tag.putInt("Buckets",buckets);}
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries){super.loadAdditional(tag,registries);content=tag.getString("Content");buckets=Math.max(0,Math.min(CAPACITY_BUCKETS,tag.getInt("Buckets")));if(buckets==0)content="";}
}
