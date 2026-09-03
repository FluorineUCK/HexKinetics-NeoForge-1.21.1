package net.sonunte.hexkinetics.neo;

import at.petrak.hexcasting.common.lib.HexRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.sonunte.hexkinetics.api.HexKineticsAPI;
import net.sonunte.hexkinetics.api.config.HexKineticsConfig;
import net.sonunte.hexkinetics.common.casting.Patterns;
import net.sonunte.hexkinetics.common.casting.actions.great_spells.OpAcceleration;
import net.sonunte.hexkinetics.common.casting.actions.great_spells.OpZeroG;
import net.sonunte.hexkinetics.hexcompat.PerWorldPatternReconciler;
import kotlin.Unit;

@Mod(HexKineticsAPI.MOD_ID)
public final class HexKineticsNeo {
    public HexKineticsNeo(IEventBus modBus, ModContainer modContainer) {
        HexKineticsConfig.setCommon(NeoConfig.COMMON_ACCESS);
        HexKineticsConfig.setClient(NeoConfig.CLIENT_ACCESS);
        HexKineticsConfig.setServer(NeoConfig.SERVER_ACCESS);
        modContainer.registerConfig(ModConfig.Type.SERVER, NeoConfig.SERVER_SPEC);

        modBus.addListener(this::registerHexContent);
        NeoForge.EVENT_BUS.addListener(HexKineticsNeo::afterServerTick);
        NeoForge.EVENT_BUS.addListener(HexKineticsNeo::onEntityLeaveLevel);
        NeoForge.EVENT_BUS.addListener(HexKineticsNeo::onServerStarted);
        HexKineticsAPI.LOGGER.info("HexKinetics NeoForge pre-39 compatibility port initialized");
    }

    private void registerHexContent(RegisterEvent event) {
        if (event.getRegistryKey().equals(HexRegistries.ACTION)) {
            Patterns.registerAll((ResourceLocation id, at.petrak.hexcasting.api.casting.ActionRegistryEntry entry) -> {
                event.register(HexRegistries.ACTION, id, () -> entry);
                return Unit.INSTANCE;
            });
        }
    }

    private static void afterServerTick(ServerTickEvent.Post event) {
        OpZeroG.tickZeroGEntities();
        OpAcceleration.tickAcceleratedEntities();
    }

    private static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        OpZeroG.unloadZeroGEntity(event.getEntity());
    }

    private static void onServerStarted(ServerStartedEvent event) {
        PerWorldPatternReconciler.reconcile(event.getServer());
        if (Boolean.getBoolean("hexkinetics.probe.exitAfterPatternValidation")) {
            Thread hardStop = new Thread(() -> {
                try {
                    Thread.sleep(5_000L);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                HexKineticsAPI.LOGGER.info("[HEXKINETICS-PROBE] hard_exit=REQUESTED");
                Runtime.getRuntime().halt(0);
            }, "hexkinetics-pattern-probe-hard-stop");
            hardStop.setDaemon(true);
            hardStop.start();
            event.getServer().halt(false);
        }
    }
}
