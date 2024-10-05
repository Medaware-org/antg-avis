package org.medaware.avis.render

import org.medaware.anterogradia.syntax.FunctionCall
import org.medaware.anterogradia.syntax.Node
import org.medaware.anterogradia.syntax.Script
import org.medaware.anterogradia.syntax.StringLiteral
import org.medaware.avis.AvisMeta
import org.medaware.avis.AvisMeta.ELEMENT_TYPE
import org.medaware.avis.AvisMeta.TEXT
import org.medaware.avis.AvisMeta.SRC
import org.medaware.avis.MedawareDesignKit
import org.medaware.avis.exception.AvisRendererException
import org.medaware.avis.model.AvisArticle
import org.medaware.avis.model.AvisElement

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Renderer(val type: String)

fun AvisArticle.renderer() = AvisRenderer(this)

fun String.node() = StringLiteral(this)

class AvisRenderer(
    val article: AvisArticle
) {

    @Renderer("HEADING")
    fun headingRenderer(element: AvisElement): Node {
        return FunctionCall(
            prefix = "avis",
            identifier = "heading",
            arguments = hashMapOf(
                "value" to element[TEXT]!!.node()
            )
        )
    }

    @Renderer("SUBHEADING")
    fun subHeadingRenderer(element: AvisElement): Node {
        return FunctionCall(
            prefix = "avis",
            identifier = "subheading",
            arguments = hashMapOf(
                "value" to element[TEXT]!!.node()
            )
        )
    }

    @Renderer("IMAGE")
    fun imageRenderer(element: AvisElement): Node {
        return FunctionCall(
            prefix = "avis",
            identifier = "img",
            arguments = hashMapOf(
                "src" to element[SRC]!!.node()
            )
        )
    }

    @Renderer("TEXT")
    fun textRenderer(element: AvisElement): Node {
        return FunctionCall(
            prefix = "avis",
            identifier = "text",
            arguments = hashMapOf(
                "text" to element[TEXT]!!.node()
            )
        )
    }

    fun renderElement(element: AvisElement): Node {
        val type = element[ELEMENT_TYPE]!!

        val method =
            AvisRenderer::class.java.declaredMethods.find {
                it.isAnnotationPresent(Renderer::class.java) &&
                        it.getAnnotation(Renderer::class.java).type.uppercase() == type.uppercase() &&
                        it.parameterCount == 1 &&
                        it.parameterTypes[0] == AvisElement::class.java &&
                        it.returnType == Node::class.java
            }

        if (method == null)
            throw AvisRendererException("Could not find a renderer for element type \"$type\".")

        return method.invoke(this, element) as Node
    }

    fun render(): Node {
        AvisMeta.validateArticle(article)

        val params = hashMapOf<String, Node>()

        article.elements.forEachIndexed { index, element ->
            params.put(index.toString(), renderElement(element))
        }

        val root = FunctionCall(
            "avis", "root", hashMapOf(
                "body" to FunctionCall("", "sequence", params, true)
            ), false
        )

        return root
    }

}