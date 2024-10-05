package org.medaware.avis.render

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.syntax.Node
import org.medaware.anterogradia.syntax.StringLiteral
import org.medaware.avis.AvisMeta
import org.medaware.avis.model.AvisArticle
import org.medaware.avis.model.AvisElement

fun AvisArticle.renderer(runtime: Runtime) = AVISRenderer(article = this, runtime = runtime)

/**
 * The Anterogradia&trade; renderer for the Tangential&trade; editor
 * @param article The article to be rendered
 * @param runtime A pre-existing Anterogradia runtime
 */
class AVISRenderer(
    val article: AvisArticle,
    val runtime: Runtime
) {

    private fun validate() {
        AvisMeta.validateArticle(article)
    }

    fun render(): Node {
        return StringLiteral("")
    }

    fun renderElement(element: AvisElement): Node {
        return StringLiteral("")
    }

}