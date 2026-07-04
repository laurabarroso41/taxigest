package cu.cupet.isp.taxigest.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cu.cupet.isp.taxigest.R
import cu.cupet.isp.taxigest.data.TaxiGestDatabase
import cu.cupet.isp.taxigest.data.model.TripWithDetails
import cu.cupet.isp.taxigest.ui.components.TaxiGestCard
import cu.cupet.isp.taxigest.ui.viewmodel.TripViewModel
import cu.cupet.isp.taxigest.ui.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(onNavigateToDailyTrips: (Long) -> Unit) {
    val context = LocalContext.current
    val database = remember { TaxiGestDatabase.getDatabase(context) }
    val viewModel: TripViewModel = viewModel(
        factory = ViewModelFactory(
            tripDao = database.tripDao(),
            taxiDao = database.taxiDao(),
            clientDao = database.clientDao()
        )
    )

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val tomorrow = today + (24 * 60 * 60 * 1000)

    val todayTrips by viewModel.getTripsWithDetailsByDate(today, tomorrow - 1)
        .collectAsStateWithLifecycle(emptyList())

    val totalRevenue = todayTrips.sumOf { it.trip.totalPrice }
    val tripCount = todayTrips.size
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.hello_user, "Martin"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.start_journey),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            IconButton(
                onClick = { },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search Bar
        var searchQuery by remember { mutableStateOf("") }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.which_classic), color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Categories (Provinces Filter)
        val todayProvinces = todayTrips.map { it.trip.province }.distinct().sorted()
        val categories = listOf(stringResource(R.string.cat_all)) + todayProvinces
        var selectedProvince by remember { mutableStateOf("Todos") }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories) { category ->
                val isSelected = category == selectedProvince
                Surface(
                    modifier = Modifier.clickable { selectedProvince = category },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Filtering logic
        val filteredTrips = todayTrips.filter { tripWithDetails ->
            val matchesSearch = if (searchQuery.isEmpty()) {
                true
            } else {
                tripWithDetails.taxi.name.contains(searchQuery, ignoreCase = true) ||
                        tripWithDetails.clients.any {
                            it.name.contains(searchQuery, ignoreCase = true) || it.surnames.contains(searchQuery, ignoreCase = true)
                        }
            }
            
            val matchesProvince = if (selectedProvince == "Todos") {
                true
            } else {
                tripWithDetails.trip.province == selectedProvince
            }
            
            matchesSearch && matchesProvince
        }

        // Featured Car Card (Resumen Diario)
        DailySummaryCard(
            dateStr = dateFormat.format(Date(today)),
            totalRevenue = totalRevenue,
            tripCount = tripCount,
            onClick = { onNavigateToDailyTrips(today) }
        )

        if (filteredTrips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (searchQuery.isEmpty()) "Taxis en viaje hoy" else "Resultados de búsqueda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            filteredTrips.forEach { tripWithDetails ->
                ActiveTaxiItem(tripWithDetails = tripWithDetails)
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else if (searchQuery.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "No se encontraron viajes para \"$searchQuery\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun ActiveTaxiItem(tripWithDetails: TripWithDetails) {
    val context = LocalContext.current
    val taxi = tripWithDetails.taxi
    val trip = tripWithDetails.trip

    TaxiGestCard(containerColor = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_directions),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = taxi.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${stringResource(R.string.price_format, trip.totalPrice)} • ${trip.passengersCount} pasajeros",
                    color = Color.Gray, 
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = {
                if (!taxi.cellphone.isNullOrBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${taxi.cellphone}"))
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Este taxi no tiene número configurado", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Llamar",
                    tint = Color(0xFFFFB800) // RideYellow
                )
            }
        }
    }
}

@Composable
fun DailySummaryCard(
    dateStr: String,
    totalRevenue: Double,
    tripCount: Int,
    onClick: () -> Unit
) {
    TaxiGestCard(
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.height(220.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${stringResource(R.string.price_format, totalRevenue)} • ★ ${tripCount} viajes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Ver Detalles", color = Color.White)
                }
            }
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_today),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 20.dp),
                tint = Color.Black.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun CarListItem(name: String, price: String, rating: String) {
    TaxiGestCard(containerColor = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${stringResource(R.string.price_per_hour, price)} • ${stringResource(R.string.rating_format, rating)}",
                    color = Color.Gray, 
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}
