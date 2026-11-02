package com.qrreader.app

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun ScanScreen(vm: MainViewModel) {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    var result by remember { mutableStateOf<ScanRecord?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchOn by remember { mutableStateOf(false) }

    // true = analysing frames; flipped off while a result is on screen
    val scanningEnabled = remember { AtomicBoolean(true) }

    fun showResult(record: ScanRecord) {
        scanningEnabled.set(false)
        vm.add(record)
        result = record
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            QrUtils.scanImage(
                context = context,
                uri = uri,
                onResult = { codes ->
                    val record = codes.firstNotNullOfOrNull { QrUtils.recordFrom(it) }
                    if (record != null) {
                        showResult(record)
                    } else {
                        Toast.makeText(context, "No QR code or barcode found in that image", Toast.LENGTH_LONG).show()
                    }
                },
                onError = {
                    Toast.makeText(context, "Couldn't read that image", Toast.LENGTH_SHORT).show()
                },
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    LaunchedEffect(torchOn, camera) {
        camera?.cameraControl?.enableTorch(torchOn)
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasPermission) {
            CameraView(
                modifier = Modifier.fillMaxSize(),
                isEnabled = { scanningEnabled.get() },
                onCamera = { camera = it },
                onBarcode = { barcode ->
                    if (scanningEnabled.getAndSet(false)) {
                        val record = QrUtils.recordFrom(barcode)
                        if (record != null) {
                            vm.add(record)
                            result = record
                        } else {
                            scanningEnabled.set(true)
                        }
                    }
                },
            )
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(250.dp)
                    .border(3.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(28.dp))
            )
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f),
            ) {
                Text(
                    "Point at a QR code or barcode",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            if (camera?.cameraInfo?.hasFlashUnit() == true) {
                IconButton(
                    onClick = { torchOn = !torchOn },
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(
                        if (torchOn) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                        contentDescription = "Toggle flashlight",
                    )
                }
            }
        } else {
            Column(
                Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(Icons.Rounded.CameraAlt, null, tint = Color.White, modifier = Modifier.size(48.dp))
                Text(
                    "Camera access is needed to scan. You can also scan a picture from your gallery.",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Allow camera")
                }
            }
        }

        FilledTonalButton(
            onClick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 28.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color.Black.copy(alpha = 0.55f),
                contentColor = Color.White,
            ),
        ) {
            Icon(Icons.Rounded.PhotoLibrary, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Choose image")
        }
    }

    result?.let { record ->
        ResultDialog(
            record = record,
            onDismiss = {
                result = null
                scanningEnabled.set(true)
            },
        )
    }
}

@Composable
private fun CameraView(
    modifier: Modifier,
    isEnabled: () -> Boolean,
    onCamera: (Camera) -> Unit,
    onBarcode: (Barcode) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = remember { context.findActivity() }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember { BarcodeScanning.getClient() }
    val latestOnBarcode by rememberUpdatedState(onBarcode)
    val latestOnCamera by rememberUpdatedState(onCamera)

    DisposableEffect(Unit) {
        onDispose {
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
            executor.shutdown()
            scanner.close()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(
                    executor,
                    BarcodeAnalyzer(scanner, isEnabled) { latestOnBarcode(it) },
                )
                try {
                    provider.unbindAll()
                    val cam = provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis,
                    )
                    latestOnCamera(cam)
                } catch (e: Exception) {
                    // camera not available on this device
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
    )
}

private class BarcodeAnalyzer(
    private val scanner: BarcodeScanner,
    private val isEnabled: () -> Boolean,
    private val onDetected: (Barcode) -> Unit,
) : ImageAnalysis.Analyzer {

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || !isEnabled()) {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { codes ->
                codes.firstOrNull { !it.rawValue.isNullOrEmpty() }?.let(onDetected)
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
