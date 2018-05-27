package s235040.wozniak.fplayer.Activities

import android.app.Activity
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_equalizer.*
import s235040.wozniak.fplayer.Playback.MusicPlayer
import s235040.wozniak.fplayer.R

class EqualizerActivity : Activity(), SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener {
    val helperMediaPlayer = MediaPlayer()
    val helperEqualizer = Equalizer(0, helperMediaPlayer.audioSessionId)
    var lowerEqualizerBandLevel: Short = -1
    var upperEqualizerBandLevel: Short = -1
    val listOfBandLevels: MutableList<Short> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)
        initializeSeekBarsAndTextViews()
    }

    private fun initializeSeekBarsAndTextViews() {
        val (presetIndex, listOfBandLevels) = MusicPlayer.getEqualizerPresetAndBandLevels()
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
            seekBar.setOnSeekBarChangeListener(this)

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
        if(listOfBandLevels != null){
            this.listOfBandLevels.clear()
            this.listOfBandLevels.addAll(listOfBandLevels)
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        helperEqualizer.usePreset(position.toShort())
        val presetIndex = position.toShort()
        getProgressDataFromSeekbars()
        updateEqualizerData(presetIndex)
    }

    private fun getProgressDataFromSeekbars(){
        val numberOfFrequencyBands = helperEqualizer.numberOfBands
        listOfBandLevels.clear()
        for(i in 0 until numberOfFrequencyBands){
            val equalizerBandIndex = i.toShort()
            val seekbar = findViewById<SeekBar>(i)
            val bandLevel = helperEqualizer.getBandLevel(equalizerBandIndex)
            listOfBandLevels.add(bandLevel)
            seekbar.progress = bandLevel - lowerEqualizerBandLevel
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

    }

    override fun onStartTrackingTouch(p0: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        val equalizerBandIndex = seekBar.id.toShort()
        val newBandLevel = (seekBar.progress + lowerEqualizerBandLevel).toShort()
        helperEqualizer.setBandLevel(equalizerBandIndex, newBandLevel)
        listOfBandLevels[equalizerBandIndex.toInt()] = newBandLevel
        Log.d("LIST", listOfBandLevels.toString())
        updateEqualizerData()
    }

    private fun updateEqualizerData(presetIndex: Short? = null){
        if(presetIndex == null){
            MusicPlayer.updateEqualizer(listOfBandLevels)
        } else {
            MusicPlayer.updateEqualizer(presetIndex, listOfBandLevels)
        }
    }
}
