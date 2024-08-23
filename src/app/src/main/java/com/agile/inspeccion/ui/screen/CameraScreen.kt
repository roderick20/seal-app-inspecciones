package com.agile.inspeccion.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.Image
import android.media.ImageReader
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun CameraScreen(onPhotoTaken: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    var textureView by remember { mutableStateOf<AutoFitTextureView?>(null) }
    var cameraDevice by remember { mutableStateOf<CameraDevice?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    when {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED -> {

        }
        else -> {

        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (checkCameraPermission(context)) {
                    textureView?.let { view ->
                        openCamera(context, view) { device, size ->
                            cameraDevice = device
                            imageCapture = ImageCapture(device, size)
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            cameraDevice?.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                AutoFitTextureView(ctx).also {
                    textureView = it
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.setAspectRatio(configuration.screenWidthDp, configuration.screenHeightDp)
            }
        )

        Button(
            onClick = {
                imageCapture?.takePicture { bitmap ->
                    onPhotoTaken(bitmap)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) {
            Text("Capturar")
        }
    }
}

class AutoFitTextureView @JvmOverloads constructor(
    context: Context,
    attrs: android.util.AttributeSet? = null,
    defStyle: Int = 0
) : TextureView(context, attrs, defStyle) {

    private var ratioWidth = 0
    private var ratioHeight = 0

    fun setAspectRatio(width: Int, height: Int) {
        require(width > 0 && height > 0) { "Size cannot be negative." }
        ratioWidth = width
        ratioHeight = height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (ratioWidth == 0 || ratioHeight == 0) {
            setMeasuredDimension(width, height)
        } else {
            if (width < height * ratioWidth / ratioHeight) {
                setMeasuredDimension(width, width * ratioHeight / ratioWidth)
            } else {
                setMeasuredDimension(height * ratioWidth / ratioHeight, height)
            }
        }
    }
}

fun checkCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}

fun openCamera(context: Context, textureView: TextureView, onCameraReady: (CameraDevice, Size) -> Unit) {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = cameraManager.cameraIdList[0]

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        return
    }

    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
    /*val maxSize = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        ?.getOutputSizes(ImageFormat.JPEG)
        ?.maxByOrNull { it.height * it.width }
        ?: Size(1080, 1920)*/

    val maxSize = Size(360, 640)

    cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            onCameraReady(camera, maxSize)
            startPreview(camera, textureView, maxSize)
        }
        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }
        override fun onError(camera: CameraDevice, error: Int) {
            Log.e("CameraScreen", "Error opening camera: $error")
            camera.close()
        }
    }, null)
}

fun startPreview(cameraDevice: CameraDevice, textureView: TextureView, previewSize: Size) {
    val surfaceTexture = textureView.surfaceTexture
    surfaceTexture?.setDefaultBufferSize(previewSize.width, previewSize.height)
    val previewSurface = Surface(surfaceTexture)

    cameraDevice.createCaptureSession(listOf(previewSurface), object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            val previewRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(previewSurface)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            }.build()
            session.setRepeatingRequest(previewRequest, null, null)
        }
        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.e("CameraScreen", "Failed to configure camera session")
        }
    }, null)
}

class ImageCapture(private val cameraDevice: CameraDevice, private val imageSize: Size) {
    fun takePicture(onImageCaptured: (Bitmap) -> Unit) {
        val imageReader = ImageReader.newInstance(imageSize.width, imageSize.height, ImageFormat.JPEG, 1)
        val captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
            addTarget(imageReader.surface)
            set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            set(CaptureRequest.JPEG_QUALITY, 100.toByte())
        }

        cameraDevice.createCaptureSession(listOf(imageReader.surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                session.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                    override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                        val image = imageReader.acquireLatestImage()
                        val bitmap = imageToBitmap(image)
                        onImageCaptured(bitmap)
                        image?.close()
                    }
                }, null)
            }
            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e("CameraScreen", "Failed to configure camera session for image capture")
            }
        }, null)
    }

    private fun imageToBitmap(image: Image?): Bitmap {
        val buffer = image?.planes?.get(0)?.buffer
        val bytes = buffer?.let { ByteArray(it.capacity()) }
        buffer?.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes?.size ?: 0)
    }
}
