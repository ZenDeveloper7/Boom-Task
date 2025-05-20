package com.zen.boom.task.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zen.boom.task.network.Resource
import com.zen.boom.task.viewmodels.AuthenticationViewModel
import timber.log.Timber

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthenticationViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val loginResponse by viewModel.loginMutableStateFlow.collectAsState()

    LaunchedEffect(loginResponse) {
        if (loginResponse is Resource.Loading) {
            //Loading
        } else if (loginResponse is Resource.Success) {
            val response = (loginResponse as Resource.Success).data
            Timber.d("Login response: $response")
            Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
            navController.navigate("home")
        } else if (loginResponse is Resource.Error) {
            val errorMessage = (loginResponse as Resource.Error).message
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Button(onClick = {
            viewModel.login(email.trim(), password.trim())
        }) {
            Text("Login")
        }
        TextButton(onClick = { navController.navigate("register") }) {
            Text("Register")
        }
    }
}