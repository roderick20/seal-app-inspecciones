package com.agile.inspeccion.ui.screen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agile.inspeccion.data.model.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("MyPrefsSealInspecciones", Context.MODE_PRIVATE) }

//    var username by remember { mutableStateOf(sharedPreferences.getString("username", "") ?: "") }
//    var password by remember { mutableStateOf(sharedPreferences.getString("password", "") ?: "") }

    var username by remember { mutableStateOf( "rquicaña") }
    var password by remember { mutableStateOf( "44142556") }


    var rememberPassword by remember { mutableStateOf(false) }

    val viewModel: LoginViewModel = viewModel()
    val loginResult by viewModel.loginResult.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val nombre by viewModel.nombre.collectAsStateWithLifecycle()
    val errorMessage by viewModel.error.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //val context = LocalContext.current
            val iconId = context.resources.getIdentifier("logo_seal", "drawable", context.packageName)


            Image(

                painter = painterResource(id = iconId), // Asegúrate de tener este recurso
                contentDescription = "App Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f/9f) // Ajusta esto según la relación de aspecto de tu logo
                    .padding(vertical = 32.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "Gestión Comercial",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberPassword,
                    onCheckedChange = { rememberPassword = it }
                )
                Text("Recordar password")
            }
            Button(
                onClick = {
                    coroutineScope.launch {


                        viewModel.login(usuario = username, password = password, deviceId = "11")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    if(loginResult) {
                        savePrefs(context, "username", username)
                        savePrefs(context, "password", password)
                        savePrefs(context, "nombre", nombre)
                        navController.navigate("MenuScreen" )
                    }

                    val iconId2 = context.resources.getIdentifier("login", "drawable", context.packageName)

                    Icon(
                        painter = painterResource(id = iconId2),
                        contentDescription = "Login Icon",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(if (isLoading) "Iniciando sesión..." else "Ingresar")
            }
            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun savePrefs(context: Context, name: String, value: String) {
    val sharedPreferences = context.getSharedPreferences("MyPrefsSealInspecciones", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString(name, value)
        apply()
    }
}

fun getPrefs(context: Context, name: String): String {
    val sharedPreferences = context.getSharedPreferences("MyPrefsSealInspecciones", Context.MODE_PRIVATE)
    return sharedPreferences.getString(name, "") ?: ""
}