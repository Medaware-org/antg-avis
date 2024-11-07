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
annotation class Defaultable(val values: Array<String>)

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

    @Defaultable(["https://localproduct.co/wp-content/uploads/2023/05/Weed-Pre-Roll-Benefits-and-Drawbacks-1200x675.jpg", "https://bhoperehab.com/wp-content/uploads/2024/04/most-addictive-drugs-scaled.jpeg",
    "https://cdn.britannica.com/05/213705-050-4331A79A.jpg", "https://images.squarespace-cdn.com/content/v1/5f7de1b2d010e35cb300cd25/1602610585195-7AGBCAW5TOIHWEBPJ9EU/010517F_44676_prerolls-e1515050215339-800x450-c-default.jpg",
    "https://grandcannabis.ca/wp-content/uploads/2023/04/cannabis-joints.jpg", "https://cdn.prod.website-files.com/65c50dacd140acc9df7b3871/66e975bd791c472c300f1555_66d8031e7292e374c732d599_Blunt-wraps-are-thicker-slowerburning.jpeg",
    "https://img.freepik.com/free-photo/marijuana-buds-with-marijuana-joints-cannabis-oil_1150-20687.jpg", "https://images.ctfassets.net/4f3rgqwzdznj/5s6mf1qhltELW9Bns3BvFN/43ed57a9cfc797e2fffc0855bbfef6ff/magic_mushrooms-1289017175.jpg",
    "https://www.dea.gov/sites/default/files/2018-07/lsd.jpg", "https://i.etsystatic.com/27005300/r/il/4d7994/4866867057/il_fullxfull.4866867057_sfjw.jpg", "https://media.karousell.com/media/photos/products/2024/9/19/etomidate_1726709096_0c2d4dbb_progressive.jpg"])
    SRC(supportsTypes = arrayOf("IMAGE")),

    @Defaultable(["Lorem ipsum dolor sit amet, consectetur adipiscing elit"])
    TEXT(supportsTypes = arrayOf("HEADING", "SUBHEADING", "TEXT"));

    fun defaultValue(): String? {
        val field = AvisMeta::class.java.declaredFields.find {
            it.name == this.toString() && it.isAnnotationPresent(Defaultable::class.java)
        } ?: return null
        return field.getAnnotation(Defaultable::class.java).values.random()
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