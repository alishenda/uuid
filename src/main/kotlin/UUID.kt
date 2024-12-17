import java.time.Instant
import kotlin.experimental.or
import kotlin.random.Random
import kotlin.random.nextUBytes

class UUID(
    val timestamp: Instant,
    val storage: IStorage
) {

    private fun timestampToByteArray(): ByteArray {
        val timestampLong = timestamp.toEpochMilli()
        val byteArray = ByteArray(6)

        for (i in 0 until 6) {
            byteArray[i] = (timestampLong shr (8 * (5-i))).toByte()
        }

        return byteArray
    }

    private fun toVerAndCustomB(): ByteArray {
        val byteArray = ByteArray(2)

        // First 4 bits set to 1000 (0x8)
        byteArray[0] = (0x80).toByte()

        // Next 4 bits set to random value
        val randomValue = Random.nextInt(16) // Generates a random number between 0 and 15
        byteArray[0] = (byteArray[0] or randomValue.toByte()).toByte()

        byteArray[1] = storage.code.toByte()

        return byteArray
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun toVar(): UByte {
        // Generate a random byte
        var randomUByte = Random.nextUBytes(1)[0]

        // Set the first two bits to 1 and 0 respectively (0x80 in hex)
        randomUByte = (randomUByte or 0x80.toUByte())
        randomUByte = (randomUByte and 0xBF.toUByte())
        return randomUByte
    }

    private fun dataToByteArray(): ByteArray {
        val encodedData: ByteArray = when (storage) {
            is Storage -> storage.data
            is Lorem -> storage.encoder(storage.data)
            else -> throw IllegalArgumentException("Unsupported storage type: ${storage.javaClass.canonicalName}")
        }

        if (encodedData.size != 6) {
            error("Data must be encoded to 6 bytes (actual is ${encodedData.size}). Padding with zeroes is recommended")
        }
        return encodedData
    }

    fun toFormattedString(): String {
        val uuidString = timestampToByteArray().joinToString("") { "%02x".format(it) } +
                toVerAndCustomB().joinToString("") { "%02x".format(it) } +
                toVar().toString(16) +
                dataToByteArray().joinToString("") { "%02x".format(it) }

        return "${uuidString.substring(0, 8)}-${uuidString.substring(8, 12)}-${uuidString.substring(12, 16)}-${uuidString.substring(16, 20)}-${uuidString.substring(20)}"
    }

    override fun toString(): String {
        return """
        UUID Details:
        - Timestamp       : $timestamp (${timestamp.toEpochMilli()} ms since epoch)
        - Storage Name    : ${storage.name}
        - Storage Code    : ${"0x${storage.code.toString(16)}"}
        - Data            : ${storage}
        - Formatted UUID  : ${toFormattedString()}
    """.trimIndent()
    }

    companion object {


        /**
         * Recréé une instance de UUID à partir de sa représentation chaîne.
         * @param uuidString La chaîne formatée représentant un UUID.
         * @param customStorage Storage personnalisé à utiliser pour désérialiser le UUID. Si non fourni, la méthode essaiera de trouver le Storage dans `reservedStorages`.
         * @throws IllegalArgumentException Si aucun storage (ni réservé, ni custom) ne correspond.
         */
        fun <T : Any> fromString(uuidString: String): UUID {
            // Suppression des tirets pour manipulation simplifiée
            val cleanString = uuidString.replace("-", "")

            // Extraction des différentes parties (selon le format généré précédemment)
            val timestampBytes = cleanString.substring(0, 12).chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            val versionAndStorage = cleanString.substring(12, 16).chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            val varByte = cleanString.substring(16, 18).toUByte(16)
            val dataBytes = cleanString.substring(18).chunked(2).map { it.toInt(16).toByte() }.toByteArray()

            // Reconstruire le timestamp depuis les 6 octets
            val timestampLong = timestampBytes.fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFF) }
            val timestamp = Instant.ofEpochMilli(timestampLong)

            // Reconstruire "storage code" depuis la partie versionAndStorage
            val storageCode = versionAndStorage[1].toUByte()

            val storage: IStorage? = StorageType.findByCode(storageCode.toInt(), dataBytes)
            // Si un customStorage est fourni, vérifier s'il correspond au storageCode

            // Recréer l'instance UUID
            return UUID(
                timestamp = timestamp,
                storage = storage!!
            )
        }
    }

}