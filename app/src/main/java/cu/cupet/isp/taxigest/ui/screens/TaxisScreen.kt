package cu.cupet.isp.taxigest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cu.cupet.isp.taxigest.R
import cu.cupet.isp.taxigest.data.TaxiGestDatabase
import cu.cupet.isp.taxigest.data.model.Taxi
import cu.cupet.isp.taxigest.ui.components.PrimaryButton
import cu.cupet.isp.taxigest.ui.components.SectionHeader
import cu.cupet.isp.taxigest.ui.components.TaxiGestCard
import cu.cupet.isp.taxigest.ui.viewmodel.TaxiViewModel
import cu.cupet.isp.taxigest.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxisScreen() {
    val context = LocalContext.current
    val database = remember { TaxiGestDatabase.getDatabase(context) }
    val viewModel: TaxiViewModel = viewModel(
        factory = ViewModelFactory(database.taxiDao())
    )
    val taxis by viewModel.taxis.collectAsStateWithLifecycle()
    
    var showDialog by remember { mutableStateOf(false) }
    var editingTaxi by remember { mutableStateOf<Taxi?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    editingTaxi = null
                    showDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_taxi))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SectionHeader(title = stringResource(R.string.taxis_title))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(taxis) { taxi ->
                    TaxiItem(
                        taxi = taxi,
                        onEdit = {
                            editingTaxi = taxi
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteTaxi(taxi) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        TaxiDialog(
            taxi = editingTaxi,
            onDismiss = { showDialog = false },
            onSave = { 
                viewModel.saveTaxi(it)
                showDialog = false
            }
        )
    }
}

@Composable
fun TaxiItem(
    taxi: Taxi,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    TaxiGestCard {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = taxi.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.seats_format, taxi.carType, taxi.seatsCount),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!taxi.cellphone.isNullOrBlank()) {
                    Text(text = "${stringResource(R.string.cellphone)}: ${taxi.cellphone}", style = MaterialTheme.typography.bodySmall)
                }
                if (taxi.hasAirConditioning) {
                    Text(
                        text = stringResource(R.string.ac_included),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        }
    }
}

@Composable
fun TaxiDialog(
    taxi: Taxi?,
    onDismiss: () -> Unit,
    onSave: (Taxi) -> Unit
) {
    var name by remember { mutableStateOf(taxi?.name ?: "") }
    var seats by remember { mutableStateOf(taxi?.seatsCount?.toString() ?: "4") }
    var carType by remember { mutableStateOf(taxi?.carType ?: "") }
    var hasAC by remember { mutableStateOf(taxi?.hasAirConditioning ?: false) }
    var cellphone by remember { mutableStateOf(taxi?.cellphone ?: "") }
    var fixPhone by remember { mutableStateOf(taxi?.fixPhone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (taxi == null) stringResource(R.string.add_taxi) else stringResource(R.string.edit_taxi)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cellphone,
                    onValueChange = { cellphone = it },
                    label = { Text(stringResource(R.string.cellphone)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fixPhone,
                    onValueChange = { fixPhone = it },
                    label = { Text(stringResource(R.string.fix_phone)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = seats,
                    onValueChange = { if (it.all { char -> char.isDigit() }) seats = it },
                    label = { Text(stringResource(R.string.seats)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = carType,
                    onValueChange = { carType = it },
                    label = { Text(stringResource(R.string.car_type)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasAC, onCheckedChange = { hasAC = it })
                    Text(stringResource(R.string.has_ac))
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = stringResource(R.string.save),
                onClick = {
                    onSave(
                        Taxi(
                            id = taxi?.id ?: 0L,
                            name = name,
                            seatsCount = seats.toIntOrNull() ?: 4,
                            carType = carType,
                            hasAirConditioning = hasAC,
                            cellphone = cellphone,
                            fixPhone = fixPhone
                        )
                    )
                },
                modifier = Modifier.width(100.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
