package cn.breezeth.kaleidoscope_grilling.fabric.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Fabric registry adapter used by the first porting stage. Registrations happen
 * immediately during class initialization, while keeping the original source's
 * DeferredRegister syntax intact.
 */
public final class DeferredRegister<T> {
    private final Registry<T> registry;
    private final String namespace;

    private DeferredRegister(Registry<T> registry, String namespace) {
        this.registry = Objects.requireNonNull(registry);
        this.namespace = Objects.requireNonNull(namespace);
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry, String namespace) {
        return new DeferredRegister<>(registry, namespace);
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, name);
        I value = supplier.get();
        Registry.register(registry, id, value);
        return new DeferredHolder<>(id, value);
    }

    /** Compatibility no-op. Accessing the register object already initialized it. */
    public void register(Object ignored) {
    }
}
