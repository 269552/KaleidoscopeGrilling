package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class OilPressContainerApi {
    public enum Probe { NOT_CONTAINER, READY, FULL, INCOMPATIBLE }
    public enum TransferStatus { SUCCESS, NO_CONTAINER, FULL, INCOMPATIBLE }
    public record TransferResult(TransferStatus status, BlockPos containerPos, ResourceLocation handlerId) {
        public boolean success() { return status == TransferStatus.SUCCESS; }
    }
    public interface Handler {
        Probe probe(Level level, BlockPos pos, int canolaBuckets);
        boolean insert(Level level, BlockPos pos, int canolaBuckets);
    }

    private static final Map<ResourceLocation, Handler> HANDLERS = new LinkedHashMap<>();

    static {
        register(new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "big_vat"), new Handler() {
            @Override public Probe probe(Level level, BlockPos pos, int buckets) {
                if (!(level.getBlockEntity(pos) instanceof BigVatBlockEntity vat)) return Probe.NOT_CONTAINER;
                if (!vat.canAccept("canola")) return Probe.INCOMPATIBLE;
                return vat.amount() + buckets * BigVatBlockEntity.BUCKET_VOLUME <= BigVatBlockEntity.CAPACITY ? Probe.READY : Probe.FULL;
            }
            @Override public boolean insert(Level level, BlockPos pos, int buckets) {
                return level.getBlockEntity(pos) instanceof BigVatBlockEntity vat && vat.insert("canola", buckets);
            }
        });
    }

    public static synchronized void register(ResourceLocation id, Handler handler) {
        Objects.requireNonNull(id, "id"); Objects.requireNonNull(handler, "handler");
        if (HANDLERS.putIfAbsent(id, handler) != null) throw new IllegalArgumentException("Oil press container handler already registered: " + id);
    }

    public static TransferResult insertNearby(Level level, BlockPos pressPos, int canolaBuckets) {
        return scanNearby(level, pressPos, canolaBuckets, true);
    }

    public static TransferResult probeNearby(Level level, BlockPos pressPos, int canolaBuckets) {
        return scanNearby(level, pressPos, canolaBuckets, false);
    }

    private static TransferResult scanNearby(Level level, BlockPos pressPos, int canolaBuckets, boolean insert) {
        if (level == null || canolaBuckets <= 0) return new TransferResult(TransferStatus.NO_CONTAINER, null, null);
        TransferResult fallback = new TransferResult(TransferStatus.NO_CONTAINER, null, null);
        Map<ResourceLocation, Handler> handlers = snapshot();
        for (BlockPos cursor : BlockPos.betweenClosed(pressPos.offset(-4, -2, -4), pressPos.offset(4, 2, 4))) {
            BlockPos pos = cursor.immutable();
            for (Map.Entry<ResourceLocation, Handler> entry : handlers.entrySet()) {
                Probe probe = entry.getValue().probe(level, pos, canolaBuckets);
                if (probe == Probe.NOT_CONTAINER) continue;
                if (probe == Probe.READY && (!insert || entry.getValue().insert(level, pos, canolaBuckets)))
                    return new TransferResult(TransferStatus.SUCCESS, pos, entry.getKey());
                TransferStatus status = probe == Probe.INCOMPATIBLE ? TransferStatus.INCOMPATIBLE : TransferStatus.FULL;
                if (fallback.status() == TransferStatus.NO_CONTAINER || status == TransferStatus.FULL)
                    fallback = new TransferResult(status, pos, entry.getKey());
            }
        }
        return fallback;
    }

    private static synchronized Map<ResourceLocation, Handler> snapshot() { return Map.copyOf(HANDLERS); }
    private OilPressContainerApi() {}
}
