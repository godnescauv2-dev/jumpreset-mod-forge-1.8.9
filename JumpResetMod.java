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

    // --- CONFIG (como Vape V4) ---
    public static double strength = 85.0;    // % de redução do KB (0-100%)
    public static double chance = 85.0;      // % de chance de ativar
    public static int minDelay = 0;           // ticks mínimos entre resets
    public static int maxDelay = 1;           // ticks máximos entre resets (random)

    // Estado interno
    private int ticksSinceLastReset = 0;
    private int delayBeforeNext = 0;
    private int hurtTimePrev = 0;
    private boolean wasInAir = false;

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

        // --- 1. Delay entre ativações ---
        if (delayBeforeNext > 0) {
            delayBeforeNext--;
            return;
        }

        // --- 2. Só ativa se tomou dano NOVO (hurtTime mudou de 0 para >0) ---
        int currentHurt = player.hurtTime;
        boolean justGotHit = (currentHurt > 0 && hurtTimePrev == 0);
        hurtTimePrev = currentHurt;

        if (!justGotHit) return;

        // --- 3. Chance de ativar (Vape V4 nunca ativa 100% das vezes) ---
        if (!checkChance()) return;

        // --- 4. Só ativa no chão (Vape não reseta no ar) ---
        if (!player.onGround) return;

        // --- 5. Executa o jump reset REDUZIDO (estilo Vape V4) ---
        applyVapeJumpReset(player);

        // --- 6. Seta delay aleatório para não ser detectado ---
        delayBeforeNext = minDelay + ThreadLocalRandom.current().nextInt(maxDelay - minDelay + 1);
        ticksSinceLastReset = 0;
    }

    /**
     * Aplica o jump reset IGUAL ao Vape V4:
     * - Reduz motionX/motionZ (não zera)
     * - Reseta motionY
     * - Pula
     */
    private void applyVapeJumpReset(EntityPlayerSP player) {
        double reduction = strength / 100.0; // 0.0 a 1.0

        // Salva a velocity original
        double origX = player.motionX;
        double origZ = player.motionZ;
        double origY = player.motionY;

        // --- REDUZ motionX/motionZ (não zera!) ---
        // Vape V4 reduz em ~80-85% e mantém 15-20% para o servidor ver
        player.motionX = origX * (1.0 - reduction);
        player.motionZ = origZ * (1.0 - reduction);

        // --- Reseta motionY (sempre) ---
        player.motionY = 0;

        // --- Pula instantaneamente ---
        player.jump();

        // --- Adiciona um pequeno desvio aleatório na velocity reduzida ---
        // para não gerar padrão detectável
        double jitter = ThreadLocalRandom.current().nextDouble(-0.005, 0.005);
        player.motionX += jitter;
        player.motionZ += jitter;
    }

    /**
     * Chance com randomização natural (Vape usa ThreadLocalRandom)
     */
    private boolean checkChance() {
        if (chance >= 100.0) return true;
        if (chance <= 0.0) return false;
        return ThreadLocalRandom.current().nextDouble(0, 100) < chance;
    }
}
