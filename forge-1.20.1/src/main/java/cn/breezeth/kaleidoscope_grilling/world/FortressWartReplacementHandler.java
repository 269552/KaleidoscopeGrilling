package cn.breezeth.kaleidoscope_grilling.world;

import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.TickEvent;

public final class FortressWartReplacementHandler {
  private static final int REPLACEMENT_PERCENT = 25;
  private static final int BLOCKS_PER_TICK = 2_048;
  private static final Map<ServerLevel, ArrayDeque<ReplacementTask>> PENDING = new IdentityHashMap<>();

  public static void onChunkLoad(ChunkEvent.Load event) {
    if (!event.isNewChunk()
        || !(event.getLevel() instanceof ServerLevel level)
        || level.dimension() != Level.NETHER
        || !(event.getChunk() instanceof LevelChunk chunk)) return;

    BoundingBox fortressBounds = getFortressBounds(level, chunk.getPos());
    if (fortressBounds == null) return;
    PENDING.computeIfAbsent(level, ignored -> new ArrayDeque<>())
        .addLast(new ReplacementTask(chunk, fortressBounds));
  }

  public static void onServerTick(TickEvent.ServerTickEvent event) {
    if (event.phase != TickEvent.Phase.END) return;
    int budget = BLOCKS_PER_TICK;
    Iterator<Map.Entry<ServerLevel, ArrayDeque<ReplacementTask>>> levels = PENDING.entrySet().iterator();
    while (levels.hasNext() && budget > 0) {
      Map.Entry<ServerLevel, ArrayDeque<ReplacementTask>> entry = levels.next();
      ArrayDeque<ReplacementTask> tasks = entry.getValue();
      while (!tasks.isEmpty() && budget > 0) {
        ReplacementTask task = tasks.peekFirst();
        budget -= task.process(entry.getKey(), budget);
        if (task.isComplete()) tasks.removeFirst();
      }
      if (tasks.isEmpty()) levels.remove();
    }
  }

  private static BoundingBox getFortressBounds(ServerLevel level, ChunkPos chunkPos) {
    Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
    Structure fortress = structures.get(BuiltinStructures.FORTRESS);
    if (fortress == null) return null;

    int minX = chunkPos.getMinBlockX();
    int minZ = chunkPos.getMinBlockZ();
    BoundingBox combined = null;
    for (StructureStart start :
        level.structureManager().startsForStructure(chunkPos, structure -> structure == fortress)) {
      BoundingBox bounds = start.getBoundingBox();
      if (!bounds.intersects(minX, minZ, minX + 15, minZ + 15)) continue;
      combined = combined == null ? bounds : combined.encapsulate(bounds);
    }
    return combined;
  }

  private static long coordinateHash(int x, int y, int z) {
    long value = x * 341873128712L ^ z * 132897987541L ^ y * 42317861L;
    value ^= value >>> 33;
    value *= 0xff51afd7ed558ccdl;
    return value ^ value >>> 33;
  }

  private static final class ReplacementTask {
    private final LevelChunk chunk;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;
    private int x;
    private int y;
    private int z;

    private ReplacementTask(LevelChunk chunk, BoundingBox bounds) {
      this.chunk = chunk;
      ChunkPos chunkPos = chunk.getPos();
      minX = Math.max(chunkPos.getMinBlockX(), bounds.minX());
      maxX = Math.min(chunkPos.getMaxBlockX(), bounds.maxX());
      minY = Math.max(chunk.getMinBuildHeight(), bounds.minY());
      maxY = Math.min(chunk.getMaxBuildHeight() - 1, bounds.maxY());
      minZ = Math.max(chunkPos.getMinBlockZ(), bounds.minZ());
      maxZ = Math.min(chunkPos.getMaxBlockZ(), bounds.maxZ());
      x = minX;
      y = minY;
      z = minZ;
    }

    private int process(ServerLevel level, int budget) {
      int processed = 0;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      while (!isComplete() && processed < budget) {
        pos.set(x, y, z);
        var state = chunk.getBlockState(pos);
        if (state.is(Blocks.NETHER_WART)
            && Math.floorMod(coordinateHash(x, y, z), 100) < REPLACEMENT_PERCENT) {
          int age =
              switch (state.getValue(NetherWartBlock.AGE)) {
                case 0 -> 0;
                case 1 -> 3;
                default -> 7;
              };
          var replacement =
              ModBlocks.HOUTTUYNIA_CROP
                  .get()
                  .defaultBlockState()
                  .setValue(HouttuyniaCropBlock.AGE, age)
                  .setValue(HouttuyniaCropBlock.RED_VARIANT, true);
          chunk.setBlockState(pos, replacement, false);
          level.sendBlockUpdated(pos, state, replacement, 2);
        }
        advance();
        processed++;
      }
      return processed;
    }

    private boolean isComplete() {
      return x > maxX || y > maxY || z > maxZ;
    }

    private void advance() {
      if (++y <= maxY) return;
      y = minY;
      if (++z <= maxZ) return;
      z = minZ;
      x++;
    }
  }

  private FortressWartReplacementHandler() {}
}
