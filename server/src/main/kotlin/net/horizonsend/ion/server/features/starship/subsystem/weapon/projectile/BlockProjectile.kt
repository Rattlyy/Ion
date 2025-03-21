package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.Projectiles
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.nms
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.util.Vector

abstract class BlockProjectile(
    starship: ActiveStarship?,
    loc: Location,
    dir: Vector,
    shooter: Controller?
) : SimpleProjectile(starship, loc, dir, shooter) {
	abstract val blockMap: Map<Vec3i, BlockData>
	private val refreshedBlocks = LongOpenHashSet()

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		val newBlocks = renderNewBlocks(newLocation)
		renderOldBlocks(oldLocation, newBlocks)
	}

	private fun renderOldBlocks(oldLocation: Location, newBlocks: Set<Block>) {
		val oldBlockOrigin = oldLocation.block

		for ((dx, dy, dz) in blockMap.keys) {
			val block = oldBlockOrigin.getRelativeIfLoaded(dx, dy, dz)
				?: continue

			if (newBlocks.contains(block)) {
				continue
			}

			if (!refreshedBlocks.add(block.blockKey)) {
				continue
			}

			val currentData = block.blockData

			sendFakeBlock(block, currentData)
		}
	}

	private fun renderNewBlocks(newLocation: Location): Set<Block> {
		val newBlocks = mutableSetOf<Block>()

		if (!newLocation.isChunkLoaded) {
			return newBlocks
		}

		val newBlockOrigin = newLocation.block

		for ((offset, blockData) in blockMap) {
			val (dx, dy, dz) = offset
			val block = newBlockOrigin.getRelativeIfLoaded(dx, dy, dz)
				?: continue

			val currentData = block.blockData

			if (currentData == blockData) {
				continue
			}

			newBlocks.add(block)

			sendFakeBlock(block, blockData)

			val blockWorld = block.world
			val blockX = block.x
			val blockY = block.y
			val blockZ = block.z
			Tasks.syncDelay(Projectiles.TICK_INTERVAL + 1) {
				updateIfLoaded(blockWorld, blockX, blockY, blockZ)
			}
		}

		return newBlocks
	}

	private fun updateIfLoaded(blockWorld: World, blockX: Int, blockY: Int, blockZ: Int) {
		val block = getBlockIfLoaded(blockWorld, blockX, blockY, blockZ) ?: return

		if (!refreshedBlocks.add(block.blockKey)) {
			return
		}

		sendFakeBlock(block, block.blockData)
	}

	private fun sendFakeBlock(block: Block, blockData: BlockData) {
		val nmsBlockPos = BlockPos(block.x, block.y, block.z)
		val packet = ClientboundBlockUpdatePacket(nmsBlockPos, blockData.nms)
		block.chunk.minecraft.playerChunk?.broadcast(packet, false)
	}
}
