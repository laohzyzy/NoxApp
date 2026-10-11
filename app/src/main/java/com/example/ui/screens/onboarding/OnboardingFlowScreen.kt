package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.AudioPlayerManager
import com.example.ui.theme.LocalWhatsAppColors
import com.example.ui.theme.WhatsAppDangerRed
import com.example.ui.theme.WhatsAppGreen
import kotlinx.coroutines.delay
import kotlin.random.Random

data class CountryInfo(
    val name: String,
    val code: String
)

private val COUNTRIES = listOf(
    CountryInfo("España", "+34"),
    CountryInfo("Argentina", "+54"),
    CountryInfo("México", "+52"),
    CountryInfo("Colombia", "+57"),
    CountryInfo("Chile", "+56"),
    CountryInfo("Perú", "+51"),
    CountryInfo("Estados Unidos", "+1"),
    CountryInfo("Uruguay", "+598"),
    CountryInfo("Ecuador", "+593"),
    CountryInfo("Venezuela", "+58")
)

@Composable
fun OnboardingFlowScreen(
    onRegistrationComplete: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(1) } // 1: Welcome, 2: Phone, 3: SMS, 4: Profile
    var selectedCountry by remember { mutableStateOf(COUNTRIES[1]) } // Default Argentina or Mexico
    var rawPhoneNumber by remember { mutableStateOf("") }
    var fullPhoneNumber by remember { mutableStateOf("") }
    var generatedSmsCode by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var isOtpError by remember { mutableStateOf(false) }
    var showSmsNotificationBanner by remember { mutableStateOf(false) }
    var resendCooldownSec by remember { mutableIntStateOf(60) }

    var userName by remember { mutableStateOf("") }
    var isInitializing by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("onboarding_screen")
    ) {
        when (currentStep) {
            1 -> {
                WelcomeStep(
                    onContinue = { currentStep = 2 }
                )
            }

            2 -> {
                PhoneInputStep(
                    selectedCountry = selectedCountry,
                    rawPhone = rawPhoneNumber,
                    onCountrySelected = { selectedCountry = it },
                    onPhoneChanged = { rawPhoneNumber = it },
                    onConfirmNumber = { phone ->
                        fullPhoneNumber = phone
                        // Generate random 6 digit SMS code
                        val code = String.format("%06d", Random.nextInt(100000, 999999))
                        generatedSmsCode = code
                        currentStep = 3
                    }
                )
            }

            3 -> {
                SmsVerificationStep(
                    phoneNumber = fullPhoneNumber,
                    generatedCode = generatedSmsCode,
                    enteredOtp = enteredOtp,
                    isError = isOtpError,
                    cooldownSec = resendCooldownSec,
                    showBanner = showSmsNotificationBanner,
                    onOtpChange = {
                        enteredOtp = it.filter { ch -> ch.isDigit() }.take(6)
                        isOtpError = false
                        if (enteredOtp.length == 6) {
                            if (enteredOtp == generatedSmsCode) {
                                currentStep = 4
                            } else {
                                isOtpError = true
                            }
                        }
                    },
                    onAutoFillClick = {
                        enteredOtp = generatedSmsCode
                        currentStep = 4
                    },
                    onTriggerBanner = {
                        showSmsNotificationBanner = true
                        AudioPlayerManager.playSmsNotificationChime()
                    },
                    onResendCode = {
                        val newCode = String.format("%06d", Random.nextInt(100000, 999999))
                        generatedSmsCode = newCode
                        enteredOtp = ""
                        isOtpError = false
                        resendCooldownSec = 60
                        showSmsNotificationBanner = true
                        AudioPlayerManager.playSmsNotificationChime()
                    }
                )
            }

            4 -> {
                ProfileSetupStep(
                    userName = userName,
                    isInitializing = isInitializing,
                    onNameChanged = { userName = it },
                    onFinish = {
                        isInitializing = true
                    },
                    onReady = {
                        onRegistrationComplete(fullPhoneNumber, userName)
                    }
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep(onContinue: () -> Unit) {
    val extraColors = LocalWhatsAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = "Te damos la bienvenida a WhatsApp",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = extraColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // WhatsApp Circular Welcome Illustration
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clip(CircleShape)
                    .background(WhatsAppGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.whatsapp_launcher_icon_1791684220563),
                    contentDescription = "WhatsApp",
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Text(
                text = "Lee nuestra Política de privacidad. Toca \"Aceptar y continuar\" para aceptar las Condiciones del servicio y habilitar el cifrado E2EE.",
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center,
                color = extraColors.textSecondary,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("welcome_accept_button")
            ) {
                Text(
                    text = "Aceptar y continuar",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PhoneInputStep(
    selectedCountry: CountryInfo,
    rawPhone: String,
    onCountrySelected: (CountryInfo) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onConfirmNumber: (String) -> Unit
) {
    val extraColors = LocalWhatsAppColors.current
    var countryMenuExpanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ingresa tu número de teléfono",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = WhatsAppGreen
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "WhatsApp necesitará verificar tu cuenta. Se te enviará un SMS con un código de seguridad. Introduce tu número real.",
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            color = extraColors.textSecondary,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Country Selector
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { countryMenuExpanded = true }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedCountry.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = extraColors.textPrimary
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Elegir país",
                    tint = WhatsAppGreen
                )
            }

            DropdownMenu(
                expanded = countryMenuExpanded,
                onDismissRequest = { countryMenuExpanded = false }
            ) {
                COUNTRIES.forEach { country ->
                    DropdownMenuItem(
                        text = { Text("${country.name} (${country.code})") },
                        onClick = {
                            onCountrySelected(country)
                            countryMenuExpanded = false
                        }
                    )
                }
            }
        }

        HorizontalDivider(color = WhatsAppGreen, thickness = 1.5.dp)

        Spacer(modifier = Modifier.height(16.dp))

        // Phone Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedCountry.code,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = extraColors.textPrimary,
                modifier = Modifier
                    .width(60.dp)
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            BasicTextField(
                value = rawPhone,
                onValueChange = {
                    val digitsOnly = it.filter { ch -> ch.isDigit() }.take(12)
                    onPhoneChanged(digitsOnly)
                    validationError = null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 18.sp,
                    color = extraColors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("phone_number_input"),
                decorationBox = { innerTextField ->
                    if (rawPhone.isEmpty()) {
                        Text(
                            text = "número de teléfono",
                            color = extraColors.textSecondary,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )
        }

        HorizontalDivider(
            color = if (validationError != null) WhatsAppDangerRed else WhatsAppGreen,
            thickness = 1.5.dp
        )

        if (validationError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = validationError!!,
                color = WhatsAppDangerRed,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val clean = rawPhone.trim()
                if (clean.length < 8) {
                    validationError = "Introduce un número de teléfono real válido (mínimo 8 dígitos)"
                } else {
                    showConfirmDialog = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_phone_button")
        ) {
            Text("Siguiente", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showConfirmDialog) {
        val fullFormatted = "${selectedCountry.code} $rawPhone"
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("¿Es correcto este número?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "$fullFormatted\n\nSe enviará un código de verificación por SMS.",
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onConfirmNumber(fullFormatted)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Editar")
                }
            }
        )
    }
}

@Composable
private fun SmsVerificationStep(
    phoneNumber: String,
    generatedCode: String,
    enteredOtp: String,
    isError: Boolean,
    cooldownSec: Int,
    showBanner: Boolean,
    onOtpChange: (String) -> Unit,
    onAutoFillClick: () -> Unit,
    onTriggerBanner: () -> Unit,
    onResendCode: () -> Unit
) {
    val extraColors = LocalWhatsAppColors.current

    // Trigger SMS delivery simulation after 1.2s
    LaunchedEffect(generatedCode) {
        delay(1200)
        onTriggerBanner()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Simulated Push Notification Banner for Incoming SMS at Top
        AnimatedVisibility(
            visible = showBanner,
            enter = fadeIn() + slideInVertically()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("sms_notification_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "SMS",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WhatsApp • Mensajes",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = extraColors.textSecondary
                        )
                        Text(
                            text = "Tu código de verificación de WhatsApp es $generatedCode. No lo compartas.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extraColors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onAutoFillClick,
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Pegar", fontSize = 12.sp)
                    }
                }
            }
        }

        Text(
            text = "Verificando tu número",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = WhatsAppGreen
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Esperando para detectar automáticamente el SMS enviado a\n$phoneNumber",
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            color = extraColors.textSecondary,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 6 Digit OTP Display Boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 6) {
                val digit = enteredOtp.getOrNull(i)?.toString() ?: ""
                val isFocused = enteredOtp.length == i

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .padding(horizontal = 4.dp)
                        .border(
                            width = if (isFocused) 2.dp else 1.dp,
                            color = if (isError) WhatsAppDangerRed else if (isFocused) WhatsAppGreen else extraColors.textSecondary.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = digit,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = extraColors.textPrimary
                    )
                }

                if (i == 2) {
                    Text(" - ", fontSize = 20.sp, color = extraColors.textSecondary)
                }
            }
        }

        // Invisible Keyboard Input for OTP
        BasicTextField(
            value = enteredOtp,
            onValueChange = onOtpChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .size(1.dp)
                .testTag("otp_hidden_input")
        )

        if (isError) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Código incorrecto. Por favor verifica el SMS recibido.",
                color = WhatsAppDangerRed,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Ingresa el código de 6 dígitos",
            fontSize = 13.sp,
            color = extraColors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onResendCode,
            enabled = cooldownSec == 0
        ) {
            Text(
                text = if (cooldownSec > 0) "Reenviar SMS en ${cooldownSec}s" else "Reenviar SMS",
                color = if (cooldownSec > 0) extraColors.textSecondary else WhatsAppGreen,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Fast autocomplete button
        Button(
            onClick = onAutoFillClick,
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("autofill_sms_button")
        ) {
            Text("Verificar con código recibido ($generatedCode)", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileSetupStep(
    userName: String,
    isInitializing: Boolean,
    onNameChanged: (String) -> Unit,
    onFinish: () -> Unit,
    onReady: () -> Unit
) {
    val extraColors = LocalWhatsAppColors.current

    LaunchedEffect(isInitializing) {
        if (isInitializing) {
            delay(1500) // Simulate E2EE Key Initialization
            onReady()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Info. del perfil",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = WhatsAppGreen
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Por favor escribe tu nombre y añade una foto de perfil opcional para chatear.",
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            color = extraColors.textSecondary
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Profile Avatar Holder
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(WhatsAppGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Foto de perfil",
                tint = WhatsAppGreen,
                modifier = Modifier.size(54.dp)
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(WhatsAppGreen)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Cambiar foto",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onNameChanged,
            label = { Text("Escribe tu nombre aquí") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("user_name_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = WhatsAppGreen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Cifrado de extremo a extremo listo para activarse",
                fontSize = 12.sp,
                color = WhatsAppGreen
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isInitializing) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = WhatsAppGreen, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Iniciando claves criptográficas E2EE...",
                    fontSize = 13.sp,
                    color = extraColors.textSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            Button(
                onClick = {
                    onFinish()
                },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("finish_onboarding_button")
            ) {
                Text("Siguiente", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
