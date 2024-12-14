package com.example.appinstallercheckerkt.model

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap


interface PackageViewObj {
    val name: String
    val packageName: String
    val icon: ImageBitmap?
    val installingPackageName: String?
    val isSystemPackage: Boolean?
}

class PackageViewObjImpl(
    val packageInfo: PackageInfo,
    private val pm: PackageManager
) : PackageViewObj {
    override val name: String by lazy {
        packageInfo.applicationInfo?.loadLabel(pm).toString()
    }
    override val packageName: String by lazy {
        packageInfo.packageName
    }
    override val icon: ImageBitmap? by lazy {
        packageInfo.applicationInfo?.loadIcon(pm)?.toBitmap()?.asImageBitmap()
    }
    override val installingPackageName: String? by lazy {
        pm.getInstallSourceInfo(packageInfo.packageName).installingPackageName
    }
    override val isSystemPackage: Boolean? by lazy {
        packageInfo.applicationInfo?.let { (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0 }
    }
}

class MockPackageViewObj(
    override var name: String,
    override var packageName: String,
    override val installingPackageName: String? = null,
    override val icon: ImageBitmap? = null,
    override val isSystemPackage: Boolean = false,
) : PackageViewObj
