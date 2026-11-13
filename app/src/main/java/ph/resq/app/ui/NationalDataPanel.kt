package ph.resq.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ph.resq.app.core.NationalDatasetImporter
import ph.resq.app.core.NationalReferenceRepository
import ph.resq.app.core.ResQDatabase

@Composable
fun NationalDataPanel(database:ResQDatabase,modifier:Modifier=Modifier){
 val context=LocalContext.current
 val repository=remember(database){NationalReferenceRepository(database)}
 val scope=rememberCoroutineScope()
 var importing by remember{mutableStateOf(false)}
 var query by remember{mutableStateOf("")}
 var status by remember{mutableStateOf("Install the bundled PSGC reference and review the separately sourced contact and evacuation records.")}
 var coverage by remember{mutableStateOf(runCatching{repository.coverage()}.getOrNull())}
 var results by remember{mutableStateOf(emptyList<ph.resq.app.core.BarangayReference>())}
 var contacts by remember{mutableStateOf(emptyList<ph.resq.app.core.PublicEmergencyContact>())}
 var centers by remember{mutableStateOf(emptyList<ph.resq.app.core.NationalEvacuationCenter>())}

 suspend fun refreshData(){
  val snapshot=withContext(Dispatchers.IO){
   listOf(repository.coverage(),repository.searchBarangays(query,50),repository.searchContacts(query,50),repository.searchEvacuationCenters(query,50))
  }
  coverage=snapshot[0] as ph.resq.app.core.NationalReferenceCoverage
  results=snapshot[1] as List<ph.resq.app.core.BarangayReference>
  contacts=snapshot[2] as List<ph.resq.app.core.PublicEmergencyContact>
  centers=snapshot[3] as List<ph.resq.app.core.NationalEvacuationCenter>
 }

 val contactsLauncher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
  if(uri!=null&&!importing)scope.launch{
   importing=true
   val result=runCatching{withContext(Dispatchers.IO){NationalDatasetImporter.importPublicContactsJson(context,uri,repository)}}
   status=result.fold({"Imported $it public-office contact records. Source attribution does not mean the number was call-verified."},{"Contact import rejected: ${it.message}"})
   runCatching{refreshData()}
   importing=false
  }
 }
 val evacuationLauncher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
  if(uri!=null&&!importing)scope.launch{
   importing=true
   val result=runCatching{withContext(Dispatchers.IO){NationalDatasetImporter.importEvacuationCentersJson(context,uri,repository)}}
   status=result.fold({"Imported $it evacuation-centre records. Confirm coordinates and current operational status locally."},{"Evacuation import rejected: ${it.message}"})
   runCatching{refreshData()}
   importing=false
  }
 }
 val csvLauncher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
  if(uri!=null&&!importing)scope.launch{
   importing=true
   val result=runCatching{withContext(Dispatchers.IO){
    context.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)
    NationalDatasetImporter.importBarangaysCsv(context,uri,repository,"2026-Q3-identity-overlay")
   }}
   status=result.fold({"Imported $it PSGC-derived barangay rows. Contacts and evacuation coverage remain separate."},{"Import rejected: ${it.message}"})
   runCatching{refreshData()}
   importing=false
  }
 }

 Card(modifier.fillMaxWidth()){
  Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
   Text("NATIONAL BARANGAY & EMERGENCY DATA",style=MaterialTheme.typography.titleLarge)
   Text("PSGC identity, public emergency contacts and evacuation facilities are separate datasets. Missing phone numbers, coordinates, capacities and current operating status are never guessed.")
   Text("The bundled 42,010-row barangay reference is PSA-derived Q2 2026 data with the official Q3 name correction applied. Q3 urban/rural attributes are not yet included.")
   Button(enabled=!importing,onClick={csvLauncher.launch(arrayOf("text/csv","text/comma-separated-values","application/vnd.ms-excel"))},modifier=Modifier.fillMaxWidth()){Text("IMPORT PSA BARANGAY CSV")}
   Button(enabled=!importing,onClick={
    importing=true
    scope.launch{
     val result=runCatching{withContext(Dispatchers.IO){NationalDatasetImporter.importBundledBarangays(context,repository)}}
     status=result.fold({"Installed $it PSGC-derived barangay rows for offline lookup."},{"PSGC install failed: ${it.message}"})
     runCatching{refreshData()}
     importing=false
    }
   },modifier=Modifier.fillMaxWidth()){Text(if(importing)"IMPORTING NATIONAL DATA…" else "INSTALL FULL 42,010 BARANGAY REFERENCE")}
   Button(enabled=!importing,onClick={contactsLauncher.launch(arrayOf("application/json","text/json"))},modifier=Modifier.fillMaxWidth()){Text("IMPORT PUBLIC EMERGENCY CONTACTS JSON")}
   Button(enabled=!importing,onClick={evacuationLauncher.launch(arrayOf("application/json","text/json"))},modifier=Modifier.fillMaxWidth()){Text("IMPORT EVACUATION CENTRES JSON")}
   Button(enabled=!importing,onClick={
    importing=true
    scope.launch{
     val result=runCatching{withContext(Dispatchers.IO){
      val contactCount=NationalDatasetImporter.importBundledPublicContacts(context,repository)
      val centerCount=NationalDatasetImporter.importBundledEvacuationCenters(context,repository)
      contactCount to centerCount
     }}
     status=result.fold({"Loaded ${it.first} public contact records and ${it.second} official-listed evacuation facilities. Records remain review-due until independently verified."},{"Starter data import failed: ${it.message}"})
     runCatching{refreshData()}
     importing=false
    }
   },modifier=Modifier.fillMaxWidth()){Text("LOAD STARTER CONTACTS + BAY / QUEZON CITY EVACUATION SITES")}
   Text(status,style=MaterialTheme.typography.bodySmall)
   coverage?.let{
    Text("PSGC-derived reference: ${it.loadedBarangays} / ${it.expectedBarangays} barangays (${String.format("%.2f",it.barangayCoveragePercent)}%)")
    Text("Barangays with public contact records: ${it.contactBarangays}")
    Text("Evacuation facilities: ${it.evacuationCenters} • with coordinates: ${it.geocodedCenters}")
    Text("Contacts cross-checked or call-verified: ${it.verifiedContacts}")
   }
   OutlinedTextField(value=query,onValueChange={query=it.take(120)},label={Text("Search barangay, PSGC code, office or evacuation site")},modifier=Modifier.fillMaxWidth())
   Button(enabled=!importing,onClick={scope.launch{
    val data=withContext(Dispatchers.IO){listOf(repository.searchBarangays(query,50),repository.searchContacts(query,50),repository.searchEvacuationCenters(query,50))}
    results=data[0] as List<ph.resq.app.core.BarangayReference>
    contacts=data[1] as List<ph.resq.app.core.PublicEmergencyContact>
    centers=data[2] as List<ph.resq.app.core.NationalEvacuationCenter>
   }},modifier=Modifier.fillMaxWidth()){Text("SEARCH NATIONAL REFERENCE DATA")}
   results.take(20).forEach{Text("${it.barangay} • ${it.cityMunicipality}, ${it.province} • PSGC ${it.psgcCode}",style=MaterialTheme.typography.bodySmall)}
   contacts.take(20).forEach{contact->
    val reviewDue=contact.expiresAt?.let{it<=System.currentTimeMillis()}?:true
    val sourceLabel=when(contact.verification.name){
     "VERIFIED_BY_CALL"->"CALL-VERIFIED"
     "CROSS_CHECKED"->"CROSS-CHECKED"
     "OFFICIAL_GOVERNMENT_SOURCE"->"OFFICIAL SOURCE"
     "UNVERIFIED_SOCIAL"->"UNVERIFIED SOCIAL"
     else->"NEEDS REVIEW"
    }
    Text("${contact.officeName} • ${contact.phone} • $sourceLabel${if(reviewDue)" • REVIEW DUE" else ""}",modifier=Modifier.fillMaxWidth().clickable{runCatching{context.startActivity(Intent(Intent.ACTION_DIAL,Uri.parse("tel:${contact.phone}"))) }},style=MaterialTheme.typography.bodySmall)
    Text("${contact.sourceTitle} • OPEN SOURCE",modifier=Modifier.fillMaxWidth().clickable{runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(contact.sourceUrl))) }},style=MaterialTheme.typography.labelSmall)
   }
   centers.take(20).forEach{center->
    val sourceLabel=if(center.operationalStatus=="UNKNOWN")"STATUS UNKNOWN • REVIEW DUE" else center.verification.name
    Text("${center.name} • ${center.capacity?.let{n->"$n ${center.capacityUnit.lowercase()}"}?:"capacity unknown"} • ${center.operationalStatus} • $sourceLabel",style=MaterialTheme.typography.bodySmall)
    Text("${center.address} • ${center.sourceTitle} • OPEN SOURCE",modifier=Modifier.fillMaxWidth().clickable{runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(center.sourceUrl))) }},style=MaterialTheme.typography.labelSmall)
   }
   Text("Source listing does not prove that a facility is currently open. DILG/OCD FOI responses did not identify a single consolidated nationwide operational-centre master file. Records without current confirmation remain UNKNOWN.",style=MaterialTheme.typography.bodySmall)
  }
 }
}
