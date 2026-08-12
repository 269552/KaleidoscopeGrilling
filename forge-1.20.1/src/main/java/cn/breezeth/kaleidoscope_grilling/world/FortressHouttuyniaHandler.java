package cn.breezeth.kaleidoscope_grilling.world;

import cn.breezeth.kaleidoscope_grilling.registry.ModItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;

public final class FortressHouttuyniaHandler {
  public static void onLoad(LootTableLoadEvent e) {
    if (!e.getName().equals(new ResourceLocation("minecraft", "chests/nether_bridge"))) return;
    e.getTable()
        .addPool(
            LootPool.lootPool()
                .name("kaleidoscope_grilling_houttuynia")
                .setRolls(UniformGenerator.between(1, 2))
                .when(LootItemRandomChanceCondition.randomChance(0.65f))
                .add(
                    LootItem.lootTableItem(ModItems.HOUTTUYNIA.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                .build());
  }

  private FortressHouttuyniaHandler() {}
}
