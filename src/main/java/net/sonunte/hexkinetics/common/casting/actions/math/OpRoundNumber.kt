package net.sonunte.hexkinetics.common.casting.actions.math

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getNumOrVec
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToInt

object OpRoundNumber : ConstMediaAction {

	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
		val num = args.getNumOrVec(0, argc)

		return num.map(
			{ lnum ->
				num.map(
					{ (lnum.roundToInt()).asActionResult }, { rvec -> Vec3(rvec.x.roundToInt().toDouble(), rvec.y.roundToInt().toDouble(), rvec.z.roundToInt().toDouble()).asActionResult }
				)
			}, { lvec ->
				num.map(
					{ rnum -> (rnum.roundToInt()).asActionResult }, { Vec3(lvec.x.roundToInt().toDouble(), lvec.y.roundToInt().toDouble(), lvec.z.roundToInt().toDouble()).asActionResult }
				)
			})
	}
}