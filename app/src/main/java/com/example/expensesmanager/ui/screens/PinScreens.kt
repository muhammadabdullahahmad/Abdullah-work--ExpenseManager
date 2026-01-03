package com.example.expensesmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinWelcomeScreen(
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Security",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Secure Your App",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Set up a PIN to protect your financial data and keep your expenses private.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNextClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Next",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PinSetupScreen(
    onPinEntered: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Setup New PIN",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter a 4-digit PIN",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        // PIN dots display
        PinDotsDisplay(pinLength = pin.length)

        Spacer(modifier = Modifier.height(48.dp))

        // Number pad
        NumberPad(
            onNumberClick = { number ->
                if (pin.length < 4) {
                    pin += number
                }
            },
            onDeleteClick = {
                if (pin.isNotEmpty()) {
                    pin = pin.dropLast(1)
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onPinEntered(pin) },
            enabled = pin.length == 4,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Next",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PinConfirmScreen(
    originalPin: String,
    onPinConfirmed: () -> Unit,
    onPinMismatch: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Confirm Your PIN",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Re-enter your PIN to confirm",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (showError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "PINs don't match. Try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // PIN dots display
        PinDotsDisplay(pinLength = pin.length, isError = showError)

        Spacer(modifier = Modifier.height(48.dp))

        // Number pad
        NumberPad(
            onNumberClick = { number ->
                if (pin.length < 4) {
                    pin += number
                    showError = false
                }
            },
            onDeleteClick = {
                if (pin.isNotEmpty()) {
                    pin = pin.dropLast(1)
                    showError = false
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (pin == originalPin) {
                    onPinConfirmed()
                } else {
                    showError = true
                    pin = ""
                    onPinMismatch()
                }
            },
            enabled = pin.length == 4,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Confirm",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PinUnlockScreen(
    onPinValidated: () -> Unit,
    validatePin: (String) -> Boolean,
    isLockedOut: () -> Boolean = { false },
    getRemainingSeconds: () -> Int = { 0 },
    onLockout: () -> Unit = {},
    onLockoutCleared: () -> Unit = {}
) {
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var attemptsLeft by remember { mutableIntStateOf(3) }
    var isLocked by remember { mutableStateOf(isLockedOut()) }
    var remainingSeconds by remember { mutableIntStateOf(getRemainingSeconds()) }

    // Timer for countdown
    LaunchedEffect(isLocked) {
        if (isLocked) {
            while (remainingSeconds > 0) {
                kotlinx.coroutines.delay(1000L)
                remainingSeconds = getRemainingSeconds()
                if (remainingSeconds <= 0) {
                    isLocked = false
                    attemptsLeft = 3
                    onLockoutCleared()
                }
            }
        }
    }

    // Check lockout on start
    LaunchedEffect(Unit) {
        if (isLockedOut()) {
            isLocked = true
            remainingSeconds = getRemainingSeconds()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            modifier = Modifier.size(60.dp),
            tint = if (isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLocked) {
            // Lockout screen
            Text(
                text = "Too Many Attempts",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Wait for 1 minute to try again",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "$remainingSeconds seconds remaining",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            // Normal PIN entry
            Text(
                text = "Enter PIN",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your PIN to access the app",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (showError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Wrong PIN, $attemptsLeft chances left",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Remind your PIN",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // PIN dots display
            PinDotsDisplay(pinLength = pin.length, isError = showError)

            Spacer(modifier = Modifier.height(48.dp))

            // Number pad
            NumberPad(
                onNumberClick = { number ->
                    if (pin.length < 4) {
                        pin += number
                        showError = false

                        // Auto-validate when 4 digits entered
                        if (pin.length == 4) {
                            if (validatePin(pin)) {
                                onPinValidated()
                            } else {
                                attemptsLeft--
                                showError = true
                                pin = ""
                                if (attemptsLeft <= 0) {
                                    isLocked = true
                                    remainingSeconds = 60
                                    onLockout()
                                }
                            }
                        }
                    }
                },
                onDeleteClick = {
                    if (pin.isNotEmpty()) {
                        pin = pin.dropLast(1)
                        showError = false
                    }
                }
            )
        }
    }
}

@Composable
fun ChangePinScreen(
    validateCurrentPin: (String) -> Boolean,
    onPinChanged: (String) -> Unit,
    onCancel: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Enter current, 2: Enter new, 3: Confirm new
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val title = when (step) {
        1 -> "Enter Current PIN"
        2 -> "Enter New PIN"
        else -> "Confirm New PIN"
    }

    val subtitle = when (step) {
        1 -> "Enter your current PIN to continue"
        2 -> "Enter a new 4-digit PIN"
        else -> "Re-enter your new PIN to confirm"
    }

    val currentPinValue = when (step) {
        1 -> currentPin
        2 -> newPin
        else -> confirmPin
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (showError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        PinDotsDisplay(pinLength = currentPinValue.length, isError = showError)

        Spacer(modifier = Modifier.height(48.dp))

        NumberPad(
            onNumberClick = { number ->
                showError = false
                when (step) {
                    1 -> {
                        if (currentPin.length < 4) {
                            currentPin += number
                            if (currentPin.length == 4) {
                                if (validateCurrentPin(currentPin)) {
                                    step = 2
                                    currentPin = ""
                                } else {
                                    showError = true
                                    errorMessage = "Wrong PIN. Try again."
                                    currentPin = ""
                                }
                            }
                        }
                    }
                    2 -> {
                        if (newPin.length < 4) {
                            newPin += number
                            if (newPin.length == 4) {
                                step = 3
                            }
                        }
                    }
                    3 -> {
                        if (confirmPin.length < 4) {
                            confirmPin += number
                            if (confirmPin.length == 4) {
                                if (confirmPin == newPin) {
                                    onPinChanged(newPin)
                                } else {
                                    showError = true
                                    errorMessage = "PINs don't match. Try again."
                                    confirmPin = ""
                                }
                            }
                        }
                    }
                }
            },
            onDeleteClick = {
                showError = false
                when (step) {
                    1 -> if (currentPin.isNotEmpty()) currentPin = currentPin.dropLast(1)
                    2 -> if (newPin.isNotEmpty()) newPin = newPin.dropLast(1)
                    3 -> if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

@Composable
fun PinDotsDisplay(
    pinLength: Int,
    isError: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < pinLength) {
                            if (isError) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        } else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = if (isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun NumberPad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "del")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        numbers.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                row.forEach { item ->
                    when (item) {
                        "" -> {
                            Spacer(modifier = Modifier.size(72.dp))
                        }
                        "del" -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .clickable { onDeleteClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⌫",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onNumberClick(item) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
