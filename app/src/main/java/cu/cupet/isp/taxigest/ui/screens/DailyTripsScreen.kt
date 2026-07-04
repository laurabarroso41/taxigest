package cu.cupet.isp.taxigest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cu.cupet.isp.taxigest.R
import cu.cupet.isp.taxigest.data.TaxiGestDatabase
import cu.cupet.isp.taxigest.data.model.TripWithDetails
import cu.cupet.isp.taxigest.ui.components.SectionHeader
import cu.cupet.isp.taxigest.ui.viewmodel.TripViewModel
import cu.cupet.isp.taxigest.ui.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTripsScreen(date: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { TaxiGestDatabase.getDatabase(context) }
    val viewModel: TripViewModel = viewModel(
        factory = ViewModelFactory(
            tripDao = database.tripDao(),
            taxiDao = database.taxiDao(),
            clientDao = database.clientDao()
        )
    )

    val calendar = Calendar.getInstance().apply {
        timeInMillis = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfDay = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val endOfDay = calendar.timeInMillis - 1

    val trips by viewModel.getTripsWithDetailsByDate(startOfDay, endOfDay)
        .collectAsStateWithLifecycle(emptyList())

    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dateFormat.format(Date(date))) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SectionHeader(title = stringResource(R.string.trips_title))
            
            if (trips.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay viajes para este día", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(trips) { tripWithDetails ->
                        TripItem(
                            tripWithDetails = tripWithDetails,
                            onDelete = { viewModel.deleteTrip(tripWithDetails.trip) },
                            onEdit = { /* No editing for now to keep it simple */ },
                            onView = { /* Viewing is fine */ },
                            onSmsSent = { viewModel.updateTripMessageStatus(tripWithDetails.trip, true) }
                        )
                    }
                }
            }
        }
    }
}
