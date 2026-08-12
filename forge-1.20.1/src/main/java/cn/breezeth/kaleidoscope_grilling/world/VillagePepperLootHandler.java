package cn.breezeth.kaleidoscope_grilling.world;

import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;

public final class VillagePepperLootHandler {
  private static final String VILLAGE_CHEST_PREFIX = "chests/village/";

  public static void onLoad(LootTableLoadEvent event) {
    ResourceLocation name = event.getName();
    if (!"minecraft".equals(name.getNamespace())
        || !name.getPath().startsWith(VILLAGE_CHEST_PREFIX)) {
      return;
    }

    event
        .getTable()
        .addPool(
            LootPool.lootPool()
                .name("kaleidoscope_grilling_village_pepper")
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(0.4F))
                .add(
                    LootItem.lootTableItem(ModItems.SICHUAN_PEPPER.get())
                        .apply(
                            SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 10.0F))))
                .build());
    event
        .getTable()
        .addPool(
            LootPool.lootPool()
                .name("kaleidoscope_grilling_village_pepper_sapling")
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(0.2F))
                .add(LootItem.lootTableItem(ModBlocks.PEPPER_SAPLING_ITEM.get()))
                .build());
  }

  private VillagePepperLootHandler() {}
}
