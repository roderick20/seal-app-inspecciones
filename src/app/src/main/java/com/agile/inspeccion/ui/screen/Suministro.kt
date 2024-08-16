package com.agile.inspeccion.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.agile.inspeccion.data.database.DatabaseHelper
import com.agile.inspeccion.data.model.DetalleImagen
import com.agile.inspeccion.data.model.SuministroModel
import com.agile.inspeccion.ui.theme.AppTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

data class ObservacionOption(val id: Int, val nombre: String, val requiereLectura: Int, val requiereFoto: Int)

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
        ObservacionOption(0,"0-Ninguna",1,0),
        ObservacionOption(1,"1-Contometro o display no se aprecia",0,1),
        ObservacionOption(2,"2-Contómetro descentrado (no hay certeza en la lectura)",0,1),
        ObservacionOption(3,"3-No displaya lectura (medidor malogrado)",0,1),
        ObservacionOption(4,"4-Medidor o caja con medidor inclinado, colgado o amarrado",1,1),
        ObservacionOption(5,"5-Medidor inaccesible",0,1),
        ObservacionOption(6,"6-Medidor interior, usuario no se encuentra (no se puede lecturar)",0,1),
        ObservacionOption(7,"7-Medidor en el techo",0,1),
        ObservacionOption(8,"8-Medidor al ras del piso",1,0),
        ObservacionOption(9,"9-Medidor en el poste",1,0),
        ObservacionOption(10,"10-Caja sin numero de suministro",1,0),
        ObservacionOption(11,"11-Zona peligrosa, perros  (no se puede lecturar)",0,1),
        ObservacionOption(12,"12-Suministro no se ubica",0,1),
        ObservacionOption(13,"13-Disco del medidor retrocede",1,0),
        ObservacionOption(14,"14-Conexion invertida",1,0),
        ObservacionOption(15,"15-Copa rota, rajada u opaca o empañada",0,1),
        ObservacionOption(16,"16-Disco del medidor no gira (plantado cuando usuario consume energía)",1,0),
        ObservacionOption(17,"17-Medidor quemado o bornera del medidor quemada",1,0),
        ObservacionOption(18,"18-Lectura actual menor a la anterior",1,0),
        ObservacionOption(19,"19-Suministro sin medidor y sin acometida",0,1),
        ObservacionOption(20,"20-Suministro sin medidor y con acometida",0,1),
        ObservacionOption(21,"21-Suministro con conexión directa a la red",1,0),
        ObservacionOption(22,"22-Medidor con conexión directa desde la bornera o puentes en la bornera",1,0),
        ObservacionOption(23,"23-Caja porta medidor sin precinto o precinto roto o manipulado",1,0),
        ObservacionOption(24,"24-Caja normalizada sin pernos o pernos sin descabezar (caja sin asegurar)",1,0),
        ObservacionOption(25,"25-Caja porta medidor sin tapa superior o inferior",1,1),
        ObservacionOption(26,"26-Caja portamedidor con sello forza retirado, roto o abollado",1,0),
        ObservacionOption(27,"27-Caja antigua sin remaches y sin soldar (caja sin asegurar)",1,0),
        ObservacionOption(28,"28-Medidor ubicado muy alto",1,0),
        ObservacionOption(29,"29-Numero de suministro mal rotulado",1,0),
        ObservacionOption(31,"31-Medidor interior, usuario no permite tomar lectura (no se puede lecturar)",0,1),
        ObservacionOption(32,"32-Suministro no pertenece a sed",1,0),
        ObservacionOption(33,"33-Persona ajena a la empresa manipula el suministro",1,0),
        ObservacionOption(34,"34-Suministro adicionado a la SED",1,0),
        ObservacionOption(35,"35-Visor empañado, sucio o pintado (no se puede lecturar)",0,1),
        ObservacionOption(36,"36-Ciclo completo del contador",1,0),
        ObservacionOption(37,"37-Conexión clandestina cercana a este suministro",1,0),
        ObservacionOption(38,"38-Luminaria encendida cercana a este suministro",1,0),
        ObservacionOption(39,"39-SED con alumbrado publico encendido",1,0),
        ObservacionOption(40,"40-Suministro con medidor nuevo",1,0),
        ObservacionOption(41,"41-No displaya lectura (causa temporal)",0,1),
        ObservacionOption(42,"42-Datos del medidor no coinciden",1,0),
        ObservacionOption(43,"43-Fuera de Libro de lectura",1,0),
        ObservacionOption(44,"44-Suministro con Ruta mal Asignada",1,0),
        ObservacionOption(45,"45-Murete Dañado o Inclinado",1,0),
        ObservacionOption(46,"46-Murete de AP sin Pintar",1,0),
        ObservacionOption(47,"47-Mastil de F°G°, para murete de AP, sin cinta Band It",1,0),
        ObservacionOption(48,"48-Medidor de AP en interior de caseta",1,0),
        ObservacionOption(49,"49-Suministro para Alumbrado Complementario",1,0),
        ObservacionOption(50,"50-Caja portamedidor energizada",1,0),
        ObservacionOption(53,"53-MEDIDOR CAMBIADO-NUEVO",1,0),
        ObservacionOption(241,"241-AÑADIDO EN CAMPO",1,1),
        ObservacionOption(54,"54-Acometida descolgada, sin punto fijación y/o anclaje",1,0),
        ObservacionOption(55,"55-Acometida sin protección mecánica (bastón)",1,0),
        ObservacionOption(56,"56-Sin deficiencia por código 66 o 67",1,1),
        ObservacionOption(57,"57-Medidor interior CON LECTURA",1,1),
        ObservacionOption(58,"60-Caja porta medidor polimérica amarilla o empañada CON LECTURA",1,1),

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
                val processedBitmap = processImage(capturedBitmap, detalle!!.contrato.toString())
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
                        /*if(observacion == 0){
                            Toast.makeText(context, "Ingrese Observación", Toast.LENGTH_SHORT).show()
                            return@Button
                        }*/

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
                                        val location = LatLng(detalle!!.latitud, detalle!!.longitud)
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

fun processImage(bitmap: Bitmap, suministro: String): Bitmap {
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint().apply {
        color = android.graphics.Color.RED
        textSize = mutableBitmap.width * 0.05f // Tamaño del texto relativo al ancho de la imagen
        isAntiAlias = true
        style = Paint.Style.FILL
        setShadowLayer(5f, 2f, 2f, android.graphics.Color.BLACK) // Sombra para mejorar la legibilidad
        textAlign = Paint.Align.RIGHT
    }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val dateTime = dateFormat.format(Date())
    val x = mutableBitmap.width - 20f
    val y = mutableBitmap.height - 20f
    canvas.drawText(suministro+"    "+dateTime, x, y, paint)
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
                    .padding(8.dp)
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
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onObservacionChange(option.nombre, option.id)
                                    onDismiss()
                                }
                                .padding(vertical = 1.dp)

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