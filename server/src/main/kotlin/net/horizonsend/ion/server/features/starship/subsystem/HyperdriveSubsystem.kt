package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.features.multiblock.hyperdrive.HyperdriveMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import org.bukkit.block.Hopper
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class HyperdriveSubsystem(starship: ActiveStarship, sign: Sign, multiblock: HyperdriveMultiblock) :
	AbstractMultiblockSubsystem<HyperdriveMultiblock>(starship, sign, multiblock) {
	private fun getHoppers(): Set<Hopper> {
		return multiblock.getHoppers(starship.serverLevel.world.getBlockAtKey(pos.toBlockKey()).getState(false) as Sign)
	}

	fun hasFuel(): Boolean = getHoppers().all { hopper ->
		hopper.inventory.asSequence()
			.filterNotNull()
			.filter(::isHypermatter)
			.sumOf { it.amount } >= Hyperspace.HYPERMATTER_AMOUNT
	}

	fun useFuel(): Unit = getHoppers().forEach { hopper ->
		var remaining = Hyperspace.HYPERMATTER_AMOUNT
		for (item: ItemStack? in hopper.inventory) {
			if (item == null) {
				continue
			}

			if (!isHypermatter(item)) {
				continue
			}
			val amount = min(item.amount, remaining)
			item.amount -= amount
			remaining -= amount
			if (remaining == 0) {
				break
			}
		}
		check(remaining == 0) { "Hopper at ${hopper.location} did not have ${Hyperspace.HYPERMATTER_AMOUNT} chetherite!" }
	}

	fun restoreFuel(): Unit = getHoppers().forEach { hopper ->
		hopper.inventory.addItem(CustomItems.MINERAL_CHETHERITE.itemStack(Hyperspace.HYPERMATTER_AMOUNT))
	}

	private fun isHypermatter(item: ItemStack) = CustomItems[item] == CustomItems.MINERAL_CHETHERITE
}
