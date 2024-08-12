package com.agile.inspeccion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agile.inspeccion.data.database.DatabaseHelper
import com.agile.inspeccion.data.model.GruposViewModel
import com.agile.inspeccion.data.model.ListModel
import com.agile.inspeccion.data.model.SuministroModel
import com.agile.inspeccion.data.service.Grupo
import com.agile.inspeccion.ui.theme.AppTheme


class MainActivity : ComponentActivity() {
    //private lateinit var viewModel: GruposViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var nombre: String = "";
        var login: String = "";

        intent.extras?.let { bundle ->
            nombre = bundle.getString("nombre", "")
            login = bundle.getString("login", "")
        }

        val databaseHelper = DatabaseHelper(this)

        enableEdgeToEdge()
        setContent {
            /*val viewModel: GruposViewModel = viewModel { GruposViewModel(databaseHelper) }
            MainScreen(nombre, login, viewModel)*/
            AppTheme {
                MyApp(nombre, login, databaseHelper)
            }
        }
    }
}

@Composable
fun MyApp(nombre: String, login: String, databaseHelper: DatabaseHelper) {
    var _nombre: String = nombre;
    var _login: String = login;


    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "MainScreen") {
        composable("MainScreen") {
            val viewModel: GruposViewModel = viewModel { GruposViewModel(databaseHelper) }
            MainScreen(navController, _nombre, _login, viewModel)
        }
        composable(
            route = "list/{inspeccion}",
            arguments = listOf(navArgument("inspeccion") { type = NavType.IntType })
        ) { backStackEntry ->
            var inspeccion = backStackEntry.arguments?.getInt("inspeccion") ?: 0

            val viewModel: ListModel = viewModel { ListModel(databaseHelper) }
            DetalleLibroScreen(navController, inspeccion, viewModel)
        }
        composable(
            route = "suministro/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            var id = backStackEntry.arguments?.getInt("id") ?: 0
            val viewModel: SuministroModel = viewModel { SuministroModel(databaseHelper) }
            SuministroInterface(navController, id, viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, nombre: String, login: String, gruposModel: GruposViewModel) {
    var showMenu by remember { mutableStateOf(false) }
    val grupos by gruposModel.grupos.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text="SEAL Gestión Comercial",
                    color = Color(0xFFFFFFFF))
                },
                actions = {
                    /*IconButton(onClick = {  }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }*/
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = Color.White)

                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Descargar Inspecciones") },
                            onClick = {
                                gruposModel.cargarDatos()
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
                        /*DropdownMenuItem(
                            text = { Text("Mostrar todo") },
                            onClick = {
                                showMenu = false
                                // TODO: Mostrar diálogo "Acerca de"
                            }
                        )*/
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF005DA4))
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
                        /*val intent = Intent(context, ListActivity::class.java).apply {
                            putExtra("inspeccion", item.inspeccion)
                        }
                        context.startActivity(intent)*/

                        navController.navigate("list/" + item.inspeccion.toString())
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
    Dialog(onDismissRequest = { /*if (!viewModel.isLoading) viewModel.dismissDownloadDialog() */}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ,
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
fun ListItem(grupo: Grupo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        ) {
        Column() {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Blue)) {
                        append(grupo.cantidad.toString().substringBefore(" "))
                    }
                    append(" ")
                    /*withStyle(style = SpanStyle(color = Color.Blue)) {
                        append(text.substringAfter(" "))
                    }*/
                },
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Blue)) {
                        append("Total[$grupo.cantidad.toString()] - ")
                    }
                    append("")
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append("Pendientes[0] - ")
                    }
                    append("Inspeccionados[0] - Enviados[0] - Fotos[0] - Fotos enviadas[0]")
                },
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )

        }
    }
}
/*
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AppTheme {
        MainScreen("Nombre Técnico","")
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun MainScreenDark() {
    AppTheme {
        MainScreen("Nombre Técnico","")
    }
}*/