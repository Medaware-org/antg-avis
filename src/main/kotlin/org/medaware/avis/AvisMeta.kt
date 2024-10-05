package org.medaware.avis

import org.medaware.avis.model.AvisArticle
import org.medaware.avis.model.AvisElement
import java.lang.RuntimeException

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class RequiredMeta

class AvisValidationException(override val message: String, override var cause: Throwable? = null) :
    RuntimeException(message)

fun AvisValidationException.causedBy(cause: Exception): AvisValidationException {
    this.cause = cause
    return this
}

enum class AvisMeta(
    val valueConstraints: Array<String>? = null,
    val supportsTypes: Array<String>? = null
) {

    /**
     * Since all elements have low specificity by default, we need to have a way to tell AVIS
     * what we want to represent with a given entry. All possible element types are declared here.
     * This enum is required for the rest of AVIS to work. The `supportsType` parameter of other
     * meta entries refers to the value constraints of this enum.
     */
    @RequiredMeta
    ELEMENT_TYPE(arrayOf("heading", "subheading", "image", "text")),

    SRC(supportsTypes = arrayOf("image")),
    TEXT(supportsTypes = arrayOf("heading", "subheading", "text"));

    fun byNameOrNull(name: String): AvisMeta? = try {
        valueOf(name)
    } catch (e: IllegalArgumentException) {
        null
    }

    /**
     * Check whether the given metadata entry is valid in context of the given type.
     * Returned exception to be used as a cause.
     */
    fun validateMetaEntry(type: String, meta: Pair<String, String>): AvisValidationException? {
        val entry = byNameOrNull(meta.first) ?: return AvisValidationException("Unknown metadata key \"${meta.first}\"")
        if (entry.supportsTypes != null && entry.supportsTypes.contains(type))
            return AvisValidationException("The metadata field \"${meta.first}\" does not support element type \"$type\"")
        val constraints = entry.valueConstraints
        if (constraints == null || constraints.contains(meta.second))
            return AvisValidationException("The value \"${meta.second}\" does not meet value constraints of \"${meta.first}\"")
        return null
    }

    /**
     * Returns a list of all required metadata entries that must be present in all elements regardless
     * of their types.
     */
    fun requiredMetadata(): List<AvisMeta> =
        AvisMeta::class.java.declaredFields.filter { it.isAnnotationPresent(RequiredMeta::class.java) }
            .map { valueOf(it.name) }

    fun validateElement(element: AvisElement): AvisValidationException? {
        val meta = element.metadata

        val prefix = "(Element ID ${element.id} \"${element.handle}\")"

        // First, we need to make sure that the element has all the unconditionally required meta entries.
        // (Just checking keys for now)
        requiredMetadata().forEach {
            if (meta[it.name] == null)
                return AvisValidationException("$prefix The element does not have the required meta entry \"${it.name}\"")
        }

        val type = meta[ELEMENT_TYPE.toString()]!!
        var cause = validateMetaEntry(type, ELEMENT_TYPE.toString() to meta[ELEMENT_TYPE.toString()]!!)

        if (cause != null)
            return AvisValidationException("$prefix The meta entry for type is invalid").causedBy(cause)

        for ((key, value) in meta) {
            cause = validateMetaEntry(type, key to value)
            if (cause != null)
                return AvisValidationException("$prefix Validation failed for meta entry \"$key\"").causedBy(cause)
        }

        return null
    }

    fun validateArticle(article: AvisArticle) {
        var cause: AvisValidationException? = null

        article.elements.forEach { element ->
            cause = validateElement(element)
            if (cause != null)
                throw AvisValidationException("Failed validation for article \"${article.title}\" ID ${article.id}")
                    .causedBy(cause)
        }
    }

}