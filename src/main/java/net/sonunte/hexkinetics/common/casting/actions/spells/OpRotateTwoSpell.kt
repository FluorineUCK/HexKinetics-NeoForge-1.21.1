package net.sonunte.hexkinetics.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object OpRotateTwoSpell : SpellAction {
    override val argc = 2
    private const val COST = (MediaConstants.DUST_UNIT * 0.75).toLong()

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val target = args.getEntity(env.world, 0, argc)
        val rotation = args.getVec3(1, argc)
        env.assertEntityInRange(target)
        return SpellAction.Result(
            Spell(target, rotation),
            COST,
            listOf(ParticleSpray.burst(target.position().add(target.lookAngle), 1.9, 100))
        )
    }

    private data class Spell(val target: Entity, val rotation: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (rotation.lengthSqr() == 0.0) {
                target.deltaMovement = Vec3.ZERO
            } else {
                val speed = target.deltaMovement.length()
                target.deltaMovement = rotation.normalize().scale(speed)
            }
            target.hurtMarked = true
        }
    }
}
