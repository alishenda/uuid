import kotlin.random.Random

interface IStorage {

    val code: Int
    val name: String
}

interface Storage : IStorage {
    val data: ByteArray
        get() = Random.nextBytes(6) // Default to a random 6-byte array
}

interface StorageWithData<T> : IStorage {
    val data: T

    fun encoder(data: T): ByteArray
    fun decoder(data: ByteArray): T

    override fun toString(): String

}

enum class StorageType(
    val storageCode: Int,
    val storageName: String
) {

    LOREM(1, "Lorem"),
    IPSUM(2, "ipsum");

    companion object {
        // Trouver et instancier un Storage par le code
        fun findByCode(code: Int, data: ByteArray): IStorage? {
            return when (code) {
                LOREM.storageCode -> {
                    val lorem = Lorem("")
                    Lorem(lorem.decoder(data)) // Instancie avec la donnée décodée
                }
                IPSUM.storageCode -> Ipsum()
                else -> null
            }
        }

        // Trouver et instancier un Storage par le nom
        fun findByName(name: String, data: ByteArray): IStorage? {
            return when (name.lowercase()) {
                LOREM.storageName.lowercase() -> {
                    val lorem = Lorem("")
                    Lorem(lorem.decoder(data)) // Instancie avec la donnée décodée
                }
                IPSUM.storageName.lowercase() -> {
                    Ipsum() // Ipsum n'a pas besoin de data
                }
                else -> null
            }
        }
    }
}