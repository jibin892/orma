package org.orma.project_90.media

actual fun isOrmaProductAiImageGenerationAvailable(): Boolean = false

actual suspend fun generateOrmaProductAiImage(
    prompt: String,
): OrmaProductAiImageResult =
    OrmaProductAiImageResult.Failure(
        title = "AI image unavailable",
        message = "AI product image generation is available from the ORMA web dashboard. You can upload a product image here.",
        code = "AI_IMAGE_WEB_ONLY",
    )
