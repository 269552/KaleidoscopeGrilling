package cn.breezeth.kaleidoscope_grilling.fabric.registry;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

/**
 * Fabric-side equivalent of the small subset of DeferredRegister used by the
 * upstream item registry. It preserves upstream factory ordering while making
 * sure Minecraft 26.2 receives the item ResourceKey before an Item is built.
 */
public final class FabricItemRegistrar {
  private final String namespace;
  private final ThreadLocal<ResourceKey<Item>> constructingKey = new ThreadLocal<>();

  public FabricItemRegistrar(String namespace) {
    this.namespace = Objects.requireNonNull(namespace);
  }

  public RegistryRef<Item> register(String path, Supplier<? extends Item> factory) {
    ResourceKey<Item> key = ResourceKey.create(
        Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, path));
    constructingKey.set(key);
    final Item item;
    try {
      item = factory.get();
    } finally {
      constructingKey.remove();
    }
    if (item instanceof BlockItem blockItem) {
      blockItem.registerBlocks(Item.BY_BLOCK, item);
    }
    return RegistryRef.of(Registry.register(BuiltInRegistries.ITEM, key, item));
  }

  /**
   * Creates keyed 26.2 item properties for the factory currently being
   * registered. This lets the original supplier-based source remain structurally
   * identical instead of rebuilding the mod's item table by hand.
   */
  public Item.Properties properties() {
    ResourceKey<Item> key = constructingKey.get();
    if (key == null) {
      throw new IllegalStateException("Item.Properties requested outside an active registration");
    }
    return new Item.Properties().setId(key);
  }
}
