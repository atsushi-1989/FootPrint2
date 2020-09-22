package jp.tominaga.atsushi.footprint

import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_edit.*

class EditActivity : AppCompatActivity() {

    lateinit var mode: ModeInEdit

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

        mode = intent.extras?.getSerializable(IntentKey.EDIT_MODE.name) as ModeInEdit

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

}
