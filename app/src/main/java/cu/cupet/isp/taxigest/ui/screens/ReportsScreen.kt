package cu.cupet.isp.taxigest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
        
        ReportCard(title = stringResource(R.string.report_gains_line)) {
            Text(stringResource(R.string.report_gains_line_desc))
        }

        ReportCard(title = stringResource(R.string.report_active_taxis_column)) {
            Text(stringResource(R.string.report_active_taxis_column_desc))
        }
        
        ReportCard(title = stringResource(R.string.report_gain_by_taxi_bar)) {
            Text(stringResource(R.string.report_gain_by_taxi_bar_desc))
        }

        ReportCard(title = stringResource(R.string.report_clients_trend_line)) {
            Text(stringResource(R.string.report_clients_trend_line_desc))
        }

        ReportCard(title = stringResource(R.string.report_passengers_stats)) {
            Text(stringResource(R.string.report_passengers_stats_desc))
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    content: @Composable () -> Unit
) {
    TaxiGestCard(modifier = Modifier.padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.height(100.dp)) {
                content()
            }
        }
    }
}
