package com.example.appinstallercheckerkt.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import com.example.appinstallercheckerkt.model.ItemFilterMode
import com.example.appinstallercheckerkt.model.PackageViewObj
import com.example.appinstallercheckerkt.model.SortAndFilterState
import com.example.appinstallercheckerkt.model.SortMode
import java.text.Collator

private const val TAG = "PackageHelper"

fun fetchPackages(context: Context): List<PackageInfo> {
    val pm: PackageManager = context.packageManager
    return pm.getInstalledPackages(0)
}

fun jumpToStore(context: Context, packageName: String, playStoreOnly: Boolean) {
    val uriBuilder = Uri.parse("market://details")
        .buildUpon()
        .appendQueryParameter("id", packageName)

    val intent = Intent(Intent.ACTION_VIEW, uriBuilder.build())
    if (playStoreOnly) {
        intent.setPackage("com.android.vending")
    }
    context.startActivity(intent)
}

fun appDetailInfo(context: Context, packageName: String) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.setData(Uri.fromParts("package", packageName, null))
    context.startActivity(intent)
}

fun SortAndFilterState.doFilter(packagesUnfiltered: List<PackageViewObj>): List<PackageViewObj> {
    var packages = packagesUnfiltered.asSequence()

    // filtering by switches
    if (systemPackageState == ItemFilterMode.Exclude) {
        packages = packages.filter { it.isSystemPackage != true }
    }

    if (packageFromSamsungStoreState == ItemFilterMode.Exclude) {
        packages = packages.filter { it.installingPackageName != "com.sec.android.app.samsungapps" }
    }

    if (packageFromPlayStoreState == ItemFilterMode.Exclude) {
        packages = packages.filter { it.installingPackageName != "com.android.vending" }
    }

    // sort
    packages = when (sortMode) {
        SortMode.Unspecified -> packages
        SortMode.PackageName -> packages.sortedBy { it.packageName }
        SortMode.DisplayName -> {
            val comparator = Collator.getInstance()
            packages.sortedWith { a, b -> comparator.compare(a.name, b.name) }
        }
    }
    val result = packages.toList()
    Log.d(TAG, "doFilter: ${packagesUnfiltered.size} -> ${result.size} packages")
    return result
}
