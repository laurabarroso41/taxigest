package cu.cupet.isp.taxigest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cu.cupet.isp.taxigest.R
import cu.cupet.isp.taxigest.data.TaxiGestDatabase
import cu.cupet.isp.taxigest.ui.components.SectionHeader
import cu.cupet.isp.taxigest.ui.components.TaxiGestCard
import cu.cupet.isp.taxigest.ui.viewmodel.TripViewModel
import cu.cupet.isp.taxigest.ui.viewmodel.ViewModelFactory

@Composable
fun ReportsScreen() {
    val context = LocalContext.current
    val database = remember { TaxiGestDatabase.getDatabase(context) }
    val viewModel: TripViewModel = viewModel(
        factory = ViewModelFactory(
            tripDao = database.tripDao(),
            taxiDao = database.taxiDao(),
            clientDao = database.clientDao()
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader(title = stringResource(R.string.reports_title))
        
        ReportCategoryItem(title = stringResource(R.string.report_gains_by_date)) {
            // Placeholder for Date Range Earning Report
            Text("Ver ganancias por rango de fechas")
        }

        ReportCategoryItem(title = stringResource(R.string.report_active_taxis)) {
            // Placeholder for Active Taxis Report
            Text("Listado de carros trabajando")
        }
        
        ReportCategoryItem(title = stringResource(R.string.report_gain_by_taxi)) {
            // Placeholder for Earning by Taxi Report
            Text("Ganancia generada por cada coche")
        }

        ReportCategoryItem(title = stringResource(R.string.report_client_count)) {
            // Placeholder for Client Count Report
            Text("Total de clientes registrados")
        }

        ReportCategoryItem(title = stringResource(R.string.report_total_gains)) {
            // Placeholder for Total Gains Report
            Text("Suma total de todas las ganancias")
        }

        ReportCategoryItem(title = stringResource(R.string.report_passenger_count)) {
            // Placeholder for Passenger Count Report
            Text("Total de pasajeros transportados")
        }
    }
}

@Composable
fun ReportCategoryItem(
    title: String,
    content: @Composable () -> Unit
) {
    TaxiGestCard(modifier = Modifier.padding(bottom = 12.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
