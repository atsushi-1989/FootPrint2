package jp.tominaga.atsushi.footprint

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.realm.Realm
import io.realm.RealmResults

import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mapFragment: SupportMapFragment


    lateinit var realm: Realm
    lateinit var results : RealmResults<PhotoInfoModel>
    lateinit var locationList: ArrayList<PhotoInfoModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)



    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.apply {
            findItem(R.id.action_settings).isVisible = true     //セッティング
            findItem(R.id.action_share).isVisible = false       //シェア
            findItem(R.id.action_comment).isVisible = false     //テロップ
            findItem(R.id.action_delete).isVisible = false      //削除
            findItem(R.id.action_edit).isVisible = false        //編集
            findItem(R.id.action_camera).isVisible = true       //カメラ
        }
        return true
    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()
        results = realm.where(PhotoInfoModel::class.java).findAll().sort(PhotoInfoModel::location.name)

        mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.container_map,mapFragment).commit()
        mapFragment.getMapAsync(this)       //onMapReadyメソッドへ

    }

    override fun onMapReady(map: GoogleMap?) {

        map?.uiSettings?.isZoomControlsEnabled = true

        if(results.size > 0){
            setUpLocationMarkers(map)
        }

    }

    private fun setUpLocationMarkers(map: GoogleMap?) {

        locationList = ArrayList<PhotoInfoModel>()
        locationList.add(results[0]!!)
        for( i in 1 until results.size -1){
            if(results[i]?.location != results[i-1]?. location){
                locationList.add(results[i]!!)
            }
        }

        val lastIndexOfLocationList = locationList.size - 1

        locationList.forEach{
            map?.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
        }

        val cameraPosition = CameraPosition.builder()
            .target(LatLng(locationList[lastIndexOfLocationList].latitude,
            locationList[lastIndexOfLocationList].longitude))
            .zoom(ZOOM_LEVEL_MASTER.toFloat()).build()

        map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_camera -> {

                val intent = Intent(this@MainActivity,EditActivity::class.java).apply{
                    putExtra(IntentKey.EDIT_MODE.name, ModeInEdit.SHOOT)
                }

                startActivity(intent)

            }
            else -> super.onOptionsItemSelected(item)
        }

        return true
    }



}
