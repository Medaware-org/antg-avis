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

    @DiscreteFunction(identifier = "root", params = ["body"])
    fun root(body: Node): String {
        return """
            <div class="tan-article">${body.evaluate(runtime)}</div>
        """.trimIndent()
    }

    @DiscreteFunction(identifier = "heading", params = ["value"])
    fun heading(value: Node): String {
        return """
            <h1>${value.evaluate(runtime)}</h1>
        """.trimIndent()
    }

    @DiscreteFunction(identifier = "img", params = ["src"])
    fun img(src: Node): String {
        return """
            <img src="${src.evaluate(runtime)}" class="tan-img"></img>
        """.trimIndent()
    }

    @DiscreteFunction(identifier = "subheading", params = ["value"])
    fun subheading(value: Node): String {
        return """
            <h2>${value.evaluate(runtime)}</h2>
        """.trimIndent()
    }

    @DiscreteFunction(identifier = "lead", params = ["value"])
    fun lead(value: Node): String {
        return """
            <p class="tan-lead">${value.evaluate(runtime)}</p>
        """.trimIndent()
    }

}