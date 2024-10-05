package org.medaware.avis.model

import java.util.UUID

data class AvisElement(val id: UUID, val handle: String, val metadata: HashMap<String, String>)

data class AvisArticle(val id: UUID, val title: String, val elements: List<AvisElement>)
