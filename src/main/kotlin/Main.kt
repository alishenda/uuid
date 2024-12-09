package orbit

import java.time.Instant


class Uuid(
    val timestamp: Long, // Représente les 48 premiers bits
    val storage: Storage, // Représente le Storage associé
    val data: ByteArray // Représente les 62 bits finaux (customC)
) {
    companion object {
        fun fromString(uuid: String): Uuid {
            // Validate UUID format
            val regex = Regex("([0-9a-fA-F]{8})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{12})")
            require(regex.matches(uuid)) { "Invalid UUID format: $uuid" }

            val matchResult = regex.find(uuid)!!
            val (part1, part2, part3, part4, part5) = matchResult.destructured

            // Parse each part of the UUID
            val msb = (part1.toLong(16) shl 32) or (part2.toLong(16) shl 16) or part3.toLong(16)
            val lsb = (part4.toLong(16) shl 48) or part5.toLong(16)

            // Extract parts
            val timestamp = (msb shr 16) and 0xFFFFFFFFFFFF
            val storageCode = (msb and 0xFFF).toInt()
            val storage = Storage.fromCode(storageCode)
            val data = ByteArray(8) // Convert the remaining bits into ByteArray
            for (i in 0 until 8) {
                data[i] = ((lsb shr (56 - i * 8)) and 0xFF).toByte()
            }

            return Uuid(timestamp, storage, data)
        }
    }

    override fun toString(): String {
        // Reconstruct UUID string from components
        val version = 8
        val variant = 0b10
        val msb = (timestamp shl 16) or (version.toLong() shl 12) or storage.code.toLong()
        val lsb = (variant.toLong() shl 62) or data.fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFF) }

        return UUIDv8Generator.formatUUID(msb, lsb)
    }
}

class Storage private constructor(val code: Int, val name: String) {
    companion object {
        // Liste réservée des storages
        private val reservedStorage = mapOf(
            0x01 to "Lorem",
            0x02 to "Ipsum"
        )

        fun fromCode(code: Int): Storage {
            val name = reservedStorage[code] ?: "Other"
            return Storage(code, name)
        }
    }
}


interface StorageDataSerializer {

    fun serialize(value: Any): ByteArray
    fun deserialize(bytes: ByteArray): Any
}

class UUIDv8Generator {
    companion object {
        fun generate(storage: Storage, data: ByteArray): String {
            require(data.size == 8) { "Data must be exactly 8 bytes" }

            val timestamp = Instant.now().toEpochMilli() and 0xFFFFFFFFFFFF // 48 bits
            val customA = timestamp.toBigEndianBytes()

            val version = 8
            val customB = storage.code and 0xFFF // 12 bits
            val variant = 0b10

            val msb = (customA shl 16) or (version shl 12).toLong() or customB.toLong()
            val lsb = (variant.toLong() shl 62) or data.fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFF) }

            return formatUUID(msb, lsb)
        }

        fun formatUUID(msb: Long, lsb: Long): String {
            return "%08x-%04x-%04x-%04x-%012x".format(
                (msb shr 32) and 0xFFFFFFFF,
                (msb shr 16) and 0xFFFF,
                msb and 0xFFFF,
                (lsb shr 48) and 0xFFFF,
                lsb and 0xFFFFFFFFFFFF
            )
        }

        private fun Long.toBigEndianBytes(): Long {
            var result = 0L
            for (i in 0..5) {
                result = (result shl 8) or ((this shr (i * 8)) and 0xFF)
            }
            return result
        }
    }
}

fun main() {
    // Example reserved and unreserved storage
    val reservedStorage = Storage.fromCode(0x01) // "Lorem"
    val unreservedStorage = Storage.fromCode(0xFF) // "Other"

    // Example data
    val data = ByteArray(8) { 0 } // Example 8-byte data

    // Generate UUIDs
    val uuidString1 = UUIDv8Generator.generate(reservedStorage, data)
    val uuidString2 = UUIDv8Generator.generate(unreservedStorage, data)

    println("Generated UUIDv8 (reserved): $uuidString1")
    println("Generated UUIDv8 (unreserved): $uuidString2")

    // Deserialize UUIDs
    val deserializedUuid1 = Uuid.fromString(uuidString1)
    val deserializedUuid2 = Uuid.fromString(uuidString2)

    println("Deserialized UUIDv8 (reserved): ${deserializedUuid1.storage.name} (${deserializedUuid1.storage.code})")
    println("Deserialized UUIDv8 (unreserved): ${deserializedUuid2.storage.name} (${deserializedUuid2.storage.code})")
}