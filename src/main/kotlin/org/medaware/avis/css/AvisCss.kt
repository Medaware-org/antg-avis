package org.medaware.avis.css

object AvisCss {

    fun cssString(): String {
        return (javaClass.getResource("/avis-css.css") ?: return "").readText()
    }

}