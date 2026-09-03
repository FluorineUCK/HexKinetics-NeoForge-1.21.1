package net.sonunte.hexkinetics.common.casting.actions.math

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPositiveDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import net.minecraft.world.phys.Vec3

object OpGetVectorsBy : ConstMediaAction {

	override val argc = 2


    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        val pos = args.getVec3(0, argc)
        val radius = args.getPositiveDouble(1, argc)

        val blockPositions = getBlockPositionsSphere(pos, radius)

        return blockPositions.map(::Vec3Iota).asActionResult
    }
    fun getBlockPositionsSphere(pos: Vec3, radius: Double): List<Vec3> {
        val blockPositions = ArrayList<Vec3>()
        val radiusInt = radius.toInt()
        val innerRadiusInt = radius - 1

        for (x in -radiusInt..radiusInt) {
            for (y in -radiusInt..radiusInt) {
                for (z in -radiusInt..radiusInt) {
                    val distanceSq = x * x + y * y + z * z
                    if (distanceSq <= radius * radius && distanceSq >= innerRadiusInt * innerRadiusInt) {
                        blockPositions.add(pos.add(x.toDouble(), y.toDouble(), z.toDouble()))
                    }
                }
            }
        }

        return blockPositions
    }
}