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
private const val STYLE_URL="https://tiles.openfreemap.org/styles/liberty"
private const val MAX_BYTES=40L*1024L*1024L
private const val MAX_TILES=3000L
@Composable fun OfflineMapPanel(modifier:Modifier=Modifier){
 val context=LocalContext.current;val owner=LocalLifecycleOwner.current
 var mapView by remember{mutableStateOf<MapView?>(null)};var ready by remember{mutableStateOf(false)}
 var downloading by remember{mutableStateOf(false)};var progress by remember{mutableFloatStateOf(0f)}
 var saved by remember{mutableIntStateOf(0)};var regionItems by remember{mutableStateOf(emptyList<OfflineRegion>())};var status by remember{mutableStateOf("Pan and zoom to the area you want to save.")}
 val manager=remember(context){
  val dispatcher=Dispatcher().apply{maxRequests=4;maxRequestsPerHost=4}
  val client=OkHttpClient.Builder().dispatcher(dispatcher).connectTimeout(20,TimeUnit.SECONDS).readTimeout(30,TimeUnit.SECONDS)
   .addInterceptor{chain->chain.proceed(chain.request().newBuilder().header("User-Agent","PH-ResQ-Android/0.1 (offline-map-client)").build())}.build()
  HttpRequestUtil.setOkHttpClient(client);MapLibre.getInstance(context)
  OfflineManager.getInstance(context).also{it.setOfflineMapboxTileCountLimit(MAX_TILES)}
 }
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
   getMapAsync{map->map.setStyle(STYLE_URL){map.cameraPosition=CameraPosition.Builder().target(LatLng(14.5995,120.9842)).zoom(10.5).build();ready=true;refresh()}}
  }})
  Button(enabled=ready&&!downloading,onClick={
   val view=mapView;if(view==null){status="Map is not ready.";return@Button}
   view.getMapAsync{map->
    val bounds=map.projection.visibleRegion.latLngBounds
    val definition=OfflineTilePyramidRegionDefinition(STYLE_URL,bounds,9.0,14.0,view.pixelRatio)
    downloading=true;progress=0f;status="Preparing map pack…"
    manager.createOfflineRegion(definition,"PH-ResQ region ${System.currentTimeMillis()}".toByteArray(),object:OfflineManager.CreateOfflineRegionCallback{
     override fun onCreate(region:OfflineRegion){
      region.setObserver(object:OfflineRegion.OfflineRegionObserver{
       override fun onStatusChanged(s:OfflineRegionStatus){
        val bytes=s.completedResourceSize
        if(bytes>MAX_BYTES){region.setDownloadState(OfflineRegion.STATE_INACTIVE);downloading=false;status="Stopped at 50-MB provider limit. Zoom closer and retry.";refresh();return}
        progress=if(s.requiredResourceCount>0)(s.completedResourceCount.toFloat()/s.requiredResourceCount).coerceIn(0f,1f)else 0f
        if(s.isComplete){region.setDownloadState(OfflineRegion.STATE_INACTIVE);downloading=false;status="Saved offline • ${s.completedTileCount} tiles • ${bytes/1048576} MB";refresh()}
        else status="Downloading ${s.completedTileCount} tiles • ${bytes/1048576} MB"
       }
       override fun onError(error:OfflineRegionError){status="Temporary download error: ${error.message}. MapLibre may retry after connectivity returns."}
       override fun mapboxTileCountLimitExceeded(limit:Long){region.setDownloadState(OfflineRegion.STATE_INACTIVE);downloading=false;status="Stopped at $limit tiles. Zoom closer and retry.";refresh()}
      })
      region.setDownloadState(OfflineRegion.STATE_ACTIVE)
     }
     override fun onError(error:String){downloading=false;status="Map pack failed: $error"}
    })
   }
  },modifier=Modifier.fillMaxWidth()){Text(if(downloading)"DOWNLOADING…" else "SAVE VISIBLE AREA OFFLINE")}
  if(downloading)LinearProgressIndicator(progress=progress,modifier=Modifier.fillMaxWidth())
  Text(status,style=MaterialTheme.typography.bodySmall);Text("Saved map regions: $saved")
  Text("© OpenFreeMap · © OpenMapTiles · © OpenStreetMap contributors",style=MaterialTheme.typography.labelSmall)
  Text("Per download: maximum 3,000 tiles or 50 MB; maximum four parallel requests. This is regional coverage, not a preloaded nationwide basemap.")
 }
}
