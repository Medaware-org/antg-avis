package org.medaware.avis

import org.medaware.avis.exception.AvisValidationException
import org.medaware.avis.exception.causedBy
import org.medaware.avis.model.AvisArticle
import org.medaware.avis.model.AvisElement

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class RequiredMeta

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Defaultable(val value: String)

enum class AvisMeta(
    val valueConstraints: Array<String>? = null,
    val supportsTypes: Array<String>? = null,

    /**
     * Defined the required **metadata keys** that need to be present when the property is set to a given value.
     * ```kotlin
     * PROPERTY (
     *     valueConstraints = arrayOf("first-option", "second-option"),
     *     requires = arrayOf(
     *         // Required when property value is `first-option`
     *         "first-option" to arrayOf("first-option-requirement"),
     *
     *         // Required when property value is `second-option`
     *         "second-option" to arrayOf("second-option-requirement"),
     *
     *         // Required regardless of the value
     *         null to arrayOf("unconditional-requirement")
     *     )
     * )
     * ```
     */
    val requires: Array<Pair<String?, Array<String>>>? = null
) {

    /**
     * Since all elements have low specificity by default, we need to have a way to tell AVIS
     * what we want to represent with a given entry. All possible element types are declared here.
     * This enum is required for the rest of AVIS to work. The `supportsType` parameter of other
     * meta entries refers to the value constraints of this enum.
     */
    @RequiredMeta
    ELEMENT_TYPE(
        valueConstraints = arrayOf("HEADING", "SUBHEADING", "IMAGE", "TEXT", "BLANK_PLACEHOLDER"),
        requires = arrayOf(
            "HEADING" to arrayOf("TEXT"),
            "SUBHEADING" to arrayOf("TEXT"),
            "IMAGE" to arrayOf("SRC"),
            "TEXT" to arrayOf("TEXT")
        )
    ),

    @Defaultable("https://localproduct.co/wp-content/uploads/2023/05/Weed-Pre-Roll-Benefits-and-Drawbacks-1200x675.jpg")
    SRC(supportsTypes = arrayOf("IMAGE")),

    @Defaultable("Lorem ipsum dolor sit amet, consectetur adipiscing elit")
    TEXT(supportsTypes = arrayOf("HEADING", "SUBHEADING", "TEXT"));

    fun defaultValue(): String? {
        val field = AvisMeta::class.java.declaredFields.find {
            it.name == this.toString() && it.isAnnotationPresent(Defaultable::class.java)
        } ?: return null
        return field.getAnnotation(Defaultable::class.java).value
    }

    fun requirements(value: String): Array<String> {
        this.requires ?: return arrayOf()
        return (this.requires.find { it.first == value.uppercase() } ?: return arrayOf()).second
    }

    companion object {
        fun byNameOrNull(name: String): AvisMeta? = try {
            valueOf(name)
        } catch (e: IllegalArgumentException) {
            null
        }

        fun <T> use(
            type: String,
            value: String,
            lambda: (meta: AvisMeta, requirements: Array<String>) -> T
        ): T? {
            val meta = AvisMeta.byNameOrNull(type)
                ?: throw AvisValidationException("Unknown meta entry '$type' for element type")
            val req = meta.requirements(value.uppercase())
            return lambda(meta, req)
        }

        /**
         * Check whether the given metadata entry is valid in context of the given type.
         * Returned exception to be used as a cause.
         */
        fun validateMetaEntry(type: String, meta: Pair<String, String>): AvisValidationException? {
            val entry =
                byNameOrNull(meta.first) ?: return AvisValidationException("Unknown metadata key \"${meta.first}\"")
            if (entry.supportsTypes != null && !entry.supportsTypes.contains(type))
                return AvisValidationException("The metadata field \"${meta.first}\" does not support element type \"$type\"")
            val constraints = entry.valueConstraints
            if (constraints != null && !constraints.contains(meta.second))
                return AvisValidationException(
                    "The value \"${meta.second}\" does not meet value constraints of \"${meta.first}\" Allowed values are: [${
                        constraints.joinToString(separator = ", ")
                    }]"
                )
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
                return AvisValidationException("$prefix The meta entry for $ELEMENT_TYPE is invalid").causedBy(cause)

            for ((key, value) in meta) forMeta@ {
                cause = validateMetaEntry(type, key to value)

                if (cause != null)
                    return AvisValidationException("$prefix Validation failed for meta entry \"$key\"").causedBy(cause)

                // Check if all required properties are present
                val metaEnum = byNameOrNull(key)!!
                metaEnum.requires?.forEach { req ->
                    if (req.first != null && req.first != value)
                        return@forEach

                    req.second.forEach { requiredKey ->
                        if (!meta.containsKey(requiredKey.uppercase()))
                            return AvisValidationException("The property \"${requiredKey.uppercase()}\" is not present, but is${if (req.first != null) "" else " unconditionally"} required by \"${key.toUpperCase()}\"")
                    }
                }
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

}