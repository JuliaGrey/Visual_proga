package com.example.calculator

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class MP3Activity : AppCompatActivity() {
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var seekbar: SeekBar
    private lateinit var music: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mp3)
        playButton = findViewById(R.id.play_button)
        pauseButton = findViewById(R.id.pause_button)
        stopButton = findViewById(R.id.stop_button)
        seekbar = findViewById(R.id.seek_bar)
        music = MediaPlayer.create(this, R.raw.example)
        SeekBarCreate()
        playButton.setOnClickListener {
            if (!music.isPlaying) {
                music.start()
            }
        }
        pauseButton.setOnClickListener {
            if (music.isPlaying) {
                music.pause()
            }
        }
        stopButton.setOnClickListener {
            if (music.isPlaying) {
                music.stop()
                music.reset()
                music = MediaPlayer.create(this, R.raw.example)
            }
        }
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean){
                if (fromUser) music?.seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        if (music.isPlaying) {
            music.stop()
        }
        music.release()
    }
    private fun SeekBarCreate(){
        seekbar.max = music!!.duration
        val handler = Handler()
        handler.postDelayed(object: Runnable {
            override fun run(){
                try {
                    seekbar.progress = music!!.currentPosition
                    handler.postDelayed(this, 1000)
                } catch(e: Exception){
                    seekbar.progress = 0
                }
            }
        }, 0)
    }
}