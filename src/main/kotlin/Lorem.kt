class Lorem(override val data: String) : StorageWithData<String> {

    override val name: String = "Lorem"
    override val code: Int = 1

    init {
        require(data.length <= 6)
    }

    override fun encoder(data: String): ByteArray {
        val result = ByteArray(6)
        data.toByteArray().copyInto(result)
        return result
    }

    override fun decoder(data: ByteArray): String {
       return data.decodeToString()
    }

    override fun toString(): String {
        return data
    }

}
