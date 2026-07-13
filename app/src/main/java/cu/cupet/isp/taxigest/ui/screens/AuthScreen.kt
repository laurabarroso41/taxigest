package cu.cupet.isp.taxigest.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cu.cupet.isp.taxigest.R
import cu.cupet.isp.taxigest.data.TaxiGestDatabase
import cu.cupet.isp.taxigest.ui.components.RideButton
import cu.cupet.isp.taxigest.ui.viewmodel.UserViewModel
import cu.cupet.isp.taxigest.ui.viewmodel.ViewModelFactory

@Composable
fun AuthScreen() {
    val context = LocalContext.current
    val database = remember { TaxiGestDatabase.getDatabase(context) }
    val userViewModel: UserViewModel = viewModel(
        factory = ViewModelFactory(userDao = database.userDao())
    )

    val loginError by userViewModel.loginError.collectAsStateWithLifecycle()

    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Upper part: Image of Taxi
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.launcher), // Placeholder for taxi image
                contentDescription = null,
                modifier = Modifier.size(250.dp),
                contentScale = ContentScale.Fit
            )
        }

        // Lower part: Form in Black Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.8f),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            color = Color(0xFF111111) // RideBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = 0.1f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isLoginMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { isLoginMode = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            color = if (isLoginMode) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (!isLoginMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { isLoginMode = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Registro",
                            color = if (!isLoginMode) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Name field (only for register)
                if (!isLoginMode) {
                    AuthTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nombre",
                        icon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Username field
                AuthTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Usuario",
                    icon = Icons.Default.Email
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = { passwordVisible = !passwordVisible }
                )

                if (loginError) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.login_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                RideButton(
                    text = if (isLoginMode) "Login" else "Register",
                    onClick = {
                        if (isLoginMode) {
                            userViewModel.login(username, password)
                        } else {
                            userViewModel.register(username, name, password)
                        }
                    },
                    enabled = username.isNotBlank() && password.isNotBlank() && (isLoginMode || name.isNotBlank())
                )
            }
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        label = { Text(label, color = Color.Gray) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onPasswordToggle?.invoke() }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = Color.White.copy(alpha = 0.3f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
        ),
        singleLine = true
    )
}
