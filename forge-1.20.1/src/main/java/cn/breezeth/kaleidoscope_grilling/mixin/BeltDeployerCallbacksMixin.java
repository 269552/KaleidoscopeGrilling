package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.ModItems;
import cn.breezeth.kaleidoscope_grilling.ModSounds;
import cn.breezeth.kaleidoscope_grilling.SeasoningAutomationApi;
import cn.breezeth.kaleidoscope_grilling.SecretSkewerItem;
import cn.breezeth.kaleidoscope_grilling.SkeweringHandler;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.BeltDeployerCallbacks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds skewer ingredients and seasoning to compatible items on a Create belt. */
@Mixin(BeltDeployerCallbacks.class)
public abstract class BeltDeployerCallbacksMixin {
  @Inject(method = "activate", at = @At("HEAD"), cancellable = true, remap = false)
  private static void grilling$skewerOnActivate(
      TransportedItemStack transported,
      TransportedItemStackHandlerBehaviour handler,
      DeployerBlockEntity blockEntity,
      Recipe<?> recipe,
      CallbackInfo ci) {
    ItemStack held =
        blockEntity.getPlayer() == null ? ItemStack.EMPTY : blockEntity.getPlayer().getMainHandItem();
    if (held.isEmpty()) return;
    ItemStack target = transported.stack;
    Level level = blockEntity.getLevel();
    if (level == null || level.isClientSide) return;

    ItemStack seasoningResult = SeasoningAutomationApi.appendIngredient(target, held);
    if (!seasoningResult.isEmpty()) {
      grilling$convertOne(
          transported,
          handler,
          blockEntity,
          held,
          seasoningResult,
          ModSounds.ACTION_SUCCESS.get(),
          .65F,
          1.0F);
      ci.cancel();
      return;
    }

    if (!target.is(Items.STICK)
        && !target.is(ModItems.UNFINISHED_SKEWER.get())
        && !(target.is(ModItems.SECRET_SKEWER.get()) && !SecretSkewerItem.isCooked(target))) return;

    ItemStack result = SkeweringHandler.appendToSkewer(target, held, level.random, null);
    if (result.isEmpty()) return;

    grilling$convertOne(
        transported,
        handler,
        blockEntity,
        held,
        result,
        SoundEvents.ITEM_PICKUP,
        .25F,
        .75F);
    ci.cancel();
  }

  @Unique
  private static void grilling$convertOne(
      TransportedItemStack transported,
      TransportedItemStackHandlerBehaviour handler,
      DeployerBlockEntity blockEntity,
      ItemStack held,
      ItemStack result,
      SoundEvent sound,
      float volume,
      float pitch) {
    ItemStack remainder = held.getCraftingRemainingItem();
    held.shrink(1);
    if (held.isEmpty()) {
      blockEntity.getPlayer().setItemInHand(InteractionHand.MAIN_HAND, remainder);
    } else if (!remainder.isEmpty()) {
      Player player = blockEntity.getPlayer();
      if (!player.getInventory().add(remainder)) player.drop(remainder, false);
    }
    // Consume one belt target and leave the rest while emitting the completed step.
    TransportedItemStack left = transported.copy();
    left.stack = transported.stack.copy();
    left.stack.shrink(1);
    TransportedItemStack produced = transported.copy();
    produced.stack = result;
    handler.handleProcessingOnItem(
        transported, TransportedResult.convertToAndLeaveHeld(java.util.List.of(produced), left));
    transported.clearFanProcessingData();
    blockEntity
        .getLevel()
        .playSound(null, blockEntity.getBlockPos(), sound, SoundSource.BLOCKS, volume, pitch);
    blockEntity.notifyUpdate();
  }
}
