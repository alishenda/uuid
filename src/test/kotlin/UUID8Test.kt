import org.junit.jupiter.api.Test
import java.time.Instant

class UUID8Test {

    @Test
    fun testUUID8() {
        // Exemple avec Lorem
        val loremStorage = Lorem("Lorem Storage", 0x10u)

        val uuidLorem: UUID<String> = UUIDBuilder(loremStorage)
            .withData("Hello")
            .withTimestamp(Instant.now())
            .build()

        println("UUID (Lorem): ${uuidLorem.toFormattedString()}")

        // Exemple avec Ipsum
        val ipsumStorage = Ipsum("Ipsum Storage", 0x20u)
        val ipsumData = IpsumData(42, "Test message")

        val uuidIpsum: UUID<IpsumData> = UUIDBuilder(ipsumStorage)
            .withData(ipsumData)
            .build()

        println("UUID (Ipsum): ${uuidIpsum.toFormattedString()}")
    }

    @Test
    fun decodeUUID8() {
        val uuid: UUID<IpsumData> = UUID.fromString("0193c70a-0d00-8920-a734-3254657374")
        println("UUID: $uuid")
    }


}
