package com.agile.inspeccion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agile.inspeccion.data.database.DatabaseHelper
import com.agile.inspeccion.data.model.GruposViewModel
import com.agile.inspeccion.data.model.ListModel
import com.agile.inspeccion.data.model.SuministroModel
import com.agile.inspeccion.ui.screen.LoginScreen


import com.agile.inspeccion.ui.screen.MenuScreen
import com.agile.inspeccion.ui.screen.granindustria.GranIndustriaLista
import com.agile.inspeccion.ui.screen.granindustria.GranIndustriaMain
import com.agile.inspeccion.ui.screen.granindustria.GranIndustriaSuministro
import com.agile.inspeccion.ui.screen.inspeccion.InspeccionLista
import com.agile.inspeccion.ui.screen.inspeccion.InspeccionMain
import com.agile.inspeccion.ui.screen.inspeccion.InspeccionSuministro
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
            AppTheme {
                MyApp(nombre, login, databaseHelper)
            }
        }
    }
}

@Composable
fun MyApp(nombre: String, login: String, databaseHelper: DatabaseHelper) {
    var _nombre: String = nombre
    var _login: String = login
    //var suministroId: Int


    val navController = rememberNavController()
    //var capturedImage by remember { mutableStateOf<Bitmap?>(null) } LoginScreen


    NavHost(navController = navController, startDestination = "LoginScreen") {
        composable("LoginScreen") {
            LoginScreen(navController)
        }
        composable("MenuScreen") {
            MenuScreen(navController)
        }
        //------------------------------------------------------------------------------------------
        composable("InspeccionMain") {
            val viewModel: GruposViewModel = viewModel { GruposViewModel(databaseHelper) }
            InspeccionMain(navController, _nombre, _login, viewModel)
        }
        composable(
            route = "InspeccionLista/{inspeccion}",
            arguments = listOf(navArgument("inspeccion") { type = NavType.IntType })
        ) { backStackEntry ->
            var inspeccion = backStackEntry.arguments?.getInt("inspeccion") ?: 0

            val viewModel: ListModel = viewModel { ListModel(databaseHelper) }
            InspeccionLista(navController, inspeccion, viewModel)
        }
        composable(
            route = "InspeccionSuministro/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            var id = backStackEntry.arguments?.getInt("id") ?: 0
            val viewModel: SuministroModel = viewModel { SuministroModel(databaseHelper, id) }
            InspeccionSuministro(navController, id, viewModel)
        }
        //------------------------------------------------------------------------------------------
        composable("GranIndustriaMain") {
            val viewModel: GruposViewModel = viewModel { GruposViewModel(databaseHelper) }
            GranIndustriaMain(navController, _nombre, _login, viewModel)
        }
        composable(
            route = "GranIndustriaLista/{inspeccion}",
            arguments = listOf(navArgument("inspeccion") { type = NavType.IntType })
        ) { backStackEntry ->
            var inspeccion = backStackEntry.arguments?.getInt("inspeccion") ?: 0

            val viewModel: ListModel = viewModel { ListModel(databaseHelper) }
            GranIndustriaLista(navController, inspeccion, viewModel)
        }
        composable(
            route = "GranIndustriaSuministro/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            var id = backStackEntry.arguments?.getInt("id") ?: 0
            val viewModel: SuministroModel = viewModel { SuministroModel(databaseHelper, id) }
            GranIndustriaSuministro(navController, id, viewModel)
        }
    }
}

