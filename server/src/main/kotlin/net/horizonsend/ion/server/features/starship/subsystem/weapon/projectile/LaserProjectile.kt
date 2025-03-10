package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector

abstract class LaserProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Controller?
) : ParticleProjectile(starship, loc, dir, shooter) {
	abstract val color: Color
	abstract val particleThickness: Double

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		// val entity = loc.world.spawnEntity(Location(loc.world, x, y, z), EntityType.BLOCK_DISPLAY) as Display

		val particle = Particle.REDSTONE
		val dustOptions = Particle.DustOptions(color, particleThickness.toFloat() * 4f)
		loc.world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, force)
	}
}
