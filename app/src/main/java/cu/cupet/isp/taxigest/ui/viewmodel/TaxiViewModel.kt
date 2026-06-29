package cu.cupet.isp.taxigest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.cupet.isp.taxigest.data.dao.TaxiDao
import cu.cupet.isp.taxigest.data.model.Taxi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaxiViewModel(private val taxiDao: TaxiDao) : ViewModel() {

    val taxis: StateFlow<List<Taxi>> = taxiDao.getAllTaxis()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveTaxi(taxi: Taxi) {
        viewModelScope.launch {
            if (taxi.id == 0L) {
                taxiDao.insertTaxi(taxi)
            } else {
                taxiDao.updateTaxi(taxi)
            }
        }
    }

    fun deleteTaxi(taxi: Taxi) {
        viewModelScope.launch {
            taxiDao.deleteTaxi(taxi)
        }
    }
}
