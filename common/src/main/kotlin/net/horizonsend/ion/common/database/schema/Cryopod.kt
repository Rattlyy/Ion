package net.horizonsend.ion.common.database.schema

import net.horizonsend.ion.common.database.*
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.utils.DBVec3i
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

data class Cryopod(
    override val _id: Oid<Cryopod>,
    val x: Int,
    val y: Int,
    val z: Int,
    val worldName: String,
    val owner: SLPlayerId,
    val active: Boolean
) : DbObject {

	companion object : OidDbObjectCompanion<Cryopod>(Cryopod::class, setup = {
		ensureIndex(Cryopod::owner)
	}) {
		operator fun get(location: DBVec3i, worldName: String) = col.findOne(
			and(Cryopod::x eq  location.x, Cryopod::y eq location.y, Cryopod::z eq location.z, Cryopod::worldName eq worldName)
		)

		fun delete(id: Oid<Cryopod>) {
			col.deleteOneById(id)
		}

		fun create(owner: SLPlayer, position: DBVec3i, worldName: String): Oid<Cryopod> = trx { session ->
			if (!none(session, and(Cryopod::x eq  position.x, Cryopod::y eq position.y, Cryopod::z eq position.z, Cryopod::worldName eq worldName))) {
				Cryopod[position, worldName]?._id?.let { col.deleteOneById(it) }
			}

			val id = objId<Cryopod>()

			col.insertOne(
				session,
				Cryopod(
					id,
					position.x,
					position.y,
					position.z,
					worldName,
					owner._id,
					true
				)
			)

			return@trx id
		}

		fun delete(location: DBVec3i, worldName: String) = Cryopod[location, worldName]?._id?.let { delete(it) }
	}

	fun vec3i(): DBVec3i = DBVec3i(x, y, z)
}
