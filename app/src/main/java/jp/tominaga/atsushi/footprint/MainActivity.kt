package jp.tominaga.atsushi.footprint

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

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
