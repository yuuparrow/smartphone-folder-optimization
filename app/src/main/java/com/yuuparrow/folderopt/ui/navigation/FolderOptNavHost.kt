package com.yuuparrow.folderopt.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yuuparrow.folderopt.data.repository.StorageRepository
import com.yuuparrow.folderopt.permission.PermissionHelper
import com.yuuparrow.folderopt.ui.duplicates.DuplicateGroupDetailScreen
import com.yuuparrow.folderopt.ui.duplicates.DuplicateListScreen
import com.yuuparrow.folderopt.ui.permission.PermissionScreen
import com.yuuparrow.folderopt.ui.preview.MediaPreviewScreen
import com.yuuparrow.folderopt.ui.storage.FolderScreen
import androidx.compose.ui.platform.LocalContext

@Composable
fun FolderOptNavHost(
    navController: NavHostController,
    storageRepository: StorageRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val startDestination = if (PermissionHelper.hasRequiredPermission(context)) {
        Routes.folder(storageRepository.rootPath)
    } else {
        Routes.PERMISSION
    }

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Routes.PERMISSION) {
            PermissionScreen(
                onPermissionGranted = {
                    navController.navigate(Routes.folder(storageRepository.rootPath)) {
                        popUpTo(Routes.PERMISSION) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Routes.FOLDER_PATTERN,
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path").orEmpty()
            FolderScreen(
                path = path,
                onOpenFolder = { childPath -> navController.navigate(Routes.folder(childPath)) },
                onBreadcrumbClick = { targetPath ->
                    navController.popBackStack(Routes.folder(targetPath), inclusive = false)
                },
                onOpenPreview = { previewPath -> navController.navigate(Routes.preview(previewPath)) }
            )
        }
        composable(
            route = Routes.PREVIEW_PATTERN,
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { backStackEntry ->
            val previewPath = backStackEntry.arguments?.getString("path").orEmpty()
            MediaPreviewScreen(
                path = previewPath,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.DUPLICATES) {
            DuplicateListScreen(
                onOpenGroup = { key -> navController.navigate(Routes.duplicateGroup(key)) }
            )
        }
        composable(
            route = Routes.DUPLICATE_GROUP_PATTERN,
            arguments = listOf(navArgument("key") { type = NavType.StringType })
        ) { backStackEntry ->
            val key = backStackEntry.arguments?.getString("key").orEmpty()
            DuplicateGroupDetailScreen(
                groupKey = key,
                onDone = { navController.popBackStack() }
            )
        }
    }
}
