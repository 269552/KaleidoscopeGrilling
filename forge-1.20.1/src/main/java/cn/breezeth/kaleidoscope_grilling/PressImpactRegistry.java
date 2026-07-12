package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PressImpactRegistry {
    private static final Map<Block,Integer> IMPACTS=new LinkedHashMap<>();
    static{register(Blocks.ANVIL,4);register(Blocks.CHIPPED_ANVIL,4);register(Blocks.DAMAGED_ANVIL,4);register(Blocks.SAND,1);register(Blocks.RED_SAND,1);register(Blocks.GRAVEL,1);}
    public static void register(Block block,int progress){if(progress>0)IMPACTS.put(block,progress);}
    public static int progress(Block block){return IMPACTS.getOrDefault(block,0);}
    private PressImpactRegistry(){}
}
