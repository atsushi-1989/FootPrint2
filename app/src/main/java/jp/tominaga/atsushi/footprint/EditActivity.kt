package jp.tominaga.atsushi.footprint

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.gms.location.LocationServices
import io.realm.Realm
import io.realm.kotlin.createObject

import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.content_edit.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class EditActivity : AppCompatActivity() {

    lateinit var mode: ModeInEdit

    val PERMISSION = arrayOf(Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION)

    var isCameraEnabled =false              //カメラの使用が許可されているか
    var isWriteStorageEnabled = false       //外部ストレージへのアクセスが許可されているか
    var isLocationAccessEnabled = false     //位置情報の取得が許可されているか

    var contentUri : Uri? = null

    var selectedPhotoInfo = PhotoInfoModel()

    var isGetLocation : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        setSupportActionBar(toolbar)

        toolbar.apply {
            setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            setNavigationOnClickListener {
                finish()
            }
        }

        mode = intent.extras!!.getSerializable(IntentKey.EDIT_MODE.name) as ModeInEdit

        if(mode == ModeInEdit.SHOOT){
            if (Build.VERSION.SDK_INT >= 23 ) permissionCheck() else launchCamera()
        }else {
            //ModeInEditがEditの場合
            //Todo 編集モードでやること
        }

        btnGoMap.setOnClickListener {
            if (mode == ModeInEdit.SHOOT && !isGetLocation){
                Toast.makeText(this@EditActivity, getString(R.string.location_not_set),Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            displayMap(selectedPhotoInfo.latitude, selectedPhotoInfo.longitude)
        }

        btnDone.setOnClickListener {
            writePhotoInfoToRealm()
        }

    }

    private fun writePhotoInfoToRealm() {

        val realm = Realm.getDefaultInstance()      //Realmの今の情報をとってくる
        realm.beginTransaction()
        var photoInfoRecord = PhotoInfoModel()

        when(mode){
            ModeInEdit.SHOOT -> {
                photoInfoRecord = realm.createObject(PhotoInfoModel::class.java)
            }
            ModeInEdit.EDIT -> {

            }
        }
        photoInfoRecord.apply {
            stringContentUrl = selectedPhotoInfo.stringContentUrl
            dateTime = selectedPhotoInfo.dateTime
            latitude = selectedPhotoInfo.latitude
            longitude = selectedPhotoInfo.longitude
            location = latitude.toString() + longitude.toString()
            comment = inputComment.text.toString()
        }
        realm.commitTransaction()

        inputComment.setText("")        //登録後はコメント削除
        Toast.makeText(this@EditActivity,getString(R.string.photo_info_written),Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun displayMap(latitude: Double, longitude: Double) {

        val geoString = "geo:" + latitude + "," + longitude + "?z=" + ZOOM_LEVEL_DETAIL
        val gmmIntentUri = Uri.parse(geoString)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)

    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)

        outState?.putParcelable(IntentKey.CONTENT_URI.name,contentUri)
    }

    private fun launchCamera() {

        //共有するファイルのパスを設定(Fileオブジェクトの作成)
        val contentFileName = SimpleDateFormat("yyyyMMdd_HHmmss_z").format(Date())
        contentUri = getgenerateContentUriFromFileName(contentFileName)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
        }

        //APIレベル21未満の場合に必要な措置
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            val context = applicationContext
            val resolvedIntentActivities = context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolvedIntentInfo in resolvedIntentActivities){
                val packageName = resolvedIntentInfo.activityInfo.packageName
                context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }
        startActivityForResult(intent, RQ_CODE_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode != Activity.RESULT_OK){
            Toast.makeText(this@EditActivity,getString(R.string.shoot_failed),Toast.LENGTH_SHORT).show()
            return
        }

        if (requestCode != RQ_CODE_CAMERA){
            Toast.makeText(this@EditActivity,getString(R.string.shoot_failed), Toast.LENGTH_SHORT).show()
            return
        }

        if (contentUri == null){
            Toast.makeText(this@EditActivity,getString(R.string.shoot_failed),Toast.LENGTH_SHORT).show()
            return
        }

        imageView.setImage(ImageSource.uri(contentUri!!))


        selectedPhotoInfo.stringContentUrl = contentUri.toString()
        selectedPhotoInfo.dateTime = SimpleDateFormat("yyyyMMdd_HHmmss_z").format(Date())

        getLocation()


        //APIレベル21以下の場合に必要になる措置
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            applicationContext.revokeUriPermission(contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

    }

    private fun getLocation() {

        val client = LocationServices.getFusedLocationProviderClient(this)


        try {
            client.lastLocation.addOnSuccessListener {
                selectedPhotoInfo.latitude = it.latitude
                selectedPhotoInfo.longitude = it.longitude

                Toast.makeText(this@EditActivity,getString(R.string.location_get) + selectedPhotoInfo.latitude.toString() +
                        " : " + selectedPhotoInfo.longitude.toString(),Toast.LENGTH_LONG).show()

                isGetLocation = true
            }
        }catch (e:SecurityException){

        }

    }

    private fun getgenerateContentUriFromFileName(contentFileName: String): Uri? {
        //Uriを生成するメソッド
        val contentFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        contentFolder!!.mkdirs()
        val contentFilePath = contentFolder.path + "/" + contentFileName + ".jpg"

        val contentFile = File(contentFilePath)
        return FileProvider.getUriForFile(this@EditActivity,applicationContext.packageName + ".fileprovider",
        contentFile)


    }

    private fun permissionCheck() {
        val permissionCheckCamera: Int = ContextCompat.checkSelfPermission(this@EditActivity,PERMISSION[0])
        val permissionCheckWriteStorage : Int = ContextCompat.checkSelfPermission(this@EditActivity,PERMISSION[1])
        val permissionCheckLocationAccess: Int = ContextCompat.checkSelfPermission(this@EditActivity,PERMISSION[2])

        if (permissionCheckCamera == PackageManager.PERMISSION_GRANTED) isCameraEnabled = true
        if (permissionCheckWriteStorage == PackageManager.PERMISSION_GRANTED) isWriteStorageEnabled = true
        if (permissionCheckLocationAccess == PackageManager.PERMISSION_GRANTED) isLocationAccessEnabled = true

        if (isCameraEnabled && isWriteStorageEnabled  && isLocationAccessEnabled) launchCamera() else permissionRequest()

    }

    private fun permissionRequest() {
        //ユーザーに説明が必要あるか判定
        val isNeedExplainForCameraPermisson = ActivityCompat
            .shouldShowRequestPermissionRationale(this@EditActivity,PERMISSION[0])

        val isNeedExplainForWriteStoragePermission = ActivityCompat
            .shouldShowRequestPermissionRationale(this@EditActivity,PERMISSION[1])

        val isNeedExplainForLocationAccess = ActivityCompat
            .shouldShowRequestPermissionRationale(this@EditActivity,PERMISSION[2])


        val isNeedExplainForPermission =
            if (isNeedExplainForCameraPermisson || isNeedExplainForWriteStoragePermission || isNeedExplainForLocationAccess){
            true
            }else false

        //許可をリクエストするパーミッションを入れるリストの設定
        val requestPermissionList = ArrayList<String>()

        //許可されていないパーミッションをリクエストのリストに入れる
        if (!isCameraEnabled) requestPermissionList.add(PERMISSION[0])
        if (!isWriteStorageEnabled) requestPermissionList.add(PERMISSION[1])
        if (!isLocationAccessEnabled) requestPermissionList.add(PERMISSION[2])

        //説明が不要な場合
        //パーミッションの許可を求める
        if (!isNeedExplainForPermission){
            ActivityCompat.requestPermissions(this@EditActivity,
                requestPermissionList.toArray(arrayOfNulls(requestPermissionList.size)),
            RQ_CODE_PERMISSION)

            return
        }

        //説明が必要な場合
        val daialog = AlertDialog.Builder(this@EditActivity).apply{
            setTitle(getString(R.string.permission_request_title))
            setMessage(getString(R.string.permission_request_message))
            setPositiveButton(getString(R.string.admit)){dialog, which->
                ActivityCompat.requestPermissions(
                    this@EditActivity,
                    requestPermissionList.toArray(arrayOfNulls(requestPermissionList.size)),
                    RQ_CODE_PERMISSION)
            }
            setNegativeButton(getString(R.string.reject)){ dialog, which->
                Toast.makeText(this@EditActivity,getString(R.string.cannot_go_any_further),Toast.LENGTH_SHORT).show()
                finish()
            }
        }.show()


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.apply {
            findItem(R.id.action_settings).isVisible = true     //セッティング
            findItem(R.id.action_share).isVisible = false       //シェア
            findItem(R.id.action_comment).isVisible = false     //テロップ
            findItem(R.id.action_delete).isVisible = true      //削除
            findItem(R.id.action_edit).isVisible = false        //編集
            findItem(R.id.action_camera).isVisible = if (mode == ModeInEdit.SHOOT) true else false   //カメラ
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item!!.itemId){
            R.id.action_delete ->{
                when(mode){
                    ModeInEdit.SHOOT ->{
                        contentResolver.delete(Uri.parse(selectedPhotoInfo.stringContentUrl),null,null)
                        Toast.makeText(this@EditActivity, getString(R.string.photo_info_deleted),Toast.LENGTH_SHORT).show()
                        finish()
                        return true
                    }

                    ModeInEdit.EDIT ->{

                    }

                }
            }

            R.id.action_camera ->{
                inputComment.setText("")
                if (Build.VERSION.SDK_INT >= 23 ) permissionCheck() else launchCamera()


            }

            else -> super.onOptionsItemSelected(item)

        }
        return true
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        if (requestCode != RQ_CODE_PERMISSION){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if(grantResults.size <= 0) return

        for(i in 0..permissions.size-1) {
            when(permissions[i]){
                PERMISSION[0] -> {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this@EditActivity, getString(R.string.cannot_go_any_further),Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }
                    isCameraEnabled = true
                }

                PERMISSION[1] ->{
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this@EditActivity,getString(R.string.cannot_go_any_further),Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }
                    isWriteStorageEnabled = true
                }

                PERMISSION[2] ->{
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this@EditActivity,getString(R.string.cannot_go_any_further),Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }
                    isLocationAccessEnabled = true
                }
            }
        }
        if (isCameraEnabled && isWriteStorageEnabled  && isLocationAccessEnabled) launchCamera() else finish()
    }

}
