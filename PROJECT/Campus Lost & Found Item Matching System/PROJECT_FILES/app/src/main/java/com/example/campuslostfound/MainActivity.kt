package com.example.campuslostfound
import com.example.campuslostfound.screens.FoundItemScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.campuslostfound.screens.HomeScreen
import com.example.campuslostfound.screens.LoginScreen
import com.example.campuslostfound.screens.RegisterScreen
import com.example.campuslostfound.ui.theme.CampusLostFoundTheme
import com.google.firebase.auth.FirebaseAuth
import com.example.campuslostfound.screens.LostItemScreen
import com.example.campuslostfound.screens.MatchesScreen
import androidx.activity.compose.BackHandler
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CampusLostFoundTheme {



                var currentScreen by remember {
                    mutableStateOf(
                        if (FirebaseAuth.getInstance().currentUser != null) {
                            "home"
                        } else {
                            "login"
                        }
                    )
                }

                BackHandler(
                    enabled = currentScreen != "home" && currentScreen != "login"
                ) {
                    currentScreen = "home"
                }

                when (currentScreen) {

                    "lost" -> {
                        LostItemScreen(
                            onSubmitted = {
                                currentScreen = "home"
                            }
                        )
                    }

                    "found" -> {
                        FoundItemScreen(
                            onSubmitted = {
                                currentScreen = "home"
                            }
                        )
                    }
                    "matches" -> {
                        MatchesScreen(
                            onBack = {
                                currentScreen = "home"
                            }
                        )
                    }

                    "login" -> {
                        LoginScreen(
                            onLoginSuccess = {
                                currentScreen = "home"
                            },
                            onRegisterClick = {
                                currentScreen = "register"
                            }
                        )
                    }

                    "register" -> {
                        RegisterScreen(
                            onRegisterSuccess = {
                                currentScreen = "home"
                            }
                        )
                    }

                    "home" -> {
                        HomeScreen(
                            onLogout = {
                                currentScreen = "login"
                            },
                            onReportLost = {
                                currentScreen = "lost"
                            },
                            onReportFound = {
                                currentScreen = "found"
                            },
                            onFindMatches = {
                                currentScreen = "matches"
                            }
                        )
                    }
                }
            }
        }
    }
}