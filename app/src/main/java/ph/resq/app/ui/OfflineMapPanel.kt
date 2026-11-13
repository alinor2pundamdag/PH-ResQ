package ph.resq.app.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.module.http.HttpRequestUtil
import org.maplibre.android.offline.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.annotations.MarkerOptions
import ph.resq.app.core.NationalReferenceRepository
import ph.resq.app.core.NationalEvacuationCenter
import ph.resq.app.core.ResQDatabase
private const val STYLE_URL="https://tiles.openfreemap.org/styles/liberty"
private const val MAX_BYTES=40L*1024L*1024L
private const val MAX_TILES_PER_REGION=3000L
private const val DEVICE_TILE_LIMIT=6000L
@Composable fun OfflineMapPanel(database:ResQDatabase,modifier:Modifier=Modifier){
 val context=LocalContext.current;val owner=LocalLifecycleOwner.current;val scope=rememberCoroutineScope()
 var activeMap by remember{mutableStateOf<MapLibreMap?>(null)}
 var evacCenters by remember{mutableStateOf(emptyList<NationalEvacuationCenter>())}
 var mapView by remember{mutableStateOf<MapView?>(null)};var ready by remember{mutableStateOf(false)}
 var downloading by remember{mutableStateOf(false)};var progress by remember{mutableFloatStateOf(0f)}
 var saved by remember{mutableIntStateOf(0)};var regionItems by remember{mutableStateOf(emptyList<OfflineRegion>())};var status by remember{mutableStateOf("Pan and zoom to the area you want to save.")}
 val manager=remember(context){
  val dispatcher=Dispatcher().apply{maxRequests=3;maxRequestsPerHost=3}
  val client=OkHttpClient.Builder().dispatcher(dispatcher).connectTimeout(20,TimeUnit.SECONDS).readTimeout(30,TimeUnit.SECONDS)
   .addInterceptor{chain->chain.proceed(chain.request().newBuilder().header("User-Agent","PH-ResQ-Android/0.1 (offline-map-client)").build())}.build()
  HttpRequestUtil.setOkHttpClient(client);MapLibre.getInstance(context)
  OfflineManager.getInstance(context).also{it.setOfflineMapboxTileCountLimit(DEVICE_TILE_LIMIT)}
 }
 suspend fun refreshEvacuationPins(){evacCenters=withContext(Dispatchers.IO){NationalReferenceRepository(database).searchEvacuationCenters("",500).filter{it.latitude!=null&&it.longitude!=null}}}
 LaunchedEffect(database){refreshEvacuationPins()}
 LaunchedEffect(activeMap,evacCenters){val map=activeMap;if(map!=null){map.removeAnnotations();evacCenters.forEach{center->map.addMarker(MarkerOptions().position(LatLng(center.latitude!!,center.longitude!!)).title(center.name).snippet("${center.operationalStatus} • ${center.verification}"))}}}
 fun refresh(){manager.listOfflineRegions(object:OfflineManager.ListOfflineRegionsCallback{
  override fun onList(regions:Array<OfflineRegion>?){regionItems=regions?.toList().orEmpty();saved=regionItems.size}
  override fun onError(error:String){status="Cannot read saved map regions: $error"}
 })}
 DisposableEffect(owner,mapView){
  val view=mapView;val observer=LifecycleEventObserver{_,event->when(event){Lifecycle.Event.ON_START->view?.onStart();Lifecycle.Event.ON_RESUME->view?.onResume();Lifecycle.Event.ON_PAUSE->view?.onPause();Lifecycle.Event.ON_STOP->view?.onStop();else->Unit}}
  owner.lifecycle.addObserver(observer)
  if(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) view?.onStart()
  if(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) view?.onResume()
  onDispose{owner.lifecycle.removeObserver(observer);view?.onPause();view?.onStop();view?.onDestroy()}
 }
 Column(modifier,verticalArrangement=Arrangement.spacedBy(8.dp)){
  Text("OFFLINE MAP",style=MaterialTheme.typography.titleLarge)
  Text("Move the map to your target area. Download only the visible region; saved packs work without internet.")
  AndroidView(modifier=Modifier.fillMaxWidth().height(280.dp),factory={ctx->MapView(ctx).apply{
   onCreate(null);mapView=this
   getMapAsync{map->map.setStyle(STYLE_URL){map.cameraPosition=CameraPosition.Builder().target(LatLng(14.5995,120.9842)).zoom(10.5).build();activeMap=map;ready=true;refresh()}}
  }})
  Text("Evacuation-centre map pins with coordinates: ${evacCenters.size}")
  Button(enabled=true,onClick={scope.launch{refreshEvacuationPins()}},modifier=Modifier.fillMaxWidth()){Text("REFRESH EVACUATION-CENTRE PINS")}
  Button(enabled=ready&&!downloading,onClick={
   val view=mapView;if(view==null){status="Map is not ready.";return@Button}
   view.getMapAsync{map->
    val bounds=map.projection.visibleRegion.latLngBounds
    val definition=OfflineTilePyramidRegionDefinition(STYLE_URL,bounds,9.0,14.0,view.pixelRatio)
    downloading=true;progress=0f;status="Preparing map pack…"
    manager.createOfflineRegion(definition,"PH-ResQ region ${System.currentTimeMillis()}".toByteArray(),object:OfflineManager.CreateOfflineRegionCallback{
     override fun onCreate(region:OfflineRegion){
      var cleanupStarted=false
      fun discardIncomplete(reason:String){
       if(cleanupStarted)return
       cleanupStarted=true
       region.setDownloadState(OfflineRegion.STATE_INACTIVE)
       region.delete(object:OfflineRegion.OfflineRegionDeleteCallback{
        override fun onDelete(){downloading=false;status=reason;refresh()}
        override fun onError(error:String){downloading=false;status="$reason Cleanup failed: $error";refresh()}
       })
      }
      region.setObserver(object:OfflineRegion.OfflineRegionObserver{
       override fun onStatusChanged(s:OfflineRegionStatus){
        val bytes=s.completedResourceSize
        if(bytes>MAX_BYTES || s.completedTileCount>MAX_TILES_PER_REGION){
         discardIncomplete("Stopped at the 40-MB safety / 3,000-tile per-region cap. The incomplete pack was removed; zoom closer and retry.")
         return
        }
        progress=if(s.requiredResourceCount>0)(s.completedResourceCount.toFloat()/s.requiredResourceCount).coerceIn(0f,1f)else 0f
        if(s.isComplete){region.setDownloadState(OfflineRegion.STATE_INACTIVE);downloading=false;status="Saved offline • ${s.completedTileCount} tiles • ${bytes/1048576} MB";refresh()}
        else status="Downloading ${s.completedTileCount} tiles • ${bytes/1048576} MB"
       }
       override fun onError(error:OfflineRegionError){
        discardIncomplete("Offline map download failed: ${error.message}. The partial region was removed; retry when connectivity is stable.")
       }
       override fun mapboxTileCountLimitExceeded(limit:Long){
        discardIncomplete("Stopped at the device limit of $limit tiles. The incomplete pack was removed; delete an old region or zoom closer.")
       }
      })
      region.setDownloadState(OfflineRegion.STATE_ACTIVE)
     }
     override fun onError(error:String){downloading=false;status="Map pack failed: $error"}
    })
   }
  },modifier=Modifier.fillMaxWidth()){Text(if(downloading)"DOWNLOADING…" else "SAVE VISIBLE AREA OFFLINE")}
  if(downloading)LinearProgressIndicator(progress=progress,modifier=Modifier.fillMaxWidth())
  Text(status,style=MaterialTheme.typography.bodySmall);Text("Saved map regions: $saved")
  regionItems.forEach { region ->
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
    Text("Offline map #${region.id}",modifier=Modifier.weight(1f))
    Button(onClick={
     region.setDownloadState(OfflineRegion.STATE_INACTIVE)
     region.delete(object:OfflineRegion.OfflineRegionDeleteCallback{
      override fun onDelete(){status="Offline map #${region.id} deleted.";refresh()}
      override fun onError(error:String){status="Could not delete map: $error"}
     })
    }){Text("DELETE")}
   }
  }
  Text("© OpenFreeMap · © OpenMapTiles · © OpenStreetMap contributors",style=MaterialTheme.typography.labelSmall)
  Text("Per download: maximum 3,000 tiles or 40 MB; maximum three parallel requests. This is regional coverage, not a preloaded nationwide basemap.")
 }
}
