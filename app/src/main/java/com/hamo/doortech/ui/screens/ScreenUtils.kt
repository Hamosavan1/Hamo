package com.hamo.doortech.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hamo.doortech.data.repo.Outcome

fun Outcome.messageOr(ok: String): String = when (this) {
    is Outcome.Fail -> message
    is Outcome.Ok -> message.ifBlank { ok }
}

/** حاوية قابلة للتمرير بحشوة وتباعد موحدين لشاشات التفاصيل والنماذج. */
@Composable
fun ScrollColumn(padding: PaddingValues, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) { content() }
}
