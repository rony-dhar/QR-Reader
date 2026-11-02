package com.qrreader.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

fun kindIcon(kind: String): ImageVector = when (kind) {
    "Link" -> Icons.Rounded.Link
    "Wi-Fi" -> Icons.Rounded.Wifi
    "Email" -> Icons.Rounded.Email
    "Phone" -> Icons.Rounded.Phone
    "SMS" -> Icons.Rounded.Sms
    "Contact" -> Icons.Rounded.Person
    "Location" -> Icons.Rounded.Place
    "Product" -> Icons.Rounded.ShoppingCart
    else -> Icons.Rounded.TextFields
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultDialog(
    record: ScanRecord,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val wifi = remember(record.content) { QrUtils.parseWifi(record.content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(record.kind) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "${record.format} · ${formatDate(record.timestamp)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (wifi != null) {
                    LabeledValue("Network", wifi["S"].orEmpty())
                    val password = wifi["P"].orEmpty()
                    if (password.isNotEmpty()) LabeledValue("Password", password)
                    val security = wifi["T"].orEmpty().ifEmpty { "None" }
                    LabeledValue("Security", if (security.equals("nopass", true)) "None" else security)
                } else {
                    SelectionContainer {
                        Text(record.content, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (record.kind == "Link") {
                        AssistChip(
                            onClick = { openLink(context, record.content.trim()) },
                            label = { Text("Open") },
                            leadingIcon = { Icon(Icons.Rounded.OpenInBrowser, null, Modifier.size(18.dp)) },
                        )
                    }
                    val copyValue = if (wifi != null) wifi["P"].orEmpty() else record.content
                    if (copyValue.isNotEmpty()) {
                        AssistChip(
                            onClick = { copyToClipboard(context, copyValue) },
                            label = { Text(if (wifi != null) "Copy password" else "Copy") },
                            leadingIcon = { Icon(Icons.Rounded.ContentCopy, null, Modifier.size(18.dp)) },
                        )
                    }
                    AssistChip(
                        onClick = { shareText(context, record.content) },
                        label = { Text("Share") },
                        leadingIcon = { Icon(Icons.Rounded.Share, null, Modifier.size(18.dp)) },
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = {
            if (onDelete != null) {
                TextButton(onClick = {
                    onDelete()
                    onDismiss()
                }) { Text("Delete") }
            }
        },
    )
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SelectionContainer {
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
