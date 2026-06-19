package org.orma.project_90.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy

private var activeIosLogoPickerDelegate: IosLogoPickerDelegate? = null
private var activeIosLogoPicker: UIImagePickerController? = null

@Composable
actual fun rememberOrmaBusinessLogoPicker(
    onResult: (OrmaLogoPickerResult) -> Unit,
): OrmaBusinessLogoPicker =
    remember(onResult) {
        OrmaBusinessLogoPicker {
            val presenter = UIApplication.sharedApplication.keyWindow?.rootViewController
            if (presenter == null) {
                onResult(
                    OrmaLogoPickerResult.Failure(
                        title = "Logo picker unavailable",
                        message = "ORMA could not find a visible iOS screen to open the photo picker.",
                        code = "IOS_LOGO_PICKER_PRESENTER_MISSING",
                    ),
                )
                return@OrmaBusinessLogoPicker
            }
            openIosLogoPicker(
                presenter = presenter,
                onResult = onResult,
            )
        }
    }

private fun openIosLogoPicker(
    presenter: UIViewController,
    onResult: (OrmaLogoPickerResult) -> Unit,
) {
    if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)) {
        onResult(
            OrmaLogoPickerResult.Failure(
                title = "Logo picker unavailable",
                message = "This iOS device cannot open the photo library.",
                code = "IOS_PHOTO_LIBRARY_UNAVAILABLE",
            ),
        )
        return
    }

    val picker = UIImagePickerController().apply {
        sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
    }
    val delegate = IosLogoPickerDelegate(
        picker = picker,
        onResult = onResult,
    )
    picker.delegate = delegate
    activeIosLogoPicker = picker
    activeIosLogoPickerDelegate = delegate
    presenter.presentViewController(
        viewControllerToPresent = picker,
        animated = true,
        completion = null,
    )
}

private class IosLogoPickerDelegate(
    private val picker: UIImagePickerController,
    private val onResult: (OrmaLogoPickerResult) -> Unit,
) : NSObject(),
    UIImagePickerControllerDelegateProtocol,
    UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val data = if (image == null) null else UIImagePNGRepresentation(image)
        val result = if (data == null || data.length.toInt() == 0) {
            OrmaLogoPickerResult.Failure(
                title = "Logo not readable",
                message = "ORMA could not read the selected iOS image.",
                code = "IOS_LOGO_READ_FAILED",
            )
        } else {
            OrmaLogoPickerResult.Success(
                OrmaPickedImage(
                    fileName = "business-logo-${NSDate().timeIntervalSince1970.toLong()}.png",
                    contentType = "image/png",
                    bytes = data.toByteArray(),
                ),
            )
        }
        finish(result)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        finish(OrmaLogoPickerResult.Cancelled)
    }

    private fun finish(result: OrmaLogoPickerResult) {
        picker.dismissViewControllerAnimated(true) {
            onResult(result)
            activeIosLogoPicker = null
            activeIosLogoPickerDelegate = null
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size <= 0) return ByteArray(0)
    val output = ByteArray(size)
    output.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, size.convert())
    }
    return output
}
