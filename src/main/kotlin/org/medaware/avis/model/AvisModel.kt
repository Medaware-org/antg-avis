package org.medaware.avis.model

import java.util.UUID

data class AvisElement(val id: UUID, val handle: String, val metadata: HashMap<String, String>) {
    operator fun get(key: Any): String? {
        return metadata[key.toString()]
    }
}

data class AvisArticle(val id: UUID, val title: String, val elements: List<AvisElement>)
