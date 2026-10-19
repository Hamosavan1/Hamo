package com.hamo.doortech.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hamo.doortech.data.db.MediaAttachment
import com.hamo.doortech.data.db.MediaKind
import com.hamo.doortech.data.db.MediaRole
import com.hamo.doortech.data.db.OwnerType
import com.hamo.doortech.data.repo.Outcome
import com.hamo.doortech.ui.MediaViewModel
import com.hamo.doortech.ui.VmFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** يحمّل صورة مصغّرة من ملف بدون مكتبات خارجية. */
@Composable
fun rememberImage(file: File, maxPx: Int): ImageBitmap? {
    val state = produceState<ImageBitmap?>(null, file.path, maxPx) {
        value = withContext(Dispatchers.IO) {
            try {
                if (!file.exists()) return@withContext null
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.path, bounds)
                if (bounds.outWidth <= 0) return@withContext null
                var sample = 1
                while (bounds.outWidth / (sample * 2) >= maxPx && bounds.outHeight / (sample * 2) >= maxPx) sample *= 2
                BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
            } catch (e: Exception) {
                null
            } catch (e: OutOfMemoryError) {
                null
            }
        }
    }
    return state.value
}

private fun openFile(context: Context, container: com.hamo.doortech.AppContainer, m: MediaAttachment, onFail: (String) -> Unit) {
    try {
        val intent = if (m.kind == MediaKind.LINK) {
            Intent(Intent.ACTION_VIEW, Uri.parse(m.path))
        } else {
            val file = container.mediaStore.fileFor(m.path)
            if (!file.exists()) { onFail("الملف غير موجود في التخزين"); return }
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(container.mediaStore.uriFor(file), container.mediaStore.mimeFor(m.path))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        onFail("لا يوجد تطبيق على الجهاز لفتح هذا المرفق")
    } catch (e: Exception) {
        onFail("تعذّر فتح المرفق")
    }
}

/**
 * قسم المرفقات: التقاط صورة، اختيار من المعرض (Photo Picker)، إرفاق مستند، إضافة رابط فيديو.
 * في زيارات الصيانة تُصنَّف الصور «قبل/بعد».
 */
@Composable
/** readOnly: عرض فقط (للأدلة داخل صفحات البرمجة/التوصيلات) — بدون أزرار إضافة أو حذف، والصور بعرض كامل. */
fun MediaSection(ownerType: String, ownerId: Long, modifier: Modifier = Modifier, readOnly: Boolean = false) {
    val container = LocalContainer.current
    val messenger = LocalMessenger.current
    val context = LocalContext.current
    val vm: MediaViewModel = viewModel(
        key = "media_${ownerType}_$ownerId",
        factory = VmFactory { MediaViewModel(container.repo, ownerType, ownerId) },
    )
    val state by vm.media.collectAsStateWithLifecycle()

    var pendingRole by rememberSaveable { mutableStateOf(MediaRole.NONE) }
    var pendingCameraPath by rememberSaveable { mutableStateOf<String?>(null) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var viewing by remember { mutableStateOf<MediaAttachment?>(null) }
    var deleting by remember { mutableStateOf<MediaAttachment?>(null) }

    fun report(r: Outcome, okMsg: String) {
        messenger.show(if (r is Outcome.Fail) r.message else okMsg)
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.addImage(uri, pendingRole) { report(it, "تمت إضافة الصورة") }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val path = pendingCameraPath
        pendingCameraPath = null
        if (path != null) {
            val f = File(path)
            if (ok) vm.addCamera(f, pendingRole) { report(it, "تمت إضافة الصورة") } else f.delete()
        }
    }
    val docPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.addDocument(uri) { report(it, "تمت إضافة المستند") }
    }

    fun launchCamera(role: String) {
        pendingRole = role
        val f = container.mediaStore.newCameraFile()
        pendingCameraPath = f.absolutePath
        try {
            camera.launch(container.mediaStore.uriFor(f))
        } catch (e: Exception) {
            pendingCameraPath = null
            messenger.show("الكاميرا غير متاحة على هذا الجهاز")
        }
    }

    fun launchPicker(role: String) {
        pendingRole = role
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    if (readOnly) {
        val list = (state as? com.hamo.doortech.ui.UiState.Success)?.value.orEmpty()
        if (list.isNotEmpty()) Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            list.filter { it.kind == MediaKind.IMAGE }.forEach { m ->
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).clickable { viewing = m }.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Thumbnail(container.mediaStore.fileFor(m.path), Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(12.dp)))
                    if (m.title.isNotBlank()) Text(m.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            list.filter { it.kind != MediaKind.IMAGE }.forEach { m ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White)
                        .clickable { openFile(context, container, m) { messenger.show(it) } }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(if (m.kind == MediaKind.LINK) Icons.Default.Link else Icons.Default.AttachFile, null, tint = MaterialTheme.colorScheme.primary)
                    Text(m.title.ifBlank { m.path }, Modifier.weight(1f), maxLines = 1)
                }
            }
        }
    } else SectionCard("الصور والمرفقات", modifier) {
        val isJob = ownerType == OwnerType.JOB
        if (isJob) {
            Text("قبل الإصلاح", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton("التقاط", { launchCamera(MediaRole.BEFORE) }, Modifier.weight(1f))
                SecondaryButton("من المعرض", { launchPicker(MediaRole.BEFORE) }, Modifier.weight(1f))
            }
            Text("بعد الإصلاح", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton("التقاط", { launchCamera(MediaRole.AFTER) }, Modifier.weight(1f))
                SecondaryButton("من المعرض", { launchPicker(MediaRole.AFTER) }, Modifier.weight(1f))
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton("التقاط صورة", { launchCamera(MediaRole.NONE) }, Modifier.weight(1f))
                SecondaryButton("من المعرض", { launchPicker(MediaRole.NONE) }, Modifier.weight(1f))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton("إرفاق مستند", { docPicker.launch(arrayOf("*/*")) }, Modifier.weight(1f))
            SecondaryButton("رابط فيديو", { showLinkDialog = true }, Modifier.weight(1f))
        }

        when (val s = state) {
            is com.hamo.doortech.ui.UiState.Loading -> Text("جارٍ التحميل...", style = MaterialTheme.typography.bodyMedium)
            is com.hamo.doortech.ui.UiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
            is com.hamo.doortech.ui.UiState.Success -> {
                val list = s.value
                if (list.isEmpty()) {
                    Text("لا توجد مرفقات بعد", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val images = list.filter { it.kind == MediaKind.IMAGE }
                    val others = list.filter { it.kind != MediaKind.IMAGE }
                    if (images.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(images, key = { it.id }) { m ->
                                Box {
                                    Thumbnail(container.mediaStore.fileFor(m.path), Modifier.size(170.dp).clip(RoundedCornerShape(10.dp)).clickable { viewing = m })
                                    if (m.role != MediaRole.NONE) {
                                        Chip(MediaRole.label(m.role), Color(0xFF0B1F3A), Color.White)
                                    }
                                    IconButton(onClick = { deleting = m }, Modifier.align(Alignment.BottomEnd).size(32.dp)) {
                                        Icon(Icons.Default.Delete, "حذف", tint = Color.White, modifier = Modifier.background(Color(0x99000000), RoundedCornerShape(50)).padding(4.dp))
                                    }
                                }
                            }
                        }
                    }
                    others.forEach { m ->
                        Row(
                            Modifier.fillMaxWidth().clickable { openFile(context, container, m) { messenger.show(it) } }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(if (m.kind == MediaKind.LINK) Icons.Default.Link else Icons.Default.AttachFile, null, tint = MaterialTheme.colorScheme.primary)
                            Column(Modifier.weight(1f)) {
                                Text(m.title.ifBlank { m.path }, maxLines = 1)
                                if (m.kind == MediaKind.LINK) Text(m.path, maxLines = 1, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { deleting = m }) { Icon(Icons.Default.Delete, "حذف") }
                        }
                    }
                }
            }
        }
    }

    if (showLinkDialog) {
        var url by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = { Text("إضافة رابط فيديو") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormField("العنوان (اختياري)", title, { title = it })
                    FormField("الرابط", url, { url = it; error = null }, error = error, placeholder = "https://")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.addLink(url, title) { r ->
                        if (r is Outcome.Fail) error = r.message else { showLinkDialog = false; messenger.show("تمت إضافة الرابط") }
                    }
                }) { Text("إضافة") }
            },
            dismissButton = { TextButton(onClick = { showLinkDialog = false }) { Text("إلغاء") } },
        )
    }

    deleting?.let { m ->
        ConfirmDialog(
            title = "حذف المرفق", text = "سيُحذف المرفق نهائيًا من التطبيق. هل تريد المتابعة؟",
            confirmLabel = "حذف", danger = true,
            onConfirm = { vm.delete(m) { report(it, "تم حذف المرفق") }; deleting = null },
            onDismiss = { deleting = null },
        )
    }

    viewing?.let { m ->
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewing = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false),
        ) {
            ZoomableImage(container.mediaStore.fileFor(m.path), m.title) { viewing = null }
        }
    }
}

/** عارض ملء الشاشة: تكبير بالقرص (حتى 8×)، تحريك بالسحب، ضغطتان للتكبير/الرجوع. */
@Composable
fun ZoomableImage(file: File, title: String, onClose: () -> Unit) {
    val img = rememberImage(file, 4096)
    var scale by androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (img != null) {
            Image(
                bitmap = img, contentDescription = title, contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .androidx_pointer(
                        onTransform = { centroidShift, zoom ->
                            val ns = (scale * zoom).coerceIn(1f, 8f)
                            scale = ns
                            offset = if (ns <= 1f) androidx.compose.ui.geometry.Offset.Zero else offset + centroidShift
                        },
                        onDoubleTap = {
                            if (scale > 1f) { scale = 1f; offset = androidx.compose.ui.geometry.Offset.Zero } else scale = 3f
                        },
                    )
                    .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y),
            )
        } else {
            Text("تعذّر عرض الصورة", color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
        Row(Modifier.fillMaxWidth().align(Alignment.TopCenter).background(Color(0x99000000)).padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, "إغلاق", tint = Color.White) }
            Text(if (title.isBlank()) "اقرص للتكبير • اضغط مرتين للتكبير السريع" else title, color = Color.White, maxLines = 1)
        }
    }
}

private fun Modifier.androidx_pointer(
    onTransform: (androidx.compose.ui.geometry.Offset, Float) -> Unit,
    onDoubleTap: () -> Unit,
): Modifier = this
    .pointerInput(Unit) { detectTapGestures(onDoubleTap = { onDoubleTap() }) }
    .pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> onTransform(pan, zoom) } }

@Composable
fun Thumbnail(file: File, modifier: Modifier = Modifier, maxPx: Int = 300, fit: ContentScale = ContentScale.Crop) {
    val img = rememberImage(file, maxPx)
    if (img != null) {
        Image(bitmap = img, contentDescription = null, modifier = modifier, contentScale = fit)
    } else {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.PhotoLibrary, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
