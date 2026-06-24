package org.orma.project_90.media

actual fun isOrmaProductAiImageGenerationAvailable(): Boolean = false

actual suspend fun generateOrmaProductAiImage(
    prompt: String,
): OrmaProductAiImageResult =
    OrmaProductAiImageResult.Failure(
        title = "AI image unavailable",
        message = "Use the Kotlin/JS web dashboard for AI product image generation.",
        code = "AI_IMAGE_JS_WEB_ONLY",
    )
