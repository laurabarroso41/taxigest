package cu.cupet.isp.taxigest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.cupet.isp.taxigest.data.dao.ClientDao
import cu.cupet.isp.taxigest.data.model.Client
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClientViewModel(private val clientDao: ClientDao) : ViewModel() {

    val clients: StateFlow<List<Client>> = clientDao.getAllClients()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveClient(client: Client) {
        viewModelScope.launch {
            if (client.id == 0L) {
                clientDao.insertClient(client)
            } else {
                clientDao.updateClient(client)
            }
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            clientDao.deleteClient(client)
        }
    }

    fun importClients(importedClients: List<Client>) {
        viewModelScope.launch {
            importedClients.forEach { clientDao.insertClient(it) }
        }
    }
}
