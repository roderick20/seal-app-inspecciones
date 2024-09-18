package com.agile.inspeccion.ui.screen.granindustria

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.agile.inspeccion.data.database.Result
import com.agile.inspeccion.data.model.GruposViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GranIndustriaMain(
    navController: NavController,
    nombre: String,
    login: String,
    gruposModel: GruposViewModel
) {
    var showMenu by remember { mutableStateOf(false) }
    val grupos by gruposModel.grupos.collectAsStateWithLifecycle()
    //var grupos = gruposModel.GetMain()
    val context = LocalContext.current

    val error by gruposModel.error.collectAsStateWithLifecycle()

    BackHandler {
        // Exit the application
        //android.os.Process.killProcess(android.os.Process.myPid())
        navController.navigate("MenuScreen")
    }


    Scaffold(
        topBar = {
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
                        //color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = Color.White
                        )

                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Descargar Inspecciones") },
                            onClick = {
                                gruposModel.cargarDatos(login)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Borrar Inspecciones") },
                            onClick = {
                                gruposModel.DeleteAll()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Enviar inspeciones") },
                            onClick = {
                                var detalles2 = gruposModel.GetDetalleNoEnviado()
                                for (detalle in detalles2) {
                                    gruposModel.SaveDetalle(detalle)
                                    gruposModel.DetalleEnviado(detalle.uniqueId)
                                }
                                showMenu = false
                                gruposModel.GetAllGrupo()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Enviar fotos") },
                            onClick = {
                                var fotos = gruposModel.GetFotoNoEnviado()
                                for (foto in fotos) {
                                    gruposModel.SaveFoto(foto)
                                    if (error == "") {
                                        gruposModel.DetalleFotoEnviado(foto.id)
                                    } else {
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                showMenu = false
                                gruposModel.GetAllGrupo()
                                Toast.makeText(context, "Fotos enviadas", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Descargar Observaciones") },
                            onClick = {
//                                var fotos = gruposModel.GetFotoNoEnviado()
//                                for(foto in fotos) {
//                                    gruposModel.SaveFoto(foto)
//
//                                    gruposModel.DetalleFotoEnviado(foto.id)
//                                }
//                                showMenu = false
//                                gruposModel.GetAllGrupo()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF005DA4))
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
//                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
//                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Técnico: ")
                    }
                    append(nombre)
                },
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(grupos) { _, item ->
                    ListItem(item) {
                        navController.navigate("GranIndustriaLista/" + item.inspeccion.toString())
                    }
                }
            }
        }
    }

    if (gruposModel.showDownloadDialog) {
        DownloadDialog(gruposModel)
    }
}

@Composable
fun DownloadDialog(viewModel: GruposViewModel) {
    Dialog(onDismissRequest = { /*if (!viewModel.isLoading) viewModel.dismissDownloadDialog() */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Descarga de datos",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = viewModel.downloadProgress,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(viewModel.downloadStatus)
            }
        }
    }
}

@Composable
fun ListItem(grupo: Result, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Column() {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("Total[${grupo.total}] - ")
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                        append("Pendientes[${grupo.pendientes}] - ")
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                        append("Inspeccionados[${grupo.inspeccionados}] - Enviados[${grupo.enviados}] - Fotos[${grupo.imagenes_enviadas + grupo.imagenes_no_enviadas}] - Fotos enviadas[${grupo.imagenes_enviadas}]")
                    }
                },
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )
        }
    }
}