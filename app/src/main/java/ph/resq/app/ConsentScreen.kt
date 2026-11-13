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
            Text("Gagamitin lamang ang microphone kapag ikaw ang nagsimula ng voice-note recording at nagbigay ng permission. Habang nagre-record, may temporary raw audio file sa private app cache; kapag natapos, ine-encrypt ito at sinusubukang burahin ang raw file. Hindi garantisado ang forensic erasure. Para sa playback o share, gagawa ng temporary decrypted copy. Kapag ibinahagi, plaintext audio ang matatanggap ng pinili mong external app.")
            Text("BLUETOOTH / NEARBY DEVICES", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Magsisimula lamang ang BLE discovery kapag pinindot mo ang scan at ibinigay ang kinakailangang permission. Sa build na ito, advertisement discovery lamang ito; hindi pa ito aktuwal na SOS mesh transmission.")
            Text("SOS AT COMMUNICATIONS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Ang SOS action ay magbubukas ng messaging app na may draft. Ikaw ang magre-review at magpapadala. Ang pagbukas ng messaging app ay hindi patunay na naipadala, natanggap, nabasa, o nasagot ang mensahe. Ang hotline action ay maaari ring mabigo dahil sa SIM, signal, network o availability ng serbisyo.")
            Text("DATA COVERAGE AT LIMITASYON", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("May 42,010 PSGC-derived barangay identity records ang app. Ang source ay Q2 2026 third-party PSA-derived snapshot na nilagyan ng opisyal na Q3 name correction; hindi pa kumpleto ang Q3 urban/rural attributes at walang barangay boundary polygons.")
            Text("May 201 public emergency-contact records mula sa national portal at piling LGU sources. Official-source listing ang provenance, hindi ito katumbas ng phone verification at hindi nationwide-complete ang coverage. Review-due pa ang mga numerong hindi direktang na-verify.")
            Text("May 429 evacuation-facility records mula sa Quezon City at Bay, Laguna. Walang verified current operating status at walang coordinates sa bundled records; itinuturing silang UNKNOWN hanggang makumpirma ng LGU.")
            Text("Available ang downloadable nationwide OpenStreetMap/ODbL basemap sa PH-ResQ release. Basemap lamang ito—hindi official hazard map, barangay boundary map, o verified shelter directory.")
            Text("Ang app ay offline-first ngunit hindi pa validated emergency-response service. BLE advertisement discovery lamang ang gumagana; wala pang SOS packet transfer o multi-hop mesh at hindi pa implemented ang Wi-Fi Direct transport. Family check-ins ay local; SMS sharing ay nagbubukas lamang ng draft na ikaw ang magpapadala. Walang remote sync o delivery acknowledgement sa build na ito.")
            Text("Huwag itong gawing tanging paraan ng paghingi ng tulong. Sa agarang panganib, gamitin ang opisyal na emergency hotline at lokal na awtoridad.")
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

