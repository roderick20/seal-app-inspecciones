package com.agile.inspeccion.ui.screen

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

data class MenuItem(val title: String, val icon: ImageVector, val color: Color, val screen: String)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(items: List<MenuItem>,  navController: NavController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SEAL Gestión Comercial",
                        color = Color(0xFFFFFFFF)
                        //color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                actions = {


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
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items) { item ->
                    Card(
                        onClick = { navController.navigate(item.screen) },
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(item.color, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(60.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.title,
                                    //style = MaterialTheme.typography.subtitle1,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Ejemplo de uso
@Composable
fun MenuScreen(navController: NavController) {
    BackHandler {
        navController.navigate("LoginScreen" )
    }

    val menuItems = listOf(
        MenuItem("Lecturas", Icons.Filled.PhotoCamera, Color(0xFF005DA4), "InspeccionMain"),
        MenuItem("Repartos", Icons.Filled.FileCopy, Color(0xFF005DA4), "InspeccionMain"),
        MenuItem("Inspecciones", Icons.Default.Search, Color(0xFF005DA4), "InspeccionMain"),
        MenuItem("Gran Industria", Icons.Default.Settings, Color(0xFF005DA4), "GranIndustriaMain")
    )

    Menu(items = menuItems, navController)
}
/*
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {

    AppTheme {
        MenuScreen()
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun LoginScreenPreviewDark() {
    AppTheme {
        MenuScreen()
    }
}*/