package cu.cupet.isp.taxigest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cu.cupet.isp.taxigest.data.dao.ClientDao
import cu.cupet.isp.taxigest.data.dao.TaxiDao
import cu.cupet.isp.taxigest.data.dao.TripDao

class ViewModelFactory(
    private val taxiDao: TaxiDao? = null,
    private val clientDao: ClientDao? = null,
    private val tripDao: TripDao? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaxiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaxiViewModel(taxiDao!!) as T
        }
        if (modelClass.isAssignableFrom(ClientViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientViewModel(clientDao!!) as T
        }
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(tripDao!!, taxiDao!!, clientDao!!) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
