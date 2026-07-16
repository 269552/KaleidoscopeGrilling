package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class OilPressBlockEntity extends BlockEntity {
    public static final int MAX_CAKES = 4;
    public static final int REQUIRED_PROGRESS = 16;
    public static final int ANVIL_PROGRESS = 4;
    public static final int PRESS_COOLDOWN_TICKS = 10;

    private final Map<UUID, Long> playerCooldowns = new HashMap<>();
    private final List<PendingImpact> pendingImpacts = new ArrayList<>();
    private int cakes;
    private int progress;
    private int residue;
    private int completionDelay;
    private boolean waitingForContainer;
    private long lastFailureMessage = -100;

    public OilPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OIL_PRESS.get(), pos, state);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, OilPressBlockEntity press) {
        if (press.completionDelay > 0) {
            press.completionDelay--;
            if (press.completionDelay == 0 && press.progress >= REQUIRED_PROGRESS) {
                press.tryFinish();
                press.sync();
            }
        }
        press.tickPendingImpacts(level);
    }

    public int cakes() { return cakes; }
    public int progress() { return progress; }
    public int residue() { return residue; }
    public boolean waitingForContainer() { return waitingForContainer; }
    public boolean addCake() { if (cakes >= MAX_CAKES) return false; cakes++; sync(); return true; }
    public int takeResidue() { int value = residue; residue = 0; sync(); return value; }

    public boolean pressWithTool(Player player, int amount) {
        if (level == null || cakes != MAX_CAKES || waitingForContainer || progress >= REQUIRED_PROGRESS || amount <= 0) return false;
        long now = level.getGameTime();
        long availableAt = playerCooldowns.getOrDefault(player.getUUID(), Long.MIN_VALUE);
        if (now < availableAt) return false;
        playerCooldowns.put(player.getUUID(), now + PRESS_COOLDOWN_TICKS);
        pendingImpacts.add(new PendingImpact(player.getUUID(), amount, AnvilPressAnimation.IMPACT_TICK));
        return true;
    }

    private void tickPendingImpacts(net.minecraft.world.level.Level level) {
        Iterator<PendingImpact> iterator = pendingImpacts.iterator();
        while (iterator.hasNext()) {
            PendingImpact pending = iterator.next();
            if (--pending.ticks > 0) continue;
            iterator.remove();
            if (!addProgress(pending.amount)) continue;
            level.playSound(null, worldPosition, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS,
                    1.15F, 0.82F + level.random.nextFloat() * 0.12F);
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.CRIT, worldPosition.getX() + 0.5, worldPosition.getY() + 0.9,
                        worldPosition.getZ() + 0.5, 14, 0.32, 0.18, 0.32, 0.08);
                ServerPlayer player = server.getServer().getPlayerList().getPlayer(pending.playerId);
                if (player != null) player.displayClientMessage(Component.translatable(
                        "message.kaleidoscope_grilling.press_status", cakes, progress, REQUIRED_PROGRESS), true);
            }
        }
    }

    /** Public integration point for other mods. Amount is measured in the same 0-16 press progress scale. */
    public boolean addProgress(int amount) {
        if (cakes != MAX_CAKES || amount <= 0 || waitingForContainer) return false;
        progress = Math.min(REQUIRED_PROGRESS, progress + amount);
        if (progress >= REQUIRED_PROGRESS && completionDelay == 0) completionDelay = PRESS_COOLDOWN_TICKS;
        sync();
        return true;
    }

    public InspectionResult inspectForContainer() {
        if (!waitingForContainer) return InspectionResult.NOT_WAITING;
        boolean completed = tryFinish(); sync();
        return completed ? InspectionResult.COMPLETED : InspectionResult.WAITING;
    }

    private boolean tryFinish() {
        OilPressContainerApi.TransferResult result = OilPressContainerApi.insertNearby(level, worldPosition, MAX_CAKES);
        if (result.success()) {
            ejectResidue(MAX_CAKES); cakes = 0; progress = 0; waitingForContainer = false;
            if (level != null) level.playSound(null, worldPosition, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.8F, 0.9F);
            return true;
        }
        progress = REQUIRED_PROGRESS - 1;
        waitingForContainer = true;
        String key = switch (result.status()) {
            case FULL -> "message.kaleidoscope_grilling.press_vat_full";
            case INCOMPATIBLE -> "message.kaleidoscope_grilling.press_wrong_vat";
            default -> "message.kaleidoscope_grilling.press_no_vat";
        };
        failure(key);
        return false;
    }

    private void ejectResidue(int count) {
        if (level == null || count <= 0) return;
        Direction facing = getBlockState().getValue(OilPressBlock.FACING);
        double x = worldPosition.getX() + 0.5 + facing.getStepX() * 0.72;
        double y = worldPosition.getY() + 0.58;
        double z = worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.72;
        ItemEntity item = new ItemEntity(level, x, y, z, new ItemStack(ModItems.OIL_RESIDUE.get(), count));
        item.setDeltaMovement(facing.getStepX() * 0.22, 0.18, facing.getStepZ() * 0.22);
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }

    private void failure(String key) {
        if (level == null || level.getGameTime() - lastFailureMessage < 40) return;
        lastFailureMessage = level.getGameTime();
        level.getEntitiesOfClass(ServerPlayer.class, new AABB(worldPosition).inflate(6))
                .forEach(player -> player.displayClientMessage(Component.translatable(key), true));
    }

    private static int visualStage(int progress) {
        if (progress <= 0) return 0;
        if (progress <= 4) return 1;
        if (progress <= 8) return 2;
        if (progress <= 12) return 3;
        return 4;
    }
    private void sync() {
        setChanged();
        if (level == null) return;
        BlockState oldState = getBlockState();
        BlockState newState = oldState.setValue(OilPressBlock.CAKE_COUNT, cakes).setValue(OilPressBlock.PRESS_STAGE, visualStage(progress));
        if (!level.isClientSide && newState != oldState) level.setBlock(worldPosition, newState, Block.UPDATE_CLIENTS);
        else level.sendBlockUpdated(worldPosition, oldState, oldState, Block.UPDATE_CLIENTS);
    }
    @Override public void onLoad() { super.onLoad(); sync(); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); tag.putInt("Cakes", cakes); tag.putInt("Progress", progress); tag.putInt("Residue", residue); tag.putInt("CompletionDelay", completionDelay); tag.putBoolean("WaitingForContainer", waitingForContainer); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); cakes = Math.max(0, Math.min(MAX_CAKES, tag.getInt("Cakes"))); progress = Math.max(0, Math.min(REQUIRED_PROGRESS, tag.getInt("Progress"))); residue = Math.max(0, tag.getInt("Residue")); completionDelay = Math.max(0, Math.min(PRESS_COOLDOWN_TICKS, tag.getInt("CompletionDelay"))); waitingForContainer = tag.getBoolean("WaitingForContainer"); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { CompoundTag tag = super.getUpdateTag(registries); saveAdditional(tag, registries); return tag; }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    public enum InspectionResult { NOT_WAITING, WAITING, COMPLETED }
    private static final class PendingImpact {
        private final UUID playerId;
        private final int amount;
        private int ticks;
        private PendingImpact(UUID playerId, int amount, int ticks) {
            this.playerId = playerId;
            this.amount = amount;
            this.ticks = ticks;
        }
    }
}
