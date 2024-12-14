import java.time.Instant
import kotlin.experimental.or
import kotlin.random.Random
import kotlin.random.nextUBytes

class UUID<T>(
    private val timestamp: Instant,
    val storage: Storage<T>,
    val data: T
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
        val encodedData: ByteArray = storage.encoder(data)

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

}