package cu.cupet.isp.taxigest.ui.screens

import android.provider.ContactsContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cu.cupet.isp.taxigest.R
import cu.cupet.isp.taxigest.data.TaxiGestDatabase
import cu.cupet.isp.taxigest.data.model.Client
import cu.cupet.isp.taxigest.ui.components.PrimaryButton
import cu.cupet.isp.taxigest.ui.components.SectionHeader
import cu.cupet.isp.taxigest.ui.viewmodel.ClientViewModel
import cu.cupet.isp.taxigest.ui.viewmodel.ViewModelFactory

data class ContactItem(
    val id: String,
    val name: String,
    val phone: String,
    val isSelected: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportContactsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { TaxiGestDatabase.getDatabase(context) }
    val viewModel: ClientViewModel = viewModel(
        factory = ViewModelFactory(clientDao = database.clientDao())
    )

    var contacts by remember { mutableStateOf<List<ContactItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val list = mutableListOf<ContactItem>()
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use { c ->
            val idIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (c.moveToNext()) {
                val id = c.getString(idIdx)
                val name = c.getString(nameIdx)
                val num = c.getString(numIdx)
                // Avoid duplicates if a contact has multiple numbers (keep first one for simplicity)
                if (list.none { it.id == id }) {
                    list.add(ContactItem(id, name, num))
                }
            }
        }
        contacts = list
        isLoading = false
    }

    val filteredContacts = contacts.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }
    
    val selectedCount = contacts.count { it.isSelected }
    val allSelected = contacts.isNotEmpty() && contacts.all { it.isSelected }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_contact)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val newValue = !allSelected
                        contacts = contacts.map { it.copy(isSelected = newValue) }
                    }) {
                        Text(if (allSelected) "Deseleccionar todo" else "Seleccionar todo")
                    }
                }
            )
        },
        bottomBar = {
            if (selectedCount > 0) {
                Surface(tonalElevation = 8.dp) {
                    PrimaryButton(
                        text = "Importar ($selectedCount)",
                        onClick = {
                            contacts.filter { it.isSelected }.forEach { contact ->
                                viewModel.saveClient(Client(name = contact.name, surnames = "", phone = contact.phone))
                            }
                            onBack()
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar contactos...") },
                singleLine = true
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredContacts) { contact ->
                        ContactRow(
                            contact = contact,
                            onToggle = {
                                contacts = contacts.map { 
                                    if (it.id == contact.id) it.copy(isSelected = !it.isSelected) else it 
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactRow(contact: ContactItem, onToggle: () -> Unit) {
    ListItem(
        headlineContent = { Text(contact.name) },
        supportingContent = { Text(contact.phone) },
        leadingContent = {
            Icon(
                imageVector = if (contact.isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (contact.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        },
        modifier = Modifier.clickable { onToggle() }
    )
}
