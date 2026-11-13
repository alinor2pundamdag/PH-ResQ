package ph.resq.app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.location.LocationManager
import android.app.Activity
import android.graphics.Bitmap
import android.content.ClipData
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ph.resq.app.core.ContactList
import ph.resq.app.core.MedicalIdQr
import ph.resq.app.core.LastKnownLocation
import ph.resq.app.core.FreshLocationProvider
import ph.resq.app.core.LocalVault
import ph.resq.app.core.PinProtectedVault
import ph.resq.app.core.ResQDatabase
import ph.resq.app.core.EncryptedFieldCipher
import ph.resq.app.core.ConsentStore
import ph.resq.app.core.EncryptedVoiceNoteRecorder
import ph.resq.app.core.BleDiscoveryManager
import ph.resq.app.core.NationalHotlineDirectory
import ph.resq.app.core.HotlineMethod
import ph.resq.app.core.NearbyBlePeer
import ph.resq.app.core.SosMessage
import ph.resq.app.core.SosState
import ph.resq.app.core.OfflineDataRepository
import ph.resq.app.core.HazardDataRepository
import ph.resq.app.core.OfflineDatasetImporter
import ph.resq.app.core.SafeVolumePolicy
import ph.resq.app.core.DeviceCapabilityProfile
import ph.resq.app.core.CapabilityState
import ph.resq.app.core.SettingsRepository
import ph.resq.app.core.FeaturePolicyCoordinator
import ph.resq.app.core.FeatureAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

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
    val settings = remember { SettingsRepository(context) }
    var highContrast by remember { mutableStateOf(settings.highContrast) }
    val colors = if (highContrast) darkColorScheme(
        primary = Color.Yellow, onPrimary = Color.Black,
        secondary = Color.Cyan, onSecondary = Color.Black,
        background = Color.Black, onBackground = Color.White,
        surface = Color.Black, onSurface = Color.White
    ) else darkColorScheme(
        primary = Color(0xFFFF5268), onPrimary = Color.Black,
        secondary = Color(0xFF7DB7FF), onSecondary = Color.Black,
        background = Color(0xFF07111F), onBackground = Color.White,
        surface = Color(0xFF101D2E), onSurface = Color.White,
        error = Color(0xFFFF6B6B), onError = Color.Black
    )
    MaterialTheme(colorScheme = colors) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (!consentAccepted) {
                ConsentScreen(
                    onAccept = {
                        consentStore.setAccepted(true)
                        consentAccepted = true
                    },
                    onDecline = { consentStore.setAccepted(false); (context as? Activity)?.finish() }
                )
            } else {
                HomeScreen(
                    onToggleHighContrast = {
                        highContrast = !highContrast
                        settings.setHighContrast(highContrast)
                    },
                    onReviewPrivacy = {
                        consentStore.setAccepted(false)
                        consentAccepted = false
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(onToggleHighContrast: () -> Unit, onReviewPrivacy: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var capabilityProfile by remember { mutableStateOf(DeviceCapabilityProfile.detect(context)) }
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) capabilityProfile = DeviceCapabilityProfile.detect(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED,
                    PowerManager.ACTION_POWER_SAVE_MODE_CHANGED,
                    LocationManager.MODE_CHANGED_ACTION -> capabilityProfile = DeviceCapabilityProfile.detect(context)
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            addAction(LocationManager.MODE_CHANGED_ACTION)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
    var medicalName by remember { mutableStateOf("") }
    var medicalBloodType by remember { mutableStateOf("") }
    var medicalAllergies by remember { mutableStateOf("") }
    var medicalMedications by remember { mutableStateOf("") }
    var medicalEmergencyContact by remember { mutableStateOf("") }
    var medicalQrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var medicalQrStatus by remember { mutableStateOf("Walang QR na ginawa.") }
    val settings = remember { SettingsRepository(context) }
    val prefs = remember { context.getSharedPreferences("ph_resq_local", Context.MODE_PRIVATE) }
    var reduceMotion by remember { mutableStateOf(settings.reduceMotion) }
    val vault = remember { LocalVault(context) }
    val pinVault = remember { PinProtectedVault(context) }
    var pinVaultEnabled by remember { mutableStateOf(prefs.getBoolean("pin_vault_enabled", false) || pinVault.isConfigured()) }
    var pinVaultUnlocked by remember { mutableStateOf(!pinVaultEnabled) }
    var pinVaultPin by remember { mutableStateOf("") }
    var pinVaultStatus by remember { mutableStateOf(if (pinVaultEnabled) "Vault locked. I-unlock bago gumamit ng saved contacts." else "Optional PIN vault is disabled.") }
    val database = remember { ResQDatabase(context) }
    val freshLocationProvider = remember { FreshLocationProvider(context) }
    var freshLocationStatus by remember { mutableStateOf("Hindi pa humihingi ng fresh location.") }
    var batterySnapshot by remember { mutableStateOf(runCatching { database.getAppState("low_battery_snapshot") }.getOrNull() ?: "Wala pang low-battery snapshot.") }
    var pendingQueueCount by remember { mutableStateOf(runCatching { database.pendingActionCount() }.getOrDefault(0)) }
    DisposableEffect(freshLocationProvider) {
        onDispose { freshLocationProvider.cancel() }
    }
    DisposableEffect(database) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val level = intent?.getIntExtra("level", -1) ?: -1
                val scale = intent?.getIntExtra("scale", 100) ?: 100
                if (level >= 0 && scale > 0) {
                    val percent = (level * 100) / scale
                    if (percent <= 5) {
                        val previous = runCatching { database.getAppState("low_battery_level") }.getOrNull()
                        if (previous != percent.toString()) {
                            val snapshot = "Battery <=5% ($percent%) • captured ${System.currentTimeMillis()}"
                            runCatching {
                                database.putAppState("low_battery_snapshot", snapshot)
                                database.putAppState("low_battery_level", percent.toString())
                                batterySnapshot = snapshot
                            }
                        }
                    } else {
                        runCatching { database.putAppState("low_battery_level", percent.toString()) }
                    }
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
    val fieldCipher = remember { EncryptedFieldCipher() }
    val initialContactsLoad = remember {
        if (pinVaultEnabled) Result.success("") else runCatching { vault.loadContacts() }
    }
    var contactsLoadAccessible by remember { mutableStateOf(initialContactsLoad.isSuccess) }
    var contactsLoadError by remember { mutableStateOf(initialContactsLoad.exceptionOrNull()?.message ?: "") }
    var contactsRaw by remember { mutableStateOf(initialContactsLoad.getOrDefault("")) }
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
    var state by remember { mutableStateOf(runCatching { database.latestSosState() }.getOrNull() ?: runCatching { SosState.valueOf(prefs.getString("sos_state", "IDLE") ?: "IDLE") }.getOrDefault(SosState.IDLE)) }
    var activeSosEventId by remember { mutableStateOf(runCatching { database.latestSosEventSnapshot()?.eventId }.getOrNull()) }
    var sosRecoveryNotice by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(database) {
        val recovery = withContext(Dispatchers.IO) {
            runCatching {
                val count = database.reconcileInterruptedSosHandoffs()
                count to (database.latestSosState() to database.latestSosEventSnapshot()?.eventId)
            }.getOrNull()
        }
        if (recovery != null && recovery.first > 0) {
            state = recovery.second.first ?: state
            activeSosEventId = recovery.second.second
            sosRecoveryNotice = "${recovery.first} interrupted SOS event(s) recovered. Review the messaging app; PH-ResQ did not resend anything."
        }
    }
    var checkIn by remember { mutableStateOf(runCatching { database.latestFamilyCheckin() }.getOrNull() ?: prefs.getString("checkin", "Hindi pa naitala") ?: "Hindi pa naitala") }
    var hotlineQuery by remember { mutableStateOf("") }
    var recentSearches by remember { mutableStateOf(runCatching { database.recentSearches() }.getOrDefault(emptyList())) }
    var savedBookmarks by remember { mutableStateOf(runCatching { database.bookmarks() }.getOrDefault(emptyList())) }
    val offlineDataRepository = remember { OfflineDataRepository(context, database) }
    val hazardDataRepository = remember { HazardDataRepository(database) }
    var offlineQuery by remember { mutableStateOf("") }
    var directoryResults by remember { mutableStateOf(emptyList<ph.resq.app.core.DirectoryRecord>()) }
    var directoryMetadata by remember { mutableStateOf(runCatching { offlineDataRepository.datasetMetadata() }.getOrNull()) }
    var advisoryItems by remember { mutableStateOf(runCatching { hazardDataRepository.advisories() }.getOrDefault(emptyList())) }
    var evacuationItems by remember { mutableStateOf(emptyList<Pair<ph.resq.app.core.EvacuationCenter, Boolean>>()) }
    var offlineImportStatus by remember { mutableStateOf("Walang offline dataset na na-import sa session na ito.") }
    val offlineImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            offlineImportStatus = runCatching { OfflineDatasetImporter.importDataset(context, uri, offlineDataRepository, hazardDataRepository) }
                .getOrElse { "Import failed: " + (it.message ?: "invalid dataset") }
            directoryMetadata = runCatching { offlineDataRepository.datasetMetadata() }.getOrNull()
            directoryResults = runCatching { offlineDataRepository.searchDirectory(offlineQuery, 100) }.getOrDefault(emptyList())
            evacuationItems = runCatching { hazardDataRepository.searchEvacuationCenters(offlineQuery) }.getOrDefault(emptyList())
            advisoryItems = runCatching { hazardDataRepository.advisories() }.getOrDefault(emptyList())
        }
    }
    val voiceRecorder = remember { EncryptedVoiceNoteRecorder.cleanupStaleRawFiles(context); EncryptedVoiceNoteRecorder() }
    val bleDiscovery = remember { BleDiscoveryManager(context) }
    var blePeers by remember { mutableStateOf(emptyList<NearbyBlePeer>()) }
    var bleStatus by remember { mutableStateOf("Bluetooth discovery not started") }
    DisposableEffect(bleDiscovery) {
        onDispose { bleDiscovery.stop() }
    }
    var voiceRecording by remember { mutableStateOf(false) }
    var voiceStatus by remember { mutableStateOf("Wala pang voice note") }
    var voicePlaybackVolume by remember { mutableStateOf(settings.voicePlaybackVolume) }
    var savedVoiceNotes by remember { mutableStateOf(voiceRecorder.listSaved(context)) }
    var voicePlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var voicePlaybackTemp by remember { mutableStateOf<File?>(null) }
    var voiceShareTemp by remember { mutableStateOf<File?>(null) }
    val voiceShareLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        voiceShareTemp?.delete()
        voiceShareTemp = null
        voiceStatus = "Voice-note share flow ended; delivery is not confirmed."
    }
    DisposableEffect(Unit) {
        onDispose {
            runCatching { voicePlayer?.stop() }
            runCatching { voicePlayer?.release() }
            voicePlayer = null
            voicePlaybackTemp?.delete()
            voicePlaybackTemp = null
        }
    }
    fun stopVoicePlayback() {
        runCatching { voicePlayer?.stop() }
        runCatching { voicePlayer?.release() }
        voicePlayer = null
        voicePlaybackTemp?.delete()
        voicePlaybackTemp = null
        voiceStatus = "Playback stopped"
    }
    fun playVoiceNote(encryptedFile: File) {
        stopVoicePlayback()
        var temp: File? = null
        var player: MediaPlayer? = null
        try {
            val playbackFile = voiceRecorder.decryptToTemporaryFile(context, encryptedFile)
            temp = playbackFile
            val activePlayer = MediaPlayer()
            player = activePlayer
            activePlayer.setDataSource(playbackFile.absolutePath)
            val safeVolume = SafeVolumePolicy.clamp(voicePlaybackVolume)
            activePlayer.setVolume(safeVolume, safeVolume)
            activePlayer.setOnCompletionListener { completed ->
                runCatching { completed.release() }
                playbackFile.delete()
                if (voicePlayer === completed) voicePlayer = null
                if (voicePlaybackTemp == playbackFile) voicePlaybackTemp = null
                voiceStatus = "Playback complete"
            }
            activePlayer.setOnErrorListener { failed, _, _ ->
                runCatching { failed.release() }
                playbackFile.delete()
                if (voicePlayer === failed) voicePlayer = null
                if (voicePlaybackTemp == playbackFile) voicePlaybackTemp = null
                voiceStatus = "Playback failed; temporary decrypted file removed"
                true
            }
            activePlayer.prepare()
            activePlayer.start()
            voicePlayer = activePlayer
            voicePlaybackTemp = playbackFile
            voiceStatus = "Playing encrypted voice note"
        } catch (_: Exception) {
            runCatching { player?.release() }
            temp?.delete()
            voiceStatus = "Cannot play this note; it may be corrupt or its Keystore key is unavailable"
        }
    }
    fun shareVoiceNote(encryptedFile: File) {
        var temp: File? = null
        try {
            val plaintextFile = voiceRecorder.decryptToTemporaryFile(context, encryptedFile)
            temp = plaintextFile
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", plaintextFile)
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "audio/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, SosMessage.build(messageNote, locationLine))
                clipData = ClipData.newUri(context.contentResolver, "PH-ResQ SOS voice note", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            voiceShareTemp?.delete()
            voiceShareTemp = plaintextFile
            voiceStatus = "Choose a messaging app and recipient; the audio is shared as plaintext. Sending/delivery is not confirmed."
            voiceShareLauncher.launch(Intent.createChooser(share, "Share SOS message and voice note"))
        } catch (_: Exception) {
            temp?.delete()
            voiceShareTemp = null
            voiceStatus = "Could not prepare voice-note share; no message was sent."
        }
    }
    var showAccessibleSosConfirm by remember { mutableStateOf(false) }
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
        capabilityProfile = DeviceCapabilityProfile.detect(context)
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
                    savedVoiceNotes = voiceRecorder.listSaved(context)
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
        capabilityProfile = DeviceCapabilityProfile.detect(context)
    }
    fun setSosState(next: SosState, draft: String? = null, recipients: String? = null): Boolean {
        val savedEventId = runCatching {
            database.recordSosEvent(
                next,
                draft,
                recipients,
                locationLine,
                eventId = if (next == SosState.QUEUED) null else activeSosEventId
            )
        }.getOrNull()
        val saved = savedEventId != null
        if (saved) activeSosEventId = savedEventId
        if (!saved) {
            Toast.makeText(context, "Hindi ligtas na na-save ang SOS state. Walang external handoff na sisimulan.", Toast.LENGTH_LONG).show()
            return false
        }
        state = next
        // SQLite is the source of truth; this preference is only a fast UI restore hint.
        prefs.edit().putString("sos_state", next.name)
            .putLong("sos_state_updated_at", System.currentTimeMillis()).commit()
        return true
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
        capabilityProfile = DeviceCapabilityProfile.detect(context)
    }

    val freshLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            freshLocationStatus = "Humihingi ng bagong location fix (hanggang 15 segundo)…"
            freshLocationProvider.request { location, message ->
                freshLocationStatus = message
                if (location != null) {
                    locationLine = "Location fix: " + LastKnownLocation.format(location)
                    runCatching {
                        prefs.edit().putString("last_location_line_cipher", fieldCipher.encrypt(locationLine)).remove("last_location_line").apply()
                        database.putAppState("last_location_captured_at", location.time.toString())
                        database.putAppState("last_location_accuracy_m", if (location.hasAccuracy()) location.accuracy.toString() else "unknown")
                    }
                }
            }
        } else {
            freshLocationStatus = "Location permission denied; walang bagong fix."
        }
        capabilityProfile = DeviceCapabilityProfile.detect(context)
    }

    fun prepareSos() {
        if (!pinVaultEnabled && !contactsLoadAccessible) {
            Toast.makeText(context, "Hindi mabasa nang ligtas ang naka-save na contacts. Hindi babaguhin o papalitan ang naka-encrypt na data.", Toast.LENGTH_LONG).show()
            return
        }
        if (pinVaultEnabled && !pinVaultUnlocked) {
            Toast.makeText(context, "I-unlock muna ang PIN-protected emergency contacts.", Toast.LENGTH_LONG).show()
            return
        }
        val recipients = ContactList.parse(contactsRaw)
        if (recipients.isEmpty()) {
            Toast.makeText(context, "Maglagay muna ng emergency contact.", Toast.LENGTH_LONG).show()
            return
        }
        val body = SosMessage.build(messageNote, locationLine)
        val recipientList = recipients.joinToString(";")
        // Persist the user's exact SOS intent and recipient snapshot before any external side effect.
        if (!setSosState(SosState.QUEUED, body, recipientList)) return
        vibrate(context)
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + recipientList)).apply { putExtra("sms_body", body) }
        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                if (!setSosState(SosState.SENDING, body, recipientList)) return
                val notificationPermissionGranted =
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                            android.content.pm.PackageManager.PERMISSION_GRANTED
                val notificationStarted = if (
                    notificationPermissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
                ) {
                    runCatching {
                        ContextCompat.startForegroundService(
                            context,
                            Intent(context, SosStatusForegroundService::class.java).setAction(SosStatusForegroundService.ACTION_START)
                        )
                    }.isSuccess
                } else {
                    false
                }
                if (!notificationStarted) {
                    Toast.makeText(context, "Magpapatuloy ang SMS handoff, pero hindi masimulan ang persistent SOS status notification.", Toast.LENGTH_LONG).show()
                }
                context.startActivity(intent)
                setSosState(SosState.HANDED_OFF, body, recipientList)
            } else {
                setSosState(SosState.FAILED, body, recipientList)
                Toast.makeText(context, "Walang messaging app na kayang magbukas ng SMS.", Toast.LENGTH_LONG).show()
            }
        } catch (_: Exception) {
            runCatching {
                context.startService(Intent(context, SosStatusForegroundService::class.java).setAction(SosStatusForegroundService.ACTION_STOP))
            }
            setSosState(SosState.FAILED, body, recipientList)
            Toast.makeText(context, "Hindi mabuksan ang messaging app; hindi naipadalang SMS.", Toast.LENGTH_LONG).show()
        }
    }

    var sosNotificationStatus by remember {
        mutableStateOf(
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                "SOS status notifications are enabled."
            } else {
                "SOS status notifications are disabled."
            }
        )
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        sosNotificationStatus = if (granted && NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            "SOS status notifications are enabled."
        } else {
            "Notifications are disabled. SOS handoff still works, but persistent status may not appear."
        }
        capabilityProfile = DeviceCapabilityProfile.detect(context)
    }

    val flagTransition = rememberInfiniteTransition(label = "phFlag")
    val wave by flagTransition.animateFloat(
        initialValue = -1.4f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "flagWave"
    )
    val colorPhase by flagTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "flagTitleColor"
    )
    val visibleWave = if (reduceMotion) 0f else wave
    val visibleColorPhase = if (reduceMotion) 0f else colorPhase
    BoxWithConstraints(Modifier.fillMaxSize().safeContentPadding()) {
    val availableWidth = maxWidth
    val adaptiveContentWidth = if (availableWidth >= 900.dp) 820.dp else 720.dp
    val adaptiveLayoutLabel = when { availableWidth >= 900.dp -> "EXPANDED TABLET"; availableWidth >= 600.dp -> "MEDIUM"; else -> "COMPACT PHONE" }
    Column(Modifier.fillMaxWidth().widthIn(max = adaptiveContentWidth).align(Alignment.TopCenter).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.fillMaxWidth().height(8.dp).graphicsLayer(rotationZ = visibleWave)
                .background(Brush.horizontalGradient(listOf(Color.Blue, Color.Red, Color.White, Color.Yellow, Color.White, Color.Red, Color.Blue)))
        )
        Text("PH-ResQ", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color(red = visibleColorPhase, green = 0f, blue = 1f - visibleColorPhase))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Emergency cell broadcasts", style = MaterialTheme.typography.titleLarge)
                Text("Ang government/carrier emergency alerts ay pinamamahalaan ng Android at mobile network. Hindi inaangking binabasa o hinaharang ng PH-ResQ ang lahat ng cell broadcasts.")
                Text("Suriin ang Emergency alerts / Wireless emergency alerts sa system settings ng device at panatilihing naka-enable ang mga alertong naaangkop sa iyong lugar.")
            }
        }
        Text("Emergency-first • offline-aware • early build")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SOS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Pindutin at hawakan nang 2 segundo upang ihanda ang SMS. Bubuksan ang messaging app; kailangan mong i-review at ipadala.")
                Text(sosNotificationStatus)
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                                android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                            sosNotificationStatus = "Notifications are blocked in Android settings. Enable them for PH-ResQ."
                            Toast.makeText(context, "I-enable ang Notifications sa Android App info para sa PH-ResQ.", Toast.LENGTH_LONG).show()
                        } else {
                            sosNotificationStatus = "SOS status notifications are enabled."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("ENABLE SOS STATUS NOTIFICATIONS") }
                SosHoldButton(
                    modifier = Modifier.fillMaxWidth(),
                    onConfirmed = { prepareSos() }
                )
                Text("Kung hindi kayang gamitin ang press-and-hold, piliin ang accessible alternative at kumpirmahin sa susunod na hakbang.")
                Button(onClick = { showAccessibleSosConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("ACCESSIBLE SOS ALTERNATIVE")
                }
                if (showAccessibleSosConfirm) {
                    AlertDialog(
                        onDismissRequest = { showAccessibleSosConfirm = false },
                        title = { Text("Ihanda ang SOS?") },
                        text = { Text("Magbubukas ang messaging app para ma-review mo ang mensahe. Hindi ito awtomatikong magpapadala at walang delivery guarantee.") },
                        confirmButton = {
                            Button(onClick = {
                                showAccessibleSosConfirm = false
                                prepareSos()
                            }) { Text("IHANDA ANG SOS") }
                        },
                        dismissButton = {
                            Button(onClick = { showAccessibleSosConfirm = false }) { Text("Kanselahin") }
                        }
                    )
                }
                Text("SOS status: " + state.label)
                sosRecoveryNotice?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                OutlinedTextField(value = messageNote, onValueChange = { messageNote = it.take(240) }, label = { Text("Optional na mensahe") }, modifier = Modifier.fillMaxWidth())
            }
        }
        Button(onClick = onToggleHighContrast, modifier = Modifier.fillMaxWidth()) {
            Text("HIGH CONTRAST: " + if (MaterialTheme.colorScheme.background == Color.Black) "ON — tap to disable" else "OFF — tap to enable")
        }
        Button(onClick = {
            reduceMotion = !reduceMotion
            settings.setReduceMotion(reduceMotion)
        }, modifier = Modifier.fillMaxWidth()) {
            Text("REDUCE MOTION: " + if (reduceMotion) "ON — tap to disable" else "OFF — tap to enable")
        }
        Button(onClick = onReviewPrivacy, modifier = Modifier.fillMaxWidth()) {
            Text("REVIEW PRIVACY NOTICE")
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DEVICE COMPATIBILITY", style = MaterialTheme.typography.titleLarge)
                Text("${capabilityProfile.manufacturer} ${capabilityProfile.model} • Android ${capabilityProfile.androidRelease} (API ${capabilityProfile.sdkInt})")
                Text("Adaptive layout: $adaptiveLayoutLabel • Display ${configuration.screenWidthDp}×${configuration.screenHeightDp} dp • Font scale ${configuration.fontScale}")
                Text("Available storage: ${capabilityProfile.availableStorageBytes / (1024L * 1024L)} MB • Battery saver: ${if (capabilityProfile.batterySaverEnabled) "ON" else "OFF"}")
                listOf(
                    "telephony" to "Phone / SIM capability", "location" to "Location permission",
                    "ble" to "Bluetooth LE", "wifi_direct" to "Wi-Fi Direct feature (transport not implemented)",
                    "microphone" to "Microphone", "notifications" to "Notifications",
                    "keystore" to "Android Keystore", "background_execution" to "Background execution policy"
                ).forEach { (id, label) ->
                    val capability = capabilityProfile.capability(id)
                    val stateLabel = when (capability.state) {
                        CapabilityState.AVAILABLE -> "AVAILABLE"
                        CapabilityState.DENIED -> "PERMISSION NEEDED"
                        CapabilityState.DISABLED -> "TURNED OFF"
                        CapabilityState.UNSUPPORTED -> "NOT SUPPORTED"
                        CapabilityState.RESTRICTED_BY_SYSTEM -> "SYSTEM RESTRICTED"
                        CapabilityState.TEMPORARILY_UNAVAILABLE -> "TEMPORARILY UNAVAILABLE"
                        CapabilityState.ERROR -> "CHECK ERROR"
                    }
                    Text("$label: $stateLabel")
                    Text(capability.reasonCode.replace('_', ' '), style = MaterialTheme.typography.bodySmall)
                }
                Text("Effective feature behavior", style = MaterialTheme.typography.titleMedium)
                FeaturePolicyCoordinator.evaluate(capabilityProfile).forEach { policy ->
                    val status = when (policy.availability) {
                        FeatureAvailability.READY -> "READY"
                        FeatureAvailability.DEGRADED -> "FALLBACK"
                        FeatureAvailability.BLOCKED -> "UNAVAILABLE"
                    }
                    Text("${policy.featureId}: $status")
                    Text(policy.reason, style = MaterialTheme.typography.bodySmall)
                }
                Text("Auto-detected on app resume and after permission decisions. Android controls permissions and system settings; PH-ResQ cannot silently enable them.")
                Button(onClick = { capabilityProfile = DeviceCapabilityProfile.detect(context) }, modifier = Modifier.fillMaxWidth()) {
                    Text("REFRESH DEVICE CHECK")
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Offline national emergency contacts", style = MaterialTheme.typography.titleLarge)
                Text("Source: Philippine Government Emergency Hotlines portal; reviewed 2026-10-27. Ang 911 ang national emergency hotline.")
                OutlinedTextField(value = hotlineQuery, onValueChange = { hotlineQuery = it.take(80) }, label = { Text("Search agency or number") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = {
                    runCatching {
                        database.recordSearch(hotlineQuery)
                        recentSearches = database.recentSearches()
                    }.onFailure {
                        Toast.makeText(context, "Hindi ma-save ang search history.", Toast.LENGTH_LONG).show()
                    }
                }, modifier = Modifier.fillMaxWidth(), enabled = hotlineQuery.isNotBlank()) { Text("SAVE SEARCH LOCALLY") }
                if (recentSearches.isNotEmpty()) {
                    Text("Recent saved searches", style = MaterialTheme.typography.titleMedium)
                    recentSearches.forEach { query ->
                        Button(onClick = { hotlineQuery = query }, modifier = Modifier.fillMaxWidth()) { Text(query) }
                    }
                    Button(onClick = {
                        runCatching {
                            database.clearSearchHistory()
                            recentSearches = emptyList()
                        }.onFailure {
                            Toast.makeText(context, "Hindi ma-clear ang search history.", Toast.LENGTH_LONG).show()
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("CLEAR SEARCH HISTORY") }
                }
                NationalHotlineDirectory.search(hotlineQuery).forEach { hotline ->
                    Text(hotline.agency, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(hotline.displayNumber + " • " + hotline.method.label)
                    Text(hotline.note)
                    Button(onClick = {
                        val scheme = if (hotline.method == HotlineMethod.SMS) "smsto:" else "tel:"
                        val action = if (hotline.method == HotlineMethod.SMS) Intent.ACTION_SENDTO else Intent.ACTION_DIAL
                        val contactIntent = Intent(action, Uri.parse(scheme + hotline.uriNumber))
                        if (contactIntent.resolveActivity(context.packageManager) != null) context.startActivity(contactIntent)
                        else Toast.makeText(context, "Hindi available ang kaukulang app sa device.", Toast.LENGTH_LONG).show()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (hotline.method == HotlineMethod.SMS) "OPEN SMS APP" else "OPEN DIALER")
                    }
                    Button(onClick = {
                        runCatching {
                            database.saveBookmark("hotline:" + hotline.agency, hotline.agency + " • " + hotline.displayNumber)
                            savedBookmarks = database.bookmarks()
                        }.onFailure {
                            Toast.makeText(context, "Hindi ma-save ang bookmark.", Toast.LENGTH_LONG).show()
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("BOOKMARK HOTLINE") }
                }
                if (savedBookmarks.isNotEmpty()) {
                    Text("Saved hotline bookmarks", style = MaterialTheme.typography.titleMedium)
                    savedBookmarks.forEach { bookmark ->
                        Text(bookmark.second)
                        Button(onClick = {
                            runCatching {
                                database.removeBookmark(bookmark.first)
                                savedBookmarks = database.bookmarks()
                            }.onFailure {
                                Toast.makeText(context, "Hindi maalis ang bookmark.", Toast.LENGTH_LONG).show()
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("REMOVE BOOKMARK") }
                    }
                }
                Text("Kailangan ng network/phone service. Ang pag-open ng dialer o SMS app ay hindi patunay ng koneksyon, delivery, o pagsagot.")
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Emergency contacts", style = MaterialTheme.typography.titleLarge)
                Text("Ilagay ang hanggang 10 numero. Nasa device lang ang listahan.")
                if (pinVaultEnabled && !pinVaultUnlocked) {
                    Text("Naka-lock ang PIN vault. Hindi mababasa ang saved contacts o makakapaghanda ng SOS hanggang ma-unlock.")
                    OutlinedTextField(value = pinVaultPin, onValueChange = { pinVaultPin = it.filter(Char::isDigit).take(12) }, label = { Text("Vault PIN") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        try {
                            val secret = pinVault.unlock(pinVaultPin.toCharArray())
                            if (secret == null) pinVaultStatus = "Maling PIN o hindi mabuksan ang vault."
                            else {
                                contactsRaw = secret
                                contactsLoadAccessible = true
                                contactsLoadError = ""
                                pinVaultUnlocked = true
                                pinVaultStatus = "Vault unlocked for this app session."
                            }
                        } catch (error: Exception) {
                            pinVaultStatus = error.message ?: "Vault unlock failed."
                        } finally { pinVaultPin = "" }
                    }, modifier = Modifier.fillMaxWidth()) { Text("UNLOCK CONTACT VAULT") }
                } else {
                    if (!pinVaultEnabled && !contactsLoadAccessible) {
                        Text("Hindi ma-decrypt ang dating contacts. Nananatiling buo ang encrypted data; naka-block ang pag-edit at SOS upang maiwasan ang aksidenteng overwrite.")
                        Text(contactsLoadError)
                        Button(onClick = {
                            val result = runCatching { vault.loadContacts() }
                            if (result.isSuccess) {
                                contactsRaw = result.getOrDefault("")
                                contactsLoadAccessible = true
                                contactsLoadError = ""
                            } else {
                                contactsLoadError = result.exceptionOrNull()?.message ?: "Decryption failed."
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("RETRY SAFE CONTACT LOAD") }
                    }
                    OutlinedTextField(value = contactsRaw, onValueChange = {
                        contactsRaw = it
                        if (!pinVaultEnabled) {
                            val saveResult = runCatching { vault.saveContacts(it) }
                            contactsLoadAccessible = saveResult.isSuccess
                            contactsLoadError = saveResult.exceptionOrNull()?.message ?: ""
                            if (!saveResult.isSuccess) Toast.makeText(context, "Hindi ligtas na na-save ang contacts. Naka-block ang SOS.", Toast.LENGTH_LONG).show()
                        }
                    }, enabled = pinVaultUnlocked && contactsLoadAccessible, label = { Text("Hal. +639171234567, 09181234567") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    Text("Valid-looking contacts: " + ContactList.parse(contactsRaw).size)
                    if (!pinVaultEnabled) {
                        OutlinedTextField(value = pinVaultPin, onValueChange = { pinVaultPin = it.filter(Char::isDigit).take(12) }, label = { Text("Optional new vault PIN (6+ digits)") }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = {
                            try {
                                pinVault.configure(pinVaultPin.toCharArray(), contactsRaw)
                                check(prefs.edit().putBoolean("pin_vault_enabled", true).commit())
                                pinVaultEnabled = true
                                pinVaultUnlocked = true
                                contactsLoadAccessible = true
                                contactsLoadError = ""
                                runCatching { vault.clearContacts() }
                                pinVaultStatus = "PIN vault enabled. Walang recovery flow; itago ang PIN nang ligtas."
                            } catch (error: Exception) {
                                pinVaultStatus = error.message ?: "Could not configure PIN vault."
                            } finally { pinVaultPin = "" }
                        }, enabled = contactsRaw.isNotBlank() && pinVaultPin.length >= 6, modifier = Modifier.fillMaxWidth()) { Text("MOVE CONTACTS TO PIN-PROTECTED VAULT") }
                    } else {
                        OutlinedTextField(value = pinVaultPin, onValueChange = { pinVaultPin = it.filter(Char::isDigit).take(12) }, label = { Text("Vault PIN to save changes") }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = {
                            try {
                                if (pinVault.replace(pinVaultPin.toCharArray(), contactsRaw)) pinVaultStatus = "Protected contacts updated."
                                else pinVaultStatus = "Maling PIN; hindi na-update ang contacts."
                            } catch (error: Exception) {
                                pinVaultStatus = error.message ?: "Could not update protected contacts."
                            } finally { pinVaultPin = "" }
                        }, enabled = pinVaultPin.length >= 6, modifier = Modifier.fillMaxWidth()) { Text("SAVE PROTECTED CONTACTS") }
                        Button(onClick = {
                            contactsRaw = ""
                            pinVaultUnlocked = false
                            pinVaultStatus = "Vault locked for this session."
                        }, modifier = Modifier.fillMaxWidth()) { Text("LOCK CONTACT VAULT") }
                    }
                }
                Text(pinVaultStatus)
                Text(if (pinVaultEnabled) "PIN-derived AES-GCM. Walang PIN recovery o duress mode; pagkawala ng PIN ay maaaring permanenteng mawala ang contacts." else "Default: Android Keystore AES-GCM. Optional PIN vault adds a PIN-derived encryption layer.")
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Last-known location", style = MaterialTheme.typography.titleLarge)
                Text(locationLine)
                Text("Ang pinakahuling lokasyon ay maaaring luma; hindi ito real-time tracking.")
                Text(freshLocationStatus)
                Button(onClick = {
                    freshLocationPermissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ))
                }, modifier = Modifier.fillMaxWidth()) { Text("GET FRESH LOCATION (15s)") }
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
                Text("Hanggang 10 segundo; naka-encrypt sa local app storage. Ang share action ay magde-decrypt sa temporary cache at magbubukas ng system chooser; pumili ng app/recipient at kumpirmahin ang pagpapadala. Hindi ito auto-send at walang delivery confirmation.")
                Text(voiceStatus)
                Text("Safe playback volume: ${(voicePlaybackVolume * 100).toInt()}% (maximum 70%; system media volume remains unchanged)")
                Slider(
                    value = voicePlaybackVolume,
                    onValueChange = { voicePlaybackVolume = SafeVolumePolicy.clamp(it) },
                    onValueChangeFinished = { settings.setVoicePlaybackVolume(voicePlaybackVolume) },
                    valueRange = SafeVolumePolicy.MIN..SafeVolumePolicy.MAX,
                    steps = 5
                )
                Button(onClick = {
                    if (voiceRecording) voiceRecorder.stop()
                    else microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (voiceRecording) "STOP AND ENCRYPT NOTE" else "RECORD VOICE NOTE")
                }
                if (voicePlayer != null) {
                    Button(onClick = { stopVoicePlayback() }, modifier = Modifier.fillMaxWidth()) {
                        Text("STOP PLAYBACK")
                    }
                }
                Text("Saved encrypted notes: " + savedVoiceNotes.size, style = MaterialTheme.typography.titleMedium)
                savedVoiceNotes.forEach { note ->
                    Text(note.name)
                    Button(onClick = { playVoiceNote(note) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (voicePlayer != null && voicePlaybackTemp != null) "PLAY / RESTART NOTE" else "PLAY NOTE")
                    }
                    Button(onClick = { shareVoiceNote(note) }, modifier = Modifier.fillMaxWidth()) {
                        Text("ATTACH TO SOS DRAFT / SHARE")
                    }
                    Button(onClick = {
                        if (voicePlaybackTemp != null && voicePlayer != null) stopVoicePlayback()
                        val removed = runCatching { voiceRecorder.deleteSaved(context, note) }.getOrDefault(false)
                        savedVoiceNotes = voiceRecorder.listSaved(context)
                        voiceStatus = if (removed) "Encrypted note deleted" else "Could not delete encrypted note"
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("DELETE NOTE")
                    }
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ligtas Ako check-in", style = MaterialTheme.typography.titleLarge)
                Text("Local status lamang; wala pang network relay o family delivery.")
                Text("Status: " + checkIn)
                Text("Pending encrypted local outbox items: " + pendingQueueCount + " • walang network sync/ack sa build na ito.")
                Button(onClick = {
                    val nextStatus = "Naitala locally • " + System.currentTimeMillis()
                    runCatching {
                        database.recordFamilyCheckin(nextStatus)
                        prefs.edit().remove("checkin").apply()
                        checkIn = database.latestFamilyCheckin() ?: nextStatus
                        pendingQueueCount = database.pendingActionCount()
                    }.onFailure {
                        checkIn = nextStatus
                        prefs.edit().putString("checkin", nextStatus).apply()
                        Toast.makeText(context, "Hindi ma-save sa database; ginamit ang local fallback.", Toast.LENGTH_LONG).show()
                    }
                }) { Text("Save check-in locally") }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Offline Medical ID QR", style = MaterialTheme.typography.titleLarge)
                Text("Gumawa lamang ng QR kapag pinili mong ibahagi ang impormasyon. Hindi naka-encrypt ang QR: maaaring mabasa ng sinumang makakita o kumuha ng larawan nito. Huwag ilagay ang impormasyong ayaw mong ibunyag.")
                OutlinedTextField(value = medicalName, onValueChange = { medicalName = it.take(80); medicalQrBitmap = null }, label = { Text("Pangalan (required)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = medicalBloodType, onValueChange = { medicalBloodType = it.take(20); medicalQrBitmap = null }, label = { Text("Blood type (optional)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = medicalAllergies, onValueChange = { medicalAllergies = it.take(160); medicalQrBitmap = null }, label = { Text("Allergies (optional)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = medicalMedications, onValueChange = { medicalMedications = it.take(160); medicalQrBitmap = null }, label = { Text("Medications (optional)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = medicalEmergencyContact, onValueChange = { medicalEmergencyContact = it.take(40); medicalQrBitmap = null }, label = { Text("Emergency contact (optional)") }, modifier = Modifier.fillMaxWidth())
                Text(medicalQrStatus)
                Button(onClick = {
                    try {
                        medicalQrBitmap = MedicalIdQr.createBitmap(medicalName, medicalBloodType, medicalAllergies, medicalMedications, medicalEmergencyContact)
                        medicalQrStatus = "QR generated on-device. It contains only the fields entered above."
                    } catch (error: Exception) {
                        medicalQrBitmap = null
                        medicalQrStatus = error.message ?: "Hindi nagawa ang QR."
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("GENERATE MEDICAL ID QR") }
                medicalQrBitmap?.let { bitmap ->
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Medical ID QR code", modifier = Modifier.fillMaxWidth().height(260.dp))
                    Button(onClick = { medicalQrBitmap = null; medicalQrStatus = "QR cleared from screen." }, modifier = Modifier.fillMaxWidth()) { Text("CLEAR QR FROM SCREEN") }
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Low-battery snapshot", style = MaterialTheme.typography.titleLarge)
                Text(batterySnapshot)
                Text("Best-effort: naitatala kapag bukas ang app at nakatanggap ito ng battery broadcast; hindi ito kapalit ng OS-level guaranteed callback.")
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Offline directory and local hazard cache", style = MaterialTheme.typography.titleLarge)
        Text("Hinahanap lamang nito ang data na na-import na sa device. Walang live PAGASA/LGU feed at hindi ito nangangahulugang kumpleto ang nationwide coverage.")
        Text("Ang JSON metadata ay hindi digital signature. I-import lamang ang file matapos mong beripikahin ang opisyal na source, lisensya at petsa; walang live download na ginagawa.")
        Button(onClick = { offlineImportLauncher.launch(arrayOf("application/json", "text/json")) }, modifier = Modifier.fillMaxWidth()) { Text("IMPORT OFFLINE JSON") }
        Text(offlineImportStatus)
        OutlinedTextField(value = offlineQuery, onValueChange = { offlineQuery = it.take(120) }, label = { Text("Barangay, lugar, agency o evacuation center") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val location = LastKnownLocation.read(context)
            if (location == null) {
                Toast.makeText(context, "Walang huling lokasyon. I-enable ang location permission at kumuha muna ng fix.", Toast.LENGTH_LONG).show()
            } else {
                directoryResults = runCatching {
                    offlineDataRepository.searchNearby(location.latitude, location.longitude, radiusKm = 25.0, limit = 100).map { it.record }
                }.getOrDefault(emptyList())
                if (directoryResults.isEmpty()) Toast.makeText(context, "Walang imported directory record na may coordinates sa loob ng 25 km.", Toast.LENGTH_LONG).show()
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("SEARCH NEAR LAST-KNOWN LOCATION (25 KM)") }
        Text("Gumagamit lamang ito ng huling kilalang lokasyon; maaaring luma ang fix. Hindi ito nagsisimula ng bagong GPS request.")
        Button(onClick = {
            directoryResults = runCatching { offlineDataRepository.searchDirectory(offlineQuery, 100) }.getOrDefault(emptyList())
            directoryMetadata = runCatching { offlineDataRepository.datasetMetadata() }.getOrNull()
            evacuationItems = runCatching { hazardDataRepository.searchEvacuationCenters(offlineQuery) }.getOrDefault(emptyList())
            advisoryItems = runCatching { hazardDataRepository.advisories() }.getOrDefault(emptyList())
            if (directoryResults.isEmpty() && evacuationItems.isEmpty() && advisoryItems.isEmpty()) {
                Toast.makeText(context, "Walang local directory/advisory data na available. Hindi nag-download ng data.", Toast.LENGTH_LONG).show()
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("SEARCH SAVED OFFLINE DATA") }
        directoryMetadata?.let { meta ->
            Text("Directory dataset: ${meta["version"] ?: "unknown"} • records: ${meta["recordCount"] ?: "unknown"}")
            Text("Source: ${meta["sourceUrl"] ?: "unknown"} • license: ${meta["license"] ?: "unknown"}")
            Text("Source-reported verification time: ${meta["verifiedAt"] ?: "unknown"} • imported at: ${meta["importedAt"] ?: "unknown"}")
        } ?: Text("Walang imported directory dataset. Mag-import lamang ng data mula sa beripikadong source at may malinaw na lisensya.")
        directoryResults.take(30).forEach { record ->
            Text(record.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(record.category + (record.phone?.let { " • $it" } ?: ""))
            if (record.latitude != null && record.longitude != null) Text("Coordinates: ${record.latitude}, ${record.longitude}")
        }
        Text("Evacuation centers (local cache)", style = MaterialTheme.typography.titleMedium)
        if (evacuationItems.isEmpty()) Text("Walang matching imported evacuation-center records.")
        evacuationItems.take(30).forEach { (center, stale) ->
            Text(center.name + if (stale) " • STALE / VERIFY LOCALLY" else " • status: ${center.status}", style = MaterialTheme.typography.titleMedium)
            Text(center.address)
            Text("Source: ${center.sourceUrl} • verified: ${center.verifiedAt}")
        }
        Text("Cached advisories", style = MaterialTheme.typography.titleMedium)
        if (advisoryItems.isEmpty()) Text("Walang cached advisories. Walang live feed request na ginawa.")
        advisoryItems.take(10).forEach { (item, stale) ->
            Text(item.title + if (stale) " • STALE / EXPIRED" else " • ${item.severity}", style = MaterialTheme.typography.titleMedium)
            Text(item.body)
            Text("Source: ${item.sourceUrl} • published: ${item.publishedAt} • expires: ${item.expiresAt}")
        }

        Text("Directory, maps, mesh and vault", style = MaterialTheme.typography.titleLarge)
                Text("Hindi pa kumpleto: walang verified nationwide data package o offline map renderer, BLE/Wi-Fi emergency transport, o remote sync. Ang PIN vault ay integrated ngunit wala pang recovery/backup o duress mode.")
            }
        }
        Text("Ang SMS handoff ay hindi patunay ng delivery. Sa agarang panganib, gamitin ang available na opisyal na emergency channel.")
    }
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
internal fun SosHoldButton(modifier: Modifier = Modifier, onConfirmed: () -> Unit) {
    val progress = remember { Animatable(0f) }
    var isHolding by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHolding = true
                        val confirmed = coroutineScope {
                            val animation = launch {
                                progress.animateTo(1f, animationSpec = tween(2_000))
                            }
                            val released = tryAwaitRelease()
                            val heldLongEnough = animation.isCompleted
                            animation.cancel()
                            released && heldLongEnough
                        }
                        progress.snapTo(0f)
                        isHolding = false
                        // A cancelled gesture never dispatches, even if its timer elapsed.
                        if (confirmed) onConfirmed()
                    }
                )
            }
        ) {
            Text(if (isHolding) "KEEP HOLDING — SOS PREPARING" else "HOLD FOR 2 SECONDS — PREPARE SOS")
        }
        LinearProgressIndicator(
            progress = progress.value,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            if (isHolding) "Panatilihing nakadiin nang 2 segundo. Bitawan para kanselahin."
            else "Kailangan ng tuloy-tuloy na 2-second hold; ang maagang pagbitaw ay kanselado."
        )
    }
}
