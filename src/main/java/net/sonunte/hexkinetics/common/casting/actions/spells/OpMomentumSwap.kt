package net.sonunte.hexkinetics.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.world.entity.Entity

object OpMomentumSwap : SpellAction {
    override val argc = 2
    private const val COST = MediaConstants.DUST_UNIT

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val first = args.getEntity(env.world, 0, argc)
        val second = args.getEntity(env.world, 1, argc)
        env.assertEntityInRange(first)
        env.assertEntityInRange(second)

        return SpellAction.Result(
            Spell(first, second),
            COST,
            listOf(ParticleSpray.burst(first.position().add(second.position()).scale(0.5), 2.0, 100))
        )
    }

    private data class Spell(val first: Entity, val second: Entity) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val firstMotion = first.deltaMovement
            val secondMotion = second.deltaMovement
            first.deltaMovement = secondMotion
            second.deltaMovement = firstMotion
            first.hurtMarked = true
            second.hurtMarked = true
        }
    }
}
