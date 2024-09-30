package org.medaware.avis

import org.medaware.anterogradia.runtime.Runtime
import org.medaware.anterogradia.runtime.library.AnterogradiaLibrary
import org.medaware.anterogradia.runtime.library.DiscreteFunction
import org.medaware.anterogradia.runtime.library.StateRetention.STATELESS

@AnterogradiaLibrary(STATELESS)
class MedawareDesignKit(val runtime: Runtime) {

    @DiscreteFunction(identifier = "about")
    fun about(): String = "Medaware Design Kit\n{C} Medaware 2024\n"

}