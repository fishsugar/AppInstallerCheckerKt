package com.example.appinstallercheckerkt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.appinstallercheckerkt.ui.MainScreen
import com.example.appinstallercheckerkt.ui.PackageListViewModel
import com.example.appinstallercheckerkt.ui.theme.AppInstallerCheckerKtTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val packageListViewModel: PackageListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppInstallerCheckerKtTheme {
                MainScreen(viewModel = packageListViewModel)
            }
        }
    }
}
