package org.orma.project_90.media

import kotlin.js.Promise
import kotlinx.coroutines.await
import org.khronos.webgl.Int8Array

actual fun isOrmaProductAiImageGenerationAvailable(): Boolean = true

actual suspend fun generateOrmaProductAiImage(
    prompt: String,
): OrmaProductAiImageResult =
    try {
        val generated = generatePuterImage(prompt).await()
        val contentType = generated.type?.takeIf { it.isNotBlank() } ?: "image/png"
        val bytes = generated.bytes.toByteArray()
        if (bytes.isEmpty()) {
            OrmaProductAiImageResult.Failure(
                title = "AI image not ready",
                message = "The generated image could not be read. Try again.",
                code = "AI_IMAGE_EMPTY",
            )
        } else {
            OrmaProductAiImageResult.Success(
                OrmaPickedImage(
                    fileName = "orma-ai-product.png",
                    contentType = contentType,
                    bytes = bytes,
                ),
            )
        }
    } catch (error: Throwable) {
        OrmaProductAiImageResult.Failure(
            title = "AI image failed",
            message = error.message ?: "Could not generate the product image. Try again.",
            code = "AI_IMAGE_FAILED",
        )
    }

private external interface JsGeneratedImage {
    val type: String?
    val bytes: Int8Array
}

@Suppress("UNUSED_PARAMETER")
private fun generatePuterImage(prompt: String): Promise<JsGeneratedImage> = js(
    """
    new Promise((resolve, reject) => {
      const run = () => {
        if (!window.puter || !window.puter.ai || !window.puter.ai.txt2img) {
          reject(new Error('Puter image generation is not available yet.'));
          return;
        }
        window.puter.ai.txt2img(prompt, { model: 'gpt-image-1-mini', quality: 'low' })
          .then((imageElement) => {
            const src = imageElement && imageElement.src;
            if (!src) {
              reject(new Error('The generated image did not include a source.'));
              return;
            }
            fetch(src)
              .then((response) => response.blob())
              .then((blob) => {
                blob.arrayBuffer()
                  .then((buffer) => {
                    resolve({
                      type: blob.type || 'image/png',
                      bytes: new Int8Array(buffer)
                    });
                  })
                  .catch(reject);
              })
              .catch(reject);
          })
          .catch(reject);
      };

      if (window.puter && window.puter.ai) {
        run();
        return;
      }

      const existing = document.querySelector('script[data-orma-puter-js="true"]');
      if (existing) {
        existing.addEventListener('load', run, { once: true });
        existing.addEventListener('error', () => reject(new Error('Could not load Puter.js.')), { once: true });
        return;
      }

      const script = document.createElement('script');
      script.src = 'https://js.puter.com/v2/';
      script.async = true;
      script.dataset.ormaPuterJs = 'true';
      script.onload = run;
      script.onerror = () => reject(new Error('Could not load Puter.js.'));
      document.head.appendChild(script);
    })
    """,
)

private fun Int8Array.toByteArray(): ByteArray {
    val output = ByteArray(length)
    copyInt8ArrayToByteArray(this, output)
    return output
}

@Suppress("UNUSED_PARAMETER")
private fun copyInt8ArrayToByteArray(
    source: Int8Array,
    target: ByteArray,
): Unit = js(
    """
    for (let index = 0; index < source.length; index += 1) {
      target[index] = source[index];
    }
    """,
)
