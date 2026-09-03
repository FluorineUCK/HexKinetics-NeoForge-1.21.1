package net.sonunte.hexkinetics.common.casting.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import net.minecraft.world.entity.projectile.Projectile

object OpSecretThree : ConstMediaAction {

	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
		val projectile = args.getEntity(ctx.world, 0, argc)

		ctx.assertEntityInRange(projectile)

		if (projectile is Projectile) {
			val target = projectile.owner

			return if (target != null) {
				if (ctx.isEntityInRange(target)) {
					target.asActionResult
				}else {
					listOf(NullIota())
				}
			}else {
				listOf(NullIota())
			}
		}else {
			return listOf(NullIota())
		}

	}
}