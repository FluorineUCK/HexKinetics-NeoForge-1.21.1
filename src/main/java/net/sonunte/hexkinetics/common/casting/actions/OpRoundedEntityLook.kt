package net.sonunte.hexkinetics.common.casting.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3
import kotlin.math.round

object OpRoundedEntityLook : ConstMediaAction {


	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
		val e = args.getEntity(ctx.world, 0, argc)
		ctx.assertEntityInRange(e)
		val roundedVector = Vec3(round(e.lookAngle.x),round(e.lookAngle.y),round(e.lookAngle.z))
		return roundedVector.asActionResult
	}
}