package com.agile.inspeccion.ui.screen.granindustria

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.agile.inspeccion.data.model.DetalleImagen
import com.agile.inspeccion.data.model.SuministroModel
import com.agile.inspeccion.ui.screen.CameraCapture
import com.agile.inspeccion.ui.screen.inspeccion.ObservacionOption
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun GranIndustriaSuministro(navController: NavController, id: Int, viewModel: SuministroModel) {
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var showCameraDialog by remember { mutableStateOf(false) }

    SuministroScreen(
        navController,
        id,
        viewModel,
        capturedImage = capturedImage,
        onTakePhotoClick = { showCameraDialog = true }
    )

    if (showCameraDialog) {
        Dialog(
            onDismissRequest = { showCameraDialog = false },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
        ) {
            CameraScreenDialog(
                onPhotoTaken = { bitmap ->
                    capturedImage = bitmap
                    showCameraDialog = false
                },
                onDismiss = { showCameraDialog = false }
            )
        }
    }
}

@Composable
fun CameraScreenDialog(onPhotoTaken: (Bitmap) -> Unit, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AlertDialogDefaults.containerColor)
    ) {
        CameraCapture(onPhotoTaken = onPhotoTaken)
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuministroScreen(
    navController: NavController,
    id: Int,
    viewModel: SuministroModel,
    capturedImage: Bitmap?,
    onTakePhotoClick: () -> Unit
) {
    val detalle by viewModel.detalle.collectAsStateWithLifecycle()
    val lectura by viewModel.lectura.collectAsStateWithLifecycle()

    //val reset by viewModel.reset.collectAsStateWithLifecycle()
    var reset by rememberSaveable { mutableStateOf(viewModel.reset.value) }
    var mdhfpa by rememberSaveable { mutableStateOf(viewModel.mdhfpa.value) }
    var eatp by rememberSaveable { mutableStateOf(viewModel.eatp.value) }
    var eahpp by rememberSaveable { mutableStateOf(viewModel.eahpp.value) }
    var mdhpp by rememberSaveable { mutableStateOf(viewModel.mdhpp.value) }
    var mdhpa by rememberSaveable { mutableStateOf(viewModel.mdhpa.value) }
    var eahfpp by rememberSaveable { mutableStateOf(viewModel.eahfpp.value) }
    var mdhfpp by rememberSaveable { mutableStateOf(viewModel.mdhfpp.value) }
    var erp by rememberSaveable { mutableStateOf(viewModel.erp.value) }
    var eatc by rememberSaveable { mutableStateOf(viewModel.eatc.value) }
    var eahpc by rememberSaveable { mutableStateOf(viewModel.eahpc.value) }
    var mdhpc by rememberSaveable { mutableStateOf(viewModel.mdhpc.value) }
    var eahfpc by rememberSaveable { mutableStateOf(viewModel.eahfpc.value) }
    var mdhfpc by rememberSaveable { mutableStateOf(viewModel.mdhfpc.value) }
    var erc by rememberSaveable { mutableStateOf(viewModel.erc.value) }

    var reset_enable = true
    var mdhfpa_enable = true
    var eatp_enable = true
    var eahpp_enable = true
    var mdhpp_enable = true
    var mdhpa_enable = true
    var eahfpp_enable = true
    var mdhfpp_enable = true
    var erp_enable = true
    var eatc_enable = true
    var eahpc_enable = true
    var mdhpc_enable = true
    var eahfpc_enable = true
    var mdhfpc_enable = true
    var erc_enable = true

    val reset_FocusRequester = remember { FocusRequester() }
    val mdhfpa_FocusRequester = remember { FocusRequester() }
    val eatp_FocusRequester = remember { FocusRequester() }
    val eahpp_FocusRequester = remember { FocusRequester() }
    val mdhpp_FocusRequester = remember { FocusRequester() }
    val mdhpa_FocusRequester = remember { FocusRequester() }
    val eahfpp_FocusRequester = remember { FocusRequester() }
    val mdhfpp_FocusRequester = remember { FocusRequester() }
    val erp_FocusRequester = remember { FocusRequester() }
    val eatc_FocusRequester = remember { FocusRequester() }
    val eahpc_FocusRequester = remember { FocusRequester() }
    val mdhpc_FocusRequester = remember { FocusRequester() }
    val eahfpc_FocusRequester = remember { FocusRequester() }
    val mdhfpc_FocusRequester = remember { FocusRequester() }
    val erc_FocusRequester = remember { FocusRequester() }

    val observacion by viewModel.observacion.collectAsStateWithLifecycle()
    val fotoTipo by viewModel.fotoTipo.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    viewModel.GetDetalleById(detalle!!.id)

    //viewModel.setReset(viewModel.detalle.value.reset)

    val recordVideoLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.CaptureVideo(),
            onResult = { success ->
                Toast.makeText(context, "Video Grabado: $success", Toast.LENGTH_SHORT).show()
            })
    var videoFile by remember {
        mutableStateOf<File?>(null)
    }
    var videoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var buttonEnabledFotoLectura by remember { mutableStateOf(false) }
    var buttonEnabledFotoPanoramica by remember { mutableStateOf(false) }
    var buttonEnabledGrabar by remember { mutableStateOf(false) }

    var expandedTipoLectura by remember { mutableStateOf(false) }
    var selectedOptionTipoLectura by remember { mutableStateOf("Manual") }
    val optionsTipoLectura = listOf("Manual", "Electronica")

    var expandedTipoMedidor by remember { mutableStateOf(false) }
    var selectedOptionTipoMedidor by remember { mutableStateOf("Tipo 1") }
    val optionsTipoMedidor = listOf("Tipo 1", "Tipo 2")

    var expandedUbicacion by remember { mutableStateOf(false) }
    var selectedOptionUbicacion by remember { mutableStateOf("Interior") }
    val optionsUbicacion = listOf("Interior", "Exterior")

    var expandedPerfilCarga by remember { mutableStateOf(false) }
    var selectedOptionPerfilCarga by remember { mutableStateOf("Si") }
    val optionsPerfilCarga = listOf("Si", "No")

    BackHandler {
        coroutineScope.launch {
            navController.navigate("GranIndustriaLista/" + detalle!!.inspeccionId.toString())
        }
    }
//--------------------------------------------------------------------------------------------------
    var location by remember { mutableStateOf<Location?>(null) }
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    val locationListener = remember {
        object : LocationListener {
            override fun onLocationChanged(newLocation: Location) {
                location = newLocation
            }
        }
    }
    LaunchedEffect(true) {
        if (true) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
            }
        } else {
            locationManager.removeUpdates(locationListener)
        }
    }
//--------------------------------------------------------------------------------------------------
    var showMapDialog by remember { mutableStateOf(false) }


    val micIcon = context.resources.getIdentifier("mic", "drawable", context.packageName)
    var showObservacionDialog by remember { mutableStateOf(false) }
    var observacionText by remember { mutableStateOf("") }

    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }


    val observacionOptions = listOf(
        ObservacionOption(0, "0-Ninguna", 1, 0),
        ObservacionOption(1, "1-Contometro o display no se aprecia", 0, 1),
        ObservacionOption(2, "2-Contómetro descentrado (no hay certeza en la lectura)", 0, 1),
        ObservacionOption(3, "3-No displaya lectura (medidor malogrado)", 0, 1),
        ObservacionOption(4, "4-Medidor o caja con medidor inclinado, colgado o amarrado", 1, 1),
        ObservacionOption(5, "5-Medidor inaccesible", 0, 1),
        ObservacionOption(
            6,
            "6-Medidor interior, usuario no se encuentra (no se puede lecturar)",
            0,
            1
        ),
        ObservacionOption(7, "7-Medidor en el techo", 0, 1),
        ObservacionOption(8, "8-Medidor al ras del piso", 1, 0),
        ObservacionOption(9, "9-Medidor en el poste", 1, 0),
        ObservacionOption(10, "10-Caja sin numero de suministro", 1, 0),
        ObservacionOption(11, "11-Zona peligrosa, perros  (no se puede lecturar)", 0, 1),
        ObservacionOption(12, "12-Suministro no se ubica", 0, 1),
        ObservacionOption(13, "13-Disco del medidor retrocede", 1, 0),
        ObservacionOption(14, "14-Conexion invertida", 1, 0),
        ObservacionOption(15, "15-Copa rota, rajada u opaca o empañada", 0, 1),
        ObservacionOption(
            16,
            "16-Disco del medidor no gira (plantado cuando usuario consume energía)",
            1,
            0
        ),
        ObservacionOption(17, "17-Medidor quemado o bornera del medidor quemada", 1, 0),
        ObservacionOption(18, "18-Lectura actual menor a la anterior", 1, 0),
        ObservacionOption(19, "19-Suministro sin medidor y sin acometida", 0, 1),
        ObservacionOption(20, "20-Suministro sin medidor y con acometida", 0, 1),
        ObservacionOption(21, "21-Suministro con conexión directa a la red", 1, 0),
        ObservacionOption(
            22,
            "22-Medidor con conexión directa desde la bornera o puentes en la bornera",
            1,
            0
        ),
        ObservacionOption(
            23,
            "23-Caja porta medidor sin precinto o precinto roto o manipulado",
            1,
            0
        ),
        ObservacionOption(
            24,
            "24-Caja normalizada sin pernos o pernos sin descabezar (caja sin asegurar)",
            1,
            0
        ),
        ObservacionOption(25, "25-Caja porta medidor sin tapa superior o inferior", 1, 1),
        ObservacionOption(
            26,
            "26-Caja portamedidor con sello forza retirado, roto o abollado",
            1,
            0
        ),
        ObservacionOption(
            27,
            "27-Caja antigua sin remaches y sin soldar (caja sin asegurar)",
            1,
            0
        ),
        ObservacionOption(28, "28-Medidor ubicado muy alto", 1, 0),
        ObservacionOption(29, "29-Numero de suministro mal rotulado", 1, 0),
        ObservacionOption(
            31,
            "31-Medidor interior, usuario no permite tomar lectura (no se puede lecturar)",
            0,
            1
        ),
        ObservacionOption(32, "32-Suministro no pertenece a sed", 1, 0),
        ObservacionOption(33, "33-Persona ajena a la empresa manipula el suministro", 1, 0),
        ObservacionOption(34, "34-Suministro adicionado a la SED", 1, 0),
        ObservacionOption(35, "35-Visor empañado, sucio o pintado (no se puede lecturar)", 0, 1),
        ObservacionOption(36, "36-Ciclo completo del contador", 1, 0),
        ObservacionOption(37, "37-Conexión clandestina cercana a este suministro", 1, 0),
        ObservacionOption(38, "38-Luminaria encendida cercana a este suministro", 1, 0),
        ObservacionOption(39, "39-SED con alumbrado publico encendido", 1, 0),
        ObservacionOption(40, "40-Suministro con medidor nuevo", 1, 0),
        ObservacionOption(41, "41-No displaya lectura (causa temporal)", 0, 1),
        ObservacionOption(42, "42-Datos del medidor no coinciden", 1, 0),
        ObservacionOption(43, "43-Fuera de Libro de lectura", 1, 0),
        ObservacionOption(44, "44-Suministro con Ruta mal Asignada", 1, 0),
        ObservacionOption(45, "45-Murete Dañado o Inclinado", 1, 0),
        ObservacionOption(46, "46-Murete de AP sin Pintar", 1, 0),
        ObservacionOption(47, "47-Mastil de F°G°, para murete de AP, sin cinta Band It", 1, 0),
        ObservacionOption(48, "48-Medidor de AP en interior de caseta", 1, 0),
        ObservacionOption(49, "49-Suministro para Alumbrado Complementario", 1, 0),
        ObservacionOption(50, "50-Caja portamedidor energizada", 1, 0),
        ObservacionOption(53, "53-MEDIDOR CAMBIADO-NUEVO", 1, 0),
        ObservacionOption(241, "241-AÑADIDO EN CAMPO", 1, 1),
        ObservacionOption(54, "54-Acometida descolgada, sin punto fijación y/o anclaje", 1, 0),
        ObservacionOption(55, "55-Acometida sin protección mecánica (bastón)", 1, 0),
        ObservacionOption(56, "56-Sin deficiencia por código 66 o 67", 1, 1),
        ObservacionOption(57, "57-Medidor interior CON LECTURA", 1, 1),
        ObservacionOption(
            58,
            "60-Caja porta medidor polimérica amarilla o empañada CON LECTURA",
            1,
            1
        ),

        ObservacionOption(
            60,
            "60-Ubicacion del puerto óptico del meidor no permite conexión al cable de comunicación",
            1,
            1
        ),
        ObservacionOption(61, "61 Equipos no puede establecer comunicación con el medidor", 1, 1),
        ObservacionOption(62, "62 Error del sistema durante comunicacion con el medidor", 1, 1),
        ObservacionOption(63, "63-Medidor electromecanico", 1, 1),
        ObservacionOption(64, "64-Medidor no compatible con el software de lectura", 1, 1),
        ObservacionOption(65, "65-Medidor no displaya todos los parametros", 1, 1),


        )
    var hasCameraPermission by remember { mutableStateOf(false) }
    var localCapturedImage by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(capturedImage) {
        if (capturedImage != null) {
            val processedBitmap = if (fotoTipo == 1) {
                processImage90(capturedImage, detalle!!.contrato.toString())
            } else {
                processImage(capturedImage, detalle!!.contrato.toString())
            }

            val photo = DetalleImagen(processedBitmap, fotoTipo)
            viewModel.agregarImagen(photo)
            localCapturedImage = null  // Reset the local state
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = buildAnnotatedString {
                            append("SEAL Gestión Comercial")
                            append("\n")
                            withStyle(
                                style = SpanStyle(
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 16.sp
                                )
                            ) {
                                append("Gran Industria")
                            }
                        },
                        color = Color.White
                    )
                },
                actions = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {

//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(horizontal = 8.dp, vertical = 2.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
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
                    Column {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                                    append(detalle!!.contrato.toString())
                                }
                                append(" | ")
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append(detalle!!.ruta)
                                }
                                append(" | ")
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                    append(detalle!!.nombres)
                                }
                                append(" | ")
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                    append(detalle!!.direccion)
                                }
                                append(" | ")
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                    append(detalle!!.nim)
                                }
                            },
                            modifier = Modifier.padding(start = 16.dp, top = 5.dp, bottom = 5.dp)
                        )
                    }
                }

                CustomExposedDropdownMenuBox(
                    expanded = expandedTipoLectura,
                    onExpandedChange = { expandedTipoLectura = it },
                    selectedOption = selectedOptionTipoLectura,
                    options = optionsTipoLectura,
                    onOptionSelected = { selectedOptionTipoLectura = it },
                    label = "Tipo de lectura"
                )

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
                                viewModel.setObservacion(0)



                            },
                            modifier = Modifier
                                .height(height = 32.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 1.dp, vertical = 1.dp),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    Button(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(0.1f)
                            .height(32.dp),
                        onClick = { showObservacionDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(".")
                    }
                }

                if (selectedOptionTipoLectura == "Electronica") {
                    CustomExposedDropdownMenuBox(
                        expanded = expandedUbicacion,
                        onExpandedChange = { expandedUbicacion = it },
                        selectedOption = selectedOptionUbicacion,
                        options = optionsTipoLectura,
                        onOptionSelected = { selectedOptionUbicacion = it },
                        label = "Ubicación"
                    )
                    CustomExposedDropdownMenuBox(
                        expanded = expandedPerfilCarga,
                        onExpandedChange = { expandedPerfilCarga = it },
                        selectedOption = selectedOptionPerfilCarga,
                        options = optionsTipoLectura,
                        onOptionSelected = { selectedOptionPerfilCarga = it },
                        label = "Perfil de carga"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            enabled = true, //buttonEnabledFotoLectura,
                            onClick = {
                                onTakePhotoClick()
                                viewModel.setFotoTipo(1)
                                buttonEnabledFotoPanoramica = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Cámara"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Foto Lectura",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Button(
                            enabled = true, //buttonEnabledFotoPanoramica,
                            onClick = {
                                onTakePhotoClick()
                                viewModel.setFotoTipo(2)
                                buttonEnabledGrabar = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Cámara"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Foto Fachada",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                }


                if (selectedOptionTipoLectura == "Manual") {
                    CustomExposedDropdownMenuBox(
                        expanded = expandedTipoMedidor,
                        onExpandedChange = { expandedTipoMedidor = it },
                        selectedOption = selectedOptionTipoMedidor,
                        options = optionsTipoMedidor,
                        onOptionSelected = { selectedOptionTipoMedidor = it },
                        label = "Tipo de Medidor"
                    )
                    if (selectedOptionTipoMedidor == "Tipo 1") {
                        OutlinedTextField(
                            value = reset,
                            onValueChange = {
                                reset = it
                                viewModel.setReset(it)
                                            },
                            label = { Text("04-Contador de Reset") },
                            singleLine = true,
                            enabled = reset_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { eatp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )

                        OutlinedTextField(
                            value = eatp,
                            onValueChange = {
                                eatp = it
                                viewModel.setEatp(it) },
                            label = { Text("05-Energia Activa Total Presente (kWh)") },
                            singleLine = true,
                            enabled = reset_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { eahpp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(eatp_FocusRequester)
                        )

                        OutlinedTextField(
                            value = eahpp,
                            onValueChange = {
                                eahpp = it
                                viewModel.setEahpp(it) },
                            label = { Text("06-Energia Activa en Horas Punta Presente (kWh)") },
                            singleLine = true,
                            enabled = eahpp_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { mdhpp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(eahpp_FocusRequester)
                        )

                        OutlinedTextField(
                            value = mdhpp,
                            onValueChange = {
                                mdhpp = it
                                viewModel.setMdhpp(it) },
                            label = { Text("07-Maxima Demanda en Horas Punta Presene (kW)") },
                            singleLine = true,
                            enabled = mdhpp_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { mdhpa_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(mdhpp_FocusRequester)
                        )

                        OutlinedTextField(
                            value = mdhpa,
                            onValueChange = {
                                mdhpa = it
                                viewModel.setMdhpa(it) },
                            label = { Text("08-Maxima Demanda en Horas Punta Acumulada (kW)") },
                            singleLine = true,
                            enabled = mdhpa_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { eahfpp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(mdhpa_FocusRequester)
                        )

                        OutlinedTextField(
                            value = eahfpp,
                            onValueChange = {
                                eahfpp = it
                                viewModel.setEahfpp(it) },
                            label = { Text("09-Energia Activa en Horas Fuera de Punta Presente (kWh)") },
                            singleLine = true,
                            enabled = eahfpp_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { mdhfpp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(eahfpp_FocusRequester)
                        )

                        OutlinedTextField(
                            value = mdhfpp,
                            onValueChange = {
                                mdhfpp = it
                                viewModel.setMdhfpp(it) },
                            label = { Text("10-Maxima Demanda en Horas Fuera de Punta Presene (kW)") },
                            singleLine = true,
                            enabled = mdhfpp_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { mdhfpa_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(mdhfpp_FocusRequester)
                        )
                        OutlinedTextField(
                            value = mdhfpa,
                            onValueChange = {
                                mdhfpa = it
                                viewModel.setMdhfpa(it) },
                            label = { Text("11-Maxima Demanda en Horas Fuera de Punta Acumulada (kW)") },
                            singleLine = true,
                            enabled = mdhfpa_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { erp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(mdhfpa_FocusRequester)
                        )
                        OutlinedTextField(
                            value = erp,
                            onValueChange = {
                                erp = it
                                viewModel.setErp(it) },
                            label = { Text("12-Energia Reactiva Presenta(kVarh)") },
                            singleLine = true,
                            enabled = erp_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { eatc_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(erp_FocusRequester)
                        )
                        OutlinedTextField(
                            value = eatc,
                            onValueChange = {
                                eatc = it
                                viewModel.setEatc(it) },
                            label = { Text("13-Energia Activa Total Congelada (kWh)") },
                            singleLine = true,
                            enabled = eatc_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { eahpc_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(eatc_FocusRequester)
                        )
                        OutlinedTextField(
                            value = eahpc,
                            onValueChange = {
                                eahpc = it
                                viewModel.setEahpc(it) },
                            label = { Text("14-Energia Activa en Horas Punta Congelada (kWh)") },
                            singleLine = true,
                            enabled = eahpc_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { mdhpc_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(eahpc_FocusRequester)
                        )

                        OutlinedTextField(
                            value = mdhpc,
                            onValueChange = {
                                mdhpc = it
                                viewModel.setMdhpc(it) },
                            label = { Text("15-Maxima Demanda en Horas Punta Congelada (kW)") },
                            singleLine = true,
                            enabled = mdhpc_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { eahfpc_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(mdhpc_FocusRequester)
                        )

                        OutlinedTextField(
                            value = eahfpc,
                            onValueChange = {
                                eahfpc = it
                                viewModel.setEahfpc(it) },
                            label = { Text("16-Energia Activa en Horas Fuera de Punta Congelada (kWh)") },
                            singleLine = true,
                            enabled = eahfpc_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { mdhfpc_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(eahfpc_FocusRequester)
                        )

                        OutlinedTextField(
                            value = mdhfpc,
                            onValueChange = {
                                mdhfpc = it
                                viewModel.setMdhfpc(it) },
                            label = { Text("17-Maxima Demanda en Horas Fuera de Punta Congelada (kW)") },
                            singleLine = true,
                            enabled = mdhfpc_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { erc_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(mdhfpc_FocusRequester)
                        )

                        OutlinedTextField(
                            value = erc,
                            onValueChange = {
                                erc = it
                                viewModel.setErc(it) },
                            label = { Text("18-Energia Reactiva Congelada(kVarh)") },
                            singleLine = true,
                            enabled = erc_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                //onNext = { eahpp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(erc_FocusRequester)
                        )
                    }

                    if (selectedOptionTipoMedidor == "Tipo 2") {
                        OutlinedTextField(
                            value = reset,
                            onValueChange = {
                                reset = it
                                viewModel.setReset(it) },
                            label = { Text("0.1.0-Contador de Reset") },
                            singleLine = true,
                            enabled = reset_enable,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { eatp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(reset_FocusRequester)
                        )

                        OutlinedTextField(
                            value = eatp,
                            onValueChange = {
                                eatp = it
                                viewModel.setEatp(it) },
                            label = { Text("1.8.0-Energia Activa Total Presente (kWh)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { eatc_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(eatp_FocusRequester)
                        )

                        OutlinedTextField(
                            value = eatc,
                            onValueChange = {
                                eatc = it
                                viewModel.setEatc(it) },
                            label = { Text("1.8.0.XX-Energia Activa Total Congelada (kWh)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { eahpp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(eatc_FocusRequester)
                        )


                        OutlinedTextField(
                            value = eahpp,
                            onValueChange = {
                                eahpp = it
                                viewModel.setEahpp(it) },
                            label = { Text("1.8.2-Energia Activa en Horas Punta Presente (kWh)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { eahpc_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(eahpp_FocusRequester)
                        )

                        OutlinedTextField(
                            value = eahpc,
                            onValueChange = {
                                eahpc = it
                                viewModel.setEahpc(it) },
                            label = { Text("1.8.2.XX-Energia Activa en Horas Punta Congelada (kWh)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { mdhpp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(eahpc_FocusRequester)
                        )

                        OutlinedTextField(
                            value = mdhpp,
                            onValueChange = {
                                mdhpp = it
                                viewModel.setMdhpp(it) },
                            label = { Text("1.6.2-Maxima Demanda en Horas Punta Presene (kW)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { mdhpc_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(mdhpp_FocusRequester)
                        )

                        OutlinedTextField(
                            value = mdhpc,
                            onValueChange = {
                                mdhpc = it
                                viewModel.setMdhpc(it) },
                            label = { Text("1.6.2.XX-Maxima Demanda en Horas Punta Congelada (kW)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { mdhpa_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(mdhpc_FocusRequester)
                        )

                        OutlinedTextField(
                            value = mdhpa,
                            onValueChange = {
                                mdhpa = it
                                viewModel.setMdhpa(it) },
                            label = { Text("1.2.2-Maxima Demanda en Horas Punta Acumulada (kW)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { eahfpp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(mdhpa_FocusRequester)
                        )

                        OutlinedTextField(
                            value = eahfpp,
                            onValueChange = {
                                eahfpp = it
                                viewModel.setEahfpp(it) },
                            label = { Text("1.8.1-Energia Activa en Horas Fuera de Punta Presente (kWh)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { eahfpc_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(eahfpp_FocusRequester)
                        )

                        OutlinedTextField(
                            value = eahfpc,
                            onValueChange = {
                                eahfpc = it
                                viewModel.setEahfpc(it) },
                            label = { Text("1.8.1.XX-Energia Activa en Horas Fuera de Punta Congelada (kWh)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { mdhfpp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(eahfpc_FocusRequester)
                        )

                        OutlinedTextField(
                            value = mdhfpp,
                            onValueChange = {
                                mdhfpp = it
                                viewModel.setMdhfpp(it) },
                            label = { Text("1.6.1-Maxima Demanda en Horas Fuera de Punta Presene (kW)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { mdhfpc_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(mdhfpp_FocusRequester)
                        )

                        OutlinedTextField(
                            value = mdhfpc,
                            onValueChange = {
                                mdhfpc = it
                                viewModel.setMdhpc(it) },
                            label = { Text("1.6.1.XX-Maxima Demanda en Horas Fuera de Punta Congelada (kW)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { mdhfpa_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(mdhfpc_FocusRequester)
                        )

                        OutlinedTextField(
                            value = mdhfpa,
                            onValueChange = {
                                mdhfpa = it
                                viewModel.setMdhfpa(it) },
                            label = { Text("1.2.1-Maxima Demanda en Horas Fuera de Punta Acumulada (kW)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { erp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(mdhfpa_FocusRequester)
                        )
                        OutlinedTextField(
                            value = erp,
                            onValueChange = {
                                erp = it
                                viewModel.setErp(it) },
                            label = { Text("3.8.0-Energia Reactiva Presenta(kVarh)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { erc_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(erp_FocusRequester)
                        )

                        OutlinedTextField(
                            value = erc,
                            onValueChange = {
                                erc = it
                                viewModel.setErc(it) },
                            label = { Text("3.8.0.XX-Energia Reactiva Congelada(kVarh)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                //onNext = { erp_FocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .focusRequester(erc_FocusRequester)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            enabled = true, //buttonEnabledFotoLectura,
                            onClick = {
                                onTakePhotoClick()
                                viewModel.setFotoTipo(1)
                                buttonEnabledFotoPanoramica = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Cámara"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Foto Lectura",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Button(
                            enabled = true, //buttonEnabledFotoPanoramica,
                            onClick = {
                                onTakePhotoClick()
                                viewModel.setFotoTipo(2)
                                buttonEnabledGrabar = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Cámara"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Foto Fachada",
                                    textAlign = TextAlign.Center
                                )
                            }
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
                            enabled = true,//buttonEnabledFotoPanoramica,
                            onClick = {
                                val timeStamp = SimpleDateFormat(
                                    "yyyyMMdd_HHmmss",
                                    Locale.getDefault()
                                ).format(Date())
                                val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)

                                videoFile = File.createTempFile(
                                    "VIDEO_${timeStamp}_",
                                    ".mp4",
                                    dir
                                )
                                videoUri = videoFile?.getUri(context = context)
                                videoUri?.let { recordVideoLauncher.launch(it) }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Grabar Video"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Grabar Video",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Button(
                            onClick = {
                                if (videoUri != null) {
                                    videoUri?.openVideoPlayer(context = context)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No video recorded",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayCircleOutline,
                                    contentDescription = null
                                )
                                Text(
                                    text = "Ver Video",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                }
                Column(
                    //verticalAlignment = Alignment.CenterVertically,
                    //horizontalArrangement = Arrangement.Center
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        enabled = buttonEnabledGrabar,
                        onClick = {
                            /*if (lectura.equals("")) {
                                Toast.makeText(context, "Ingrese lectura", Toast.LENGTH_SHORT)
                                    .show()
                                return@Button
                            }*/
                            /*if((viewModel.imagenesCapturadas .filter { it.tipo == 1 }).size == 0){
                            Toast.makeText(context, "Ingrese foto lectura", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if((viewModel.imagenesCapturadas .filter { it.tipo == 2 }).size == 0){
                            Toast.makeText(context, "Ingrese foto panoramica", Toast.LENGTH_SHORT).show()
                            return@Button
                        }*/

                            if (viewModel.imagenesCapturadas.size < 2) {
                                Toast.makeText(context, "Ingrese foto lectura", Toast.LENGTH_SHORT)
                                    .show()
                                return@Button
                            }

                            when {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    getLocation(fusedLocationClient) { location ->
                                        latitude = location.latitude
                                        longitude = location.longitude
                                    }
                                }

                                else -> {
                                    //requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }

                            val currentDateTime = LocalDateTime.now()
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            val formatted = currentDateTime.format(formatter)



                            viewModel.updateDetalle(
                                detalle!!.id,
                                "0",
                                observacion.toString(),
                                location?.latitude ?: 0.0,
                                location?.longitude ?: 0.0,
                                formatted,
                                reset,
                                mdhfpa,
                                eatp,
                                eahpp,
                                mdhpp,
                                mdhpa,
                                eahfpp,
                                mdhfpp,
                                erp,
                                eatc,
                                eahpc,
                                mdhpc,
                                eahfpc,
                                mdhfpc,
                                erc,
                                selectedOptionTipoLectura,
                                selectedOptionTipoMedidor
                            )

                            viewModel.GetDetalleById(detalle!!.id)



                            viewModel.imagenesCapturadas.forEach {
                                viewModel.addImage(it.foto, detalle!!.id, it.tipo)
                            }
                            if (videoUri != null) {
                                viewModel.addVideo(detalle!!.id, videoUri.toString())

                                val mimeType =
                                    context.contentResolver.getType(videoUri!!) ?: "video/*"
                                val fileExtension =
                                    MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                                        ?: "mp4"
                                // Crear un archivo temporal
                                val tempFile = File.createTempFile(
                                    "video",
                                    ".$fileExtension",
                                    context.cacheDir
                                )
                                // Copiar el contenido del URI al archivo temporal
                                context.contentResolver.openInputStream(videoUri!!)?.use { input ->
                                    FileOutputStream(tempFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }

                                viewModel.SendVideo(detalle!!.id.toString(), tempFile, mimeType)

//                                // Crear el MultipartBody.Part para el archivo de video
//                                //val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
//                                //val videoPart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
//
//                                // Crear la instancia de la API y hacer la llamada
//                                //val apiService = retrofit.create(GrabarVideoApi::class.java)
//                                //val response = apiService.grabarVideo(detalleidBody, tipoBody, videoPart)
                            }


                            var detalles2 = viewModel.GetDetalleNoEnviado()
                            for (detalle in detalles2) {
                                viewModel.SaveDetalle(detalle)
                                viewModel.DetalleEnviado(detalle.uniqueId)
                            }
                            Toast.makeText(context, "Lectura grabada", Toast.LENGTH_SHORT).show()
                            var siguiente = viewModel.siguiente(detalle!!.id)
                            if (siguiente != null) {
                                navController.navigate("GranIndustriaSuministro/" + siguiente!!.id.toString())
                            } else {
                                navController.navigate("GranIndustriaLista/" + detalle!!.inspeccionId.toString())
                            }

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Grabar",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grabar", style = MaterialTheme.typography.titleMedium)
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

                Spacer(modifier = Modifier.height(16.dp))


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
                if (showObservacionDialog) {
                    ObservacionDialog(
                        observacionText = observacionText,
                        onObservacionChange = { text, id ->
                            observacionText = text
                            viewModel.setObservacion(id)

                            val optionset = observacionOptions.first { it.id == id }
                            if(optionset.requiereLectura == 0 || optionset.id == 6){
                                reset = ""
                                mdhfpa  = ""
                                eatp  = ""
                                eahpp  = ""
                                mdhpp  = ""
                                mdhpa  = ""
                                eahfpp  = ""
                                mdhfpp  = ""
                                erp = ""
                                eatc  = ""
                                eahpc  = ""
                                mdhpc  = ""
                                eahfpc  = ""
                                mdhfpc  = ""
                                erc  = ""

                                reset_enable = false
                                mdhfpa_enable = false
                                eatp_enable = false
                                eahpp_enable = false
                                mdhpp_enable = false
                                mdhpa_enable = false
                                eahfpp_enable = false
                                mdhfpp_enable = false
                                erp_enable = false
                                eatc_enable = false
                                eahpc_enable = false
                                mdhpc_enable = false
                                eahfpc_enable = false
                                mdhfpc_enable = false
                                erc_enable = false
                            }

                            else{
                                reset_enable = true
                                mdhfpa_enable = true
                                eatp_enable = true
                                eahpp_enable = true
                                mdhpp_enable = true
                                mdhpa_enable = true
                                eahfpp_enable = true
                                mdhfpp_enable = true
                                erp_enable = true
                                eatc_enable = true
                                eahpc_enable = true
                                mdhpc_enable = true
                                eahfpc_enable = true
                                mdhfpc_enable = true
                                erc_enable = true
                            }
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
                                            val location =
                                                LatLng(detalle!!.latitud, detalle!!.longitud)
                                            googleMap.moveCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    location,
                                                    15f
                                                )
                                            )
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
}
/*
private fun checkCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

private fun requestCameraPermission() {
    ActivityCompat.requestPermissions(
        this,
        arrayOf(android.Manifest.permission.CAMERA),
        CAMERA_PERMISSION_CODE
    )
}*/
/*
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == CAMERA_PERMISSION_CODE) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permiso concedido, iniciar la grabación de video
            startVideoRecording()
        } else {
            // Permiso denegado, mostrar un mensaje al usuario
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }
}*/

@Composable
fun ObservacionRow(
    observacion: Int,
    observacionOptions: List<ObservacionOption>,
    onObservacionChange: (String) -> Unit,
    onShowDialog: () -> Unit
) {
    var textFieldValue by remember { mutableStateOf("") }

    LaunchedEffect(observacion) {
        textFieldValue = observacionOptions.find { it.id == observacion }?.nombre ?: ""
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onObservacionChange(it)
        },
        label = { Text("Observación") },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        ),
        trailingIcon = {
            IconButton(
                onClick = onShowDialog,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Mostrar opciones",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomExposedDropdownMenuBox(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    label: String
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        onOptionSelected(option)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

fun getLocation(
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

fun processImage90(bitmap: Bitmap, suministro: String): Bitmap {
    val matrix = Matrix().apply { postRotate(90f) }
    val rotatedBitmap = Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
    )
    val mutableBitmap = rotatedBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint().apply {
        color = android.graphics.Color.RED
        textSize = mutableBitmap.width * 0.05f // Tamaño del texto relativo al ancho de la imagen
        isAntiAlias = true
        style = Paint.Style.FILL
        setShadowLayer(
            5f,
            2f,
            2f,
            android.graphics.Color.BLACK
        ) // Sombra para mejorar la legibilidad
        textAlign = Paint.Align.RIGHT
    }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val dateTime = dateFormat.format(Date())
    val x = mutableBitmap.width - 20f
    val y = mutableBitmap.height - 20f
    canvas.drawText(suministro + "    " + dateTime, x, y, paint)
    return mutableBitmap
}

fun processImage(bitmap: Bitmap, suministro: String): Bitmap {


    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint().apply {
        color = android.graphics.Color.RED
        textSize = mutableBitmap.width * 0.05f // Tamaño del texto relativo al ancho de la imagen
        isAntiAlias = true
        style = Paint.Style.FILL
        setShadowLayer(
            5f,
            2f,
            2f,
            android.graphics.Color.BLACK
        ) // Sombra para mejorar la legibilidad
        textAlign = Paint.Align.RIGHT
    }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val dateTime = dateFormat.format(Date())
    val x = mutableBitmap.width - 20f
    val y = mutableBitmap.height - 20f
    canvas.drawText(suministro + "    " + dateTime, x, y, paint)
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

fun Context.createVideoFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val dir = this.getExternalFilesDir(Environment.DIRECTORY_MOVIES)

    return File.createTempFile(
        "VIDEO_${timeStamp}_",
        ".mp4",
        dir
    )
}

fun File.getUri(context: Context): Uri? {
    return FileProvider.getUriForFile(
        context,
        context.applicationContext.packageName + ".fileprovider",
        this
    )
}

fun Uri.openVideoPlayer(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(this, "video/*")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No video player found", Toast.LENGTH_LONG).show()
    }
}