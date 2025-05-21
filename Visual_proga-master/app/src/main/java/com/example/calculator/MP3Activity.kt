package com.example.calculator

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.provider.Settings
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MP3Activity : AppCompatActivity() {
    private lateinit var music: MediaPlayer
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var songTitle: TextView
    private lateinit var seekbar: SeekBar
    private lateinit var selectButton: Button
    private lateinit var currentTimeText: TextView
    private lateinit var totalTimeText: TextView
    private lateinit var songsListView: ListView

    private val handler = Handler(Looper.getMainLooper())
    private val songsList = mutableListOf<File>()
    private var currentSongIndex = 0
    private val REQUEST_PERMISSION_CODE = 123
    private var selectedDirectory: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)

    private val directoryPickerResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedDirectory = getDirectoryFromTreeUri(uri) ?: return@let
                scanDirectory()
            }
        }
    }

    private fun getDirectoryFromTreeUri(uri: Uri): File? {
        if (uri.scheme != "content") return null

        val docId = DocumentsContract.getTreeDocumentId(uri)
        val split = docId.split(":")
        val type = split[0]

        return when (type) {
            "primary" -> File(Environment.getExternalStorageDirectory(), split[1])
            else -> File("/storage/$type/${split[1]}")
        }.takeIf { it.exists() }
    }

    private val updateSeekBar = object : Runnable {
        override fun run() {
            if (::music.isInitialized && music.isPlaying) {
                seekbar.progress = music.currentPosition
                currentTimeText.text = formatTime(music.currentPosition)
                handler.postDelayed(this, 200)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mp3)
        initViews()
        setupMediaPlayer()
        setupButtons()

        if (checkPermission()) {
            scanDirectory()
        } else {
            requestPermission()
        }
    }

    private fun initViews() {
        playButton = findViewById(R.id.play_button)
        pauseButton = findViewById(R.id.pause_button)
        stopButton = findViewById(R.id.stop_button)
        seekbar = findViewById(R.id.seek_bar)
        selectButton = findViewById(R.id.select_button)
        songTitle = findViewById(R.id.song_title)
        currentTimeText = findViewById(R.id.current_time)
        totalTimeText = findViewById(R.id.total_time)
        songsListView = findViewById(R.id.songs_list_view)
    }

    private fun setupMediaPlayer() {
        music = MediaPlayer().apply {
            setOnPreparedListener {
                seekbar.max = duration
                totalTimeText.text = formatTime(duration)
                start()
                startSeekbarUpdate()
                updateUI()
            }
            setOnCompletionListener {
                playNextSong()
            }
        }
    }

    private fun setupButtons() {
        selectButton.setOnClickListener {
            openDirectoryPicker()
        }

        playButton.setOnClickListener {
            if (songsList.isNotEmpty()) {
                if (!music.isPlaying) {
                    if (music.currentPosition > 0) {
                        music.start()
                    } else {
                        playSelectedSong()
                    }
                    startSeekbarUpdate()
                    updateUI()
                }
            }
        }

        pauseButton.setOnClickListener {
            if (music.isPlaying) {
                music.pause()
                updateUI()
            }
        }

        stopButton.setOnClickListener {
            if (::music.isInitialized) {
                music.stop()
                seekbar.progress = 0
                currentTimeText.text = formatTime(0)
                updateUI()
            }
        }

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && music.isPlaying) {
                    music.seekTo(progress)
                    currentTimeText.text = formatTime(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) = handler.removeCallbacks(updateSeekBar)
            override fun onStopTrackingTouch(seekBar: SeekBar) = startSeekbarUpdate()
        })

        songsListView.setOnItemClickListener { _, _, position, _ ->
            currentSongIndex = position
            playSelectedSong()
        }
    }

    private fun playSelectedSong() {
        try {
            music.reset()
            music.setDataSource(songsList[currentSongIndex].absolutePath)
            music.prepareAsync()
            songTitle.text = "Loading: ${songsList[currentSongIndex].nameWithoutExtension}"
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading song", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playNextSong() {
        if (songsList.isNotEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songsList.size
            playSelectedSong()
        }
    }

    private fun startSeekbarUpdate() {
        handler.removeCallbacks(updateSeekBar)
        handler.post(updateSeekBar)
    }

    private fun updateUI() {
        songTitle.text = when {
            !::music.isInitialized -> "No song selected"
            music.isPlaying -> "Playing: ${songsList[currentSongIndex].nameWithoutExtension}"
            else -> "Paused: ${songsList[currentSongIndex].nameWithoutExtension}"
        }
    }

    private fun scanDirectory() {
        songsList.clear()
        selectedDirectory.listFiles()?.forEach { file ->
            if (file.isFile && isAudioFile(file)) {
                songsList.add(file)
            }
        }
        updateSongsList()
        if (songsList.isNotEmpty()) {
            playSelectedSong()
        } else {
            Toast.makeText(this, "No audio files found in directory", Toast.LENGTH_SHORT).show()
            songTitle.text = "No songs found"
        }
    }

    private fun updateSongsList() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            songsList.map { it.nameWithoutExtension }
        )
        songsListView.adapter = adapter
    }

    private fun isAudioFile(file: File): Boolean {
        val name = file.name.lowercase()
        return name.endsWith(".mp3")
    }

    private fun openDirectoryPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            putExtra("android.content.extra.SHOW_ADVANCED", true)
        }
        directoryPickerResult.launch(intent)
    }

    private fun formatTime(millis: Int): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            scanDirectory()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (::music.isInitialized) {
            music.release()
        }
        super.onDestroy()
    }
}