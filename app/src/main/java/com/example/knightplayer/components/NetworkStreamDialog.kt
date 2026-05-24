package com.example.knightplayer.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun NetworkStreamDialog(
    onDismiss: () -> Unit,
    onPlay: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        KnightGlassCard(cornerRadius = 24.dp) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Network Stream", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Paste URL (.mp4, .m3u8)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { if(url.isNotBlank()) onPlay(url) }) { Text("Play") }
                }
            }
        }
    }
}
