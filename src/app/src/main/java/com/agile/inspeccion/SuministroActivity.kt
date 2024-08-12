package com.agile.inspeccion

//import android.content.Context
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
//import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.agile.inspeccion.data.database.DatabaseHelper
import com.agile.inspeccion.data.model.DetalleImagen
import com.agile.inspeccion.data.model.SuministroModel
import com.agile.inspeccion.data.service.Detalle
import com.agile.inspeccion.ui.theme.AppTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
//import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale




data class ObservacionOption(val id: Int, val nombre: String)

/*class SuministroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var id = 0
        intent.extras?.let { bundle ->
            id = bundle.getInt("id", 0)
        }
        val databaseHelper = DatabaseHelper(this)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val viewModel: SuministroModel = viewModel { SuministroModel(databaseHelper) }
                SuministroInterface(id, viewModel)
            }
        }
    }
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuministroInterface(navController: NavController, id: Int, viewModel: SuministroModel) {

    val detalle by viewModel.detalle.collectAsStateWithLifecycle()
    val lectura by viewModel.lectura.collectAsStateWithLifecycle()
    val observacion by viewModel.observacion.collectAsStateWithLifecycle()
    val fotoTipo by viewModel.fotoTipo.collectAsStateWithLifecycle()
    val context = LocalContext.current
    viewModel.GetDetalleById(id)

    val coroutineScope = rememberCoroutineScope()
    BackHandler {
        coroutineScope.launch {
            navController.navigate("list/" + detalle!!.inspeccionId.toString())
        }
    }



    var showMapDialog by remember { mutableStateOf(false) }


    val micIcon = context.resources.getIdentifier("mic", "drawable", context.packageName)
    var showObservacionDialog by remember { mutableStateOf(false) }
    var observacionText by remember { mutableStateOf("") }

    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }



    val observacionOptions = listOf(
        ObservacionOption(0, ""),
        ObservacionOption(1, "Opción 1"),
        ObservacionOption(2, "Opción 2"),
        ObservacionOption(3, "Opción 3"),
        ObservacionOption(4, "Opción 4")
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso concedido, obtener ubicación
            getLocation(context, fusedLocationClient) { location ->
                latitude = location.latitude
                longitude = location.longitude
            }
        } else {
            //locationText = "Permiso de ubicación denegado"
        }
    }


    var hasCameraPermission by remember { mutableStateOf(false) }
    //val file = remember { context.createImageFile() }
    /*val uri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }*/

    var capturedImages by remember { mutableStateOf(listOf<Pair<Bitmap, String>>()) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap: Bitmap? ->
            bitmap?.let { capturedBitmap ->
                val processedBitmap = processImage(capturedBitmap)
                //val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                //val currentTimestamp = dateFormat.format(Date())
                var photo = DetalleImagen(processedBitmap, fotoTipo )
                viewModel.agregarImagen(photo)
            }
        }
    )



    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) {
                cameraLauncher.launch(null)
            } else {
                // Aquí puedes mostrar un mensaje al usuario explicando por qué necesitas el permiso
            }
        }
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SEAL Sistema Comercial",
                        color = Color.White
                    )
                },
                actions = {
                    /*IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = Color.White
                        )
                    }*/
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* Acción para el botón Nuevo */ }
                ) {
                    Icon(
                        painter = painterResource(id = micIcon),
                        contentDescription = "Icono de Micrófono",
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Dictado", Modifier.padding(start = 4.dp))
                }

                Button(
                    onClick = { showMapDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Icono de Mapa"
                    )
                    Text("Mapa", Modifier.padding(start = 4.dp))
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                ) {
                Column{
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.Black)) {
                                append(detalle?.contrato.toString())
                            }
                            append(" ")
                            withStyle(style = SpanStyle(color = Color.Blue)) {
                                append("1202005000819 - ")
                            }
                            append(" ")
                            withStyle(style = SpanStyle(color = Color.Gray)) {
                                append("498266")
                            }
                            append(" ")
                            withStyle(style = SpanStyle(color = Color.Gray)) {
                                append(detalle?.direccion.toString())
                            }
                            withStyle(style = SpanStyle(color = Color.Red)) {
                                append(" - 0")
                            }
                        },
                        modifier = Modifier.padding(start = 16.dp, top = 5.dp, bottom = 5.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                //horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                //horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Lectura: ",
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(start = 8.dp, top = 5.dp, bottom = 0.dp)
                        .weight(0.4f)

                )
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .weight(0.7f)
                        .border(1.dp, Color.Gray, RectangleShape)
                ) {


                    BasicTextField(
                        value = lectura,
                        onValueChange = {
                            viewModel.setLectura(it)
                        },
                        modifier = Modifier
                            .height(height = 32.dp)
                            .fillMaxWidth()
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 1.dp, vertical = 1.dp),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 18.sp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Observación: ",
                    modifier = Modifier
                        .padding(start = 8.dp, top = 0.dp, bottom = 5.dp)
                        .weight(0.4f)
                )

                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .weight(0.6f)
                        .border(1.dp, Color.Gray, RectangleShape)
                ) {


                    BasicTextField(
                        value = (observacionOptions.find { it.id == observacion })!!.nombre,
                        onValueChange = {
                            observacionText = it
                            viewModel.setObservacion(0) // Reset ID when text is manually changed
                        },
                        modifier = Modifier
                            .height(height = 32.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 1.dp, vertical = 1.dp),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 18.sp)
                    )
                }

                Button(
                    //contentPadding = PaddingValues(all = 1.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(0.1f)
                        .height(32.dp),
                    onClick = { showObservacionDialog = true },
                    //modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(".")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        viewModel.setFotoTipo(1)
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                cameraLauncher.launch(null)
                            }

                            else -> {
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        }
                    },
                    //modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Foto \nLectura")
                }
                Button(
                    onClick = {
                        viewModel.setFotoTipo(2)
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                cameraLauncher.launch(null)
                            }

                            else -> {
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        }
                    }
                ) {
                    Text("Foto \nPanoramica")
                }
                Button(
                    onClick = {

                        if(lectura.equals("")){
                            Toast.makeText(context, "Ingrese lectura", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if(observacion == 0){
                            Toast.makeText(context, "Ingrese Observación", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if((viewModel.imagenesCapturadas .filter { it.tipo == 1 }).size == 0){
                            Toast.makeText(context, "Ingrese foto lectura", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if((viewModel.imagenesCapturadas .filter { it.tipo == 2 }).size == 0){
                            Toast.makeText(context, "Ingrese foto panoramica", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        when {

                            ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                getLocation(context, fusedLocationClient) { location ->
                                    latitude = location.latitude
                                    longitude = location.longitude
                                }
                            }
                            else -> {
                                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }

                        val currentDateTime = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        val formatted = currentDateTime.format(formatter)

                        viewModel.updateDetalle(
                            detalle!!.id,
                            lectura,
                            observacion.toString(),
                            latitude,
                            longitude,
                            formatted)

                        viewModel.imagenesCapturadas.forEach {
                            viewModel.addImage(it.foto, detalle!!.id, it.tipo)
                        }

                        Toast.makeText(context, "Lectura grabada", Toast.LENGTH_SHORT).show()

                        var siguiente = viewModel.siguiente(detalle!!.id)
                        /*val intent = Intent(context, SuministroActivity::class.java).apply {
                            putExtra("id", siguiente!!.id)
                        }
                        context.startActivity(intent)*/
                        navController.navigate("suministro/" + siguiente!!.id.toString())

                    },

                ) {
                    Text("Grabar")
                }
            }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                items(viewModel.imagenesCapturadas) { bitmap ->
                    Box(
                        modifier = Modifier
                            //.padding(horizontal = .dp)
                            .clickable {
                                viewModel.seleccionarImagenParaAmpliar(bitmap)
                            }
                    ) {
                        Image(
                            bitmap = bitmap.foto.asImageBitmap(),
                            contentDescription = "Foto capturada",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        IconButton(
                            onClick = { viewModel.eliminarImagen(bitmap) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(horizontal = 5.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar foto",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (viewModel.imagenesCapturadas.isNotEmpty()) {
                Text(
                    "Fotos capturadas: ${viewModel.imagenesCapturadas.size}",
                    modifier = Modifier.padding(16.dp)
                )
            }
            viewModel.imagenAmpliada?.let { bitmap ->
                Dialog(onDismissRequest = { viewModel.cerrarImagenAmpliada() }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { viewModel.cerrarImagenAmpliada() }
                    ) {
                        Image(
                            bitmap = bitmap.foto.asImageBitmap(),
                            contentDescription = "Foto ampliada",
                            modifier = Modifier
                                .fillMaxSize()
                                .aspectRatio(bitmap.foto.width.toFloat() / bitmap.foto.height.toFloat())
                                .align(Alignment.Center),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
/*
            if (capturedImages.isNotEmpty()) {
                Text("Fotos capturadas: ${capturedImages.size}", modifier = Modifier.padding(16.dp))
            }

            if (capturedImages.isNotEmpty()) {
                Text(
                    "Fotos capturadas: ${capturedImages.size}",
                    modifier = Modifier.padding(16.dp)
                )
            }*/

            if (showObservacionDialog) {
                ObservacionDialog(
                    observacionText = observacionText,
                    onObservacionChange = { text, id ->
                        observacionText = text
                        viewModel.setObservacion(id)
                    },
                    options = observacionOptions,
                    onDismiss = { showObservacionDialog = false }
                )
            }


            if (showMapDialog) {
                Dialog(onDismissRequest = { showMapDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        AndroidView(
                            factory = { context ->
                                MapView(context).apply {
                                    onCreate(null)
                                    getMapAsync { googleMap ->
                                        // Coordenadas de ejemplo (puedes cambiarlas según tus necesidades)
                                        val location =LatLng(detalle!!.latitud, detalle!!.longitud)
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                                        googleMap.addMarker(MarkerOptions().position(location))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        ) { mapView ->
                            mapView.onResume()
                        }
                    }
                }
            }
        }
    }
}






fun getLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationResult: (Location) -> Unit
) {
    try {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let(onLocationResult)
            }
    } catch (e: SecurityException) {
        // Manejar la excepción si el permiso no está concedido
    }
}

fun processImage(bitmap: Bitmap): Bitmap {
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = mutableBitmap.width * 0.05f // Tamaño del texto relativo al ancho de la imagen
        isAntiAlias = true
        style = Paint.Style.FILL
        setShadowLayer(5f, 2f, 2f, android.graphics.Color.BLACK) // Sombra para mejorar la legibilidad
        textAlign = Paint.Align.RIGHT
    }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val dateTime = dateFormat.format(Date())
    val x = mutableBitmap.width - 20f
    val y = mutableBitmap.height - 20f
    canvas.drawText(dateTime, x, y, paint)
    return mutableBitmap
}

@Composable
fun ObservacionDialog(
    observacionText: String,
    onObservacionChange: (String, Int) -> Unit,
    options: List<ObservacionOption>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Observación", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = observacionText,
                    onValueChange = { onObservacionChange(it, 0) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ingrese observación") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Seleccione una opción:", style = MaterialTheme.typography.headlineSmall)

                LazyColumn {
                    items(options) { option ->
                        Text(
                            text = option.nombre,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onObservacionChange(option.nombre, option.id)
                                    onDismiss()
                                }
                                .padding(vertical = 8.dp)
                        )
                        Divider()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SuministroPreview() {
    val previewViewModel = SuministroModel(DatabaseHelper(LocalContext.current))
    val navController = rememberNavController()
    AppTheme {
        SuministroInterface( navController,1, previewViewModel )
    }
}