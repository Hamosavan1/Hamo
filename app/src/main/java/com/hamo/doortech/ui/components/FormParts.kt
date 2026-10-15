package com.hamo.doortech.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hamo.doortech.data.db.Verification
import com.hamo.doortech.util.Formatting
import java.util.Calendar
import java.util.TimeZone

/** محرر قائمة نصوص (أعراض، خطوات...) بإضافة وحذف. */
@Composable
fun StringListEditor(
    title: String,
    items: SnapshotStateList<String>,
    addLabel: String,
    itemLabel: String,
    multiline: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        items.forEachIndexed { index, value ->
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FormField(
                    label = "$itemLabel ${index + 1}",
                    value = value,
                    onChange = { items[index] = it },
                    modifier = Modifier.weight(1f),
                    singleLine = !multiline,
                    minLines = if (multiline) 2 else 1,
                )
                IconButton(onClick = { items.removeAt(index) }) { Icon(Icons.Default.Delete, "حذف") }
            }
        }
        SecondaryButton(addLabel, { items.add("") }, Modifier.fillMaxWidth())
    }
}

@Composable
fun LevelDropdown(level: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    DropdownField(
        label = "درجة التوثيق",
        options = Verification.all.map { it to Verification.label(it) },
        selected = level,
        onSelect = onChange,
        modifier = modifier,
    )
}

@Composable
fun AddRow(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Icon(Icons.Default.Add, null)
        Text(label, modifier = Modifier.padding(start = 6.dp))
    }
}

private fun localToPickerUtc(local: Long): Long {
    val c = Calendar.getInstance().apply { timeInMillis = local }
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear(); set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
    }
    return u.timeInMillis
}

private fun pickerUtcToLocalNoon(utc: Long): Long {
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utc }
    return Calendar.getInstance().apply {
        clear(); set(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH), 12, 0, 0)
    }.timeInMillis
}

/** حقل تاريخ يفتح منتقي التاريخ. [allowClear] يسمح بإفراغ القيمة (اختياري). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    label: String,
    value: Long?,
    onChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    allowClear: Boolean = false,
    error: String? = null,
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value?.let { Formatting.date(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            placeholder = { Text("اختر التاريخ") },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
            trailingIcon = {
                if (allowClear && value != null) {
                    IconButton(onClick = { onChange(null) }) { Icon(Icons.Default.Close, "مسح") }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.matchParentSize().clickable { open = true })
    }
    if (open) {
        val state = rememberDatePickerState(initialSelectedDateMillis = localToPickerUtc(value ?: System.currentTimeMillis()))
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onChange(pickerUtcToLocalNoon(it)) }
                    open = false
                }) { Text("تأكيد") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("إلغاء") } },
        ) { DatePicker(state = state) }
    }
}
