package cn.breezeth.kaleidoscope_grilling.world;

import cn.breezeth.kaleidoscope_grilling.registry.ModItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.event.LootTableLoadEvent;

public final class FortressHouttuyniaHandler {
  public static void onLoad(LootTableLoadEvent e) {
    if (!e.getName().equals(ResourceLocation.withDefaultNamespace("chests/nether_bridge"))) return;
    e.getTable()
        .addPool(
            LootPool.lootPool()
                .setRolls(UniformGenerator.between(1, 2))
                .when(LootItemRandomChanceCondition.randomChance(0.65f))
                .add(
                    LootItem.lootTableItem(ModItems.HOUTTUYNIA.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                .build());
  }

  private FortressHouttuyniaHandler() {}
}
