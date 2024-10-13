package org.medaware.avis.resource

import java.lang.IllegalStateException

object AvisCallback {

    var resourceCallback: (src: String) -> String =
        { throw IllegalStateException("The AVIS resource callback is not set.") }

}