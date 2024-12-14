import java.time.Instant

class UUIDBuilder<T : Any>(
    private var storage: Storage<T>
) {
    private var timestamp: Instant = Instant.now()
    private var data: T? = null

    /**
     * Définit votre propre stockage personnalisé.
     */
    fun withCustomStorage(customStorage: Storage<T>): UUIDBuilder<T> {
        this.storage = customStorage
        return this
    }

    /**
     * Définit le timestamp pour le UUID.
     * Par défaut, il utilise l'instant actuel si ce champ n'est pas défini par l'utilisateur.
     */
    fun withTimestamp(timestamp: Instant): UUIDBuilder<T> {
        this.timestamp = timestamp
        return this
    }

    /**
     * Définit les données spécifiques pour le UUID.
     * Le type des données doit être compatible avec le type attendu par le Storage.
     */
    fun withData(data: T): UUIDBuilder<T> {
        this.data = data
        return this
    }

    /**
     * Construit une instance finalisée de UUID.
     */
    fun build(): UUID<T> {
        val finalData = data ?: throw IllegalArgumentException("Data must be defined")
        return UUID(timestamp, storage, finalData)
    }
}