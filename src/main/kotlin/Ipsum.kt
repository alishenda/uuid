class Ipsum : Storage {

    override val name: String = "ipsum"
    override val code: Int = 2

    override fun toString(): String = data.joinToString("")
}