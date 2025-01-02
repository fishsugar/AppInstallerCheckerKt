package com.example.appinstallercheckerkt.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.ShopTwo
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appinstallercheckerkt.R
import com.example.appinstallercheckerkt.data.SettingsRepository
import com.example.appinstallercheckerkt.model.MockPackageViewObj
import com.example.appinstallercheckerkt.model.PackageViewObj
import com.example.appinstallercheckerkt.ui.theme.AppInstallerCheckerKtTheme
import com.example.appinstallercheckerkt.util.appDetailInfo
import com.example.appinstallercheckerkt.util.filterBySearchString
import com.example.appinstallercheckerkt.util.jumpToStore

@Composable
fun MainScreen(viewModel: PackageListViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MainScreen(viewModel, uiState)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MainScreen(
    viewModel: PackageListViewModel,
    uiState: PackageListUiState,
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    var searchActive by remember { mutableStateOf(false) }
    var searchString by remember { mutableStateOf("") }

    if (searchActive) {
        SearchView(
            searchString,
            onQueryChange = { searchString = it },
            onDismiss = { searchActive = false; searchString = "" },
        ) {
            if (searchString.isEmpty()) {
                // empty
            } else {
                val packagesToShow = remember(uiState, searchString) {
                    uiState.packagesFiltered.filterBySearchString(searchString)
                }

                if (!uiState.refreshing && packagesToShow.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "0 of ${uiState.packagesUnfiltered.size} package available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    PackageListView(
                        appItems = packagesToShow,
                        refreshing = uiState.refreshing,
                        refresh = viewModel::refresh
                    )
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.app_name))
                    },
                    actions = {
                        IconButton(onClick = { searchActive = true; searchString = "" }) {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(imageVector = Icons.Filled.FilterList, contentDescription = "Filter and sort")
                        }
                    },
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            Surface(Modifier.padding(innerPadding)) {
                if (uiState.refreshing && uiState.packagesUnfiltered.isEmpty()) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Text("Loading packages...")
                    }
                } else if (!uiState.refreshing && uiState.packagesUnfiltered.isEmpty()) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("No package available")
                        TextButton(onClick = viewModel::refresh) {
                            Text("Refresh")
                        }
                    }
                } else if (!uiState.refreshing && uiState.packagesFiltered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("0 of ${uiState.packagesUnfiltered.size} package available")
                    }
                } else {
                    PackageListView(
                        appItems = uiState.packagesFiltered,
                        refreshing = uiState.refreshing,
                        refresh = viewModel::refresh
                    )
                }
            }
        }
    }
    if (showFilterDialog) {
        FilterDialog(
            uiState.filterObj,
            viewModel::updateFilterObj,
            onDismiss = { showFilterDialog = false },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchView(
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    SearchBar(
        modifier = Modifier.focusRequester(focusRequester),
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {},
                expanded = true,
                onExpandedChange = {
                    if (!it) {
                        onDismiss()
                    }
                },
                leadingIcon = {
                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                trailingIcon = { Icon(Icons.Filled.Search, "Search") },
                placeholder = { Text("Search packages") },
            )
        },
        expanded = true,
        onExpandedChange = {
            if (!it) {
                onDismiss()
            }
       },
    ) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PackageItemRow(packageObj: PackageViewObj) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .combinedClickable(
                onClick = { jumpToStore(context, packageObj.packageName, packageObj.installingPackageName == null) },
                onLongClick = { jumpToStore(context, packageObj.packageName, false) },
            ),
    ) {
        PackageIcon(
            packageObj,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = packageObj.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = packageObj.packageName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            packageObj.installingPackageName?.let {
                Text(
                    text = "Installer: $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }

        Box {
            var expanded by remember { mutableStateOf(false) }
            IconButton(onClick = { expanded = true }, modifier = Modifier.size(20.dp)) {
                Icon(
                    Icons.Default.MoreVert, contentDescription = "More",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Open in Play Store") },
                    onClick = {
                        jumpToStore(context, packageObj.packageName, true)
                        expanded = false
                    },
                    leadingIcon = { Icon(Icons.Filled.Shop, contentDescription = null) },
                )
                DropdownMenuItem(
                    text = { Text("Open in store with ...") },
                    onClick = {
                        jumpToStore(context, packageObj.packageName, false)
                        expanded = false
                    },
                    leadingIcon = { Icon(Icons.Filled.ShopTwo, contentDescription = null) },
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Manage app") },
                    onClick = {
                        appDetailInfo(context, packageObj.packageName)
                        expanded = false
                    },
                    leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                )
            }
        }
    }
}

@Composable
private fun PackageIcon(packageObj: PackageViewObj, modifier: Modifier = Modifier) {
    if (packageObj.icon != null) {
        Image(
            bitmap = packageObj.icon!!,
            contentDescription = "",
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "",
            modifier = modifier
                .clip(RoundedCornerShape(5.dp))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackageListView(
    appItems: List<PackageViewObj>,
    refreshing: Boolean = false,
    refresh: () -> Unit = {}
) {
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = refresh,
    ) {
        LazyColumn {
            items(appItems, contentType = { it.javaClass }) { item ->
                PackageItemRow(item)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun createPreviewViewModel(): PackageListViewModel {
    val context = LocalContext.current
    val settingsRepository = SettingsRepository(context)
    return PackageListViewModel(settingsRepository, context)
}

@Composable
private fun createPreviewData(): List<MockPackageViewObj> = listOf(
    MockPackageViewObj("StoreAppChecker", "com.sample.package", "com.sample.store"),
    MockPackageViewObj("Package2", "com.package2"),
    MockPackageViewObj("Very long title (very very very very very very very long)", "com.package2"),
)

@PreviewLightDark
@Composable
fun MainScreenPreview() {
    AppInstallerCheckerKtTheme {
        val packages = createPreviewData()
        MainScreen(
            viewModel = createPreviewViewModel(), uiState = PackageListUiState(
                packagesUnfiltered = packages,
                packagesFiltered = packages,
            )
        )
    }
}

@PreviewLightDark
@Composable
fun NoDataPreview() {
    AppInstallerCheckerKtTheme {
        MainScreen(
            viewModel = createPreviewViewModel(), uiState = PackageListUiState(
                packagesUnfiltered = emptyList(),
                packagesFiltered = emptyList(),
            )
        )
    }
}