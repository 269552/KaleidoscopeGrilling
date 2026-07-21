package cn.breezeth.kaleidoscope_grilling.mixin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public interface FoodDataAccessor {
  @Accessor("foodLevel")
  int grilling$getFoodLevel();

  @Accessor("foodLevel")
  void grilling$setFoodLevel(int value);

  @Accessor("saturationLevel")
  float grilling$getSaturationLevel();

  @Accessor("saturationLevel")
  void grilling$setSaturationLevel(float value);

  @Accessor("exhaustionLevel")
  float grilling$getExhaustionLevel();

  @Accessor("exhaustionLevel")
  void grilling$setExhaustionLevel(float value);
}
