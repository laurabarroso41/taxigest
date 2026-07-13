package cu.cupet.isp.taxigest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import cu.cupet.isp.taxigest.R
import cu.cupet.isp.taxigest.data.TaxiGestDatabase
import cu.cupet.isp.taxigest.ui.components.SectionHeader
import cu.cupet.isp.taxigest.ui.viewmodel.TripViewModel
import cu.cupet.isp.taxigest.ui.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(type: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { TaxiGestDatabase.getDatabase(context) }
    val viewModel: TripViewModel = viewModel(
        factory = ViewModelFactory(
            tripDao = database.tripDao(),
            taxiDao = database.taxiDao(),
            clientDao = database.clientDao()
        )
    )

    val title = when (type) {
        "gains_date" -> stringResource(R.string.report_gains_by_date)
        "active_taxis" -> stringResource(R.string.report_active_taxis)
        "gain_taxi" -> stringResource(R.string.report_gain_by_taxi)
        "client_count" -> stringResource(R.string.report_client_count)
        "total_gains" -> stringResource(R.string.report_total_gains)
        "passenger_count" -> stringResource(R.string.report_passenger_count)
        else -> "Reporte"
    }

    var showRangePicker by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    // Default range: Last 7 days
    var endDate by remember { mutableLongStateOf(calendar.timeInMillis) }
    calendar.add(Calendar.DAY_OF_YEAR, -7)
    var startDate by remember { mutableLongStateOf(calendar.timeInMillis) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showRangePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Rango de fechas")
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
                .verticalScroll(rememberScrollState())
        ) {
            when (type) {
                "gains_date" -> GainsByDateContent(viewModel, startDate, endDate)
                "active_taxis" -> ActiveTaxisContent(viewModel, startDate, endDate)
                "gain_taxi" -> GainByTaxiContent(viewModel, startDate, endDate)
                "client_count" -> ClientCountContent(viewModel, startDate, endDate)
                "passenger_count" -> PassengerCountContent(viewModel, startDate, endDate)
                "total_gains" -> TotalGainsContent(viewModel, startDate, endDate)
                else -> {
                    SectionHeader(title = "Resumen del Reporte")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Visualización para: $title",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Próximamente estaremos habilitando este reporte específico.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRangePicker) {
        val state = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedStartDateMillis?.let { startDate = it }
                    state.selectedEndDateMillis?.let { endDate = it }
                    showRangePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DateRangePicker(
                state = state,
                modifier = Modifier.weight(1f),
                title = { Text("Selecciona el rango", modifier = Modifier.padding(16.dp)) },
                headline = { Text("Filtro de Fechas", modifier = Modifier.padding(16.dp)) },
                showModeToggle = false
            )
        }
    }
}

@Composable
fun TotalGainsContent(viewModel: TripViewModel, startDate: Long, endDate: Long) {
    val cal = Calendar.getInstance()
    cal.timeInMillis = startDate
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    val start = cal.timeInMillis

    cal.timeInMillis = endDate
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    val end = cal.timeInMillis

    val totalGain by viewModel.getGainsReport(start, end).collectAsStateWithLifecycle(0.0)
    val dailyGains by viewModel.getDailyGainsReport(start, end).collectAsStateWithLifecycle(emptyList())

    SectionHeader(title = "Resumen de Ganancias")

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Ganancia Total en el Periodo", style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.price_format, totalGain ?: 0.0),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (dailyGains.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(title = "Promedio Diario")
        val avg = totalGain?.div(dailyGains.size) ?: 0.0
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Promedio por día", style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.price_format, avg), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PassengerCountContent(viewModel: TripViewModel, startDate: Long, endDate: Long) {
    val cal = Calendar.getInstance()
    cal.timeInMillis = startDate
    cal.set(Calendar.HOUR_OF_DAY, 0)
    val start = cal.timeInMillis

    cal.timeInMillis = endDate
    cal.set(Calendar.HOUR_OF_DAY, 23)
    val end = cal.timeInMillis

    val passengerData by viewModel.getClientsCountReport(start, end).collectAsStateWithLifecycle(emptyList())
    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

    val modelProducer = remember { CartesianChartModelProducer() }
    val labelKey = remember { ExtraStore.Key<List<String>>() }

    LaunchedEffect(passengerData) {
        if (passengerData.isNotEmpty()) {
            val labels = passengerData.map { dateFormat.format(Date(it.date)) }
            modelProducer.runTransaction {
                columnModel {
                    series(passengerData.map { it.count.toFloat() })
                }
                extras { it[labelKey] = labels }
            }
        }
    }

    SectionHeader(title = "Pasajeros Transportados")

    if (passengerData.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("No hay datos", color = Color.Gray)
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth().height(250.dp).padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            CartesianChartHost(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = { context, value, _ ->
                            val labels = context.model.extraStore.getOrNull(labelKey)
                            val index = value.toInt()
                            if (labels != null && index in labels.indices) labels[index] else index.toString()
                        }
                    )
                ),
                modelProducer = modelProducer
            )
        }
    }
}

@Composable
fun ClientCountContent(viewModel: TripViewModel, startDate: Long, endDate: Long) {
    val cal = Calendar.getInstance()
    cal.timeInMillis = startDate
    cal.set(Calendar.HOUR_OF_DAY, 0)
    val start = cal.timeInMillis

    cal.timeInMillis = endDate
    cal.set(Calendar.HOUR_OF_DAY, 23)
    val end = cal.timeInMillis

    val typeData by viewModel.getPassengersByTripTypeReport(start, end).collectAsStateWithLifecycle(emptyList())

    SectionHeader(title = "Distribución por Tipo de Viaje")

    if (typeData.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("No hay datos", color = Color.Gray)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            typeData.forEach { item ->
                val typeName = if (item.tripType == 0) "Solo Ida" else "Ida y Vuelta"
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(typeName, fontWeight = FontWeight.Bold)
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ) {
                            Text(
                                "${item.count} pasajeros",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GainByTaxiContent(viewModel: TripViewModel, startDate: Long, endDate: Long) {
    // Normalizar fechas
    val cal = Calendar.getInstance()
    cal.timeInMillis = startDate
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    val start = cal.timeInMillis

    cal.timeInMillis = endDate
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    val end = cal.timeInMillis

    val taxiGains by viewModel.getGainByTaxiReport(start, end).collectAsStateWithLifecycle(emptyList())
    val taxis by viewModel.taxis.collectAsStateWithLifecycle()

    // Vico Chart Logic
    val modelProducer = remember { CartesianChartModelProducer() }
    val labelKey = remember { ExtraStore.Key<List<String>>() }

    LaunchedEffect(taxiGains, taxis) {
        if (taxiGains.isNotEmpty() && taxis.isNotEmpty()) {
            val labels = taxiGains.map { item ->
                taxis.find { it.id == item.taxiId }?.name ?: "ID: ${item.taxiId}"
            }
            modelProducer.runTransaction {
                columnModel {
                    series(taxiGains.map { it.gain.toFloat() })
                }
                extras { it[labelKey] = labels }
            }
        }
    }

    SectionHeader(title = "Ganancias por Taxi")

    if (taxiGains.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("No hay datos para este rango", color = Color.Gray)
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth().height(250.dp).padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            CartesianChartHost(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = { context, value, _ ->
                            val labels = context.model.extraStore.getOrNull(labelKey)
                            val index = value.toInt()
                            if (labels != null && index in labels.indices) {
                                labels[index]
                            } else {
                                index.toString()
                            }
                        }
                    )
                ),
                modelProducer = modelProducer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader(title = "Detalle por Vehículo")

        // Tabla
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Taxi", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.weight(1f))
                    Text("Ganancia", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                taxiGains.forEach { item ->
                    val taxiName = taxis.find { it.id == item.taxiId }?.name ?: "ID: ${item.taxiId}"
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(taxiName, modifier = Modifier.weight(1f))
                        Text(
                            stringResource(R.string.price_format, item.gain),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTAL GANANCIAS", fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    Text(
                        stringResource(R.string.price_format, taxiGains.sumOf { it.gain }),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveTaxisContent(viewModel: TripViewModel, startDate: Long, endDate: Long) {
    // Normalizar fechas para incluir el día completo
    val cal = Calendar.getInstance()
    cal.timeInMillis = startDate
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    val start = cal.timeInMillis

    cal.timeInMillis = endDate
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    val end = cal.timeInMillis

    val tripsByDate by viewModel.getTripsByDateReport(start, end).collectAsStateWithLifecycle(emptyList())
    
    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    val fullDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Vico Chart Logic
    val modelProducer = remember { CartesianChartModelProducer() }
    val labelKey = remember { ExtraStore.Key<List<String>>() }

    LaunchedEffect(tripsByDate) {
        if (tripsByDate.isNotEmpty()) {
            val labels = tripsByDate.map { dateFormat.format(Date(it.date)) }
            modelProducer.runTransaction {
                columnModel {
                    series(tripsByDate.map { it.count.toFloat() })
                }
                extras { it[labelKey] = labels }
            }
        }
    }

    SectionHeader(title = "Viajes por Día")
    
    if (tripsByDate.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("No hay datos para este rango", color = Color.Gray)
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth().height(250.dp).padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            CartesianChartHost(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = { context, value, _ ->
                            val labels = context.model.extraStore.getOrNull(labelKey)
                            val index = value.toInt()
                            if (labels != null && index in labels.indices) {
                                labels[index]
                            } else {
                                index.toString()
                            }
                        }
                    )
                ),
                modelProducer = modelProducer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader(title = "Detalle de Actividad")

        // Tabla
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                // Header Tabla
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Fecha", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.weight(1f))
                    Text("Viajes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
                
                tripsByDate.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(fullDateFormat.format(Date(item.date)), modifier = Modifier.weight(1f))
                        Text(
                            item.count.toString(),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
                
                // Total final
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTAL VIAJES", fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    Text(
                        tripsByDate.sumOf { it.count }.toString(),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun GainsByDateContent(viewModel: TripViewModel, startDate: Long, endDate: Long) {
    // Normalizar fechas para incluir el día completo
    val cal = Calendar.getInstance()
    cal.timeInMillis = startDate
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    val start = cal.timeInMillis

    cal.timeInMillis = endDate
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    val end = cal.timeInMillis

    val dailyGains by viewModel.getDailyGainsReport(start, end).collectAsStateWithLifecycle(emptyList())
    
    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    val fullDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Vico Chart Logic
    val modelProducer = remember { CartesianChartModelProducer() }
    val labelKey = remember { ExtraStore.Key<List<String>>() }

    LaunchedEffect(dailyGains) {
        if (dailyGains.isNotEmpty()) {


            val labels = dailyGains.map { dateFormat.format(Date(it.date)) }
            modelProducer.runTransaction {
                lineModel {
                    series(dailyGains.map { it.gain })
                }
                extras { it[labelKey] = labels }
            }

        }
    }

    SectionHeader(title = "Evolución de Ganancias")
    
    if (dailyGains.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("No hay datos para este rango", color = Color.Gray)
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth().height(250.dp).padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            CartesianChartHost(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = { context, value, _ ->
                            val labels = context.model.extraStore.getOrNull(labelKey)
                            val index = value.toInt()
                            if (labels != null && index in labels.indices) {
                                labels[index]
                            } else {
                                index.toString()
                            }
                        }
                    )
                ),
                modelProducer = modelProducer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader(title = "Detalle por Día")

        // Tabla
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                // Header Tabla
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Fecha", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.weight(1f))
                    Text("Ganancia", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
                
                dailyGains.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(fullDateFormat.format(Date(item.date)), modifier = Modifier.weight(1f))
                        Text(
                            stringResource(R.string.price_format, item.gain),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
                
                // Total final
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTAL", fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    Text(
                        stringResource(R.string.price_format, dailyGains.sumOf { it.gain }),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
