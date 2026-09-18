package cn.breezeth.kaleidoscope_grilling.fabric.registry;

import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Small Fabric porting wrapper that preserves the old NeoForge DeferredHolder#get()
 * call pattern while the registry code is migrated.
 */
public final class DeferredHolder<R, T extends R> implements Supplier<T> {
    private final Identifier id;
    private final T value;

    DeferredHolder(Identifier id, T value) {
        this.id = Objects.requireNonNull(id);
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public T get() {
        return value;
    }

    public Identifier getId() {
        return id;
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
