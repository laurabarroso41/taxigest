package cu.cupet.isp.taxigest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.cupet.isp.taxigest.data.dao.ClientDao
import cu.cupet.isp.taxigest.data.dao.TaxiDao
import cu.cupet.isp.taxigest.data.dao.TripDao
import cu.cupet.isp.taxigest.data.model.Client
import cu.cupet.isp.taxigest.data.model.Taxi
import cu.cupet.isp.taxigest.data.model.Trip
import cu.cupet.isp.taxigest.data.model.TripClient
import cu.cupet.isp.taxigest.data.model.TripWithDetails
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TripViewModel(
    private val tripDao: TripDao,
    private val taxiDao: TaxiDao,
    private val clientDao: ClientDao
) : ViewModel() {

    val trips: StateFlow<List<TripWithDetails>> = tripDao.getAllTripsWithDetails()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val taxis: StateFlow<List<Taxi>> = taxiDao.getAllTaxis()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clients: StateFlow<List<Client>> = clientDao.getAllClients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveTrip(trip: Trip, tripClients: List<TripClient>) {
        viewModelScope.launch {
            if (trip.id == 0L) {
                tripDao.insertTripWithClients(trip, tripClients)
            } else {
                tripDao.updateTripWithClients(trip, tripClients)
            }
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            tripDao.deleteTrip(trip)
            tripDao.deleteTripClientsByTripId(trip.id)
        }
    }

    fun updateTripMessageStatus(trip: Trip, sent: Boolean) {
        viewModelScope.launch {
            tripDao.updateTrip(trip.copy(isClientMessageSent = sent))
        }
    }

    fun getGainsReport(startDate: Long, endDate: Long): Flow<Double?> = tripDao.getGainsReport(startDate, endDate)
    fun getTripsByDateReport(startDate: Long, endDate: Long): Flow<List<TripDao.DateCount>> = tripDao.getTripsByDateReport(startDate, endDate)
    fun getGainByTaxiReport(startDate: Long, endDate: Long): Flow<List<TripDao.TaxiGain>> = tripDao.getGainByTaxiReport(startDate, endDate)
    fun getClientsCountReport(startDate: Long, endDate: Long): Flow<List<TripDao.DateCount>> = tripDao.getClientsCountReport(startDate, endDate)
    fun getPassengersByTripTypeReport(startDate: Long, endDate: Long): Flow<List<TripDao.TripTypeCount>> = tripDao.getPassengersByTripTypeReport(startDate, endDate)
    fun getTripsWithDetailsByDate(startDate: Long, endDate: Long): Flow<List<TripWithDetails>> = tripDao.getTripsWithDetailsByDate(startDate, endDate)
    fun getDailyGainsReport(startDate: Long, endDate: Long): Flow<List<TripDao.DailyGain>> = tripDao.getDailyGainsReport(startDate, endDate)
}
