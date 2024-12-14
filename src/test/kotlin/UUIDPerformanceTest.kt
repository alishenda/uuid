import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.system.measureTimeMillis

class UUIDPerformanceTest {

    companion object {
        private const val NUMBER_OF_UUIDS = 1_000_000 // Nombre d'UUID à générer dans chaque test
    }

    /**
     * Teste les performances de la génération standard des UUID.
     */
    @Test
    fun testStandardUUIDGeneration() {
        val loremStorage = Lorem("Lorem Storage", 0x10u)

        val timeTaken = measureTimeMillis {
            repeat(NUMBER_OF_UUIDS) {
                val uuid = UUIDBuilder(loremStorage)
                    .withTimestamp(Instant.now()) // Un timestamp unique par UUID
                    .withData("TestData") // Données constantes
                    .build().toFormattedString()
            }
        }

        println("Standard UUID generation for $NUMBER_OF_UUIDS UUIDs took: ${timeTaken}ms")
    }
}