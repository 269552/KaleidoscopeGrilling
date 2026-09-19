package cn.breezeth.kaleidoscope_grilling.fabric.registry;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Tiny loader-neutral holder used by the Fabric port to preserve the original
 * DeferredHolder#get() call sites while registrations are migrated one-for-one.
 */
public final class RegistryRef<T> implements Supplier<T> {
  private final T value;

  private RegistryRef(T value) {
    this.value = Objects.requireNonNull(value);
  }

  public static <T> RegistryRef<T> of(T value) {
    return new RegistryRef<>(value);
  }

  @Override
  public T get() {
    return value;
  }
}
