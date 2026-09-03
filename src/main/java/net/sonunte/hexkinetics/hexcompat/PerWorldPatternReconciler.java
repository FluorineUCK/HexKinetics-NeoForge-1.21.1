package net.sonunte.hexkinetics.hexcompat;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.server.ScrungledPatternsSave;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.sonunte.hexkinetics.api.HexKineticsAPI;

import java.util.List;

/**
 * Hex Casting 1.21.1 persists per-world patterns only when the table is first
 * created. Installing HexKinetics into an existing world leaves its great
 * spells absent from that table, so ancient scrolls cannot resolve them.
 */
public final class PerWorldPatternReconciler {
    private PerWorldPatternReconciler() {
    }

    public static void reconcile(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        Registry<ActionRegistryEntry> actions = IXplatAbstractions.INSTANCE.getActionRegistry();
        ScrungledPatternsSave savedPatterns = ScrungledPatternsSave.open(overworld);

        List<ResourceKey<ActionRegistryEntry>> expected = actions.registryKeySet().stream()
                .filter(key -> key.location().getNamespace().equals(HexKineticsAPI.MOD_ID))
                .filter(key -> HexUtils.isOfTag(actions, key, HexTags.Actions.PER_WORLD_PATTERN))
                .toList();
        List<ResourceKey<ActionRegistryEntry>> missing = expected.stream()
                .filter(key -> savedPatterns.lookupReverse(key) == null
                        || PatternRegistryManifest.getCanonicalStrokesPerWorld(key, overworld) == null)
                .toList();

        if (missing.isEmpty()) {
            HexKineticsAPI.LOGGER.info(
                    "[HEXKINETICS-PROBE] per_world_patterns=PASS expected={} missing=0",
                    expected.size()
            );
            return;
        }

        HexKineticsAPI.LOGGER.warn(
                "Hex Casting's saved per-world pattern table is missing {} HexKinetics action(s): {}. "
                        + "Recalculating it with the current registry.",
                missing.size(),
                missing.stream().map(key -> key.location().toString()).toList()
        );

        ScrungledPatternsSave rebuilt = ScrungledPatternsSave.createFromScratch(overworld.getSeed());
        overworld.getDataStorage().set(ScrungledPatternsSave.TAG_SAVED_DATA, rebuilt);

        List<ResourceKey<ActionRegistryEntry>> unresolved = expected.stream()
                .filter(key -> rebuilt.lookupReverse(key) == null
                        || PatternRegistryManifest.getCanonicalStrokesPerWorld(key, overworld) == null)
                .toList();
        if (unresolved.isEmpty()) {
            HexKineticsAPI.LOGGER.info(
                    "[HEXKINETICS-PROBE] per_world_patterns=PASS expected={} repaired={}",
                    expected.size(),
                    missing.size()
            );
        } else {
            HexKineticsAPI.LOGGER.error(
                    "[HEXKINETICS-PROBE] per_world_patterns=FAIL unresolved={}",
                    unresolved.stream().map(key -> key.location().toString()).toList()
            );
        }
    }
}
