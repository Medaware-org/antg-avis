package org.medaware.avis

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.runtime.library.StateRetention.STATELESS
import org.medaware.anterogradia.syntax.Node

@AnterogradiaLibrary(STATELESS)
class MedawareDesignKit(val runtime: Runtime) {

    @DiscreteFunction(identifier = "about")
    fun about(): String = "Medaware Design Kit\n{C} Medaware 2024\n"

    @DiscreteFunction(identifier = "header")
    fun headerRenderer(): String {
        return """
            <!-- (Article header present) -->
        """.trimIndent()
    }

    @DiscreteFunction(identifier = "root", params = ["body"])
    fun root(body: Node): String {
        return """
            <div class="tan-article">${body.evaluate(runtime)}</div>
        """.trimIndent()
    }

    @DiscreteFunction(identifier = "heading", params = ["value"])
    fun heading(value: Node): String {
        return """
            <p class="tan-element tan-heading">${value.evaluate(runtime)}</p>
        """.trimIndent()
    }

    @DiscreteFunction(identifier = "img", params = ["src"])
    fun img(src: Node): String {
        return """
            <img src="${src.evaluate(runtime)}" class="tan-element tan-img"></img>
        """.trimIndent()
    }

    @DiscreteFunction(identifier = "subheading", params = ["value"])
    fun subheading(value: Node): String {
        return """
            <p class="tan-element tan-sub-heading">${value.evaluate(runtime)}</p>
        """.trimIndent()
    }

    @DiscreteFunction(identifier = "text", params = ["text"])
    fun text(text: Node): String {
        return """
            <p class="tan-element tan-text">${text.evaluate(runtime)}</p>
        """.trimIndent()
    }

    @DiscreteFunction(identifier = "blank")
    fun blank(): String {
        return """
            <div class="tan-element tan-blank">Blank Element</div>
        """.trimIndent()
    }

    @DiscreteFunction(identifier = "id_wrap", params = ["id", "value"])
    fun id_wrap(id: Node, value: Node): String {
        return """
            <div class="tan-${id.evaluate(runtime)}">
                ${value.evaluate(runtime)}
            </div>
        """.trimIndent()
    }

}