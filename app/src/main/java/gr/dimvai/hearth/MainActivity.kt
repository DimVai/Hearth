package gr.dimvai.hearth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import gr.dimvai.hearth.ui.navigation.Screen
import gr.dimvai.hearth.ui.screens.AddScreen
import gr.dimvai.hearth.ui.screens.DashboardScreen
import gr.dimvai.hearth.ui.screens.EditScreen
import gr.dimvai.hearth.ui.screens.SettingsScreen
import gr.dimvai.hearth.ui.theme.HearthTheme
import gr.dimvai.hearth.ui.viewmodel.AddViewModel
import gr.dimvai.hearth.ui.viewmodel.DashboardViewModel
import gr.dimvai.hearth.ui.viewmodel.EditViewModel
import gr.dimvai.hearth.ui.viewmodel.SettingsViewModel
import gr.dimvai.hearth.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            HearthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val repository = (application as HearthApplication).repository

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard
                    ) {
                        composable<Screen.Dashboard> {
                            val viewModel: DashboardViewModel = viewModel(
                                factory = ViewModelFactory(repository)
                            )
                            DashboardScreen(
                                viewModel = viewModel,
                                onAddClick = { 
                                    navController.navigate(Screen.AddConnection) {
                                        launchSingleTop = true
                                    }
                                },
                                onEditClick = { id -> 
                                    navController.navigate(Screen.EditConnection(id)) {
                                        launchSingleTop = true
                                    }
                                },
                                onSettingsClick = { 
                                    navController.navigate(Screen.Settings) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable<Screen.AddConnection> {
                            val viewModel: AddViewModel = viewModel(
                                factory = ViewModelFactory(repository)
                            )
                            AddScreen(
                                viewModel = viewModel,
                                onBackClick = { 
                                    if (navController.previousBackStackEntry != null) {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }

                        composable<Screen.EditConnection> { backStackEntry ->
                            val route: Screen.EditConnection = backStackEntry.toRoute()
                            val viewModel: EditViewModel = viewModel(
                                factory = ViewModelFactory(repository, route.connectionId)
                            )
                            EditScreen(
                                viewModel = viewModel,
                                onBackClick = { 
                                    if (navController.previousBackStackEntry != null) {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }

                        composable<Screen.Settings> {
                            val viewModel: SettingsViewModel = viewModel(
                                factory = ViewModelFactory(repository)
                            )
                            SettingsScreen(
                                viewModel = viewModel,
                                onBackClick = { 
                                    if (navController.previousBackStackEntry != null) {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
