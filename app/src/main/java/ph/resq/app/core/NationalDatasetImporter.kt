package ph.resq.app.core
import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import org.json.JSONObject
object NationalDatasetImporter{
 private const val MAX_BYTES=15*1024*1024
 fun importBarangaysCsv(context:Context,uri:Uri,repository:NationalReferenceRepository,version:String):Int{
  val input=context.contentResolver.openInputStream(uri)?:error("Cannot open CSV.")
  val bytes=input.use{stream->val out=ByteArrayOutputStream();val b=ByteArray(8192);var total=0;while(true){val n=stream.read(b);if(n<0)break;total+=n;require(total<=MAX_BYTES){"CSV exceeds 15 MB."};out.write(b,0,n)};out.toByteArray()}
  val rows=parseCsv(bytes.toString(Charsets.UTF_8).removePrefix("\uFEFF"));require(rows.isNotEmpty()){"CSV is empty."}
  val header=rows.first().map{it.trim().lowercase()};val required=listOf("psgc_code","region","province","city_municipality","barangay");required.forEach{require(header.contains(it)){"Missing column: $it"}}
  val ix=required.associateWith{header.indexOf(it)};val urban=header.indexOf("urban_rural")
  val records=rows.drop(1).filter{it.any(String::isNotBlank)}.mapIndexed{rowNo,row->
   fun f(name:String):String{val i=ix.getValue(name);require(i<row.size){"Malformed CSV row ${rowNo+2}"};return row[i].trim()}
   BarangayReference(f("psgc_code"),f("region"),f("province"),f("city_municipality"),f("barangay"),if(urban in row.indices)row[urban].trim().ifBlank{null}else null)
  }
  require(records.size==NationalReferenceRepository.EXPECTED_BARANGAYS_Q3_2026){"Expected 42,010 barangays; found ${records.size}. Partial import blocked."}
  return repository.replaceBarangays(records,NationalReferenceRepository.PSA_SOURCE,version)
 }

 fun importPublicContactsJson(context:Context,uri:Uri,repository:NationalReferenceRepository):Int{
  val root=JSONObject(readText(context,uri));require(root.optString("type")=="emergency_contacts"){"Expected emergency_contacts dataset."}
  val records=root.optJSONArray("records")?:error("Missing records array.");require(records.length() in 1..100_000);var imported=0
  for(i in 0 until records.length()){
   val o=records.getJSONObject(i)
   fun required(k:String)=o.optString(k,"").trim().also{require(it.isNotBlank()){"Record ${i+1} missing $k"}}
   val source=required("source_url");val host=runCatching{android.net.Uri.parse(source).host.orEmpty().lowercase()}.getOrDefault("")
   val verification=if(host.endsWith(".gov.ph")||host=="gov.ph")ContactVerification.OFFICIAL_GOVERNMENT_SOURCE else ContactVerification.UNVERIFIED_SOCIAL
   repository.addPublicContact(PublicEmergencyContact(required("id"),o.optString("psgc_code").takeIf{it.isNotBlank()},required("scope").uppercase(),required("category").uppercase(),required("office_name"),required("phone"),source,required("source_title"),verification,null,null));imported++
  };return imported
 }
 fun importEvacuationCentersJson(context:Context,uri:Uri,repository:NationalReferenceRepository):Int{
  val root=JSONObject(readText(context,uri));require(root.optString("type")=="evacuation_centers"){"Expected evacuation_centers dataset."}
  val records=root.optJSONArray("records")?:error("Missing records array.");require(records.length() in 1..50_000);var imported=0
  for(i in 0 until records.length()){
   val o=records.getJSONObject(i)
   fun required(k:String)=o.optString(k,"").trim().also{require(it.isNotBlank()){"Record ${i+1} missing $k"}}
   val source=required("source_url");val host=runCatching{android.net.Uri.parse(source).host.orEmpty().lowercase()}.getOrDefault("")
   val verification=if(host.endsWith(".gov.ph")||host=="gov.ph")ContactVerification.OFFICIAL_GOVERNMENT_SOURCE else ContactVerification.NEEDS_REVIEW
   val lat=if(o.isNull("latitude"))null else o.getDouble("latitude");val lon=if(o.isNull("longitude"))null else o.getDouble("longitude")
   val capacity=if(o.isNull("capacity"))null else o.getInt("capacity")
   val status=o.optString("operational_status","UNKNOWN").uppercase().takeIf{it in setOf("OPEN","LIMITED","FULL","CLOSED","UNKNOWN","UNDER_CONSTRUCTION")}?:"UNKNOWN"
   repository.addEvacuationCenter(NationalEvacuationCenter(required("id"),o.optString("psgc_code").takeIf{it.isNotBlank()},required("name"),required("address"),o.optString("facility_type").takeIf{it.isNotBlank()},lat,lon,capacity,status,source,required("source_title"),verification,null));imported++
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
