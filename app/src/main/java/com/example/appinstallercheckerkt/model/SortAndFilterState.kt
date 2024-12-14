package com.example.appinstallercheckerkt.model

import kotlinx.serialization.Serializable

enum class SortMode {
    Unspecified,
    DisplayName,
    PackageName,
}

enum class ItemFilterMode {
    Unspecified,
    Exclude,
}

@Serializable
data class SortAndFilterState(
    val sortMode: SortMode = SortMode.DisplayName,
    val systemPackageState: ItemFilterMode = ItemFilterMode.Exclude,
    val packageFromSamsungStoreState: ItemFilterMode = ItemFilterMode.Unspecified,
    val packageFromPlayStoreState: ItemFilterMode = ItemFilterMode.Unspecified,
) {
    companion object {
        val DEFAULT = SortAndFilterState()
    }
}
