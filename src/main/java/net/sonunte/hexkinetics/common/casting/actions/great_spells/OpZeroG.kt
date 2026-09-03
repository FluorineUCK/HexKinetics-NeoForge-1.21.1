package net.sonunte.hexkinetics.common.casting.actions.great_spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player

object OpZeroG : SpellAction {
    override val argc = 2
    private val entityTicks = HashMap<Entity, Int>()

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val target = args.getEntity(env.world, 0, argc)
        val time = args.getDouble(1, argc)
        env.assertEntityInRange(target)

        val cost = if (time in 0.0..1.0) {
            time.toLong() * MediaConstants.DUST_UNIT
        } else {
            (time * 2).toLong() * MediaConstants.DUST_UNIT
        }
        return SpellAction.Result(
            Spell(target, time),
            cost,
            listOf(ParticleSpray.burst(target.position().add(0.0, target.eyeHeight / 2.0, 0.0), 1.0))
        )
    }

    private data class Spell(val target: Entity, val time: Double) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (!entityTicks.containsKey(target)) {
                entityTicks[target] = time.coerceAtLeast(0.0).toInt() * 20
                target.isNoGravity = true
                target.hurtMarked = true
            }
        }
    }

    @JvmStatic
    fun tickZeroGEntities() {
        val iterator = entityTicks.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val entity = entry.key
            val ticks = entry.value

            if (entity.isRemoved || ticks <= 0) {
                iterator.remove()
                entity.isNoGravity = false
                continue
            }

            entity.resetFallDistance()
            if (entity !is Player || !entity.isFallFlying) {
                entity.push(
                    entity.deltaMovement.x * 0.1,
                    entity.deltaMovement.y * 0.01,
                    entity.deltaMovement.z * 0.1
                )
                entity.hurtMarked = true
            }
            entry.setValue(ticks - 1)
        }
    }

    @JvmStatic
    fun unloadZeroGEntity(entity: Entity) {
        if (entityTicks.remove(entity) != null) {
            entity.isNoGravity = false
        }
    }
}
