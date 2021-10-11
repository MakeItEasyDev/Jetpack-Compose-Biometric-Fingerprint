package com.jetpack.biometricfingerprint

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.jetpack.biometricfingerprint.ui.theme.BiometricFingerPrintTheme
import com.jetpack.biometricfingerprint.ui.theme.LightGray100
import com.jetpack.biometricfingerprint.ui.theme.Purple500

class MainActivity : ComponentActivity() {
    private var cancellationSignal: CancellationSignal? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometricFingerPrintTheme {
                Surface(color = MaterialTheme.colors.background) {
                    BioMetricScreen(onClick = { launchBiometric() })
                }
            }
        }
    }

    private val authenticationCallback: BiometricPrompt.AuthenticationCallback =
        @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(this@MainActivity, "Authentication Succeeded", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@MainActivity, "Authentication Error code: $errorCode", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }

            override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                super.onAuthenticationHelp(helpCode, helpString)
            }
        }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkBiometricSupport(): Boolean {
        val keyGuardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyGuardManager.isDeviceSecure) {
            return true
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            return false
        }

        return packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun launchBiometric() {
        if (checkBiometricSupport()) {
            val biometricPrompt = BiometricPrompt.Builder(this)
                .apply {
                    setTitle(getString(R.string.prompt_info_title))
                    setSubtitle(getString(R.string.prompt_info_subtitle))
                    setDescription(getString(R.string.prompt_info_description))
                    setConfirmationRequired(false)
                    setNegativeButton(getString(R.string.prompt_info_use_app_password), mainExecutor, { _, _, ->
                        Toast.makeText(this@MainActivity, "Authentication Cancelled", Toast.LENGTH_SHORT).show()
                    })
                }.build()

            biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, authenticationCallback)
        }
    }

    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            Toast.makeText(this, "Authentication Cancelled Signal", Toast.LENGTH_SHORT).show()
        }

        return cancellationSignal as CancellationSignal
    }
}

@Composable
fun BioMetricScreen(
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val emailVal = remember { mutableStateOf("") }
    val passwordVal = remember { mutableStateOf("") }
    val passwordVisibility = remember { mutableStateOf(false) }
    val checked = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Purple500),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "BioMetric FingerPrint",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.login_img),
                contentDescription = "Login image",
                modifier = Modifier
                    .width(250.dp)
                    .height(250.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.padding(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(LightGray100),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.padding(15.dp))

                OutlinedTextField(
                    value = emailVal.value,
                    onValueChange = { emailVal.value = it },
                    label = { Text(text = "Email Address") },
                    placeholder = { Text(text = "Email Address") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                OutlinedTextField(
                    value = passwordVal.value,
                    onValueChange = { passwordVal.value = it },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                passwordVisibility.value = !passwordVisibility.value
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.password_eye),
                                contentDescription = "password eye",
                                tint = if (passwordVisibility.value) Purple500 else Color.Gray
                            )
                        }
                    },
                    label = { Text(text = "Password") },
                    placeholder = { Text(text = "Password") },
                    leadingIcon = {  Icon(Icons.Filled.Lock, contentDescription = "Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisibility.value)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                Spacer(modifier = Modifier.padding(10.dp))

                Button(
                    onClick = {
                        if (emailVal.value.isEmpty()) {
                            Toast.makeText(context, "Please enter email address!", Toast.LENGTH_SHORT).show()
                        } else if (passwordVal.value.isEmpty()) {
                            Toast.makeText(context, "Please enter password!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Logged Successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                ) {
                    Text(text = "Sign In", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.padding(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(text = "Enable Biometric Prompt", fontSize = 20.sp)

                    Switch(
                        checked = checked.value,
                        onCheckedChange = {
                            checked.value = it
                            if (checked.value) {
                                onClick()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.padding(10.dp))
            }
        }
    }
}















