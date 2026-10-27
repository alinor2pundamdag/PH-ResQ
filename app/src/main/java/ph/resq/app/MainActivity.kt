package ph.resq.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ph.resq.app.core.ContactList
import ph.resq.app.core.LastKnownLocation
import ph.resq.app.core.LocalVault
import ph.resq.app.core.ResQDatabase
import ph.resq.app.core.EncryptedFieldCipher
import ph.resq.app.core.ConsentStore
import ph.resq.app.core.EncryptedVoiceNoteRecorder
import ph.resq.app.core.BleDiscoveryManager
import ph.resq.app.core.NearbyBlePeer
import ph.resq.app.core.SosMessage
import ph.resq.app.core.SosState
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ResQRoot() }
    }
}

@Composable
private fun ResQRoot() {
    val context = LocalContext.current
    val consentStore = remember { ConsentStore(context) }
    var consentAccepted by remember { mutableStateOf(consentStore.isAccepted()) }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (!consentAccepted) {
                ConsentScreen(
                    onAccept = {
                        consentStore.setAccepted(true)
                        consentAccepted = true
                    },
                    onDecline = { (context as? Activity)?.finish() }
                )
            } else {
                HomeScreen()
            }
        }
    }
}

@Composable
private fun ConsentScreen(onAccept: () -> Unit, onDecline: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("PH-ResQ", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("Offline-first emergency tools for the Philippines.")
        Spacer(Modifier.height(12.dp))
        Text("Early build: wala pang verified nationwide directory, offline map packages, mesh transport, o guaranteed emergency delivery. Bubuksan ang messaging app para ma-review at ikaw ang magpadala ng SMS.")
        Spacer(Modifier.height(20.dp))
        Button(onClick = onAccept, modifier = Modifier.fillMaxWidth()) { Text("Nauunawaan ko at magpatuloy") }
        Button(onClick = onDecline, modifier = Modifier.fillMaxWidth()) { Text("Hindi ako pumapayag — lumabas") }
    }
}

@Composable
private fun HomeScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ph_resq_local", Context.MODE_PRIVATE) }
    val vault = remember { LocalVault(context) }
    val database = remember { ResQDatabase(context) }
    val fieldCipher = remember { EncryptedFieldCipher(context) }
    var contactsRaw by remember { mutableStateOf(runCatching { vault.loadContacts() }.getOrNull() ?: "") }
    var locationLine by remember {
        mutableStateOf(
            runCatching { fieldCipher.decrypt(prefs.getString("last_location_line_cipher", null)) }.getOrNull()
                ?: prefs.getString("last_location_line", null)?.also { legacy ->
                    runCatching {
                        prefs.edit().putString("last_location_line_cipher", fieldCipher.encrypt(legacy)).remove("last_location_line").apply()
                    }
                }
                ?: "Lokasyon: walang pinakahuling fix; hindi available"
        )
    }
    var messageNote by remember { mutableStateOf("") }
    var state by remember { mutableStateOf(database.latestSosState() ?: runCatching { SosState.valueOf(prefs.getString("sos_state", "IDLE") ?: "IDLE") }.getOrDefault(SosState.IDLE)) }
    var checkIn by remember { mutableStateOf(prefs.getString("checkin", "Hindi pa naitala") ?: "Hindi pa naitala") }
    val voiceRecorder = remember { EncryptedVoiceNoteRecorder.cleanupStaleRawFiles(context); EncryptedVoiceNoteRecorder() }
    val bleDiscovery = remember { BleDiscoveryManager(context) }
    var blePeers by remember { mutableStateOf(emptyList<NearbyBlePeer>()) }
    var bleStatus by remember { mutableStateOf("Bluetooth discovery not started") }
    DisposableEffect(bleDiscovery) {
        onDispose { bleDiscovery.stop() }
    }
    var voiceRecording by remember { mutableStateOf(false) }
    var voiceStatus by remember { mutableStateOf("Wala pang voice note") }
    fun beginBleScan() {
        try {
            bleStatus = "Scanning for up to 15 seconds…"
            bleDiscovery.start(
                onDevicesChanged = { peers -> blePeers = peers },
                onFinished = { bleStatus = "Scan complete • " + blePeers.size + " nearby device(s); no data exchanged" }
            )
        } catch (_: Exception) {
            bleStatus = "Bluetooth unavailable, disabled, or permission denied."
            Toast.makeText(context, bleStatus, Toast.LENGTH_LONG).show()
        }
    }
    val blePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) beginBleScan()
        else bleStatus = "Bluetooth permission denied; discovery not started."
    }
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                voiceRecorder.start(context) { file ->
                    voiceRecording = false
                    voiceStatus = if (file != null) "Encrypted local note saved: " + file.name + " (hindi pa nakakabit sa SMS)" else "Hindi na-save ang recording."
                }
                voiceRecording = true
                voiceStatus = "Recording… maximum 10 seconds"
            } catch (_: Exception) {
                voiceRecording = false
                voiceStatus = "Hindi masimulan ang recording."
                Toast.makeText(context, "Hindi masimulan ang voice recording.", Toast.LENGTH_LONG).show()
            }
        } else {
            voiceStatus = "Microphone permission denied; walang recording."
        }
    }
    fun setSosState(next: SosState, draft: String? = null, recipients: String? = null) {
        state = next
        prefs.edit().putString("sos_state", next.name).putLong("sos_state_updated_at", System.currentTimeMillis()).apply()
        runCatching { database.recordSosEvent(next, draft, recipients, locationLine) }
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val location = LastKnownLocation.read(context)
        locationLine = if (location == null) {
            "Lokasyon: walang pinakahuling fix; hindi available"
        } else {
            "Huling lokasyon (hindi live guarantee): " + LastKnownLocation.format(location)
        }
        runCatching { prefs.edit().putString("last_location_line_cipher", fieldCipher.encrypt(locationLine)).remove("last_location_line").apply() }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("PH-ResQ", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Emergency-first • offline-aware • early build")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("National Emergency Hotline", style = MaterialTheme.typography.titleLarge)
                Text("911 • Source: Philippine Government Emergency Hotlines portal • reviewed 2026-10-27")
                Button(onClick = {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                    if (dialIntent.resolveActivity(context.packageManager) != null) context.startActivity(dialIntent)
                    else Toast.makeText(context, "Hindi available ang phone dialer sa device na ito.", Toast.LENGTH_LONG).show()
                }, modifier = Modifier.fillMaxWidth()) { Text("OPEN DIALER — 911") }
                Text("Kailangan ng suportadong phone/dialer at network. Hindi ito automatic call o garantiya ng pagsagot.")
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SOS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Pindutin at hawakan nang 2 segundo upang ihanda ang SMS. Bubuksan ang messaging app; kailangan mong i-review at ipadala.")
                SosHoldButton(
                    modifier = Modifier.fillMaxWidth(),
                    onConfirmed = {
                        setSosState(SosState.QUEUED)
                        vibrate(context)
                        val recipients = ContactList.parse(contactsRaw)
                        if (recipients.isEmpty()) {
                            Toast.makeText(context, "Maglagay muna ng emergency contact.", Toast.LENGTH_LONG).show()
                            setSosState(SosState.FAILED)
                        } else {
                            val body = SosMessage.build(messageNote, locationLine)
                            val uri = Uri.parse("smsto:" + recipients.joinToString(";"))
                            val intent = Intent(Intent.ACTION_SENDTO, uri).apply { putExtra("sms_body", body) }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                                setSosState(SosState.HANDED_OFF, body, recipients.joinToString(";"))
                            } else {
                                state = SosState.FAILED
                                Toast.makeText(context, "Walang messaging app na kayang magbukas ng SMS.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
                Text("SOS status: " + state.label)
                OutlinedTextField(value = messageNote, onValueChange = { messageNote = it.take(240) }, label = { Text("Optional na mensahe") }, modifier = Modifier.fillMaxWidth())
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Emergency contacts", style = MaterialTheme.typography.titleLarge)
                Text("Ilagay ang mga numero na pinaghihiwalay ng kuwit o bagong linya (hanggang 10). Nasa device lang ang listahan.")
                OutlinedTextField(value = contactsRaw, onValueChange = {
                    contactsRaw = it
                    val saved = runCatching { vault.saveContacts(it) }.isSuccess
                    if (!saved) Toast.makeText(context, "Hindi ligtas na na-save ang contacts. Huwag umasa sa storage na ito.", Toast.LENGTH_LONG).show()
                }, label = { Text("Hal. +639171234567, 09181234567") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Text("Valid-looking contacts: " + ContactList.parse(contactsRaw).size)
                Text("Naka-encrypt at rest gamit ang Android Keystore; wala pang PIN vault o recovery flow.")
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Last-known location", style = MaterialTheme.typography.titleLarge)
                Text(locationLine)
                Text("Ang pinakahuling lokasyon ay maaaring luma; hindi ito real-time tracking.")
                Button(onClick = {
                    locationPermissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ))
                }) { Text("Request permission and read last-known location") }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nearby BLE devices", style = MaterialTheme.typography.titleLarge)
                Text("15-second low-power discovery window. This only detects nearby BLE advertisements; it does not send SOS packets or implement multi-hop mesh.")
                Text(bleStatus)
                Button(onClick = {
                    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
                    } else {
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    blePermissionLauncher.launch(permissions)
                }, modifier = Modifier.fillMaxWidth()) { Text("SCAN NEARBY DEVICES (15s)") }
                blePeers.take(10).forEach { peer -> Text(peer.label + " • RSSI " + peer.rssi + " dBm (approximate proximity only)") }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Voice note", style = MaterialTheme.typography.titleLarge)
                Text("Hanggang 10 segundo; naka-encrypt sa local app storage. Sa kasalukuyan, hindi pa ito awtomatikong nakakabit sa SMS.")
                Text(voiceStatus)
                Button(onClick = {
                    if (voiceRecording) voiceRecorder.stop()
                    else microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (voiceRecording) "STOP AND ENCRYPT NOTE" else "RECORD VOICE NOTE")
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ligtas Ako check-in", style = MaterialTheme.typography.titleLarge)
                Text("Local status lamang; wala pang network relay o family delivery.")
                Text("Status: " + checkIn)
                Button(onClick = {
                    checkIn = "Naitala locally • " + System.currentTimeMillis()
                    prefs.edit().putString("checkin", checkIn).apply()
                }) { Text("Save check-in locally") }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Directory, maps, mesh and vault", style = MaterialTheme.typography.titleLarge)
                Text("Hindi pa available sa build na ito. Walang pekeng helpline, mapa, peer scan, o security status.")
            }
        }
        Text("Ang SMS handoff ay hindi patunay ng delivery. Sa agarang panganib, gamitin ang available na opisyal na emergency channel.")
    }
}

private fun vibrate(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(150)
        }
    } catch (_: Exception) {
        // Haptics are best-effort; the app remains usable without vibration hardware.
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SosHoldButton(modifier: Modifier = Modifier, onConfirmed: () -> Unit) {
    Button(
        onClick = {},
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    val releasedEarly = withTimeoutOrNull(2_000L) { tryAwaitRelease() }
                    if (releasedEarly == null) onConfirmed()
                }
            )
        }
    ) {
        Text("HOLD FOR 2 SECONDS — PREPARE SOS")
    }
}
