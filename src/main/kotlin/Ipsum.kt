data class IpsumData(val id: Int, val message: String)

class Ipsum(override val name: String, override val code: UByte) : Storage<IpsumData> {

    override fun encoder(value: IpsumData): ByteArray {
        // Exemple simple : concaténer l'id et le message sous forme de chaîne limités à 6 octets
        val encodedId = value.id.toString().toByteArray().take(2) // Max 2 octets pour l'id
        val encodedMessage = value.message.toByteArray().take(4) // Max 4 octets pour le message
        return (encodedId + encodedMessage).toByteArray()
    }

    override fun decoder(value: ByteArray): IpsumData {
        // Exemple simple : récupérer l'id et le message depuis le tableau d'octets
        val id = value.take(2).toByteArray().toString(Charsets.UTF_8).toIntOrNull() ?: 0
        val message = value.drop(2).toByteArray().toString(Charsets.UTF_8)
        return IpsumData(id, message)
    }
}