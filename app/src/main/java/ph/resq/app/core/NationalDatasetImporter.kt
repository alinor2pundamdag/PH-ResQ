package ph.resq.app.core
import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import org.json.JSONObject
object NationalDatasetImporter{
 private const val MAX_BYTES=15*1024*1024
 fun importBarangaysCsv(context:Context,uri:Uri,repository:NationalReferenceRepository,version:String):Int =
  importBarangayRows(parseCsv(readText(context,uri).removePrefix("\uFEFF")),repository,version)
 fun importBundledBarangays(context:Context,repository:NationalReferenceRepository):Int{
  val names=(1..12).map{"data/psgc_q3_part_${it.toString().padStart(2,'0')}.csv"}
  val rows=mutableListOf<List<String>>()
  names.forEachIndexed{index,name->
   val shard=context.assets.open(name).bufferedReader().use{it.readText()}
   val parsed=parseCsv(shard.removePrefix("\uFEFF"))
   require(parsed.isNotEmpty()){"Empty bundled PSGC shard: $name"}
   if(index==0)rows.addAll(parsed) else rows.addAll(parsed.drop(1))
  }
  return importBarangayRows(rows,repository,"2026-Q3-identity-overlay")
 }
 private fun importBarangayRows(rows:List<List<String>>,repository:NationalReferenceRepository,version:String):Int{
  require(version=="2026-Q3-identity-overlay"){"Unexpected PSGC source version."}
  require(rows.isNotEmpty()){"CSV is empty."}
  val header=rows.first().map{it.trim().lowercase()}
  val required=listOf("psgc_code","region","province","city_municipality","barangay","source_url","source_version")
  required.forEach{require(header.contains(it)){"Missing column: $it"}}
  val ix=required.associateWith{header.indexOf(it)};val urban=header.indexOf("urban_rural")
  val records=rows.drop(1).filter{it.any(String::isNotBlank)}.mapIndexed{rowNo,row->
   fun f(name:String):String{val i=ix.getValue(name);require(i<row.size){"Malformed CSV row ${rowNo+2}"};return row[i].trim()}
   require(f("source_url")==NationalReferenceRepository.PSA_SOURCE){"Row ${rowNo+2} does not identify the official PSA PSGC source."}
   require(f("source_version")==version){"Row ${rowNo+2} has an unexpected source version."}
   BarangayReference(f("psgc_code"),f("region"),f("province"),f("city_municipality"),f("barangay"),if(urban in row.indices)row[urban].trim().ifBlank{null}else null)
  }
  require(records.size==NationalReferenceRepository.EXPECTED_BARANGAYS_Q3_2026){"Expected 42,010 PSGC barangays; found ${records.size}. Partial import blocked."}
  return repository.replaceBarangays(records,NationalReferenceRepository.PSA_SOURCE,version)
 }

 fun importPublicContactsJson(context:Context,uri:Uri,repository:NationalReferenceRepository):Int =
  importContactsPayload(readText(context,uri),repository)
 fun importBundledPublicContacts(context:Context,repository:NationalReferenceRepository):Int =
  importContactsPayload(context.assets.open("data/public_emergency_contacts.json").bufferedReader().use{it.readText()},repository)
 private fun importContactsPayload(payload:String,repository:NationalReferenceRepository):Int{
  val root=JSONObject(payload);require(root.optString("type")=="emergency_contacts"){"Expected emergency_contacts dataset."}
  val records=root.optJSONArray("records")?:error("Missing records array.");require(records.length() in 1..100_000);var imported=0
  for(i in 0 until records.length()){
   val o=records.getJSONObject(i)
   fun required(k:String)=o.optString(k,"").trim().also{require(it.isNotBlank()){"Record ${i+1} missing $k"}}
   val source=required("source_url");val host=runCatching{android.net.Uri.parse(source).host.orEmpty().lowercase()}.getOrDefault("")
   val verification=if(host.endsWith(".gov.ph")||host=="gov.ph")ContactVerification.OFFICIAL_GOVERNMENT_SOURCE else ContactVerification.UNVERIFIED_SOCIAL
   val expiry=o.optLong("expires_at",-1L).takeIf{it>=0}
   repository.addPublicContact(PublicEmergencyContact(required("id"),o.optString("psgc_code").takeIf{it.isNotBlank()},required("scope").uppercase(),required("category").uppercase(),required("office_name"),required("phone"),source,required("source_title"),verification,null,expiry));imported++
  };return imported
 }
 fun importEvacuationCentersJson(context:Context,uri:Uri,repository:NationalReferenceRepository):Int =
  importEvacuationPayload(readText(context,uri),repository)
 fun importBundledEvacuationCenters(context:Context,repository:NationalReferenceRepository):Int =
  importEvacuationPayload(context.assets.open("data/bay_evacuation_centers.json").bufferedReader().use{it.readText()},repository)
 private fun importEvacuationPayload(payload:String,repository:NationalReferenceRepository):Int{
  val root=JSONObject(payload);require(root.optString("type")=="evacuation_centers"){"Expected evacuation_centers dataset."}
  val records=root.optJSONArray("records")?:error("Missing records array.");require(records.length() in 1..50_000);var imported=0
  for(i in 0 until records.length()){
   val o=records.getJSONObject(i)
   fun required(k:String)=o.optString(k,"").trim().also{require(it.isNotBlank()){"Record ${i+1} missing $k"}}
   val source=required("source_url");val host=runCatching{android.net.Uri.parse(source).host.orEmpty().lowercase()}.getOrDefault("")
   val verification=if(host.endsWith(".gov.ph")||host=="gov.ph")ContactVerification.OFFICIAL_GOVERNMENT_SOURCE else ContactVerification.NEEDS_REVIEW
   val lat=if(o.isNull("latitude"))null else o.getDouble("latitude");val lon=if(o.isNull("longitude"))null else o.getDouble("longitude")
   val capacity=if(o.isNull("capacity"))null else o.getInt("capacity")
   val capacityUnit=o.optString("capacity_unit","UNKNOWN").uppercase().takeIf{it in setOf("PERSONS","FAMILIES","HOUSEHOLDS","UNKNOWN")}?:"UNKNOWN"
   val status=o.optString("operational_status","UNKNOWN").uppercase().takeIf{it in setOf("OPEN","LIMITED","FULL","CLOSED","UNKNOWN","UNDER_CONSTRUCTION")}?:"UNKNOWN"
   repository.addEvacuationCenter(NationalEvacuationCenter(required("id"),o.optString("psgc_code").takeIf{it.isNotBlank()},required("name"),required("address"),o.optString("facility_type").takeIf{it.isNotBlank()},lat,lon,capacity,capacityUnit,status,source,required("source_title"),verification,null));imported++
  };return imported
 }
 private fun readText(context:Context,uri:Uri):String{
  val input=context.contentResolver.openInputStream(uri)?:error("Cannot open selected file.")
  return input.use{stream->val out=ByteArrayOutputStream();val b=ByteArray(8192);var total=0
   while(true){val n=stream.read(b);if(n<0)break;total+=n;require(total<=MAX_BYTES){"JSON exceeds 15 MB."};out.write(b,0,n)}
   out.toString(Charsets.UTF_8.name())
  }
 }

 private fun parseCsv(text:String):List<List<String>>{
  val rows=mutableListOf<List<String>>();var row=mutableListOf<String>();val field=StringBuilder();var quoted=false;var i=0
  while(i<text.length){val ch=text[i];when{ch=='"'&&quoted&&i+1<text.length&&text[i+1]=='"'->{field.append('"');i++};ch=='"'->{quoted=!quoted};ch==','&&!quoted->{row.add(field.toString());field.setLength(0)};(ch=='\n'||ch=='\r')&&!quoted->{row.add(field.toString());field.setLength(0);if(row.any(String::isNotBlank))rows.add(row);row=mutableListOf();if(ch=='\r'&&i+1<text.length&&text[i+1]=='\n')i++};else->field.append(ch)};i++}
  require(!quoted){"Unclosed CSV quote."};row.add(field.toString());if(row.any(String::isNotBlank))rows.add(row);return rows
 }
}
