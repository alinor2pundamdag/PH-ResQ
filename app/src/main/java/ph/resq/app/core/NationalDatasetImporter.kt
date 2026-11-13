package ph.resq.app.core
import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
object NationalDatasetImporter{
 private const val MAX_BYTES=15*1024*1024
 fun importBarangaysCsv(context:Context,uri:Uri,repository:NationalReferenceRepository,version:String):Int{
  val input=context.contentResolver.openInputStream(uri)?:error("Cannot open CSV.")
  val bytes=input.use{stream->val out=ByteArrayOutputStream();val b=ByteArray(8192);var total=0;while(true){val n=stream.read(b);if(n<0)break;total+=n;require(total<=MAX_BYTES){"CSV exceeds 15 MB."};out.write(b,0,n)};out.toByteArray()}
  val rows=parseCsv(bytes.toString(Charsets.UTF_8).removePrefix("\uFEFF"));require(rows.isNotEmpty()){"CSV is empty."}
  val header=rows.first().map{it.trim().lowercase()};val required=listOf("psgc_code","region","province","city_municipality","barangay");required.forEach{require(header.contains(it)){"Missing column: $it"}}
  val ix=required.associateWith{header.indexOf(it)};val urban=header.indexOf("urban_rural")
  val records=rows.drop(1).filter{it.any(String::isNotBlank)}.mapIndexed{rowNo,row->
   fun f(name:String):String{val i=ix.getValue(name);require(i<row.size){"Malformed CSV row \${rowNo+2}"};return row[i].trim()}
   BarangayReference(f("psgc_code"),f("region"),f("province"),f("city_municipality"),f("barangay"),if(urban in row.indices)row[urban].trim().ifBlank{null}else null)
  }
  require(records.size==NationalReferenceRepository.EXPECTED_BARANGAYS_Q3_2026){"Expected 42,010 barangays; found \${records.size}. Partial import blocked."}
  return repository.replaceBarangays(records,NationalReferenceRepository.PSA_SOURCE,version)
 }
 private fun parseCsv(text:String):List<List<String>>{
  val rows=mutableListOf<List<String>>();var row=mutableListOf<String>();val field=StringBuilder();var quoted=false;var i=0
  while(i<text.length){val ch=text[i];when{ch=='"'&&quoted&&i+1<text.length&&text[i+1]=='"'->{field.append('"');i++};ch=='"'->{quoted=!quoted};ch==','&&!quoted->{row.add(field.toString());field.setLength(0)};(ch=='\n'||ch=='\r')&&!quoted->{row.add(field.toString());field.setLength(0);if(row.any(String::isNotBlank))rows.add(row);row=mutableListOf();if(ch=='\r'&&i+1<text.length&&text[i+1]=='\n')i++};else->field.append(ch)};i++}
  require(!quoted){"Unclosed CSV quote."};row.add(field.toString());if(row.any(String::isNotBlank))rows.add(row);return rows
 }
}
