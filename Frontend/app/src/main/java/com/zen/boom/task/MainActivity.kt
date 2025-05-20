package com.zen.boom.task

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.zen.boom.task.screens.HomeFeedScreen
import com.zen.boom.task.screens.LoginScreen
import com.zen.boom.task.screens.RegisterScreen
import com.zen.boom.task.screens.UploadVideoScreen
import com.zen.boom.task.ui.theme.BoomTaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            BoomTaskTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainController(innerPadding)
                }
            }
        }
    }
}

@Composable
fun MainController(innerPadding: PaddingValues) {
    val navController = rememberNavController()
    NavHost(
        modifier = Modifier.padding(innerPadding),
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeFeedScreen(navController) }
        composable("upload") { UploadVideoScreen(navController) }
    }
}

