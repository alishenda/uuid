interface Storage<T>  {
    val name: String
    val code: UByte

    fun encoder(value: T): ByteArray
    fun decoder(value: ByteArray): T
}