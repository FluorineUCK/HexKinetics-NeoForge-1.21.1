package net.sonunte.hexkinetics.common.casting.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import net.minecraft.world.entity.TamableAnimal

object OpSecretTwo : ConstMediaAction {

	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
		val vehicle = args.getEntity(ctx.world, 0, argc)
		val target = vehicle.firstPassenger

		ctx.assertEntityInRange(vehicle)


		return target.asActionResult
	}
}