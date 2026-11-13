package ph.resq.app.ui

import android.os.StatFs
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView as MapsforgeMapView
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.internal.MapsforgeThemes
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

private object MapsforgeGraphicsRuntime{
    @Volatile private var initialized=false
    @Synchronized fun initialize(context:Context){if(!initialized){AndroidGraphicFactory.createInstance(context.applicationContext);initialized=true}}
}

private const val NATIONWIDE_MAP_URL="https://github.com/alinor2pundamdag/PH-ResQ/releases/download/offline-map-latest/philippines.map"
private const val NATIONWIDE_MAP_SHA_URL="https://github.com/alinor2pundamdag/PH-ResQ/releases/download/offline-map-latest/philippines.map.sha256"

@Composable
fun NationwideOfflineMapPanel(modifier:Modifier=Modifier){
    val context=LocalContext.current
    val scope=rememberCoroutineScope()
    val mapFile=remember(context){File(context.getExternalFilesDir(null)?:context.filesDir,"maps/philippines.map")}
    val checksumFile=remember(context){File(mapFile.parentFile,"philippines.map.sha256")}
    val verificationMarker=remember(context){File(mapFile.parentFile,"philippines.map.verified")}
    var available by remember{mutableStateOf(false)}
    var checkingExisting by remember{mutableStateOf(true)}
    var downloading by remember{mutableStateOf(false)}
    var progress by remember{mutableFloatStateOf(0f)}
    var status by remember{mutableStateOf("Nationwide map package is published by the PH-ResQ map release workflow.")}
    var mapView by remember{mutableStateOf<MapsforgeMapView?>(null)}
    var mapLoaded by remember{mutableStateOf(false)}
    var mapLoadFailed by remember{mutableStateOf(false)}
    val client=remember{OkHttpClient.Builder().connectTimeout(30,TimeUnit.SECONDS).readTimeout(90,TimeUnit.SECONDS).callTimeout(30,TimeUnit.MINUTES).build()}

    // Verify an installed map once per file version. The marker is tied to the file size,
    // modification time and published SHA-256; any changed file is re-hashed before use.
    LaunchedEffect(mapFile, checksumFile, verificationMarker){
        val valid=withContext(Dispatchers.IO){
            runCatching{
                if(!mapFile.isFile || mapFile.length()<=100_000_000L || !checksumFile.isFile) return@runCatching false
                val expected=checksumFile.readText().trim().split(Regex("\\s+")).firstOrNull()
                    ?.takeIf{it.matches(Regex("[a-fA-F0-9]{64}"))}?:return@runCatching false
                val markerValue="${mapFile.length()}|${mapFile.lastModified()}|${expected.lowercase()}"
                if(verificationMarker.isFile && verificationMarker.readText().trim()==markerValue) return@runCatching true
                val digest=MessageDigest.getInstance("SHA-256")
                FileInputStream(mapFile).use{input->
                    val buffer=ByteArray(1024*1024)
                    while(true){val count=input.read(buffer);if(count<0)break;digest.update(buffer,0,count)}
                }
                val actual=digest.digest().joinToString(""){"%02x".format(it)}
                if(!actual.equals(expected,ignoreCase=true)){
                    mapFile.delete();checksumFile.delete();verificationMarker.delete()
                    return@runCatching false
                }
                verificationMarker.writeText(markerValue)
                true
            }.getOrDefault(false)
        }
        available=valid
        checkingExisting=false
        status=if(valid)"Installed nationwide map integrity verified." else "No verified nationwide map is installed. Download the checksum-verified release to enable offline rendering."
    }

    DisposableEffect(mapView){
        val view=mapView
        if(view==null) onDispose { }
        else onDispose{
            view.destroyAll()
            AndroidGraphicFactory.clearResourceMemoryCache()
        }
    }

    Column(modifier,verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text("NATIONWIDE PHILIPPINES OFFLINE MAP",style=MaterialTheme.typography.titleLarge)
        Text("Full-country Philippines vector basemap in Mapsforge v5 format. It is separate from the smaller MapLibre area packs.")
        if(!available){
            if(checkingExisting) Text("Checking the installed map file…",style=MaterialTheme.typography.bodySmall)
            Button(enabled=!downloading&&!checkingExisting,onClick={
                scope.launch{
                    downloading=true;progress=0f;status="Checking map release, checksum and available storage…"
                    val result=runCatching{
                        withContext(Dispatchers.IO){
                            mapFile.parentFile?.mkdirs()
                            val expected=client.newCall(Request.Builder().url(NATIONWIDE_MAP_SHA_URL).get().build()).execute().use{response->
                                check(response.isSuccessful){"Map checksum is not published yet (HTTP ${response.code})."}
                                response.body?.string()?.trim()?.substringBefore(" ")?.also{check(it.matches(Regex("[a-fA-F0-9]{64}"))){"Invalid map checksum file."}}?:error("Missing checksum response.")
                            }
                            val size=client.newCall(Request.Builder().url(NATIONWIDE_MAP_URL).head().build()).execute().use{response->
                                check(response.isSuccessful){"Nationwide map is not published yet (HTTP ${response.code})."}
                                response.header("Content-Length")?.toLongOrNull()?:-1L
                            }
                            check(size>100_000_000L){"Map release size is invalid."}
                            val free=StatFs(mapFile.parentFile!!.absolutePath).availableBytes
                            check(free>size+150L*1024L*1024L){"Not enough free space. Need at least ${(size+150L*1024L*1024L)/1048576L} MB free."}
                            val temp=File(mapFile.parentFile,"philippines.map.part")
                            val backup=File(mapFile.parentFile,"philippines.map.backup")
                            temp.delete();backup.delete()
                            try{
                                val digest=MessageDigest.getInstance("SHA-256")
                                var downloaded=0L
                                client.newCall(Request.Builder().url(NATIONWIDE_MAP_URL).get().build()).execute().use{response->
                                    check(response.isSuccessful){"Map download failed (HTTP ${response.code})."}
                                    val body=response.body?:error("Empty map download.")
                                    val total=body.contentLength().takeIf{it>0}?:size
                                    body.byteStream().use{input->
                                        FileOutputStream(temp).use{output->
                                            val buffer=ByteArray(1024*1024);var lastUpdate=0L
                                            while(true){
                                                currentCoroutineContext().ensureActive()
                                                val count=input.read(buffer);if(count<0)break
                                                output.write(buffer,0,count);digest.update(buffer,0,count);downloaded+=count
                                                if(downloaded-lastUpdate>=8L*1024L*1024L){
                                                    lastUpdate=downloaded
                                                    withContext(Dispatchers.Main){progress=(downloaded.toFloat()/total.toFloat()).coerceIn(0f,1f)}
                                                }
                                            }
                                            output.fd.sync()
                                        }
                                    }
                                }
                                check(downloaded==size && temp.length()==size){"Downloaded map size does not match the release metadata."}
                                val actual=digest.digest().joinToString(""){"%02x".format(it)}
                                check(actual.equals(expected,ignoreCase=true)){"SHA-256 mismatch. The downloaded map was rejected."}
                                val oldChecksum=checksumFile.takeIf{it.isFile}?.readText()
                                val oldMarker=verificationMarker.takeIf{it.isFile}?.readText()
                                val hadPrevious=mapFile.exists()
                                if(hadPrevious)check(mapFile.renameTo(backup)){"Could not stage the previous map safely."}
                                try{
                                    check(temp.renameTo(mapFile)){"Could not install the verified map file."}
                                    checksumFile.writeText("$expected  philippines.map\n")
                                    val markerValue=mapFile.length().toString()+"|"+mapFile.lastModified()+"|"+expected.lowercase()
                                    verificationMarker.writeText(markerValue)
                                    backup.delete()
                                }catch(error:Throwable){
                                    mapFile.delete()
                                    if(hadPrevious && backup.exists())backup.renameTo(mapFile)
                                    if(oldChecksum!=null)checksumFile.writeText(oldChecksum)else checksumFile.delete()
                                    if(oldMarker!=null)verificationMarker.writeText(oldMarker)else verificationMarker.delete()
                                    throw error
                                }
                            }finally{
                                temp.delete()
                            }
                        }
                    }
                    result.exceptionOrNull()?.let { if(it is CancellationException) throw it }
                    status=result.fold({"Nationwide map downloaded and SHA-256 verified."},{"Download failed: ${it.message}"})
                    available=result.isSuccess&&mapFile.exists()&&checksumFile.exists()&&verificationMarker.exists()
                    downloading=false
                    mapLoaded=false
                }
            },modifier=Modifier.fillMaxWidth()){Text(if(downloading)"DOWNLOADING NATIONWIDE MAP…" else "DOWNLOAD FULL PHILIPPINES MAP (WI-FI RECOMMENDED)")}
            if(downloading)LinearProgressIndicator(progress={progress},modifier=Modifier.fillMaxWidth())
        }else{
            Text("Installed map size: ${mapFile.length()/1048576L} MB")
            AndroidView(
                modifier=Modifier.fillMaxWidth().height(340.dp),
                factory={ctx->
                    MapsforgeGraphicsRuntime.initialize(ctx)
                    MapsforgeMapView(ctx).apply{
                        mapView=this
                        setBuiltInZoomControls(true)
                        mapScaleBar.setVisible(true)
                    }
                },
                update={view->
                    if(!mapLoaded && !mapLoadFailed){
                        runCatching{
                            val tileCache=AndroidUtil.createTileCache(context,"ph-resq-philippines",view.model.displayModel.tileSize,1f,view.model.frameBufferModel.overdrawFactor)
                            val dataStore=MapFile(FileInputStream(mapFile))
                            val layer=TileRendererLayer(tileCache,dataStore,view.model.mapViewPosition,AndroidGraphicFactory.INSTANCE)
                            layer.setXmlRenderTheme(MapsforgeThemes.MOTORIDER)
                            view.layerManager.layers.add(layer)
                            view.setCenter(LatLong(12.8797,121.7740))
                            view.setZoomLevel(5.toByte())
                            mapLoaded=true
                            mapLoadFailed=false
                            status="Nationwide offline map is ready."
                        }.onFailure{status="Map file could not be opened: ${it.message}";mapLoaded=true;mapLoadFailed=true}
                    }
                }
            )
            if(mapLoadFailed){
                Text("The map passed checksum validation but the renderer could not open it. The downloaded file is retained; retry after checking available memory and storage.")
                Button(onClick={mapLoaded=false;mapLoadFailed=false;status="Retrying map renderer…"},modifier=Modifier.fillMaxWidth()){Text("RETRY MAP RENDERING")}
            }
            Button(onClick={
                mapView?.destroyAll()
                mapView=null
                mapLoaded=false
                mapLoadFailed=false
                available=false
                mapFile.delete();checksumFile.delete();verificationMarker.delete()
                status="Nationwide map deleted from app storage."
            },modifier=Modifier.fillMaxWidth()){Text("DELETE NATIONWIDE MAP AND FREE STORAGE")}
        }
        Text(status,style=MaterialTheme.typography.bodySmall)
        Text("Map data © OpenStreetMap contributors • Open Database License (ODbL). This is a basemap, not an authoritative barangay boundary, emergency-contact or evacuation-centre dataset.")
        Text("Stored in app-specific storage. No broad file/storage permission is requested. The full-country file is large; allow sufficient free space and prefer Wi-Fi.")
    }
}
