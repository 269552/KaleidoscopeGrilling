package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.level.ChunkEvent;

public final class FortressWartReplacementHandler {
    private static final int REPLACEMENT_PERCENT = 25;

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.NETHER || !(event.getChunk() instanceof LevelChunk chunk)) return;

        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x < minX + 16; x++) {
            for (int z = minZ; z < minZ + 16; z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    pos.set(x, y, z);
                    var state = chunk.getBlockState(pos);
                    if (!state.is(Blocks.NETHER_WART) || Math.floorMod(coordinateHash(x, y, z), 100) >= REPLACEMENT_PERCENT) continue;
                    int age = Math.min(2, state.getValue(NetherWartBlock.AGE));
                    level.setBlock(pos, ModBlocks.HOUTTUYNIA_CROP.get().defaultBlockState()
                            .setValue(HouttuyniaCropBlock.AGE, age), 2);
                }
            }
        }
    }

    private static long coordinateHash(int x, int y, int z) {
        long value = x * 341873128712L ^ z * 132897987541L ^ y * 42317861L;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdl;
        return value ^ value >>> 33;
    }

    private FortressWartReplacementHandler() {}
}
