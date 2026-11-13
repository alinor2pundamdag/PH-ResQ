package ph.resq.app.ui
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ph.resq.app.core.NationalReferenceRepository
import ph.resq.app.core.ResQDatabase
import ph.resq.app.core.NationalDatasetImporter
@Composable fun NationalDataPanel(database:ResQDatabase,modifier:Modifier=Modifier){
 val context=LocalContext.current;val repository=remember(database){NationalReferenceRepository(database)}
 var query by remember{mutableStateOf("")};var status by remember{mutableStateOf("No complete PSA Q3-2026 barangay snapshot imported yet.")}
 var coverage by remember{mutableStateOf(runCatching{repository.coverage()}.getOrNull())}
 var results by remember{mutableStateOf(emptyList<ph.resq.app.core.BarangayReference>())}
 var contacts by remember{mutableStateOf(emptyList<ph.resq.app.core.PublicEmergencyContact>())}
 var centers by remember{mutableStateOf(emptyList<ph.resq.app.core.NationalEvacuationCenter>())}

 val contactsLauncher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
  if(uri!=null){
   val result=runCatching{NationalDatasetImporter.importPublicContactsJson(context,uri,repository)}
   status=result.fold({"Imported $it public-office contact records. They are source-attributed, not automatically call-verified."},{"Contact import rejected: \${it.message}"})
   coverage=runCatching{repository.coverage()}.getOrNull()
   contacts=runCatching{repository.searchContacts(query,50)}.getOrDefault(emptyList());centers=runCatching{repository.searchEvacuationCenters(query,50)}.getOrDefault(emptyList())
  }
 }
 val evacuationLauncher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
  if(uri!=null){
   val result=runCatching{NationalDatasetImporter.importEvacuationCentersJson(context,uri,repository)}
   status=result.fold({"Imported $it evacuation-center records. Confirm operational status and coordinates locally."},{"Evacuation import rejected: \${it.message}"})
   coverage=runCatching{repository.coverage()}.getOrNull()
   centers=runCatching{repository.searchEvacuationCenters(query,50)}.getOrDefault(emptyList())
  }
 }

 val csvLauncher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
  if(uri!=null){
   val result=runCatching{
    context.contentResolver.takePersistableUriPermission(uri,android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    NationalDatasetImporter.importBarangaysCsv(context,uri,repository,"PSGC Q3 2026")
   }
   status=result.fold({"Imported $it official PSGC barangays. Contact and evacuation coverage remain separate."},{"Import rejected: \${it.message}"})
   coverage=runCatching{repository.coverage()}.getOrNull()
  }
 }
 Card(modifier.fillMaxWidth()){
  Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
   Text("NATIONAL BARANGAY & EMERGENCY DATA",style=MaterialTheme.typography.titleLarge)
   Text("PSA PSGC identity, public office contacts and evacuation-center records are separate datasets. Missing phone numbers, coordinates, capacities and facility statuses are never guessed.")
   Button(onClick={csvLauncher.launch(arrayOf("text/csv","text/comma-separated-values","application/vnd.ms-excel"))},modifier=Modifier.fillMaxWidth()){Text("IMPORT COMPLETE PSA BARANGAY CSV")}
   Button(onClick={contactsLauncher.launch(arrayOf("application/json","text/json"))},modifier=Modifier.fillMaxWidth()){Text("IMPORT PUBLIC EMERGENCY CONTACTS JSON")}
   Button(onClick={evacuationLauncher.launch(arrayOf("application/json","text/json"))},modifier=Modifier.fillMaxWidth()){Text("IMPORT EVACUATION CENTERS JSON")}
   Text(status,style=MaterialTheme.typography.bodySmall)
   coverage?.let{
    Text("PSA Q3-2026 reference: \${it.loadedBarangays} / \${it.expectedBarangays} barangays (\${String.format("%.2f",it.barangayCoveragePercent)}%)")
    Text("Barangays with public contact records: \${it.contactBarangays}")
    Text("Evacuation centers: \${it.evacuationCenters} • with coordinates: \${it.geocodedCenters}")
    Text("Contacts cross-checked or verified by call: \${it.verifiedContacts}")
   }
   OutlinedTextField(value=query,onValueChange={query=it.take(120)},label={Text("Search barangay, PSGC code or office")},modifier=Modifier.fillMaxWidth())
   Button(onClick={results=runCatching{repository.searchBarangays(query,50)}.getOrDefault(emptyList());contacts=runCatching{repository.searchContacts(query,50)}.getOrDefault(emptyList())},modifier=Modifier.fillMaxWidth()){Text("SEARCH NATIONAL REFERENCE DATA")}
   results.take(20).forEach{Text("\${it.barangay} • \${it.cityMunicipality}, \${it.province} • PSGC \${it.psgcCode}",style=MaterialTheme.typography.bodySmall)}
   contacts.take(20).forEach{Text("\${it.officeName} • \${it.phone} • \${it.verification}",style=MaterialTheme.typography.bodySmall);Text("\${it.sourceTitle} • \${it.sourceUrl}",style=MaterialTheme.typography.labelSmall)}
   centers.take(20).forEach{Text("\${it.name} • \${it.operationalStatus} • \${it.verification}",style=MaterialTheme.typography.bodySmall);Text("\${it.address} • \${it.sourceTitle}",style=MaterialTheme.typography.labelSmall)}
   Text("Coverage remains incomplete until source datasets are imported and independently checked. DILG does not publish a single consolidated nationwide operational-center database.",style=MaterialTheme.typography.bodySmall)
  }
 }
}
