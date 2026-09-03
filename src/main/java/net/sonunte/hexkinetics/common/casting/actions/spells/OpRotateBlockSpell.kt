package net.sonunte.hexkinetics.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.RailBlock.SHAPE
import net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING
import net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING_HOPPER
import net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING
import net.minecraft.world.level.block.state.properties.BlockStateProperties.VERTICAL_DIRECTION
import net.minecraft.world.level.block.state.properties.RailShape
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

object OpRotateBlockSpell : SpellAction {
    override val argc = 2
    private const val COST = (MediaConstants.DUST_UNIT * 0.125).toLong()

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val target = args.getVec3(0, argc)
        val rotation = args.getVec3(1, argc)
        env.assertVecInRange(target)
        return SpellAction.Result(
            Spell(target, rotation),
            COST,
            listOf(ParticleSpray.burst(target, 2.0, 100))
        )
    }

    private data class Spell(val target: Vec3, val rotation: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val blockPos = BlockPos.containing(target)
            val blockState = env.world.getBlockState(blockPos)
            if (!env.canEditBlockAt(blockPos)) return
            if (!IXplatAbstractions.INSTANCE.isBreakingAllowed(env.world, blockPos, blockState, env.caster)) return
            setBlockDirection(env.world, blockPos, getDirectionFromVector(rotation))
        }
    }

    private fun setBlockDirection(world: ServerLevel, blockPos: BlockPos, direction: Direction) {
        val state = world.getBlockState(blockPos)
        val block = state.block
        if (block.explosionResistance >= 600) return

        val modified = when {
            state.hasProperty(SHAPE) -> state.setValue(SHAPE, getRailShapeFromDirection(direction))
            state.hasProperty(FACING) && FACING.possibleValues.contains(direction) -> state.setValue(FACING, direction)
            state.hasProperty(FACING_HOPPER) -> state.setValue(
                FACING_HOPPER,
                if (direction == Direction.UP) Direction.DOWN else direction
            )
            state.hasProperty(HORIZONTAL_FACING) -> state.setValue(
                HORIZONTAL_FACING,
                if (direction.axis.isHorizontal) direction else Direction.NORTH
            )
            state.hasProperty(VERTICAL_DIRECTION) && direction.axis.isVertical ->
                state.setValue(VERTICAL_DIRECTION, direction)
            else -> return
        }

        world.setBlockAndUpdate(blockPos, modified)
        world.updateNeighborsAt(blockPos, block)
    }

    private fun getDirectionFromVector(vector: Vec3): Direction {
        if (vector.lengthSqr() == 0.0) return Direction.NORTH
        val x = abs(vector.x)
        val y = abs(vector.y)
        val z = abs(vector.z)
        return when {
            x > z && x >= y -> if (vector.x > 0) Direction.EAST else Direction.WEST
            y > x && y > z -> if (vector.y > 0) Direction.UP else Direction.DOWN
            else -> if (vector.z > 0) Direction.SOUTH else Direction.NORTH
        }
    }

    private fun getRailShapeFromDirection(direction: Direction): RailShape = when (direction) {
        Direction.EAST, Direction.WEST -> RailShape.EAST_WEST
        else -> RailShape.NORTH_SOUTH
    }
}
