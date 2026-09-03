package net.sonunte.hexkinetics.common.casting.actions.great_spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.Vec3
import net.sonunte.hexkinetics.api.config.HexKineticsConfig

object OpMoveBlock : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val source = args.getVec3(0, argc)
        val offset = args.getVec3(1, argc)
        val destination = source.add(offset)

        env.assertVecInRange(source)
        env.assertVecInWorld(destination)

        val distance = offset.length()
        val cost = when {
            distance <= 1.0 -> MediaConstants.SHARD_UNIT
            distance < 100.0 -> MediaConstants.CRYSTAL_UNIT * 5
            distance <= 10_000.0 -> MediaConstants.CRYSTAL_UNIT * 10
            distance < 30_000.0 -> MediaConstants.CRYSTAL_UNIT * 10 +
                ((distance - 10_000.0) * MediaConstants.SHARD_UNIT).toLong()
            else -> 0L
        }

        return SpellAction.Result(
            Spell(source, offset),
            cost,
            listOf(
                ParticleSpray.burst(source, 1.0, 50),
                ParticleSpray.burst(destination, 1.0, 50)
            )
        )
    }

    private data class Spell(val source: Vec3, val offset: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val destinationVec = source.add(offset)
            if (!env.isVecInWorld(destinationVec) || offset.length() > 30_000.0) return

            val sourcePos = BlockPos.containing(source)
            val destinationPos = BlockPos.containing(destinationVec)
            if (sourcePos == destinationPos) return

            val sourceState = env.world.getBlockState(sourcePos)
            val destinationState = env.world.getBlockState(destinationPos)
            if (sourceState.isAir) return
            if (!HexKineticsConfig.server.isTranslocationAllowed(BuiltInRegistries.BLOCK.getKey(sourceState.block))) return
            if (!env.canEditBlockAt(sourcePos) || !env.canEditBlockAt(destinationPos)) return
            if (!IXplatAbstractions.INSTANCE.isBreakingAllowed(env.world, sourcePos, sourceState, env.caster)) return
            if (!IXplatAbstractions.INSTANCE.isBreakingAllowed(env.world, destinationPos, destinationState, env.caster)) return

            switchBlocks(env.world, env, sourcePos, destinationPos)
        }
    }

    private fun switchBlocks(
        world: ServerLevel,
        env: CastingEnvironment,
        source: BlockPos,
        destination: BlockPos
    ) {
        val sourceState = world.getBlockState(source)
        val destinationState = world.getBlockState(destination)
        val sourceHardness = sourceState.getDestroySpeed(world, source)
        val destinationHardness = destinationState.getDestroySpeed(world, destination)

        if (sourceHardness == destinationHardness) return
        val sourceEntity = world.getBlockEntity(source)
        if (sourceEntity != null && !HexKineticsConfig.server.moveTileEntities) return

        if ((sourceHardness > destinationHardness && destinationHardness >= 0) || sourceHardness < 0) {
            val entityTag = sourceEntity?.saveWithFullMetadata(world.registryAccess())
            world.destroyBlock(destination, true, env.caster)
            world.removeBlockEntity(source)
            world.setBlockAndUpdate(destination, sourceState)
            if (entityTag != null) {
                BlockEntity.loadStatic(destination, sourceState, entityTag, world.registryAccess())
                    ?.let(world::setBlockEntity)
            }
            world.setBlockAndUpdate(source, Blocks.AIR.defaultBlockState())
        } else if (sourceHardness < destinationHardness || destinationHardness < 0) {
            world.removeBlockEntity(source)
            world.setBlockAndUpdate(source, Blocks.AIR.defaultBlockState())
        }
    }
}
