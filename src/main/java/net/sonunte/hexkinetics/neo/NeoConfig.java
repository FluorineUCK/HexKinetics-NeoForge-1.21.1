package net.sonunte.hexkinetics.neo;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.sonunte.hexkinetics.api.config.HexKineticsConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;

final class NeoConfig {
    static final HexKineticsConfig.CommonConfigAccess COMMON_ACCESS = new HexKineticsConfig.CommonConfigAccess() {};
    static final HexKineticsConfig.ClientConfigAccess CLIENT_ACCESS = new HexKineticsConfig.ClientConfigAccess() {};

    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.BooleanValue MOVE_TILE_ENTITIES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> TRANSLOCATION_DENY_LIST;

    static {
        SERVER_BUILDER.push("translocation");
        MOVE_TILE_ENTITIES = SERVER_BUILDER
            .comment("Whether Greater Translocation may move block entities.")
            .define("moveTileEntities", HexKineticsConfig.ServerConfigAccess.DEFAULT_MOVE_TILE_ENTITIES);
        TRANSLOCATION_DENY_LIST = SERVER_BUILDER
            .comment("Blocks that Greater Translocation may not move.")
            .defineListAllowEmpty(
                "translocationDenyList",
                HexKineticsConfig.ServerConfigAccess.Companion.getDEFAULT_TRANSLOCATION_DENY_LIST(),
                NeoConfig::isResourceLocation);
        SERVER_BUILDER.pop();
    }

    static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    static final HexKineticsConfig.ServerConfigAccess SERVER_ACCESS = new HexKineticsConfig.ServerConfigAccess() {
        @Override
        public boolean getMoveTileEntities() {
            return MOVE_TILE_ENTITIES.get();
        }

        @Override
        public int getExampleConstActionCost() {
            // The upstream option was never wired to an action (and the old
            // Forge declaration was null). Keep its effective behavior: zero.
            return 0;
        }

        @Override
        public boolean isTranslocationAllowed(@NotNull ResourceLocation blockId) {
            return HexKineticsConfig.INSTANCE.noneMatch(TRANSLOCATION_DENY_LIST.get(), blockId);
        }
    };

    private static boolean isResourceLocation(Object value) {
        return value instanceof String string && ResourceLocation.tryParse(string) != null;
    }

    private NeoConfig() {}
}
