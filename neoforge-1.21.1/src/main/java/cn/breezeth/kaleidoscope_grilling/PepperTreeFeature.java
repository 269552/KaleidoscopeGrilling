package cn.breezeth.kaleidoscope_grilling;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class PepperTreeFeature extends Feature<NoneFeatureConfiguration> {
    public PepperTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int height = 2 + random.nextInt(2);

        for (int y = 0; y < height + 3; y++) {
            if (!level.getBlockState(origin.above(y)).isAir()
                    && !level.getBlockState(origin.above(y)).canBeReplaced())
                return false;
        }

        BlockState ground = level.getBlockState(origin.below());
        if (!ground.is(BlockTags.DIRT))
            return false;

        BlockState log = ModBlocks.PEPPER_LOG.get().defaultBlockState();
        for (int y = 0; y < height; y++) {
            level.setBlock(origin.above(y), log, 3);
        }

        BlockPos.MutableBlockPos leafPos = new BlockPos.MutableBlockPos();
        int trunkMinY = origin.getY();
        int trunkMaxY = origin.getY() + height - 1;

        // Top layer: 3x3
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                leafPos.set(origin.getX() + dx, origin.getY() + height, origin.getZ() + dz);
                if (level.isEmptyBlock(leafPos)) {
                    int dist = distanceToTrunk(origin.getX(), trunkMinY, trunkMaxY, origin.getZ(),
                            leafPos.getX(), leafPos.getY(), leafPos.getZ());
                    level.setBlock(leafPos, leafState(dist, random), 3);
                }
            }
        }

        int topY = origin.getY() + height - 1;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (random.nextFloat() < 0.4F) continue;
                leafPos.set(origin.getX() + dx, topY, origin.getZ() + dz);
                if (level.isEmptyBlock(leafPos)) {
                    int dist = distanceToTrunk(origin.getX(), trunkMinY, trunkMaxY, origin.getZ(),
                            leafPos.getX(), leafPos.getY(), leafPos.getZ());
                    level.setBlock(leafPos, leafState(dist, random), 3);
                }
            }
        }

        int bottomY = origin.getY() + height - 2;
        if (bottomY > origin.getY()) {
            for (int d = -1; d <= 1; d += 2) {
                if (random.nextFloat() < 0.5F) continue;
                leafPos.set(origin.getX() + d, bottomY, origin.getZ());
                if (level.isEmptyBlock(leafPos)) {
                    int dist = distanceToTrunk(origin.getX(), trunkMinY, trunkMaxY, origin.getZ(),
                            leafPos.getX(), leafPos.getY(), leafPos.getZ());
                    level.setBlock(leafPos, leafState(dist, random), 3);
                }
                leafPos.set(origin.getX(), bottomY, origin.getZ() + d);
                if (level.isEmptyBlock(leafPos)) {
                    int dist = distanceToTrunk(origin.getX(), trunkMinY, trunkMaxY, origin.getZ(),
                            leafPos.getX(), leafPos.getY(), leafPos.getZ());
                    level.setBlock(leafPos, leafState(dist, random), 3);
                }
            }
        }

        return true;
    }

    private static int distanceToTrunk(int trunkX, int trunkMinY, int trunkMaxY, int trunkZ,
                                        int leafX, int leafY, int leafZ) {
        int minDist = 7;
        for (int logY = trunkMinY; logY <= trunkMaxY; logY++) {
            int d = Math.abs(leafX - trunkX) + Math.abs(leafY - logY) + Math.abs(leafZ - trunkZ);
            if (d < minDist) minDist = d;
        }
        return Math.min(minDist, 7);
    }

    private static BlockState leafState(int distance, RandomSource random) {
        BlockState state = ModBlocks.PEPPER_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.DISTANCE, Math.max(1, Math.min(7, distance)));
        if (random.nextInt(3) == 0) {
            state = state.setValue(PepperLeavesBlock.HAS_PEPPER, true);
        }
        return state;
    }
}
