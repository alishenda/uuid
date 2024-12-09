package orbit

import java.time.Instant


class Uuid(
    val timestamp: Long,
    val storage: Storage,
    val data: ByteArray
) {
    companion object {
        fun fromString(uuid: String): Uuid {
            val regex = Regex("([0-9a-fA-F]{8})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{12})")
            require(regex.matches(uuid)) { "Invalid UUID format: $uuid" }

            val matchResult = regex.find(uuid)!!
            val (part1, part2, part3, part4, part5) = matchResult.destructured

            val msb = (part1.toLong(16) shl 32) or (part2.toLong(16) shl 16) or part3.toLong(16)
            val lsb = (part4.toLong(16) shl 48) or part5.toLong(16)

            val timestamp = (msb shr 16) and 0xFFFFFFFFFFFF
            val storageCode = (msb and 0xFFF).toInt()
            val storage = Storage.fromCode(storageCode)
            val data = ByteArray(8) { i -> ((lsb shr (56 - i * 8)) and 0xFF).toByte() }

            return Uuid(timestamp, storage, data)
        }
    }

    override fun toString(): String {
        val version = 8
        val variant = 0b10
        val msb = (timestamp shl 16) or (version.toLong() shl 12) or storage.code.toLong()
        val lsb = (variant.toLong() shl 62) or data.fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFF) }

        return UUIDv8Generator.formatUUID(msb, lsb)
    }
}


class Storage private constructor(
    val code: Int,
    val name: String,
    val serializer: StorageDataSerializer
) {
    companion object {
        // Liste réservée des storages avec leurs sérialiseurs
        private val reservedStorage = mapOf(
            0x01 to Storage(0x01, "Lorem", object : StorageDataSerializer {
                override fun serialize(value: Any): ByteArray {
                    require(value is String) { "Expected a String for Lorem storage" }
                    val rawBytes = value.toByteArray()
                    return rawBytes.padTo8Bytes()
                }

                override fun deserialize(bytes: ByteArray): Any {
                    require(bytes.size == 8) { "Expected 8 bytes for Lorem storage" }
                    return String(bytes).trimEnd('\u0000')
                }
            }),
            0x02 to Storage(0x02, "Ipsum", object : StorageDataSerializer {
                override fun serialize(value: Any): ByteArray {
                    require(value is Int) { "Expected an Int for Ipsum storage" }
                    val rawBytes = ByteArray(4) { i -> ((value shr (8 * (3 - i))) and 0xFF).toByte() }
                    return rawBytes.padTo8Bytes()
                }

                override fun deserialize(bytes: ByteArray): Any {
                    require(bytes.size == 8) { "Expected 8 bytes for Ipsum storage" }
                    val meaningfulBytes = bytes.sliceArray(0 until 4) // Take only the first 4 bytes
                    return meaningfulBytes.fold(0) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xFF) }
                }
            })
        )


        // Fonction de création ou récupération de Storage
        fun fromCode(code: Int): Storage {
            return reservedStorage[code] ?: Storage(code, "Other", DefaultStorageDataSerializer)
        }
    }
}

fun ByteArray.padTo8Bytes(): ByteArray {
    return if (this.size >= 8) {
        this.copyOf(8)
    } else {
        this + ByteArray(8 - this.size) { 0 }
    }
}

// Par défaut pour les storages "Other"
object DefaultStorageDataSerializer : StorageDataSerializer {
    private var userDefinedSerializer: StorageDataSerializer? = null

    fun overrideSerializer(serializer: StorageDataSerializer) {
        userDefinedSerializer = serializer
    }

    override fun serialize(value: Any): ByteArray {
        return userDefinedSerializer?.serialize(value)
            ?: throw IllegalStateException("No serializer defined for 'Other' storage")
    }

    override fun deserialize(bytes: ByteArray): Any {
        return userDefinedSerializer?.deserialize(bytes)
            ?: throw IllegalStateException("No deserializer defined for 'Other' storage")
    }
}

interface StorageDataSerializer {

    fun serialize(value: Any): ByteArray
    fun deserialize(bytes: ByteArray): Any
}

class UUIDv8Generator {
    companion object {
        fun generate(storage: Storage, value: Any): String {
            val data = storage.serializer.serialize(value)
            require(data.size == 8) { "Serialized data must be exactly 8 bytes" }

            val timestamp = Instant.now().toEpochMilli() and 0xFFFFFFFFFFFF
            val msb = (timestamp shl 16) or (8L shl 12) or storage.code.toLong()
            val lsb = (0b10L shl 62) or data.fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFF) }

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
    }
}

fun main() {
    // Exemples de sérialisation
    val loremStorage = Storage.fromCode(0x01)
    val ipsumStorage = Storage.fromCode(0x02)
    val otherStorage = Storage.fromCode(0xFF)

    // Overriding serializer for "Other"
    DefaultStorageDataSerializer.overrideSerializer(object : StorageDataSerializer {
        override fun serialize(value: Any): ByteArray {
            require(value is Long) { "Expected a Long for Other storage" }
            return ByteArray(8) { i -> ((value shr (56 - i * 8)) and 0xFF).toByte() }
        }

        override fun deserialize(bytes: ByteArray): Any {
            require(bytes.size == 8) { "Expected 8 bytes for Other storage" }
            return bytes.fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFF) }
        }
    })

    // Generate UUIDs
    val uuidLorem = UUIDv8Generator.generate(loremStorage, "tHello")
    val uuidIpsum = UUIDv8Generator.generate(ipsumStorage, 42)
    val uuidOther = UUIDv8Generator.generate(otherStorage, 123456789L)

    println("UUID Lorem: $uuidLorem")
    println("UUID Ipsum: $uuidIpsum")
    println("UUID Other: $uuidOther")

    // Deserialize
    val deserializedOther = Uuid.fromString(uuidOther)
    println("Deserialized Other data: ${deserializedOther.storage.serializer.deserialize(deserializedOther.data)}")
}