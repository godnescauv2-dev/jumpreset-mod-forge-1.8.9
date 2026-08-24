package me.jumpreset;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.ThreadLocalRandom;

@Mod(modid = JumpResetMod.MODID, version = JumpResetMod.VERSION, name = JumpResetMod.NAME)
public class JumpResetMod {

    public static final String MODID = "jumpresetv4";
    public static final String NAME = "Jump Reset V4";
    public static final String VERSION = "1.0.0";

    public static boolean enabled = true;

    // Config (ajustável por comando)
    public static double strength = 85.0;  // 0-100%
    public static double chance = 85.0;    // 0-100%
    public static int minDelay = 0;
    public static int maxDelay = 1;

    // Estado interno
    private int delayBeforeNext = 0;
    private int hurtTimePrev = 0;

    private static JumpResetMod instance;

    public JumpResetMod() {
        instance = this;
    }

    public static JumpResetMod getInstance() {
        return instance;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new CommandJumpReset());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!enabled) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player != Minecraft.getMinecraft().thePlayer) return;

        EntityPlayerSP player = (EntityPlayerSP) event.player;

        // Delay entre ativações
        if (delayBeforeNext > 0) {
            delayBeforeNext--;
            return;
        }

        // Detecta dano novo
        int currentHurt = player.hurtTime;
        boolean justGotHit = (currentHurt > 0 && hurtTimePrev == 0);
        hurtTimePrev = currentHurt;

        if (!justGotHit) return;

        // Chance
        if (!checkChance()) return;

        // Só no chão
        if (!player.onGround) return;

        // Aplica jump reset reduzido
        applyJumpReset(player);

        // Delay aleatório anti-padrão
        delayBeforeNext = minDelay + ThreadLocalRandom.current().nextInt(maxDelay - minDelay + 1);
    }

    private void applyJumpReset(EntityPlayerSP player) {
        double reduction = strength / 100.0;

        double origX = player.motionX;
        double origZ = player.motionZ;

        // Reduz motionX/motionZ (não zera!)
        player.motionX = origX * (1.0 - reduction);
        player.motionZ = origZ * (1.0 - reduction);

        // Reseta motionY e pula
        player.motionY = 0;
        player.jump();

        // Jitter para evitar padrão
        double jitter = ThreadLocalRandom.current().nextDouble(-0.005, 0.005);
        player.motionX += jitter;
        player.motionZ += jitter;
    }

    private boolean checkChance() {
        if (chance >= 100.0) return true;
        if (chance <= 0.0) return false;
        return ThreadLocalRandom.current().nextDouble(0, 100) < chance;
    }
}
