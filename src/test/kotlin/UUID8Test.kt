import org.junit.jupiter.api.Test
import java.time.Instant

class UUID8Test {

    @Test
    fun `test UUID generation with Lorem storage and valid data`() {
        val timestamp = Instant.now()
        val data = "Hello"

        val uuid = UUIDBuilder().withStorage(Lorem("1234"))
        val uuid2 = UUIDBuilder().withStorage(Ipsum())

        println(uuid.build())
        println(uuid2.build())

    }

}
