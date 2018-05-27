package s235040.wozniak.fplayer.Activities

import android.app.Activity
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_equalizer.*
import s235040.wozniak.fplayer.Playback.MusicPlayer
import s235040.wozniak.fplayer.R

class EqualizerActivity : Activity(), AdapterView.OnItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)
        initializeSpinnerOptions()
    }

    private fun initializeSpinnerOptions() {
        val helperMediaPlayer = MediaPlayer()
        val helperEqualizer = Equalizer(0, helperMediaPlayer.audioSessionId)
        val presetIndex = MusicPlayer.getEqualizerPresetIndex()
        val presetNames = ArrayList<String>()
        val equalizerPresetSpinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, presetNames)
        equalizerPresetSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        for (i in 0 until helperEqualizer.numberOfPresets){
            presetNames.add(helperEqualizer.getPresetName(i.toShort()))
        }

        activity_equalizer_spinner_preset_type.adapter = equalizerPresetSpinnerAdapter
        activity_equalizer_spinner_preset_type.onItemSelectedListener = this
        if(presetIndex != null){
            activity_equalizer_spinner_preset_type.setSelection(presetIndex.toInt())
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val presetIndex = position.toShort()
        updateEqualizerPreset(presetIndex)
    }

    private fun updateEqualizerPreset(presetIndex: Short){
        MusicPlayer.updateEqualizer(presetIndex)
    }
}
