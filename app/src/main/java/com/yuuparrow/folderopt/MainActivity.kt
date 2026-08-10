package com.yuuparrow.folderopt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yuuparrow.folderopt.ui.navigation.FolderOptNavHost
import com.yuuparrow.folderopt.ui.navigation.Routes
import com.yuuparrow.folderopt.ui.theme.FolderOptTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FolderOptTheme {
                FolderOptApp()
            }
        }
    }
}

@Composable
private fun FolderOptApp() {
    val app = (LocalContext.current.applicationContext as FolderOptApplication)
    val navController = rememberNavController()
    val storageRootRoute = Routes.folder(app.container.storageRepository.rootPath)

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination
            val onPermissionScreen = currentRoute?.hierarchy?.any { it.route == Routes.PERMISSION } == true

            if (!onPermissionScreen) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute?.hierarchy?.any { it.route == Routes.FOLDER_PATTERN } == true,
                        onClick = {
                            navController.navigate(storageRootRoute) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Filled.Storage, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_storage)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute?.hierarchy?.any { it.route == Routes.DUPLICATES } == true,
                        onClick = {
                            navController.navigate(Routes.DUPLICATES) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_duplicates)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        FolderOptNavHost(
            navController = navController,
            storageRepository = app.container.storageRepository,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
