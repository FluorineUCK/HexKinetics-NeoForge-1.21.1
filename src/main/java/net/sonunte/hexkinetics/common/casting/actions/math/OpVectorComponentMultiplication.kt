package net.sonunte.hexkinetics.common.casting.actions.math

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3

object OpVectorComponentMultiplication : ConstMediaAction {

	override val argc = 2

	override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
		val f = args.getVec3(0, argc)
		val s = args.getVec3(1, argc)

		val multi = Vec3(f.x * s.x, f.y * s.y, f.z * s.z)
		return multi.asActionResult
	}
}