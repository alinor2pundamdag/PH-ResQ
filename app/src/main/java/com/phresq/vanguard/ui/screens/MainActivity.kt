package com.phresq.vanguard.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phresq.vanguard.data.mesh.BleMeshManager
import com.phresq.vanguard.data.spatial.SpatialEngine

sealed class OnboardingState {
    object PendingConsent : OnboardingState()
    object DeclinedLocked : OnboardingState()
    object Initialized : OnboardingState()
}

class MainActivity : ComponentActivity() {

    private lateinit var bleMeshManager: BleMeshManager
    private val spatialEngine = SpatialEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bleMeshManager = BleMeshManager(applicationContext)

        setContent {
            var onboardingState by remember { mutableStateOf<OnboardingState>(OnboardingState.PendingConsent) }
            var sosActive by remember { mutableStateOf(false) }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF0A0A0A),
                    surface = Color(0xFF1A1A1A),
                    primary = Color(0xFFCE1126),
                    secondary = Color(0xFF0038A3)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (onboardingState) {
                        is OnboardingState.PendingConsent -> ConsentScreen(
                            onAccept = { onboardingState = OnboardingState.Initialized },
                            onDecline = { onboardingState = OnboardingState.DeclinedLocked }
                        )
                        is OnboardingState.DeclinedLocked -> HardLockoutScreen()
                        is OnboardingState.Initialized -> DashboardScreen(
                            sosActive = sosActive,
                            onToggleSos = { sosActive = !sosActive }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleMeshManager.stopMesh()
    }
}

@Composable
fun ConsentScreen(onAccept: () -> Unit, onDecline: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PH-RESQ VANGUARD 2026",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFCD116)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Republic Act No. 10173 (Data Privacy Act) Compliance Notice:\n\n" +
                    "This crisis application operates entirely offline via local P2P Bluetooth mesh. " +
                    "Your Medical ID and emergency keys are encrypted locally inside your device's hardware SecureVault. " +
                    "By clicking Accept, you consent to participate in local emergency relay routing.",
            fontSize = 14.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAccept,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0038A3))
        ) {
            Text("TANGGAPIN / ACCEPT TERMS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onDecline,
            modifier = Modifier.fillMaxWidth().height(58.dp)
        ) {
            Text("TANGGI / DECLINE & EXIT", fontSize = 16.sp, color = Color(0xFFCE1126))
        }
    }
}

@Composable
fun HardLockoutScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Access Denied. Consent is mandatory for PH-ResQ emergency operations.",
            color = Color(0xFFCE1126),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DashboardScreen(sosActive: Boolean, onToggleSos: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = if (sosActive) "🚨 SOS BROADCASTING ACTIVE" else "🟢 P2P MESH STANDBY",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (sosActive) Color(0xFFCE1126) else Color(0xFF00FF00)
            )
            Text(
                text = "Region IX • Zamboanga Sibugay • Naga",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Button(
            onClick = onToggleSos,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (sosActive) Color.DarkGray else Color(0xFFCE1126)
            )
        ) {
            Text(
                text = if (sosActive) "CANCEL SOS" else "PANIC SOS / SAKLOLO",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Text(
            text = "Mesh peers within range: Scanning...",
            fontSize = 12.sp,
            color = Color.LightGray
        )
    }
}
