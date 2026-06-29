package cu.cupet.isp.taxigest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import cu.cupet.isp.taxigest.ui.Screen
import cu.cupet.isp.taxigest.ui.screens.*
import cu.cupet.isp.taxigest.ui.theme.TaxiGestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaxiGestTheme {
                TaxiGestApp()
            }
        }
    }
}

@Composable
fun TaxiGestApp() {
    val backStack = remember { mutableStateListOf<Any>(Screen.Home) }
    val currentScreen = backStack.lastOrNull() as? Screen ?: Screen.Home

    Box(modifier = Modifier.fillMaxSize()) {
        BackHandler(enabled = backStack.size > 1) {
            backStack.removeLastOrNull()
        }

        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { key ->
                when (key) {
                    is Screen.Home -> NavEntry(key) { HomeScreen() }
                    is Screen.Taxis -> NavEntry(key) { TaxisScreen() }
                    is Screen.Clients -> NavEntry(key) { 
                        ClientsScreen(onImportContacts = { backStack.add(Screen.ImportContacts) }) 
                    }
                    is Screen.Trips -> NavEntry(key) { TripsScreen() }
                    is Screen.Reports -> NavEntry(key) { ReportsScreen() }
                    is Screen.ImportContacts -> NavEntry(key) { ImportContactsScreen(onBack = { backStack.removeLastOrNull() }) }
                    else -> NavEntry(Unit) { Text(stringResource(R.string.unknown_route)) }
                }
            }
        )

        // Custom Floating Navigation Bar
        if (currentScreen != Screen.ImportContacts) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding() // Respeta el espacio de la barra de navegación del sistema
                    .padding(bottom = 24.dp) // Margen adicional por encima del área segura
                    .height(72.dp)
                    .width(300.dp),
                shape = CircleShape,
                color = Color.Black,
                contentColor = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavIcon(Icons.Default.Home, currentScreen == Screen.Home) {
                        backStack.clear()
                        backStack.add(Screen.Home)
                    }
                    NavIcon(Icons.Default.LocalTaxi, currentScreen == Screen.Taxis) {
                        if (currentScreen != Screen.Taxis) backStack.add(Screen.Taxis)
                    }
                    NavIcon(Icons.Default.Person, currentScreen == Screen.Clients) {
                        if (currentScreen != Screen.Clients) backStack.add(Screen.Clients)
                    }
                    NavIcon(Icons.Default.Route, currentScreen == Screen.Trips) {
                        if (currentScreen != Screen.Trips) backStack.add(Screen.Trips)
                    }
                }
            }
        }
    }
}

@Composable
fun NavIcon(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color(0xFFFFB800) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.Black else Color.White
        )
    }
}
