package com.example.appinstallercheckerkt.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.appinstallercheckerkt.model.ItemFilterMode
import com.example.appinstallercheckerkt.model.SortAndFilterState
import com.example.appinstallercheckerkt.model.SortMode
import com.example.appinstallercheckerkt.ui.theme.AppInstallerCheckerKtTheme

@Composable
fun FullScreenDialog(
    title: String,
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .padding(top = 12.dp)
                        .height(56.dp)
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(4.dp),
                    ) {
                        Icon(Icons.Filled.Close, "Close")
                    }
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 56.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = 56.dp + 24.dp)
                        .fillMaxSize()
                ) {
                    Box(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun FilterDialog(
    filterObj: SortAndFilterState,
    onFilterObjChange: (SortAndFilterState) -> Unit,
    onDismiss: () -> Unit = {},
) {
    FullScreenDialog(
        title = "Filter and sort",
        onDismiss = onDismiss,
    ) {
        Column {
            Text("Sort by ...", style = MaterialTheme.typography.titleMedium)
            Column(Modifier.selectableGroup()) {
                SortMode.entries
                    .filter { it != SortMode.Unspecified }
                    .forEach {
                        ListItem(
                            headlineContent = {
                                Text(text = it.name)
                            },
                            leadingContent = {
                                RadioButton(
                                    selected = it == filterObj.sortMode,
                                    onClick = null,
                                )
                            },
                            modifier = Modifier.selectable(
                                selected = it == filterObj.sortMode,
                                onClick = { onFilterObjChange(filterObj.copy(sortMode = it)) },
                                role = Role.RadioButton,
                            )
                        )
                    }
            }

            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(text = "Filter by ...", style = MaterialTheme.typography.titleMedium)
            FilterItem("Exclude system package",
                { filterObj.systemPackageState },
                { onFilterObjChange(filterObj.copy(systemPackageState = it)) }
            )

            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(text = "Filter by installer ...", style = MaterialTheme.typography.titleMedium)
            FilterItem("Exclude Galaxy Store",
                { filterObj.packageFromSamsungStoreState },
                { onFilterObjChange(filterObj.copy(packageFromSamsungStoreState = it)) }
            )
            FilterItem("Exclude Play Store",
                { filterObj.packageFromPlayStoreState },
                { onFilterObjChange(filterObj.copy(packageFromPlayStoreState = it)) }
            )
        }
    }
}

@Composable
private inline fun FilterItem(
    text: String,
    crossinline getValue: () -> ItemFilterMode,
    crossinline setValue: (ItemFilterMode) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(text = text)
        },
        trailingContent = {
            Switch(
                checked = getValue() == ItemFilterMode.Exclude,
                onCheckedChange = {
                    setValue(
                        when {
                            it -> ItemFilterMode.Exclude
                            else -> ItemFilterMode.Unspecified
                        }
                    )
                },
            )
        }
    )
}

@Preview
@Composable
fun PreviewFullScreenDialog() {
    AppInstallerCheckerKtTheme {
        FilterDialog(SortAndFilterState.DEFAULT, {})
    }
}