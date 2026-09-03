package net.sonunte.hexkinetics.common.casting.actions.math

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import net.minecraft.world.phys.Vec3

object OpGetVectorsFrom : ConstMediaAction {

	override val argc = 1


    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        val poss = args.getListVec3(0, argc)


        return findVectorsBetween(poss).map(::Vec3Iota).asActionResult
    }
    private fun List<Iota>.getListVec3(idx: Int, argc: Int = 0): List<Vec3> {
        val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
        val out = mutableListOf<Vec3>()

        when (x) {
            is ListIota -> {
                for (v in x.list) {
                    if (v is Vec3Iota) {
                        out.add(v.vec3)
                    } else {
                        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "veclist")
                    }
                }
            }
            else -> throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "veclist")
        }

        return out
    }
    private fun findVectorsBetween(vectors: List<Vec3>): List<Vec3> {
        val result = mutableListOf<Vec3>()

        if (vectors.size < 2) {
            return result
        }

        // Iterate through adjacent vector pairs. The upstream 0.7.3 loop used
        // vectors[i + 1] while iterating through the final element and always
        // crashed; this is the intended cuboid-line behavior described in the book.
        for (i in 0 until vectors.lastIndex) {
            val start = vectors[i]
            val end = vectors[i + 1]

            val diffX = end.x - start.x
            val diffY = end.y - start.y
            val diffZ = end.z - start.z

            val steps = maxOf(Math.abs(diffX), Math.abs(diffY), Math.abs(diffZ))

            if (steps == 0.0) {
                result.add(start)
                continue
            }

            for (step in 0..steps.toInt()) {
                val intermediateX = start.x + diffX / steps * step
                val intermediateY = start.y + diffY / steps * step
                val intermediateZ = start.z + diffZ / steps * step
                result.add(Vec3(intermediateX, intermediateY, intermediateZ))
            }
        }

        return result
    }
}
