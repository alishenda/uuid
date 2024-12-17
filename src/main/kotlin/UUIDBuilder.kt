import java.time.Instant

class UUIDBuilder {
    private var timestamp: Instant = Instant.now()
    private var storage: IStorage? = null

    /**
     * Définit le timestamp pour le UUID.
     * Par défaut, il utilise l'instant actuel si ce champ n'est pas défini par l'utilisateur.
     */
    fun withTimestamp(timestamp: Instant): UUIDBuilder {
        this.timestamp = timestamp
        return this
    }

    /**
     * Définit les données spécifiques pour le UUID.
     * Le type des données doit être compatible avec le type attendu par le Storage.
     */
    fun withStorage(storage: IStorage): UUIDBuilder {
        this.storage = storage
        return this
    }

    /**
     * Construit une instance finalisée de UUID.
     */
    fun build(): UUID {
        val finalData = storage ?: throw IllegalArgumentException("Storage must be defined")
        return UUID(timestamp, finalData)
    }
}