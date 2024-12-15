import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class UUID8Test {

    private val loremStorage = Lorem("Lorem Storage", 0x10u)

    @Test
    fun `test UUID generation with Lorem storage and valid data`() {
        val timestamp = Instant.now()
        val data = "Hello"

        val uuid = UUIDBuilder(loremStorage)
            .withTimestamp(timestamp)
            .withData(data)
            .build()

        assertEquals(loremStorage, uuid.storage)
        assertEquals(timestamp, uuid.timestamp)
        assertEquals(data, uuid.data)
    }

    @Test
    fun customStorage() {
        class CustomStorage(override val name: String, override val code: UByte) : Storage<Map<String, Any>> {

            override fun encoder(value: Map<String, Any>): ByteArray {
                // Exemple d'encodage d'une Map : concaténer clé-valeur; limiter à 6 octets
                val rawData = value.entries.joinToString(",") { "${it.key}:${it.value}" }
                    .toByteArray().take(6).toByteArray()

                // Padding pour atteindre exactement 6 octets
                val paddedData = ByteArray(6)
                rawData.copyInto(paddedData)

                println("${paddedData.size}")
                return paddedData
            }

            override fun decoder(value: ByteArray): Map<String, Any> {
                // Décodage simple : séparer les paires clé-valeur
                val decodedString = value.toString(Charsets.UTF_8).trim()
                return decodedString.split(',')
                    .mapNotNull {
                        val (key, v) = it.split(":", limit = 2).takeIf { it.size == 2 } ?: return@mapNotNull null
                        key to v
                    }.toMap()
            }
        }

        // Définition d'un stockage personnalisé
        val customStorage = CustomStorage("Custom Map Storage", 0x30u)

        // Construction du UUID avec le stockage personnalisé
        val customUUID = UUIDBuilder(customStorage)
            .withTimestamp(Instant.now()) // Timestamp
            .withData(mapOf("key1" to "value1", "key2" to 42)) // Données
            .build()

        println("Custom UUID: $customUUID")

        val stringUuid = UUID.fromString("0193c74e-252e-8330-8a6b-6579313a76", customStorage)
        println("Custom UUID: $stringUuid")

    }

}
