package com.example.appinstallercheckerkt.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appinstallercheckerkt.data.SettingsRepository
import com.example.appinstallercheckerkt.model.PackageViewObj
import com.example.appinstallercheckerkt.model.PackageViewObjImpl
import com.example.appinstallercheckerkt.model.SortAndFilterState
import com.example.appinstallercheckerkt.util.doFilter
import com.example.appinstallercheckerkt.util.fetchPackages
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

data class PackageListUiState(
    val filterObj: SortAndFilterState = SortAndFilterState.DEFAULT,
    val packagesUnfiltered: List<PackageViewObj> = emptyList(),
    val packagesFiltered: List<PackageViewObj> = emptyList(),
    val refreshing: Boolean = false,
) {
    companion object {
        val LOADING = PackageListUiState(refreshing = true)
    }
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class PackageListViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val filterObjFlow = settingsRepository.filterObjFlow.stateIn(viewModelScope, SharingStarted.Eagerly, SortAndFilterState.DEFAULT)

    private val refreshRequestVersion = MutableStateFlow<Int>(0)
    private var _filterRequestSubVersion = AtomicInteger(0)

    val uiState: StateFlow<PackageListUiState>


    init {

        // unfiltered packages flow
        val packagesUnfilteredVersioned = refreshRequestVersion
            .mapLatest { version ->
                val packages = withContext(Dispatchers.IO) {
                    fetchPackages(context).map {
                        PackageViewObjImpl(it, context.packageManager)
                    }
                }
                Pair(packages, version)
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, Pair(emptyList(), 0))

        // filtering flows
        data class FilteringVersion(
            val refreshReqVersion: Int,
            val subVersion: Int,
        )
        val filterRequestsFlow =
            combine(packagesUnfilteredVersioned, filterObjFlow) { (packages, refreshReqVersion), filterObj ->
                val resultVersion = FilteringVersion(refreshReqVersion, _filterRequestSubVersion.incrementAndGet())
                Triple(packages, resultVersion, filterObj)
            }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)
        val filterRequestVersion = filterRequestsFlow
            .map { (_, version, _) -> version }
            .stateIn(viewModelScope, SharingStarted.Lazily, FilteringVersion(0, 0))
        val packagesFilteredVersioned = filterRequestsFlow
            .map { (packages, filterReqVersion, filterObj) ->
                val packages = withContext(Dispatchers.IO) {
                    filterObj.doFilter(packages)
                }
                Pair(packages, filterReqVersion)
            }.stateIn(viewModelScope, SharingStarted.Lazily, Pair(emptyList(), FilteringVersion(0, 0)))

        // derived data
        val refreshing =
            combine(
                refreshRequestVersion,
                filterRequestVersion, packagesFilteredVersioned,
            ) { refreshReqVersion, filteringReqVersion, (_, resultVersion) ->
                !(refreshReqVersion != 0 && resultVersion.refreshReqVersion == refreshReqVersion && resultVersion == filteringReqVersion)
            }.stateIn(viewModelScope, SharingStarted.Lazily, true)
        uiState =
            combine(
                filterObjFlow,
                packagesUnfilteredVersioned,
                packagesFilteredVersioned,
                refreshing,
            ) { filterObj, (packagesUnfiltered, _), (packagesFiltered, resultVersion), refreshing ->
                PackageListUiState(
                    filterObj = filterObj,
                    packagesUnfiltered = packagesUnfiltered,
                    packagesFiltered = packagesFiltered,
                    refreshing = refreshing,
                )
            }.stateIn(viewModelScope, started = SharingStarted.Lazily, PackageListUiState.LOADING)

        refresh()
    }

    fun refresh() {
        refreshRequestVersion.update { it + 1 }
    }

    fun updateFilterObj(newValue: SortAndFilterState) {
        viewModelScope.launch {
            settingsRepository.saveFilterObj(newValue)
        }
    }
}