package org.medaware.avis

import org.medaware.avis.model.AvisArticle
import org.medaware.avis.model.AvisElement
import java.util.UUID

fun main() {

    val article = AvisArticle(UUID.randomUUID(), "Lorem ipsum", listOf(
        AvisElement(UUID.randomUUID(), "a", hashMapOf(
            "ELEMENT_TYPE" to "heading",
            "TEXT" to "Hello, World!"
        ))
    ))

    AvisMeta.validateArticle(article)

}