package ph.resq.app

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ph.resq.app.core.ConsentStore

@Composable
internal fun ConsentScreen(onAccept: () -> Unit, onDecline: () -> Unit) {
    // The scrollable notice and fixed actions are siblings: neither choice can scroll off-screen.
    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = true).fillMaxWidth().verticalScroll(rememberScrollState()).testTag("consent-notice-scroll"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("PH-ResQ", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Privacy at safety notice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Basahin ang paunawang ito bago magpatuloy. Maaari kang mag-scroll sa buong nilalaman; mananatiling nakikita sa ibaba ang Accept at Decline.")
            Text("ANO ANG NAIIMBAK SA DEVICE", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Ang emergency contact numbers at ilang sensitibong SOS/location field ay iniimbak nang naka-encrypt gamit ang Android Keystore. Ang SOS event history, search history, hotline bookmarks at family check-ins ay lokal na nakaimbak sa app database. Walang remote family sync sa build na ito.")
            Text("LOCATION", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Hindi ito tuloy-tuloy na location tracking. Hihingi lamang ang app ng location permission kapag gumamit ka ng location feature. Maaaring luma o hindi available ang last-known location; suriin ang timestamp at huwag ipagpalagay na live o eksakto ito.")
            Text("MICROPHONE AT VOICE NOTES", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Gagamitin lamang ang microphone kapag ikaw ang nagsimula ng voice-note recording at nagbigay ng permission. Ang note ay naka-encrypt habang nakaimbak. Kapag ibinahagi mo ito sa ibang app, gagawa ang PH-ResQ ng temporary decrypted copy para maipasa; ang recipient app at recipient ay nasa iyong pagpili.")
            Text("BLUETOOTH / NEARBY DEVICES", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Magsisimula lamang ang BLE discovery kapag pinindot mo ang scan at ibinigay ang kinakailangang permission. Sa build na ito, advertisement discovery lamang ito; hindi pa ito aktuwal na SOS mesh transmission.")
            Text("SOS AT COMMUNICATIONS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Ang SOS action ay magbubukas ng messaging app na may draft. Ikaw ang magre-review at magpapadala. Ang pagbukas ng messaging app ay hindi patunay na naipadala, natanggap, nabasa, o nasagot ang mensahe. Ang hotline action ay maaari ring mabigo dahil sa SIM, signal, network o availability ng serbisyo.")
            Text("LIMITASYON NG BUILD", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Early test build ito. Wala pang verified nationwide barangay directory at licensed offline map package; hindi pa kumpleto ang Wi-Fi Direct/radio mesh, remote sync, data export/recovery at lahat ng device validation. Huwag itong ituring na validated emergency-response service at huwag itong gawing tanging paraan ng paghingi ng tulong.")
            Text("IYONG PAGPILI", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Ang Accept ay nagtatala na nabasa at tinanggap mo ang privacy at safety notice na ito para sa policy version " + ConsentStore.CURRENT_POLICY_VERSION + ". Maaari kang tumanggi at lumabas sa app. Ang pagtanggi ay hindi magbibigay ng access sa main screen.")
            Spacer(Modifier.height(8.dp))
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Kailangan ang iyong pagpili upang magpatuloy.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Button(onClick = onAccept, modifier = Modifier.fillMaxWidth()) { Text("ACCEPT — Nauunawaan ko at magpatuloy") }
                OutlinedButton(onClick = onDecline, modifier = Modifier.fillMaxWidth()) {
                    Text("DECLINE — Withdraw consent and exit")
                }
            }
        }
    }
}

