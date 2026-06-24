package org.orma.project_90.media

sealed interface OrmaProductAiImageResult {
    data class Success(
        val image: OrmaPickedImage,
    ) : OrmaProductAiImageResult

    data class Failure(
        val title: String,
        val message: String,
        val code: String? = null,
    ) : OrmaProductAiImageResult
}

expect fun isOrmaProductAiImageGenerationAvailable(): Boolean

expect suspend fun generateOrmaProductAiImage(
    prompt: String,
): OrmaProductAiImageResult
