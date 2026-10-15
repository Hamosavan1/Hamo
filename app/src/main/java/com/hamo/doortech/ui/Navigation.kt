package com.hamo.doortech.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hamo.doortech.AppContainer
import com.hamo.doortech.ui.components.LocalContainer
import com.hamo.doortech.ui.components.LocalMessenger
import com.hamo.doortech.ui.components.Messenger
import com.hamo.doortech.ui.screens.BackupScreen
import com.hamo.doortech.ui.screens.BrandsScreen
import com.hamo.doortech.ui.screens.CategoriesScreen
import com.hamo.doortech.ui.screens.FaultDetailScreen
import com.hamo.doortech.ui.screens.FaultEditScreen
import com.hamo.doortech.ui.screens.FaultStepsScreen
import com.hamo.doortech.ui.screens.FavoritesScreen
import com.hamo.doortech.ui.screens.GuideDetailScreen
import com.hamo.doortech.ui.screens.GuideEditScreen
import com.hamo.doortech.ui.screens.GuidesScreen
import com.hamo.doortech.ui.screens.HomeScreen
import com.hamo.doortech.ui.screens.JobDetailScreen
import com.hamo.doortech.ui.screens.JobEditScreen
import com.hamo.doortech.ui.screens.JobsScreen
import com.hamo.doortech.ui.screens.MediaLibraryScreen
import com.hamo.doortech.ui.screens.ModelDetailScreen
import com.hamo.doortech.ui.screens.ModelEditScreen
import com.hamo.doortech.ui.screens.ModelsScreen
import com.hamo.doortech.ui.screens.MoreScreen
import com.hamo.doortech.ui.screens.PartsScreen
import com.hamo.doortech.ui.screens.SearchScreen
import com.hamo.doortech.ui.screens.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val DIAGNOSE = "diagnose"
    const val JOBS = "jobs"
    const val MORE = "more"
    const val CATEGORIES = "categories"
    const val BRANDS = "brands"
    const val MODELS = "models"
    const val PARTS = "parts"
    const val FAVORITES = "favorites"
    const val MEDIA = "media"
    const val BACKUP = "backup"
    const val SETTINGS = "settings"

    const val MODEL = "model/{id}"
    const val MODEL_EDIT = "model_edit/{id}?brand={brand}"
    const val FAULT = "fault/{id}"
    const val FAULT_STEPS = "fault_steps/{id}"
    const val FAULT_EDIT = "fault_edit/{id}?model={model}"
    const val GUIDES = "guides/{type}"
    const val GUIDE = "guide/{id}"
    const val GUIDE_EDIT = "guide_edit/{id}?type={type}"
    const val JOB = "job/{id}"
    const val JOB_EDIT = "job_edit/{id}"

    fun model(id: Long) = "model/$id"
    fun modelEdit(id: Long, brand: Long = 0) = "model_edit/$id?brand=$brand"
    fun fault(id: Long) = "fault/$id"
    fun faultSteps(id: Long) = "fault_steps/$id"
    fun faultEdit(id: Long, model: Long = 0) = "fault_edit/$id?model=$model"
    fun guides(type: String) = "guides/$type"
    fun guide(id: Long) = "guide/$id"
    fun guideEdit(id: Long, type: String = "INSTALL") = "guide_edit/$id?type=$type"
    fun job(id: Long) = "job/$id"
    fun jobEdit(id: Long) = "job_edit/$id"
}

private data class TopItem(val route: String, val label: String, val icon: ImageVector)

private val topItems = listOf(
    TopItem(Routes.HOME, "الرئيسية", Icons.Default.Home),
    TopItem(Routes.SEARCH, "بحث", Icons.Default.Search),
    TopItem(Routes.DIAGNOSE, "تشخيص", Icons.Default.MedicalServices),
    TopItem(Routes.JOBS, "الصيانة", Icons.Default.Build),
    TopItem(Routes.MORE, "المزيد", Icons.Default.MoreHoriz),
)

private val idArg = listOf(navArgument("id") { type = NavType.LongType })

@Composable
fun HamoRoot(container: AppContainer) {
    val nav: NavHostController = rememberNavController()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val messenger = remember { Messenger(snack, scope) }
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    CompositionLocalProvider(LocalContainer provides container, LocalMessenger provides messenger) {
        Scaffold(
            snackbarHost = { SnackbarHost(snack) },
            bottomBar = {
                if (topItems.any { it.route == route }) {
                    NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                        topItems.forEach { item ->
                            NavigationBarItem(
                                selected = route == item.route,
                                onClick = {
                                    nav.navigate(item.route) {
                                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(item.icon, null) },
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = com.hamo.doortech.ui.theme.Navy,
                                    selectedTextColor = com.hamo.doortech.ui.theme.Navy,
                                    indicatorColor = com.hamo.doortech.ui.theme.OrangeSoft,
                                ),
                            )
                        }
                    }
                }
            },
        ) { inner ->
            NavHost(
                navController = nav,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(inner).consumeWindowInsets(inner),
            ) {
                composable(Routes.HOME) { HomeScreen(nav) }
                composable(Routes.SEARCH) { SearchScreen(nav, diagnose = false) }
                composable(Routes.DIAGNOSE) { SearchScreen(nav, diagnose = true) }
                composable(Routes.JOBS) { JobsScreen(nav) }
                composable(Routes.MORE) { MoreScreen(nav) }
                composable(Routes.CATEGORIES) { CategoriesScreen(nav) }
                composable(Routes.BRANDS) { BrandsScreen(nav) }
                composable(Routes.MODELS) { ModelsScreen(nav) }
                composable(Routes.PARTS) { PartsScreen(nav) }
                composable(Routes.FAVORITES) { FavoritesScreen(nav) }
                composable(Routes.MEDIA) { MediaLibraryScreen(nav) }
                composable(Routes.BACKUP) { BackupScreen(nav) }
                composable(Routes.SETTINGS) { SettingsScreen(nav) }

                composable(Routes.MODEL, idArg) { ModelDetailScreen(nav, it.arguments?.getLong("id") ?: 0L) }
                composable(
                    Routes.MODEL_EDIT,
                    listOf(
                        navArgument("id") { type = NavType.LongType },
                        navArgument("brand") { type = NavType.LongType; defaultValue = 0L },
                    ),
                ) { ModelEditScreen(nav, it.arguments?.getLong("id") ?: 0L, it.arguments?.getLong("brand") ?: 0L) }
                composable(Routes.FAULT, idArg) { FaultDetailScreen(nav, it.arguments?.getLong("id") ?: 0L) }
                composable(Routes.FAULT_STEPS, idArg) { FaultStepsScreen(nav, it.arguments?.getLong("id") ?: 0L) }
                composable(
                    Routes.FAULT_EDIT,
                    listOf(
                        navArgument("id") { type = NavType.LongType },
                        navArgument("model") { type = NavType.LongType; defaultValue = 0L },
                    ),
                ) { FaultEditScreen(nav, it.arguments?.getLong("id") ?: 0L, it.arguments?.getLong("model") ?: 0L) }
                composable(Routes.GUIDES, listOf(navArgument("type") { type = NavType.StringType })) {
                    GuidesScreen(nav, it.arguments?.getString("type") ?: "INSTALL")
                }
                composable(Routes.GUIDE, idArg) { GuideDetailScreen(nav, it.arguments?.getLong("id") ?: 0L) }
                composable(
                    Routes.GUIDE_EDIT,
                    listOf(
                        navArgument("id") { type = NavType.LongType },
                        navArgument("type") { type = NavType.StringType; defaultValue = "INSTALL" },
                    ),
                ) { GuideEditScreen(nav, it.arguments?.getLong("id") ?: 0L, it.arguments?.getString("type") ?: "INSTALL") }
                composable(Routes.JOB, idArg) { JobDetailScreen(nav, it.arguments?.getLong("id") ?: 0L) }
                composable(Routes.JOB_EDIT, idArg) { JobEditScreen(nav, it.arguments?.getLong("id") ?: 0L) }
            }
        }
    }
}
