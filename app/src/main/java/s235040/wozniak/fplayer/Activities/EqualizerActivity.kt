package s235040.wozniak.fplayer.Activities

import android.app.Activity
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_equalizer.*
import s235040.wozniak.fplayer.Playback.MusicPlayer
import s235040.wozniak.fplayer.R

class EqualizerActivity : Activity(), AdapterView.OnItemSelectedListener {

    val helperMediaPlayer = MediaPlayer()
    val helperEqualizer = Equalizer(0, helperMediaPlayer.audioSessionId)
    var lowerEqualizerBandLevel: Short = -1
    var upperEqualizerBandLevel: Short = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)
        initializeSpinnerOptions()
    }

    private fun initializeSpinnerOptions() {
        val presetIndex = MusicPlayer.getEqualizerPresetIndex()
        val numberOfFrequencyBands = helperEqualizer.numberOfBands

        lowerEqualizerBandLevel = helperEqualizer.bandLevelRange[0]
        upperEqualizerBandLevel = helperEqualizer.bandLevelRange[1]

        val linearLayout = activity_equalizer_linear_layout

        for (i in 0 until numberOfFrequencyBands){
            val equalizerBandIndex = i.toShort()
            val frequencyHeaderTextView = TextView(this)
            frequencyHeaderTextView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
            frequencyHeaderTextView.gravity = Gravity.CENTER_HORIZONTAL
            frequencyHeaderTextView.text = "${helperEqualizer.getCenterFreq(equalizerBandIndex) / 1000} Hz"
            linearLayout.addView(frequencyHeaderTextView)

            val seekBarRowLayout = LinearLayout(this)
            seekBarRowLayout.orientation = LinearLayout.HORIZONTAL


            val lowerEqualizerBandLevelTextView = TextView(this)
            lowerEqualizerBandLevelTextView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lowerEqualizerBandLevelTextView.text = "${lowerEqualizerBandLevel / 100} dB"

            val upperEqualizerBandLevelTextView = TextView(this)
            upperEqualizerBandLevelTextView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
            upperEqualizerBandLevelTextView.text = "${upperEqualizerBandLevel / 100} dB"

            val seekbarLayoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
            seekbarLayoutParams.weight = 1f

            val seekBar = SeekBar(this)
            seekBar.id = i

            seekBar.layoutParams = seekbarLayoutParams
            seekBar.max = (upperEqualizerBandLevel - lowerEqualizerBandLevel)
            seekBar.progress = helperEqualizer.getBandLevel(equalizerBandIndex).toInt()
            seekBar.isEnabled = false

            seekBarRowLayout.addView(lowerEqualizerBandLevelTextView)
            seekBarRowLayout.addView(seekBar)
            seekBarRowLayout.addView(upperEqualizerBandLevelTextView)
            linearLayout.addView(seekBarRowLayout)
        }
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
        helperEqualizer.usePreset(presetIndex)
        updateSeekBars()
        updateEqualizerPreset(presetIndex)
    }

    private fun updateSeekBars() {
        val numberOfFrequencyBands = helperEqualizer.numberOfBands
        for(i in 0 until numberOfFrequencyBands){
            val equalizerBandIndex = i.toShort()
            val seekbar = findViewById<SeekBar>(i)
            val bandLevel = helperEqualizer.getBandLevel(equalizerBandIndex)
            seekbar.progress = bandLevel - lowerEqualizerBandLevel
        }
    }

    private fun updateEqualizerPreset(presetIndex: Short){
        MusicPlayer.updateEqualizer(presetIndex)
    }
}
