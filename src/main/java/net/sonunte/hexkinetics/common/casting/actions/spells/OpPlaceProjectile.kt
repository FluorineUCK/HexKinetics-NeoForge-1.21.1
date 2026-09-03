package net.sonunte.hexkinetics.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.entity.projectile.EyeOfEnder
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.SmallFireball
import net.minecraft.world.entity.projectile.Snowball
import net.minecraft.world.entity.projectile.SpectralArrow
import net.minecraft.world.entity.projectile.ThrownEgg
import net.minecraft.world.entity.projectile.ThrownEnderpearl
import net.minecraft.world.entity.projectile.ThrownTrident
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ArrowItem
import net.minecraft.world.item.EggItem
import net.minecraft.world.item.EnderEyeItem
import net.minecraft.world.item.EnderpearlItem
import net.minecraft.world.item.FireChargeItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.SnowballItem
import net.minecraft.world.item.TridentItem
import net.minecraft.world.phys.Vec3

object OpPlaceProjectile : SpellAction {
    override val argc = 1
    private const val COST = MediaConstants.SHARD_UNIT

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val caster = env.castingEntity ?: throw MishapBadCaster()
        val position = args.getVec3(0, argc)
        env.assertVecInRange(position)
        return SpellAction.Result(
            Spell(position, caster),
            COST,
            listOf(ParticleSpray.burst(position, 0.7))
        )
    }

    private data class Spell(val position: Vec3, val caster: LivingEntity) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val sourceStack = env.queryForMatchingStack(::isSupportedProjectile) ?: return
            if (sourceStack.isEmpty) return
            val projectileStack = sourceStack.copyWithCount(1)

            val projectile = when (sourceStack.item) {
                Items.ARROW, Items.TIPPED_ARROW ->
                    Arrow(env.world, position.x, position.y, position.z, projectileStack, null)
                Items.SPECTRAL_ARROW ->
                    SpectralArrow(env.world, position.x, position.y, position.z, projectileStack, null)
                Items.ENDER_PEARL ->
                    ThrownEnderpearl(env.world, caster)
                Items.SNOWBALL ->
                    Snowball(env.world, position.x, position.y, position.z)
                Items.EGG ->
                    ThrownEgg(env.world, position.x, position.y, position.z)
                Items.TRIDENT ->
                    ThrownTrident(env.world, position.x, position.y, position.z, projectileStack)
                Items.ENDER_EYE ->
                    EyeOfEnder(env.world, position.x, position.y, position.z).also { it.setItem(projectileStack) }
                Items.FIRE_CHARGE ->
                    SmallFireball(env.world, position.x, position.y, position.z, Vec3.ZERO)
                else -> return
            }

            if (projectile is Projectile) projectile.owner = caster
            projectile.setPos(position)
            env.world.addFreshEntity(projectile)
            env.withdrawItem(
                { candidate -> ItemStack.isSameItemSameComponents(candidate, sourceStack) },
                1,
                true
            )
        }
    }

    private fun isSupportedProjectile(stack: ItemStack): Boolean = when (stack.item) {
        is ArrowItem, is EnderpearlItem, is SnowballItem, is EggItem,
        is TridentItem, is EnderEyeItem, is FireChargeItem -> true
        else -> false
    }
}
