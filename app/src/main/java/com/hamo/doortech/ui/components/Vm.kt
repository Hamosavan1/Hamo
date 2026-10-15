package com.hamo.doortech.ui.components

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.hamo.doortech.ui.VmFactory

/** ينشئ ViewModel بمصنع بسيط ويربطه بالشاشة الحالية. */
@Composable
inline fun <reified VM : ViewModel> hamoVm(key: String? = null, noinline make: () -> VM): VM =
    viewModel(key = key, factory = VmFactory(make))

/** تنقل لوجهة رئيسية (شريط سفلي) مع حفظ الحالة. */
fun NavController.goTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
