package com.example.calculator

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException


class MP3Activity : AppCompatActivity() {
    private lateinit var music: MediaPlayer
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var songTitle: TextView
    private lateinit var seekbar: SeekBar
    private lateinit var selectButton: Button
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button

    private var currentSongUri: Uri? = null
    private var currentSongName: String = "No song selected"
    private val handler = Handler(Looper.getMainLooper())
    private val songsList = mutableListOf<Uri>()
    private var currentSongIndex = -1

    private val filePickerResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                songsList.add(uri)
                currentSongIndex = songsList.size - 1
                loadSong(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mp3)
        initializeViews()
        setupMediaPlayer()
        setupButtons()
    }

    private fun initializeViews() {
        playButton = findViewById(R.id.play_button)
        pauseButton = findViewById(R.id.pause_button)
        stopButton = findViewById(R.id.stop_button)
        seekbar = findViewById(R.id.seek_bar)
        selectButton = findViewById(R.id.select_button)
        songTitle = findViewById(R.id.song_title)
        nextButton = findViewById(R.id.next_button)
        previousButton = findViewById(R.id.previous_button)
    }

    private fun setupMediaPlayer() {
        music = MediaPlayer().apply {
            setOnCompletionListener {
                playNextSong()
            }
            setOnPreparedListener {
                seekbar.max = music.duration
            }
        }
    }

    private fun setupButtons() {
        selectButton.setOnClickListener {
            openFilePicker()
        }

        playButton.setOnClickListener {
            if (currentSongUri != null) {
                if (!music.isPlaying) {
                    music.start()
                    startSeekbarUpdate()
                    updateUI()
                }
            } else {
                showToast("Please select a song first")
            }
        }

        pauseButton.setOnClickListener {
            if (music.isPlaying) {
                music.pause()
                updateUI()
            }
        }

        stopButton.setOnClickListener {
            if (music.isPlaying) {
                music.stop()
                setupMediaPlayer()
                updateUI()
            }
        }

        nextButton.setOnClickListener {
            playNextSong()
        }

        previousButton.setOnClickListener {
            playPreviousSong()
        }

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && music.isPlaying) {
                    music.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "audio/mpeg", "audio/wav", "audio/x-wav",
                "audio/aac", "audio/ogg", "audio/mp4"
            ))
        }
        filePickerResult.launch(intent)
    }

    private fun loadSong(uri: Uri) {
        try {
            music.reset()
            currentSongUri = uri
            getSongNameFromUri(uri)
            music.setDataSource(applicationContext, uri)
            music.prepareAsync()
            updateUI()
        } catch (e: IOException) {
            showToast("Error loading song")
            e.printStackTrace()
        }
    }

    private fun getSongNameFromUri(uri: Uri) {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                currentSongName = cursor.getString(
                    cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                )
                updateUI()
            }
        }
    }

    private fun playNextSong() {
        if (songsList.isNotEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songsList.size
            loadSong(songsList[currentSongIndex])
            if (music.isPlaying) {
                music.start()
            }
        }
    }

    private fun playPreviousSong() {
        if (songsList.isNotEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + songsList.size) % songsList.size
            loadSong(songsList[currentSongIndex])
            if (music.isPlaying) {
                music.start()
            }
        }
    }

    private fun startSeekbarUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                if (music.isPlaying) {
                    seekbar.progress = music.currentPosition
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun updateUI() {
        songTitle.text = when {
            currentSongUri == null -> "No song selected"
            music.isPlaying -> "Playing: $currentSongName"
            else -> "Ready: $currentSongName"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        music.release()
        super.onDestroy()
    }
}