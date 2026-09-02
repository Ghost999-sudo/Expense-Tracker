package com.example.expensetracker.ui.screens

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.expensetracker.util.ReceiptParser
import com.example.expensetracker.viewmodel.ParsedReceipt
import com.example.expensetracker.viewmodel.ScanState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onScanComplete: (ParsedReceipt) -> Unit,
    onBack: () -> Unit
) {

    val cameraPermission = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    when {

        cameraPermission.status.isGranted -> {

            CameraContent(
                onScanComplete = onScanComplete,
                onBack = onBack
            )
        }

        cameraPermission.status.shouldShowRationale -> {

            PermissionRationale(
                message = "Camera access is needed to scan receipts. " +
                    "Grant the permission to continue.",
                onRequest = {
                    cameraPermission.launchPermissionRequest()
                },
                onBack = onBack
            )
        }

        else -> {

            PermissionDenied(
                onBack = onBack
            )
        }
    }
}

@Composable
private fun CameraContent(
    onScanComplete: (ParsedReceipt) -> Unit,
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var scanState: ScanState by remember {
        mutableStateOf(ScanState.Idle)
    }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val recognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->

                val previewView = PreviewView(ctx)
                val cameraProviderFuture =
                    ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({

                    val cameraProvider =
                        cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(
                                previewView.surfaceProvider
                            )
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("CameraScreen", "Bind failed", e)
                    }

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Capture button / scanning indicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {

            when (scanState) {

                is ScanState.Scanning -> CircularProgressIndicator()

                else -> FilledIconButton(
                    onClick = {

                        if (scanState !is ScanState.Scanning) {

                            scanState = ScanState.Scanning

                            imageCapture.takePicture(
                                executor,
                                object : ImageCapture.OnImageCapturedCallback() {

                                    override fun onCaptureSuccess(
                                        image: ImageProxy
                                    ) {

                                        scope.launch {

                                            val inputImage =
                                                InputImage.fromMediaImage(
                                                    image.image!!,
                                                    image.imageInfo.rotationDegrees
                                                )

                                            recognizer.process(inputImage)
                                                .addOnSuccessListener { visionText ->

                                                    val parsed =
                                                        ReceiptParser.parse(
                                                            visionText.text
                                                        )

                                                    image.close()
                                                    onScanComplete(parsed)
                                                }
                                                .addOnFailureListener { e ->

                                                    image.close()
                                                    scanState = ScanState.Error(
                                                        e.message
                                                            ?: "OCR failed"
                                                    )
                                                }
                                        }
                                    }

                                    override fun onError(
                                        exception: ImageCaptureException
                                    ) {
                                        scanState = ScanState.Error(
                                            exception.message
                                                ?: "Capture failed"
                                        )
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Scan receipt",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        if (scanState is ScanState.Error) {

            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = (scanState as ScanState.Error).message,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PermissionRationale(
    message: String,
    onRequest: () -> Unit,
    onBack: () -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        androidx.compose.material3.Card(
            modifier = Modifier.padding(24.dp)
        ) {

            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )

                androidx.compose.material3.Button(
                    onClick = onRequest
                ) {
                    Text("Grant Permission")
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = onBack
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}

@Composable
private fun PermissionDenied(
    onBack: () -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        androidx.compose.material3.Card(
            modifier = Modifier.padding(24.dp)
        ) {

            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Camera Access Denied",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Please enable camera permission in Settings to scan receipts.",
                    style = MaterialTheme.typography.bodyMedium
                )

                androidx.compose.material3.OutlinedButton(
                    onClick = onBack
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}
