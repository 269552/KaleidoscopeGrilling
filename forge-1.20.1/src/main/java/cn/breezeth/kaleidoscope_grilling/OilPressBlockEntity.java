package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;

public final class OilPressBlockEntity extends BlockEntity {
    public static final int MAX_CAKES = 4;
    public static final int REQUIRED_PROGRESS = 8;
    private int cakes;
    private int progress;
    private int residue;

    public OilPressBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.OIL_PRESS.get(), pos, state); }
    public int cakes(){return cakes;} public int progress(){return progress;} public int residue(){return residue;}
    public boolean addCake(){if(cakes>=MAX_CAKES)return false;cakes++;sync();return true;}
    public int takeResidue(){int result=residue;residue=0;sync();return result;}

    public boolean impact(int amount){
        if(cakes==0||amount<=0)return false;
        progress=Math.min(REQUIRED_PROGRESS,progress+amount);
        if(progress>=REQUIRED_PROGRESS)tryFinish();
        sync();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OilPressBlockEntity press){
        AABB area=new AABB(pos.getX(),pos.getY()+1,pos.getZ(),pos.getX()+1,pos.getY()+2.25,pos.getZ()+1);
        for(FallingBlockEntity falling:level.getEntitiesOfClass(FallingBlockEntity.class,area)){
            if(falling.getPersistentData().getBoolean("GrillingPressImpact")||falling.fallDistance<1.0F)continue;
            int amount=PressImpactRegistry.progress(falling.getBlockState().getBlock());
            if(amount>0&&press.impact(amount))falling.getPersistentData().putBoolean("GrillingPressImpact",true);
        }
    }

    private void tryFinish(){
        BigVatBlockEntity vat=findVat();
        if(vat==null||vat.buckets()+cakes>BigVatBlockEntity.CAPACITY_BUCKETS||!vat.canAccept("canola"))return;
        if(vat.insert("canola",cakes)){residue+=cakes;cakes=0;progress=0;}
    }

    private BigVatBlockEntity findVat(){
        if(level==null)return null;
        for(BlockPos p:BlockPos.betweenClosed(worldPosition.offset(-2,-1,-2),worldPosition.offset(2,1,2)))
            if(level.getBlockEntity(p)instanceof BigVatBlockEntity vat&&vat.canAccept("canola"))return vat;
        return null;
    }

    private void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.putInt("Cakes",cakes);tag.putInt("Progress",progress);tag.putInt("Residue",residue);}
    @Override public void load(CompoundTag tag){super.load(tag);cakes=Math.max(0,Math.min(MAX_CAKES,tag.getInt("Cakes")));progress=Math.max(0,Math.min(REQUIRED_PROGRESS,tag.getInt("Progress")));residue=Math.max(0,tag.getInt("Residue"));}
}
