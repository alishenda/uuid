class Lorem(override val name: String, override val code: UByte) : Storage<String> {
    override fun encoder(value: String): ByteArray {
        // Conversion de la chaîne en tableau de bytes (max 6 octets)
        val rawData = value.toByteArray().take(6).toByteArray()

        // Ajout du padding pour garantir un tableau de 6 octets
        val paddedData = ByteArray(6)
        rawData.copyInto(paddedData)

        return paddedData
    }

    override fun decoder(value: ByteArray): String {
        return value.toString(Charsets.UTF_8)
    }
}
