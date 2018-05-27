package s235040.wozniak.fplayer.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*
import s235040.wozniak.fplayer.R

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initializeControls()
    }

    private fun initializeControls() {
        activity_settings_button_equalizer.setOnClickListener{ _ -> showEqualizer() }
    }

    private fun showEqualizer() {
        val intent = Intent(this, EqualizerActivity::class.java)
        startActivity(intent)
    }
}
