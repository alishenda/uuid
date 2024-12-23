import java.io.*
import java.time.Instant
import java.util.Base64

class Dolor<T>(
    val timestamp: Instant,
    val storage: Storage<T>,
    val data: T
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L

        @Throws(IOException::class, ClassNotFoundException::class)
        fun <T> deserialize(inputStream: InputStream): Dolor<T> {
            ObjectInputStream(inputStream).use { return it.readObject() as Dolor<T> }
        }
    }

    abstract class Storage<T>(val name: String, val code: UByte) : Serializable {

        companion object {
            private const val serialVersionUID = 1L
        }

        abstract fun encode(data: T): ByteArray

        abstract fun decode(encodedData: ByteArray): T
    }

    class Base64Storage(name: String, code: UByte) : Storage<String>(name, code) {
        override fun encode(data: String): ByteArray {
            return Base64.getEncoder().encode(data.toByteArray())
        }

        override fun decode(encodedData: ByteArray): String {
            return String(Base64.getDecoder().decode(encodedData))
        }
    }

    class HexStorage(name: String, code: UByte) : Storage<ByteArray>(name, code) {
        override fun encode(data: ByteArray): ByteArray {
            return data.joinToString(separator = "") { String.format("%02x", it) }.toByteArray()
        }

        override fun decode(encodedData: ByteArray): ByteArray {
            val hexString = String(encodedData)
            return hexString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        }
    }

    class Builder<T> {
        private var timestamp: Instant = Instant.now()
        private var storage: Storage<T>? = null
        private var data: T? = null

        fun setTimestamp(timestamp: Instant) = apply {
            this.timestamp = timestamp
        }

        fun setStorage(storage: Storage<T>) = apply {
            this.storage = storage
        }

        fun setData(data: T) = apply {
            this.data = data
        }

        fun build(): Dolor<T> {
            val storage = this.storage
                ?: throw IllegalStateException("Storage must be initialized")
            val data = this.data
                ?: throw IllegalStateException("Data must be initialized")

            return Dolor(timestamp, storage, data)
        }

        companion object {
            fun <T> forStorage(storage: Storage<T>): Builder<T> {
                return Builder<T>().setStorage(storage)
            }
        }
    }

    @Throws(IOException::class)
    fun serialize(outputStream: OutputStream) {
        ObjectOutputStream(outputStream).use { it.writeObject(this) }
    }
}