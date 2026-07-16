package cn.breezeth.kaleidoscope_grilling;
import net.minecraft.world.item.BlockItem; import net.minecraft.world.item.ItemStack; import net.minecraft.world.item.TooltipFlag; import net.minecraft.network.chat.Component; import net.minecraft.world.level.block.Block; import java.util.List;
public final class SeasoningBottleBlockItem extends BlockItem { public SeasoningBottleBlockItem(Block b,Properties p){super(b,p);}@Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){SeasoningTooltip.appendUnfinished(s,t);}}
