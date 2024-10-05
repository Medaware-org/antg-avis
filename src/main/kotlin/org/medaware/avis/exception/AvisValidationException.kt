package org.medaware.avis.exception

import java.lang.RuntimeException

class AvisValidationException(override val message: String, override var cause: Throwable? = null) :
    RuntimeException(message)

fun AvisValidationException.causedBy(cause: Exception): AvisValidationException {
    this.cause = cause
    return this
}
