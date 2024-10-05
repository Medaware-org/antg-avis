package org.medaware.avis.exception

import java.lang.RuntimeException

class AvisRendererException(override val message: String) : RuntimeException(message)