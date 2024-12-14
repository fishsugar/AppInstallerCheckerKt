package com.example.appinstallercheckerkt.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun SettingsScreen() {
    var notificationsEnabled by remember { mutableStateOf(false) }
    var darkThemeEnabled by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        SettingItem(
            title = "Notifications",
            description = "Enable notifications",
            value = notificationsEnabled,
            onValueChanged = { notificationsEnabled = it }
        )
        SettingItem(
            title = "Dark Theme",
            description = "Enable dark theme",
            value = darkThemeEnabled,
            onValueChanged = { darkThemeEnabled = it }
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    value: Boolean,
    onValueChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = value,
            onCheckedChange = onValueChanged
        )
    }
}