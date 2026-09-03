package net.sonunte.hexkinetics.common.casting.actions.great_spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object OpAcceleration : SpellAction {
    override val argc = 3

    private data class AccelerationState(
        var remainingTicks: Int,
        var waitTicks: Int,
        val force: Vec3
    )

    private val acceleratedEntities = HashMap<Entity, AccelerationState>()

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val target = args.getEntity(env.world, 0, argc)
        val time = Mth.clamp(args.getDouble(1, argc), 0.0, 200.0)
        val force = args.getVec3(2, argc)
        env.assertEntityInRange(target)

        val cost = when {
            force.length() >= 1 -> (force.lengthSqr() * time * MediaConstants.DUST_UNIT).toLong()
            force.length() > 0 -> MediaConstants.DUST_UNIT * time.toLong()
            else -> 0L
        }

        return SpellAction.Result(
            Spell(target, time, force),
            cost,
            listOf(ParticleSpray.burst(target.position().add(0.0, target.eyeHeight / 2.0, 0.0), 1.0))
        )
    }

    private data class Spell(val target: Entity, val time: Double, val force: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            acceleratedEntities.putIfAbsent(
                target,
                AccelerationState(time.toInt() * 5 + 5, 0, force)
            )
        }
    }

    @JvmStatic
    fun tickAcceleratedEntities() {
        val iterator = acceleratedEntities.iterator()
        while (iterator.hasNext()) {
            val (entity, state) = iterator.next()
            if (entity.isRemoved || state.remainingTicks <= 0) {
                iterator.remove()
                continue
            }

            if (state.waitTicks == 5) {
                entity.push(state.force.x, state.force.y, state.force.z)
                entity.hurtMarked = true
            }
            state.waitTicks = if (state.waitTicks >= 5) 0 else state.waitTicks + 1
            state.remainingTicks--
        }
    }
}
