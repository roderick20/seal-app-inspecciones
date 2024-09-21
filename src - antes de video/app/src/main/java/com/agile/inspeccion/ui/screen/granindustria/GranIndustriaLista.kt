package com.agile.inspeccion.ui.screen.granindustria

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.agile.inspeccion.data.model.ListModel
import com.agile.inspeccion.data.service.Detalle
import com.agile.inspeccion.ui.screen.inspeccion.removeNonNumeric
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GranIndustriaLista (navController: NavController, inspeccion: Int, viewModel: ListModel) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    val detalles by viewModel.detalles.collectAsStateWithLifecycle()

    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Ruta", "Medidor/MIN", "Suministro")
    var selectedOption by remember { mutableStateOf("") }
    var measurement1 by remember { mutableStateOf("") }
    var showMapDialog by remember { mutableStateOf(false) }

    var showPending by remember { mutableStateOf(false) }
    var showInspected by remember { mutableStateOf(false) }
    var showAll by remember { mutableStateOf(true) }

    viewModel.GetDetalle(inspeccion, showPending, showInspected, showAll)

    val coroutineScope = rememberCoroutineScope()
    BackHandler {
        coroutineScope.launch {
            navController.navigate("GranIndustriaMain")
        }
    }

    val speechRecognizer = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let {
                measurement1 = removeNonNumeric(it)
                viewModel.SearchDetalle("contrato", measurement1)
            }
        }
    }



    // Function to start speech recognition
    fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Ingresar número de suministro")
        }
        try {
            speechRecognizer.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "El reconocimiento de voz no está disponible en este dispositivo",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val micIcon = context.resources.getIdentifier("mic", "drawable", context.packageName)



    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = buildAnnotatedString {
                        append("SEAL Gestión Comercial")
                        append("\n")
                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic,fontSize = 16.sp)) {
                            append("Gran Industria")
                        }
                    },
                    color = Color(0xFFFFFFFF)
                )
            },
            actions = {
                /*IconButton(onClick = {  }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }*/
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        tint = Color.White
                    )

                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Mostrar pendientes",
                                    modifier = Modifier.padding(start = 4.dp).weight(0.8f)
                                )
                                Checkbox(
                                    checked = showPending,
                                    onCheckedChange = {
                                        showPending = it
                                        showInspected = false
                                        showAll = false
                                        showMenu = false
                                        viewModel.GetDetalle(inspeccion, showPending, showInspected, showAll)
                                        //onFilterChange("pending", it)
                                    }
                                )
                            }
                        },
                        onClick = {
                            showPending = !showPending

                            //onFilterChange("pending", showPending)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Mostrar inspeccionados",
                                    modifier = Modifier.padding(start = 4.dp).weight(0.8f)
                                )
                                Checkbox(
                                    checked = showInspected,
                                    onCheckedChange = {
                                        showInspected = it
                                        showPending = false
                                        showAll = false
                                        showMenu = false
                                        viewModel.GetDetalle(inspeccion, showPending, showInspected, showAll)
                                        //onFilterChange("inspected", it)
                                    }
                                )

                            }
                        },
                        onClick = {
                            showInspected = !showInspected
                            //onFilterChange("inspected", showInspected)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Mostrar todo",
                                    modifier = Modifier.padding(start = 4.dp).weight(0.8f)
                                )
                                Checkbox(
                                    checked = showAll,
                                    onCheckedChange = {
                                        showAll = it
                                        showInspected = false
                                        showPending = false

                                        showMenu = false
                                        viewModel.GetDetalle(inspeccion, showPending, showInspected, showAll)
                                        //onFilterChange("all", it)
                                    }
                                )

                            }
                        },
                        onClick = {
                            showAll = !showAll
                            //onFilterChange("all", showAll)
                        }
                    )

                }
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF005DA4))
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { startSpeechRecognition() }) {
                    Icon(
                        painter = painterResource(id = micIcon),
                        contentDescription = "Icono de Micrófono",
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Dictado", Modifier.padding(start = 4.dp))
                }

                Button(onClick = { showMapDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Place, contentDescription = "Icono de Mapa"
                    )
                    Text("Mapa", Modifier.padding(start = 4.dp))
                }
            }
            Text(
                text = "Completado ${detalles.filter { it.actualizado == 1 }.size} de ${detalles.size}",

                fontSize = 14.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .height(height = 45.dp)
                        .weight(0.5f)
                    //.padding(PaddingValues(horizontal = 16.dp, vertical = 4.dp)),

                ) {
                    TextField(
                        value = selectedOption,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            //.height(40.dp)
                            .menuAnchor()
                            .fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp)
                    )
                    ExposedDropdownMenu(expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontSize = 12.sp) },


                                onClick = {

                                    selectedOption = option
                                    expanded = false
                                })
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .height(45.dp)
                        .weight(0.5f)
                        .border(1.dp, Color.Gray, RectangleShape)
                ) {


                    BasicTextField(
                        value = measurement1,
                        onValueChange = {

                            measurement1 = it
                            if(measurement1 == ""){
                                viewModel.GetDetalle(inspeccion, showPending, showInspected, showAll)
                            }
                            else{
                                viewModel.SearchDetalle("contrato", measurement1)
                            }

                            //viewModel.SearchDetalle(selectedOption, it)
                        },
                        modifier = Modifier
                            .height(height = 32.dp)
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 18.sp)
                    )
                }

            }
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(detalles) { _, item ->
                    ListItem1(item) {
                        /*val intent = Intent(context, SuministroActivity::class.java).apply {
                            putExtra("id", item.id)
                        }
                        context.startActivity(intent)*/
                        navController.navigate("GranIndustriaSuministro/" + item.id.toString())
                    }
                }
            }

            if (showMapDialog) {
                Dialog(onDismissRequest = { showMapDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                            .padding(8.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        AndroidView(
                            factory = { context ->
                                MapView(context).apply {
                                    onCreate(null)
                                    getMapAsync { googleMap ->
                                        detalles.forEach { detalle ->
                                            val location =
                                                LatLng(detalle!!.latitud, detalle!!.longitud)
                                            googleMap.moveCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    location, 15f
                                                )
                                            )
                                            googleMap.addMarker(MarkerOptions().position(location))
                                        }
                                    }
                                }
                            }, modifier = Modifier.fillMaxSize()
                        ) { mapView ->
                            mapView.onResume()
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun ListItem1(item: Detalle, onClick: () -> Unit) {
//    val backgroundColor = if (item.actualizado == 0) {
//        MaterialTheme.colorScheme.surfaceVariant
//    } else {
//        MaterialTheme.colorScheme.secondaryContainer
//    }
    val backgroundColor = if (item.actualizado == 0) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val borderColor = if (item.actualizado == 0) {
        MaterialTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        )
    ) {
        Column() {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                        append(item.contrato.toString())
                    }
                    append(" | ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(item.ruta)
                    }
                    append(" | ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append(item.nombres)
                    }
                    append(" | ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append(item.direccion)
                    }
                    append(" | ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append(item.nim)
                    }
                },
                modifier = Modifier.padding(start = 16.dp, top = 5.dp, bottom = 5.dp)
            )
        }
    }
}