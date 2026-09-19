package cn.breezeth.kaleidoscope_grilling.compat.create;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.SeasoningAutomationApi;
import cn.breezeth.kaleidoscope_grilling.SeasoningAutomationApi.MixPlan;
import cn.breezeth.kaleidoscope_grilling.SeasoningAutomationApi.SlotTake;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningData;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.logistics.filter.ListFilterItem;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe.Builder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.items.IItemHandler;

/** Dynamic Create mixing bridge that preserves seasoning ingredients on the output bottle. */
public final class CreateSeasoningCompat {
  private static MixingRecipe recipe;

  public static MixingRecipe recipe() {
    if (recipe == null)
      recipe =
          new Builder<>(
                  MixingRecipe::new,
                  ResourceLocation.fromNamespaceAndPath(
                      KaleidoscopeGrilling.MOD_ID, "automated_seasoning"))
              .duration(80)
              .output(ModItems.SPECIAL_SEASONING.get())
              .build();
    return recipe;
  }

  public static boolean isRecipe(Recipe<?> candidate) {
    return candidate == recipe();
  }

  public static boolean process(BasinBlockEntity basin, boolean simulate) {
    if (basin.getLevel() == null) return false;
    IItemHandler items = basin.getInvs().getFirst();
    List<ItemStack> snapshot = new ArrayList<>(items.getSlots());
    for (int slot = 0; slot < items.getSlots(); slot++)
      snapshot.add(items.getStackInSlot(slot).copy());
    List<List<String>> targets = readFilterTargets(basin);
    boolean targeted = targets != null;
    MixPlan plan =
        targeted
            ? SeasoningAutomationApi.plan(snapshot, targets)
            : SeasoningAutomationApi.plan(snapshot);
    if (plan == null) return false;
    ItemStack output = SeasoningAutomationApi.finish(plan, basin.getLevel().random);
    if (output.isEmpty() || (!targeted && !basin.getFilter().test(output))) return false;
    if (!basin.acceptOutputs(List.of(output), Collections.emptyList(), true)) return false;
    if (simulate) return true;

    int[] required = new int[items.getSlots()];
    for (SlotTake take : plan.takes()) required[take.slot()] += take.amount();
    for (int slot = 0; slot < required.length; slot++) {
      if (required[slot] > 0
          && items.extractItem(slot, required[slot], true).getCount() != required[slot]) return false;
    }
    for (SlotTake take : plan.takes()) {
      ItemStack extracted = items.extractItem(take.slot(), take.amount(), false);
      if (extracted.getCount() != take.amount()) return false;
    }
    if (!basin.acceptOutputs(List.of(output), Collections.emptyList(), false)) return false;
    basin.notifyChangeOfContents();
    basin.sendData();
    return true;
  }

  /** Returns null for normal filtering and a possibly empty list for targeted filtering. */
  private static List<List<String>> readFilterTargets(BasinBlockEntity basin) {
    ItemStack filter = basin.getFilter().getFilter();
    if (!(filter.getItem() instanceof ListFilterItem listFilter)) return null;
    if (filter.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false)
        || !filter.getOrDefault(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, false)) return null;

    IItemHandler entries = listFilter.getFilterItemHandler(filter);
    List<List<String>> targets = new ArrayList<>();
    for (int slot = 0; slot < entries.getSlots(); slot++) {
      ItemStack sample = entries.getStackInSlot(slot);
      if (!sample.is(ModItems.SPECIAL_SEASONING.get())) continue;
      List<String> ingredients = SeasoningData.get(sample);
      if (SeasoningAutomationApi.isValidTarget(ingredients)) targets.add(List.copyOf(ingredients));
    }
    return targets;
  }

  private CreateSeasoningCompat() {}
}
