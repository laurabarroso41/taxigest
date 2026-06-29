package cu.cupet.isp.taxigest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cu.cupet.isp.taxigest.R
import cu.cupet.isp.taxigest.data.TaxiGestDatabase
import cu.cupet.isp.taxigest.data.model.Client
import cu.cupet.isp.taxigest.data.model.Taxi
import cu.cupet.isp.taxigest.data.model.Trip
import cu.cupet.isp.taxigest.data.model.TripClient
import cu.cupet.isp.taxigest.data.model.TripWithDetails
import cu.cupet.isp.taxigest.ui.components.PrimaryButton
import cu.cupet.isp.taxigest.ui.components.RideButton
import cu.cupet.isp.taxigest.ui.components.SectionHeader
import cu.cupet.isp.taxigest.ui.components.TaxiGestCard
import cu.cupet.isp.taxigest.ui.viewmodel.TripViewModel
import cu.cupet.isp.taxigest.ui.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TripsScreen() {
    val context = LocalContext.current
    val database = remember { TaxiGestDatabase.getDatabase(context) }
    val viewModel: TripViewModel = viewModel(
        factory = ViewModelFactory(
            tripDao = database.tripDao(),
            taxiDao = database.taxiDao(),
            clientDao = database.clientDao()
        )
    )
    val trips by viewModel.trips.collectAsStateWithLifecycle()
    
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_trip))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SectionHeader(title = stringResource(R.string.trips_title))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(trips) { tripWithDetails ->
                    TripItem(
                        tripWithDetails = tripWithDetails,
                        onDelete = { viewModel.deleteTrip(tripWithDetails.trip) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        val taxis by viewModel.taxis.collectAsStateWithLifecycle(emptyList())
        val clients by viewModel.clients.collectAsStateWithLifecycle(emptyList())

        TripWorkflowDialog(
            taxis = taxis,
            clients = clients,
            onDismiss = { showDialog = false },
            onSave = { trip, tripClients ->
                viewModel.saveTrip(trip, tripClients)
                showDialog = false
            }
        )
    }
}

@Composable
fun TripItem(
    tripWithDetails: TripWithDetails,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, EEE", Locale.getDefault())
    TaxiGestCard {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tripWithDetails.trip.province,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(Date(tripWithDetails.trip.date)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = tripWithDetails.taxi.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.clients_count, tripWithDetails.clients.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.price_format, tripWithDetails.trip.totalPrice),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = Color.Red.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripWorkflowDialog(
    taxis: List<Taxi>,
    clients: List<Client>,
    onDismiss: () -> Unit,
    onSave: (Trip, List<TripClient>) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    
    // Trip Data
    var selectedTaxi by remember { mutableStateOf<Taxi?>(null) }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var province by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf("") }
    var driverPrice by remember { mutableStateOf("") }
    var passengersCount by remember { mutableStateOf("1") }
    var hasMinors by remember { mutableStateOf(false) }

    // Trip Clients Data
    val selectedTripClients = remember { mutableStateListOf<TripClientState>() }

    // DatePicker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date
    )

    // Time Selection state for Pickup and Return
    var showPickupTimePicker by remember { mutableStateOf(false) }
    var showReturnTimePicker by remember { mutableStateOf(false) }
    
    var pickupHour by remember { mutableStateOf(5) }
    var pickupMinute by remember { mutableStateOf(0) }
    var returnHour by remember { mutableStateOf(22) }
    var returnMinute by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Custom Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (step > 1) step-- else onDismiss() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when(step) {
                            1 -> stringResource(R.string.step_date_time)
                            2 -> stringResource(R.string.step_client_location)
                            else -> stringResource(R.string.step_vehicle_pricing)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (step) {
                        1 -> {
                            // STEP 1: DATE & TIME & PROVINCE
                            ProvinceDropdown(province) { province = it }

                            Spacer(modifier = Modifier.height(8.dp))

                            DatePicker(
                                state = datePickerState,
                                showModeToggle = false,
                                title = null,
                                headline = null
                            )
                        }
                        2 -> {
                            // STEP 2: CLIENTS, ADDRESSES
                            Text(stringResource(R.string.clients_section), style = MaterialTheme.typography.titleSmall)

                            selectedTripClients.forEachIndexed { index, clientState ->
                                ClientTripEntry(
                                    clients = clients,
                                    state = clientState,
                                    onRemove = { selectedTripClients.removeAt(index) }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }

                            TextButton(onClick = { selectedTripClients.add(TripClientState()) }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.add_client_to_trip))
                            }
                        }
                        3 -> {
                            // STEP 3: TAXI, PASSENGERS, PRICES
                            Text(stringResource(R.string.trip_details), style = MaterialTheme.typography.titleSmall)
                            
                            OutlinedTextField(
                                value = passengersCount,
                                onValueChange = { if (it.all { char -> char.isDigit() }) passengersCount = it },
                                label = { Text(stringResource(R.string.passengers_count)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            )
                            
                            val pCount = passengersCount.toIntOrNull() ?: 0
                            val filteredTaxis = taxis.filter { it.seatsCount >= pCount }
                            
                            TaxiDropdown(filteredTaxis, selectedTaxi) { selectedTaxi = it }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = totalPrice,
                                    onValueChange = { totalPrice = it },
                                    label = { Text(stringResource(R.string.total_price)) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                OutlinedTextField(
                                    value = driverPrice,
                                    onValueChange = { driverPrice = it },
                                    label = { Text(stringResource(R.string.driver_price)) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = hasMinors, onCheckedChange = { hasMinors = it })
                                Text(stringResource(R.string.has_minors))
                            }
                        }
                    }
                }

                // Action Button
                Box(modifier = Modifier.padding(24.dp)) {
                    RideButton(
                        text = if (step < 3) stringResource(R.string.next_step) else stringResource(R.string.save_trip),
                        onClick = {
                            if (step < 3) {
                                step++
                                if (step == 2) {
                                    date = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                                }
                            } else {
                                val trip = Trip(
                                    taxiId = selectedTaxi?.id ?: 0L,
                                    date = date,
                                    totalPrice = totalPrice.toDoubleOrNull() ?: 0.0,
                                    driverPrice = driverPrice.toDoubleOrNull() ?: 0.0,
                                    province = province,
                                    hasMinors = hasMinors,
                                    passengersCount = passengersCount.toIntOrNull() ?: 1
                                )
                                val tripClients = selectedTripClients.map { 
                                    TripClient(
                                        tripId = 0,
                                        clientId = it.client?.id ?: 0L,
                                        pickupAddress = it.pickupAddress,
                                        destinationAddress = it.destinationAddress,
                                        arrivalTime = it.arrivalTime,
                                        departureTime = it.departureTime
                                    )
                                }
                                onSave(trip, tripClients)
                            }
                        },
                        enabled = when(step) {
                            1 -> datePickerState.selectedDateMillis != null && province.isNotBlank()
                            2 -> selectedTripClients.isNotEmpty()
                            else -> selectedTaxi != null
                        }
                    )
                }
            }
        }
    }

    if (showPickupTimePicker) {
        TimePickerWithDialog(
            initialHour = pickupHour,
            initialMinute = pickupMinute,
            onDismiss = { showPickupTimePicker = false },
            onConfirm = { h, m ->
                pickupHour = h
                pickupMinute = m
                showPickupTimePicker = false
            }
        )
    }

    if (showReturnTimePicker) {
        TimePickerWithDialog(
            initialHour = returnHour,
            initialMinute = returnMinute,
            onDismiss = { showReturnTimePicker = false },
            onConfirm = { h, m ->
                returnHour = h
                returnMinute = m
                showReturnTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerWithDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)

    TimePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.select_time_title)) }
    ) {
        TimePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectionItem(
    label: String,
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit,
    onTextClick: () -> Unit
) {
    val timeStr = String.format("%02d:%02d %s", 
        if (hour == 0 || hour == 12) 12 else hour % 12, 
        minute, 
        if (hour < 12) "AM" else "PM"
    )
    
    val totalMinutes = (hour * 60 + minute).toFloat()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.width(70.dp), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        
        Slider(
            value = totalMinutes,
            onValueChange = { newValue ->
                val h = newValue.toInt() / 60
                val m = newValue.toInt() % 60
                onTimeChange(h, m)
            },
            valueRange = 0f..(24f * 60f - 1f),
            modifier = Modifier.weight(1f),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .background(Color.Black.copy(alpha = 0.1f))
                )
            },
            track = { sliderState ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.LightGray, CircleShape)
                ) {
                    val fraction = (sliderState.value - sliderState.valueRange.start) / 
                                   (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .background(Color.Black, CircleShape)
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = timeStr,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier
                .width(64.dp)
                .clickable { onTextClick() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvinceDropdown(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val provinces = listOf(
        stringResource(R.string.p_pinar),
        stringResource(R.string.p_artemisa),
        stringResource(R.string.p_habana),
        stringResource(R.string.p_mayabeque),
        stringResource(R.string.p_matanzas),
        stringResource(R.string.p_vclara),
        stringResource(R.string.p_cienfuegos),
        stringResource(R.string.p_sancti),
        stringResource(R.string.p_ciego),
        stringResource(R.string.p_camaguey),
        stringResource(R.string.p_las_tunas),
        stringResource(R.string.p_holguin),
        stringResource(R.string.p_granma),
        stringResource(R.string.p_santiago),
        stringResource(R.string.p_guantanamo),
        stringResource(R.string.p_isla)
    )
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.province)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            provinces.forEach { province ->
                DropdownMenuItem(
                    text = { Text(province) },
                    onClick = {
                        onSelect(province)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiDropdown(taxis: List<Taxi>, selected: Taxi?, onSelect: (Taxi) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.select_taxi)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            taxis.forEach { taxi ->
                DropdownMenuItem(
                    text = { Text(taxi.name) },
                    onClick = {
                        onSelect(taxi)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientTripEntry(
    clients: List<Client>,
    state: TripClientState,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = state.client?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.select_client)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    clients.forEach { client ->
                        DropdownMenuItem(
                            text = { Text("${client.name} ${client.surnames}") },
                            onClick = {
                                state.client = client
                                expanded = false
                            }
                        )
                    }
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
            }
        }

        OutlinedTextField(
            value = state.pickupAddress,
            onValueChange = { state.pickupAddress = it },
            label = { Text(stringResource(R.string.pickup_address)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        OutlinedTextField(
            value = state.destinationAddress,
            onValueChange = { state.destinationAddress = it },
            label = { Text(stringResource(R.string.destination_address)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = formatTime(state.departureTime),
                onValueChange = {},
                readOnly = false, enabled = true,
                label = { Text(stringResource(R.string.departure_time)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            )
            OutlinedTextField(
                value = formatTime(state.arrivalTime),
                onValueChange = {},
                readOnly = false, enabled = true,
                label = { Text(stringResource(R.string.arrival_time)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

class TripClientState {
     var client by mutableStateOf<Client?>(null)
    var pickupAddress by mutableStateOf("")
    var destinationAddress by mutableStateOf("")
    var arrivalTime by mutableStateOf(System.currentTimeMillis())
    var departureTime by mutableStateOf(System.currentTimeMillis())
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
