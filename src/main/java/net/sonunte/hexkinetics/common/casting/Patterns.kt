@file:Suppress("unused")

package net.sonunte.hexkinetics.common.casting

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import net.minecraft.resources.ResourceLocation
import net.sonunte.hexkinetics.api.HexKineticsAPI.modLoc
import net.sonunte.hexkinetics.common.casting.actions.*
import net.sonunte.hexkinetics.common.casting.actions.great_spells.OpAcceleration
import net.sonunte.hexkinetics.common.casting.actions.great_spells.OpMoveBlock
import net.sonunte.hexkinetics.common.casting.actions.great_spells.OpZeroG
import net.sonunte.hexkinetics.common.casting.actions.math.*
import net.sonunte.hexkinetics.common.casting.actions.spells.*

/** Stable 1.21.1 action inventory. Per-world status is supplied by the
 * `hexcasting:per_world_pattern` action tag, not by a private registry. */
object Patterns {
    data class Definition(
        val pattern: HexPattern,
        val id: ResourceLocation,
        val action: Action,
        val perWorld: Boolean
    )

    @JvmField
    val PATTERNS: MutableList<Definition> = ArrayList()

    @JvmStatic
    fun registerAll(registrar: (ResourceLocation, ActionRegistryEntry) -> Unit) {
        PATTERNS.forEach { registrar(it.id, ActionRegistryEntry(it.pattern, it.action)) }
    }

    @JvmField val DIRECTION_LOOK = make("waa", HexDir.EAST, "direction/const", OpRoundedEntityLook)
    @JvmField val IS_VISIBLE = make("aqadwawaw", HexDir.NORTH_WEST, "visibility/const", OpVisibilityDistillation)
    @JvmField val IS_GRAVITY = make("daad", HexDir.EAST, "is_gravity/const", OpGravityPurification)
    @JvmField val VECTORS_MULTI = make("awaqawa", HexDir.WEST, "hadamard/const", OpVectorComponentMultiplication)
    @JvmField val PIXEL_RAY = make("weqaqded", HexDir.EAST, "pixel/raycast", OpPixelRaycast)
    @JvmField val SECRET = make("qqdeewee", HexDir.SOUTH_EAST, "get_vehicle/const", OpSecret)
    @JvmField val SECRET_TWO = make("qqdeeaedeaee", HexDir.SOUTH_EAST, "get_rider/const", OpSecretTwo)
    @JvmField val SECRET_THREE = make("aadedade", HexDir.EAST, "get_shooter/const", OpSecretThree)
    @JvmField val ROUND_NUM = make("aadeeaa", HexDir.SOUTH_EAST, "round/const", OpRoundNumber)
    @JvmField val VECTOR_REFLECTION = make("qqqqqdqqqqq", HexDir.SOUTH_EAST, "reflection/const", OpVectorReflection)
    @JvmField val GET_VECTORS_BY = make("qqqqqeddedq", HexDir.SOUTH_EAST, "get_vec/const", OpGetVectorsBy)
    @JvmField val GET_VECTORS_FROM = make("qaqeeqaq", HexDir.WEST, "get_vec_from/const", OpGetVectorsFrom)
    @JvmField val ROTATE_SPELL = make("qqqadeeed", HexDir.EAST, "rotate/spell", OpRotateSpell)
    @JvmField val ROTATE_TWO_SPELL = make("eeedaqqqa", HexDir.WEST, "rotate_two/spell", OpRotateTwoSpell)
    @JvmField val ROTATE_BLOCK_SPELL = make("qqqqqaqqqwadeeed", HexDir.SOUTH_EAST, "rotate_block/spell", OpRotateBlockSpell)
    @JvmField val MOTION_SWAP = make("adaadaqedaddad", HexDir.SOUTH_WEST, "swap/spell", OpMomentumSwap)
    @JvmField val LESSER_TELEPORT = make("edqdewqaeaq", HexDir.NORTH_EAST, "lesser_teleport/spell", OpLesserTeleport)
    @JvmField val PLACE_PROJECTILE = make("weeeweede", HexDir.SOUTH_EAST, "projectile/spell", OpPlaceProjectile)
    @JvmField val ZERO_G = make("wwqqqwadaadawqqqww", HexDir.SOUTH_WEST, "zero_g/spell", OpZeroG, true)
    @JvmField val ACCELERATION = make("wqeqaaeeeweeeaaqeqqaaq", HexDir.SOUTH_WEST, "fast/spell", OpAcceleration, true)
    @JvmField val MOVE_BLOCK = make("eeqeeqeeeqeeqdeeqeqqwqqqeeqeqqwqq", HexDir.SOUTH_EAST, "move_block/spell", OpMoveBlock, true)

    private fun make(
        signature: String,
        startDir: HexDir,
        path: String,
        action: Action,
        perWorld: Boolean = false
    ): PatternIota {
        val pattern = HexPattern.fromAngles(signature, startDir)
        PATTERNS.add(Definition(pattern, modLoc(path), action, perWorld))
        return PatternIota(pattern)
    }
}
