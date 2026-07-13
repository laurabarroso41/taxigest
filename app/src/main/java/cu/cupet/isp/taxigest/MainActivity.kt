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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import cu.cupet.isp.taxigest.ui.Screen
import cu.cupet.isp.taxigest.ui.screens.*
import cu.cupet.isp.taxigest.ui.theme.TaxiGestTheme
import cu.cupet.isp.taxigest.ui.viewmodel.UserViewModel
import cu.cupet.isp.taxigest.ui.viewmodel.ViewModelFactory
import cu.cupet.isp.taxigest.data.TaxiGestDatabase
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaxiGestTheme {
                val context = LocalContext.current
                val database = remember { TaxiGestDatabase.getDatabase(context) }
                val userViewModel: UserViewModel = viewModel(
                    factory = ViewModelFactory(userDao = database.userDao())
                )
                val isAuthenticated by userViewModel.isAuthenticated.collectAsStateWithLifecycle()
                val currentUser by userViewModel.currentUser.collectAsStateWithLifecycle()

                if (isAuthenticated) {
                    TaxiGestApp(
                        userName = currentUser?.name ?: "",
                        onLogout = { userViewModel.logout() }
                    )
                } else {
                    AuthScreen()
                }
            }
        }
    }
}

@Composable
fun TaxiGestApp(userName: String, onLogout: () -> Unit) {
    val backStack = remember { mutableStateListOf<Any>(Screen.Home) }
    val currentScreen = backStack.lastOrNull() as? Screen ?: Screen.Home
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.Black,
                drawerContentColor = Color.White
            ) {
                Spacer(Modifier.height(48.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(24.dp))
                
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.nav_home)) },
                    selected = currentScreen == Screen.Home,
                    onClick = {
                        scope.launch { drawerState.close() }
                        backStack.clear()
                        backStack.add(Screen.Home)
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFFFFB800),
                        selectedTextColor = Color.Black,
                        unselectedTextColor = Color.White,
                        unselectedIconColor = Color.White
                    )
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.2f))
                Text(
                    text = stringResource(R.string.nav_reports),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                )

                val reportOptions = listOf(
                    R.string.report_gains_by_date to "gains_date",
                    R.string.report_active_taxis to "active_taxis",
                    R.string.report_gain_by_taxi to "gain_taxi",
                    R.string.report_client_count to "client_count",
                    R.string.report_total_gains to "total_gains",
                    R.string.report_passenger_count to "passenger_count"
                )

                reportOptions.forEach { (resId, type) ->
                    NavigationDrawerItem(
                        label = { Text(stringResource(resId)) },
                        selected = (currentScreen as? Screen.ReportDetail)?.type == type,
                        onClick = {
                            scope.launch { drawerState.close() }
                            backStack.add(Screen.ReportDetail(type))
                        },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFFFFB800),
                            selectedTextColor = Color.Black,
                            unselectedTextColor = Color.White,
                            unselectedIconColor = Color.White
                        )
                    )
                }

                Spacer(Modifier.weight(1f))
                
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.logout)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    icon = { Icon(Icons.Default.Logout, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = Color.White,
                        unselectedIconColor = Color.White
                    )
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    ) {
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
                        is Screen.Home -> NavEntry(key) { 
                            HomeScreen(
                                userName = userName, 
                                onNavigateToDailyTrips = { date -> backStack.add(Screen.DailyTrips(date)) },
                                onOpenMenu = { scope.launch { drawerState.open() } }
                            ) 
                        }
                        is Screen.Taxis -> NavEntry(key) { TaxisScreen() }
                        is Screen.Clients -> NavEntry(key) { 
                            ClientsScreen(onImportContacts = { backStack.add(Screen.ImportContacts) }) 
                        }
                        is Screen.Trips -> NavEntry(key) { TripsScreen() }
                        is Screen.Reports -> NavEntry(key) { ReportsScreen() }
                        is Screen.ImportContacts -> NavEntry(key) { ImportContactsScreen(onBack = { backStack.removeLastOrNull() }) }
                        is Screen.DailyTrips -> NavEntry(key) { DailyTripsScreen(date = key.date, onBack = { backStack.removeLastOrNull() }) }
                        is Screen.ReportDetail -> NavEntry(key) { ReportDetailScreen(type = key.type, onBack = { backStack.removeLastOrNull() }) }
                        else -> NavEntry(Unit) { Text(stringResource(R.string.unknown_route)) }
                    }
                }
            )

            // Custom Floating Navigation Bar
            if (currentScreen != Screen.ImportContacts && currentScreen !is Screen.DailyTrips) {
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
