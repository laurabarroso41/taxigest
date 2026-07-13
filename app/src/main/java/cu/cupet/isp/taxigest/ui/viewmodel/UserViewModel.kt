package cu.cupet.isp.taxigest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.cupet.isp.taxigest.data.dao.UserDao
import cu.cupet.isp.taxigest.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val userDao: UserDao) : ViewModel() {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _loginError = MutableStateFlow(false)
    val loginError: StateFlow<Boolean> = _loginError

    init {
        // Crear un usuario por defecto si no hay ninguno
        viewModelScope.launch {
            if (userDao.getUserCount() == 0) {
                userDao.insertUser(User(username = "admin", name = "Administrador", password = "admin"))
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val user = userDao.getUserByUsername(username)
            if (user != null && user.password == password) {
                _currentUser.value = user
                _isAuthenticated.value = true
                _loginError.value = false
            } else {
                _loginError.value = true
            }
        }
    }

    fun register(username: String, name: String, password: String) {
        viewModelScope.launch {
            val existingUser = userDao.getUserByUsername(username)
            if (existingUser != null) {
                _loginError.value = true // O un error específico de "usuario ya existe"
            } else {
                val newUser = User(username = username, name = name, password = password)
                userDao.insertUser(newUser)
                _currentUser.value = newUser
                _isAuthenticated.value = true
                _loginError.value = false
            }
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _currentUser.value = null
    }
}
