package net.sonunte.hexkinetics.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.getNumOrVec
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapImmuneEntity
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.common.casting.actions.spells.great.OpTeleport
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import kotlin.math.absoluteValue
import kotlin.math.floor

object OpLesserTeleport : SpellAction {
    override val argc = 2
    private const val COST = (MediaConstants.DUST_UNIT * 0.2).toLong()

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val entity = args.getEntity(env.world, 0, argc)
        val number = args.getNumOrVec(1, argc)
        val fractionX = Mth.clamp(number.map({ it.absoluteValue }, { it.x.absoluteValue }), 0.0001, 99.99999999)
        val fractionY = Mth.clamp(number.map({ it.absoluteValue }, { it.y.absoluteValue }), 0.0001, 99.99999999)
        val fractionZ = Mth.clamp(number.map({ it.absoluteValue }, { it.z.absoluteValue }), 0.0001, 99.99999999)
        env.assertEntityInRange(entity)

        if (entity.type.`is`(HexTags.Entities.CANNOT_TELEPORT)) {
            throw MishapImmuneEntity(entity)
        }

        return SpellAction.Result(
            Spell(entity, fractionX, fractionY, fractionZ),
            COST,
            listOf(ParticleSpray.burst(entity.position(), 1.0))
        )
    }

    private data class Spell(
        val entity: Entity,
        val fractionX: Double,
        val fractionY: Double,
        val fractionZ: Double
    ) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val position = entity.position()
            val target = Vec3(
                coordinateWithFraction(position.x, fractionX),
                coordinateWithFraction(position.y, fractionY),
                coordinateWithFraction(position.z, fractionZ)
            )
            OpTeleport.teleportRespectSticky(entity, target.subtract(position), env.world)
        }
    }

    private fun coordinateWithFraction(position: Double, percentage: Double): Double {
        val fraction = percentage / 100.0
        return if (position < 0.0) floor(position) + 1.0 - fraction else floor(position) + fraction
    }
}
