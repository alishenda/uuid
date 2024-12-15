class Lorem(override val name: String, override val code: UByte) : Storage<String> {
    override fun encoder(value: String): ByteArray {

        val rawData = value.toByteArray().take(6).toByteArray()

        val paddedData = ByteArray(6)
        rawData.copyInto(paddedData)

        return paddedData
    }

    override fun decoder(value: ByteArray): String {
        return value.toString(Charsets.UTF_8)
    }
}
