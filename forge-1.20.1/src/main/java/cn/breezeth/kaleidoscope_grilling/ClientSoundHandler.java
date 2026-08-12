package cn.breezeth.kaleidoscope_grilling;


import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
public final class ClientSoundHandler {
  private static ShakeSeasoningSound shake;

  @SubscribeEvent
  public static void clientTick(TickEvent.ClientTickEvent event) {
    if (event.phase != TickEvent.Phase.END) return;
    var player = Minecraft.getInstance().player;
    boolean shaking =
        player != null
            && player.isUsingItem()
            && player.getUseItem().is(ModItems.PENDING_SEASONING.get());
    if (shaking && shake == null) {
      shake = new ShakeSeasoningSound(player);
      Minecraft.getInstance().getSoundManager().play(shake);
    } else if (!shaking && shake != null) {
      shake.end();
      shake = null;
    }
    spawnPremiumOilPotParticles();
  }

  private static void spawnPremiumOilPotParticles() {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level == null
        || minecraft.player == null
        || minecraft.level.getGameTime() % 8L != 0L) return;
    BlockPos center = minecraft.player.blockPosition();
    BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    for (int x = -8; x <= 8; x++)
      for (int y = -4; y <= 4; y++)
        for (int z = -8; z <= 8; z++) {
          cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
          if (!(minecraft.level.getBlockEntity(cursor) instanceof TypedOilPotAccess oil)
              || !"premium_chili".equals(oil.grilling$getOilType())
              || minecraft.level.random.nextInt(5) != 0) continue;
          double px = cursor.getX() + 0.42D + minecraft.level.random.nextDouble() * 0.16D;
          double py = cursor.getY() + 0.72D;
          double pz = cursor.getZ() + 0.42D + minecraft.level.random.nextDouble() * 0.16D;
          minecraft.level.addParticle(ParticleTypes.LAVA, px, py, pz, 0.0D, 0.14D, 0.0D);
          minecraft.level.addParticle(ParticleTypes.FLAME, px, py, pz, 0.0D, 0.11D, 0.0D);
        }
  }

  private ClientSoundHandler() {}
}
