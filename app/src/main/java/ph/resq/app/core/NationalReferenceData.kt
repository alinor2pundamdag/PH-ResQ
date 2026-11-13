package ph.resq.app.core
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
enum class ContactVerification { NEEDS_REVIEW, OFFICIAL_GOVERNMENT_SOURCE, CROSS_CHECKED, VERIFIED_BY_CALL, UNVERIFIED_SOCIAL }
data class BarangayReference(val psgcCode:String,val region:String,val province:String,val cityMunicipality:String,val barangay:String,val urbanRural:String?=null)
data class PublicEmergencyContact(val id:String,val psgcCode:String?,val scope:String,val category:String,val officeName:String,val phone:String,val sourceUrl:String,val sourceTitle:String,val verification:ContactVerification,val verifiedAt:Long?=null,val expiresAt:Long?=null)
data class NationalEvacuationCenter(val id:String,val psgcCode:String?,val name:String,val address:String,val facilityType:String?,val latitude:Double?,val longitude:Double?,val capacity:Int?,val capacityUnit:String,val operationalStatus:String,val sourceUrl:String,val sourceTitle:String,val verification:ContactVerification,val verifiedAt:Long?=null)
data class NationalCoverageSnapshot(val expectedBarangays:Int,val loadedBarangays:Int,val contactBarangays:Int,val evacuationCenters:Int,val geocodedCenters:Int,val verifiedContacts:Int,val barangayCoveragePercent:Double,val publicContactRecords:Int=0,val barangayContactRecords:Int=0,val cityMunicipalityContactRecords:Int=0,val provinceContactRecords:Int=0,val regionalContactRecords:Int=0,val nationalContactRecords:Int=0,val linkedBarangayContactRecords:Int=0,val unlinkedBarangayContactRecords:Int=0,val linkedEvacuationCenters:Int=0,val unlinkedEvacuationCenters:Int=0)
class NationalReferenceRepository(private val database:ResQDatabase) {
 companion object { const val EXPECTED_BARANGAYS_Q3_2026=42_010; const val PSA_SOURCE="https://psa.gov.ph/classification/psgc" }
 fun replaceBarangays(records:List<BarangayReference>,sourceUrl:String,sourceVersion:String):Int {
  require(sourceUrl==PSA_SOURCE); require(sourceVersion.isNotBlank()&&sourceVersion.length<=80)
  require(records.size==EXPECTED_BARANGAYS_Q3_2026){"Expected 42,010 Q3-2026 barangays; received ${records.size}."}
  require(records.map{it.psgcCode}.distinct().size==records.size)
  records.forEach{require(it.psgcCode.matches(Regex("\\d{10}")));require(listOf(it.region,it.province,it.cityMunicipality,it.barangay).all(String::isNotBlank))}
  val db=database.writableDatabase;db.beginTransaction()
  try {
   db.delete("barangay_reference",null,null)
   records.forEach{r->val v=ContentValues().apply{put("psgc_code",r.psgcCode);put("region",r.region);put("province",r.province);put("city_municipality",r.cityMunicipality);put("barangay",r.barangay);put("urban_rural",r.urbanRural);put("source_url",sourceUrl);put("source_version",sourceVersion);put("imported_at",System.currentTimeMillis())};db.insertOrThrow("barangay_reference",null,v)}
   val m=ContentValues().apply{put("dataset_name","psgc_barangays");put("source_url",sourceUrl);put("license","PSA CC BY 4.0 unless otherwise stated");put("version",sourceVersion);put("verified_at",0L);put("imported_at",System.currentTimeMillis());put("record_count",records.size)}
   db.insertWithOnConflict("dataset_meta",null,m,SQLiteDatabase.CONFLICT_REPLACE);db.setTransactionSuccessful()
  } finally {db.endTransaction()}
  linkContactsToBarangays()
  linkEvacuationCentersToBarangays()
  return records.size
 }
 private fun normalizeLabel(value:String):String {
  val normalized=value.trim().lowercase()
   .replace(Regex("^sta\\.?\\s+"),"santa ").replace(Regex("^sto\\.?\\s+"),"santo ")
   .replace(Regex("^st\\.?\\s+"),"saint ").replace("kapitoly","kapitolyo").replace("siena","sienna").replace("ñ","n")
   .replace(Regex("[^a-z0-9]+")," ").trim()
  return normalized
   .replace(Regex("\\bquirino\\s+([23])\\s+([ac])\\b"),"quirino $1$2")
   .replace(Regex("\\bescopa\\s+i\\b"),"escopa 1").replace(Regex("\\bescopa\\s+ii\\b"),"escopa 2")
   .replace(Regex("\\bescopa\\s+iii\\b"),"escopa 3").replace(Regex("\\bescopa\\s+iv\\b"),"escopa 4")
   .replace(Regex("\\bu\\s+p\\s+(campus|village)\\b"),"up $1")
   .replace(Regex("\\bquirino 3b claro\\b"),"claro")
   .let { if(it=="pasong putik") "pasong putik proper" else it }
 }
 private fun resolveBarangayPsgc(label:String):String? {
  // Addresses may include "Barangay X, City/Municipality, Province". Use the
  // province when supplied; without it, never choose arbitrarily among duplicate
  // city/barangay names elsewhere in the Philippines.
  val matches=Regex("Barangay\\s+([^,]+),\\s*([^,]+)(?:,\\s*([^,]+))?",RegexOption.IGNORE_CASE).findAll(label)
  for(match in matches){
   val barangay=normalizeLabel(match.groupValues[1])
   val rawCity=match.groupValues[2].trim()
   val rawProvince=match.groupValues.getOrNull(3)?.trim().orEmpty()
   val baseCity=rawCity.replace(Regex("^(City|Municipality) of\\s+",RegexOption.IGNORE_CASE),"")
    .replace(Regex("\\s+(City|Municipality)$",RegexOption.IGNORE_CASE),"").trim()
   val cityVariants=listOf(rawCity,baseCity,"City of $baseCity","$baseCity City","Municipality of $baseCity").distinct()
   val normalizedCities=cityVariants.map(::normalizeLabel).toSet()
   val placeholders=cityVariants.joinToString(","){"?"}
   val candidates=database.readableDatabase.rawQuery(
    "SELECT psgc_code,province,city_municipality,barangay FROM barangay_reference WHERE city_municipality IN ($placeholders)",
    cityVariants.toTypedArray()
   )
   val matchedCodes=linkedSetOf<String>()
   candidates.use { c ->
    while(c.moveToNext()){
     val storedProvince=normalizeLabel(c.getString(1))
     val storedCity=normalizeLabel(c.getString(2))
     val storedBarangay=normalizeLabel(c.getString(3))
     val provinceMatches=rawProvince.isBlank()||storedProvince==normalizeLabel(rawProvince)
     if(provinceMatches&&storedBarangay==barangay&&storedCity in normalizedCities)matchedCodes.add(c.getString(0))
    }
   }
   if(matchedCodes.size==1)return matchedCodes.first()
  }
  return null
 }
 private fun linkEvacuationCentersToBarangays(){
  val db=database.writableDatabase
  val pending=mutableListOf<Pair<String,String>>()
  db.rawQuery("SELECT center_id,address FROM national_evacuation_centers WHERE psgc_code IS NULL",null).use{c->while(c.moveToNext())pending.add(c.getString(0) to c.getString(1))}
  pending.forEach{(id,label)->resolveBarangayPsgc(label)?.let{code->db.execSQL("UPDATE national_evacuation_centers SET psgc_code=? WHERE center_id=?",arrayOf(code,id))}}
 }
 private fun linkContactsToBarangays() {
  val db=database.writableDatabase
  val pending=mutableListOf<Pair<String,String>>()
  db.rawQuery("SELECT contact_id,office_name FROM public_emergency_contacts WHERE psgc_code IS NULL AND scope='BARANGAY'",null).use{c->while(c.moveToNext())pending.add(c.getString(0) to c.getString(1))}
  pending.forEach{(id,label)->resolveBarangayPsgc(label)?.let{code->db.execSQL("UPDATE public_emergency_contacts SET psgc_code=? WHERE contact_id=?",arrayOf(code,id))}}
 }
 fun searchBarangays(query:String,limit:Int=100):List<BarangayReference>{
  val q=query.trim().take(120);val n=limit.coerceIn(1,500)
  val sql="SELECT psgc_code,region,province,city_municipality,barangay,urban_rural FROM barangay_reference "+(if(q.isBlank())"" else "WHERE barangay LIKE ? OR city_municipality LIKE ? OR province LIKE ? OR psgc_code = ? ")+"ORDER BY barangay LIMIT $n"
  val args=if(q.isBlank())null else arrayOf("%$q%","%$q%","%$q%",q);val out=mutableListOf<BarangayReference>()
  database.readableDatabase.rawQuery(sql,args).use{c->while(c.moveToNext())out.add(BarangayReference(c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),if(c.isNull(5))null else c.getString(5)))}
  return out
 }
 private fun isValidPublicPhone(phone:String):Boolean =
  phone in setOf("911","122","117","143","136","1342","7890","1627","1626") ||
   phone.matches(Regex("[+0-9() .,-]{5,40}"))

 fun addPublicContact(c:PublicEmergencyContact){
  require(c.id.isNotBlank()&&c.id.length<=160&&(c.psgcCode==null||c.psgcCode.matches(Regex("\\d{10}"))))
  require(c.scope in setOf("BARANGAY","CITY_MUNICIPALITY","PROVINCE","REGIONAL","NATIONAL"))
  require(c.category in setOf("BARANGAY_HALL","DRRMO","POLICE","FIRE","AMBULANCE","HOSPITAL","COAST_GUARD","RESCUE","OTHER"))
  require(c.officeName.isNotBlank()&&isValidPublicPhone(c.phone)&&c.sourceUrl.startsWith("https://")&&c.sourceTitle.isNotBlank())
  val resolvedPsgc=c.psgcCode ?: if(c.scope=="BARANGAY") resolveBarangayPsgc(c.officeName) else null
  val v=ContentValues().apply{put("contact_id",c.id);put("psgc_code",resolvedPsgc);put("scope",c.scope);put("category",c.category);put("office_name",c.officeName.trim());put("phone",c.phone.trim());put("source_url",c.sourceUrl);put("source_title",c.sourceTitle);put("verification",c.verification.name);if(c.verifiedAt==null)putNull("verified_at")else put("verified_at",c.verifiedAt);if(c.expiresAt==null)putNull("expires_at")else put("expires_at",c.expiresAt);put("updated_at",System.currentTimeMillis())}
  val rowId=database.writableDatabase.insertWithOnConflict("public_emergency_contacts",null,v,SQLiteDatabase.CONFLICT_REPLACE)
  check(rowId!=-1L){"Could not store public contact ${c.id}."}
 }
 fun addPublicContacts(records:List<PublicEmergencyContact>):Int{
  require(records.size in 1..100_000)
  require(records.map{it.id}.distinct().size==records.size){"Duplicate public contact IDs in import."}
  val db=database.writableDatabase;db.beginTransaction()
  try{
   records.forEachIndexed{index,record->
    try{addPublicContact(record)}catch(error:Exception){
     throw IllegalArgumentException("Contact record ${index+1} (${record.id}) rejected: ${error.message}",error)
    }
   }
   db.setTransactionSuccessful()
  }finally{db.endTransaction()}
  return records.size
 }
 fun searchContacts(query:String,limit:Int=100):List<PublicEmergencyContact>{
  val q=query.trim().take(120);val n=limit.coerceIn(1,500)
  val sql="SELECT contact_id,psgc_code,scope,category,office_name,phone,source_url,source_title,verification,verified_at,expires_at FROM public_emergency_contacts "+(if(q.isBlank())"" else "WHERE office_name LIKE ? OR category LIKE ? OR phone LIKE ? OR psgc_code = ? ")+"ORDER BY CASE verification WHEN 'VERIFIED_BY_CALL' THEN 0 WHEN 'CROSS_CHECKED' THEN 1 WHEN 'OFFICIAL_GOVERNMENT_SOURCE' THEN 2 ELSE 3 END,office_name LIMIT $n"
  val args=if(q.isBlank())null else arrayOf("%$q%","%$q%","%$q%",q);val out=mutableListOf<PublicEmergencyContact>()
  database.readableDatabase.rawQuery(sql,args).use{c->while(c.moveToNext())out.add(PublicEmergencyContact(c.getString(0),if(c.isNull(1))null else c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6),c.getString(7),runCatching{ContactVerification.valueOf(c.getString(8))}.getOrDefault(ContactVerification.NEEDS_REVIEW),if(c.isNull(9))null else c.getLong(9),if(c.isNull(10))null else c.getLong(10)))}
  return out
 }
 fun addEvacuationCenter(c:NationalEvacuationCenter){
  require(c.id.isNotBlank()&&c.id.length<=160&&(c.psgcCode==null||c.psgcCode.matches(Regex("\\d{10}"))))
  require(c.name.isNotBlank()&&c.address.isNotBlank()&&c.sourceUrl.startsWith("https://")&&c.sourceTitle.isNotBlank())
  require((c.latitude==null)==(c.longitude==null));if(c.latitude!=null)require(c.latitude.isFinite()&&c.latitude in -90.0..90.0&&c.longitude!!.isFinite()&&c.longitude in -180.0..180.0)
  require(c.capacity==null||c.capacity in 1..1_000_000);require(c.capacityUnit in setOf("PERSONS","FAMILIES","HOUSEHOLDS","UNKNOWN"));require(c.operationalStatus in setOf("OPEN","LIMITED","FULL","CLOSED","UNKNOWN","UNDER_CONSTRUCTION"))
  val resolvedCode=c.psgcCode?:resolveBarangayPsgc(c.address)
  val v=ContentValues().apply{put("center_id",c.id);put("psgc_code",resolvedCode);put("name",c.name);put("address",c.address);put("facility_type",c.facilityType);if(c.latitude==null)putNull("latitude")else put("latitude",c.latitude);if(c.longitude==null)putNull("longitude")else put("longitude",c.longitude);if(c.capacity==null)putNull("capacity")else put("capacity",c.capacity);put("capacity_unit",c.capacityUnit);put("operational_status",c.operationalStatus);put("source_url",c.sourceUrl);put("source_title",c.sourceTitle);put("verification",c.verification.name);if(c.verifiedAt==null)putNull("verified_at")else put("verified_at",c.verifiedAt);put("updated_at",System.currentTimeMillis())}
  val rowId=database.writableDatabase.insertWithOnConflict("national_evacuation_centers",null,v,SQLiteDatabase.CONFLICT_REPLACE)
  check(rowId!=-1L){"Could not store evacuation center ${c.id}."}
 }
 fun addEvacuationCenters(records:List<NationalEvacuationCenter>):Int{
  require(records.size in 1..50_000)
  require(records.map{it.id}.distinct().size==records.size){"Duplicate evacuation center IDs in import."}
  val db=database.writableDatabase;db.beginTransaction()
  try{
   records.forEachIndexed{index,record->
    try{addEvacuationCenter(record)}catch(error:Exception){
     throw IllegalArgumentException("Evacuation record ${index+1} (${record.id}) rejected: ${error.message}",error)
    }
   }
   db.setTransactionSuccessful()
  }finally{db.endTransaction()}
  return records.size
 }

 fun searchEvacuationCenters(query:String,limit:Int=100):List<NationalEvacuationCenter>{
  val q=query.trim().take(120);val n=limit.coerceIn(1,500)
  val sql="SELECT center_id,psgc_code,name,address,facility_type,latitude,longitude,capacity,capacity_unit,operational_status,source_url,source_title,verification,verified_at FROM national_evacuation_centers "+
   (if(q.isBlank())"" else "WHERE name LIKE ? OR address LIKE ? OR psgc_code = ? ")+"ORDER BY name LIMIT $n"
  val args=if(q.isBlank())null else arrayOf("%$q%","%$q%",q);val out=mutableListOf<NationalEvacuationCenter>()
  database.readableDatabase.rawQuery(sql,args).use{c->while(c.moveToNext())out.add(NationalEvacuationCenter(
   c.getString(0),if(c.isNull(1))null else c.getString(1),c.getString(2),c.getString(3),if(c.isNull(4))null else c.getString(4),
   if(c.isNull(5))null else c.getDouble(5),if(c.isNull(6))null else c.getDouble(6),if(c.isNull(7))null else c.getInt(7),c.getString(8),c.getString(9),
   c.getString(10),c.getString(11),runCatching{ContactVerification.valueOf(c.getString(12))}.getOrDefault(ContactVerification.NEEDS_REVIEW),
   if(c.isNull(13))null else c.getLong(13)))}
  return out
 }

 fun coverage():NationalCoverageSnapshot{
  fun count(sql:String)=database.readableDatabase.rawQuery(sql,null).use{if(it.moveToFirst())it.getInt(0)else 0}
  val loaded=count("SELECT COUNT(*) FROM barangay_reference");val contactBarangays=count("SELECT COUNT(DISTINCT psgc_code) FROM public_emergency_contacts WHERE psgc_code IS NOT NULL AND scope='BARANGAY'")
  val centers=count("SELECT COUNT(*) FROM national_evacuation_centers");val geocoded=count("SELECT COUNT(*) FROM national_evacuation_centers WHERE latitude IS NOT NULL AND longitude IS NOT NULL")
  val verified=count("SELECT COUNT(*) FROM public_emergency_contacts WHERE verification IN ('CROSS_CHECKED','VERIFIED_BY_CALL')")
  val publicContacts=count("SELECT COUNT(*) FROM public_emergency_contacts")
  val barangayContactRecords=count("SELECT COUNT(*) FROM public_emergency_contacts WHERE scope='BARANGAY'")
  val cityMunicipalityContactRecords=count("SELECT COUNT(*) FROM public_emergency_contacts WHERE scope='CITY_MUNICIPALITY'")
  val provinceContactRecords=count("SELECT COUNT(*) FROM public_emergency_contacts WHERE scope='PROVINCE'")
  val regionalContactRecords=count("SELECT COUNT(*) FROM public_emergency_contacts WHERE scope='REGIONAL'")
  val nationalContactRecords=count("SELECT COUNT(*) FROM public_emergency_contacts WHERE scope='NATIONAL'")
  val linkedBarangayContacts=count("SELECT COUNT(*) FROM public_emergency_contacts WHERE scope='BARANGAY' AND psgc_code IS NOT NULL")
  val linkedEvacuationCenters=count("SELECT COUNT(*) FROM national_evacuation_centers WHERE psgc_code IS NOT NULL")
  return NationalCoverageSnapshot(EXPECTED_BARANGAYS_Q3_2026,loaded,contactBarangays,centers,geocoded,verified,(loaded.toDouble()/EXPECTED_BARANGAYS_Q3_2026*100.0).coerceAtMost(100.0),publicContacts,barangayContactRecords,cityMunicipalityContactRecords,provinceContactRecords,regionalContactRecords,nationalContactRecords,linkedBarangayContacts,barangayContactRecords-linkedBarangayContacts,linkedEvacuationCenters,centers-linkedEvacuationCenters)
 }
}
