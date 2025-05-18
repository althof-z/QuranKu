package com.example.quranku

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.SeekBar

class AudioPlayerHelper(
    private val context: Context,
    private val seekBar: SeekBar,
    private val playButton: ImageButton
) {
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekbarRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    seekBar.progress = it.currentPosition
                    if (seekBar.progress >= seekBar.max) {
                        handler.removeCallbacks(this)
                        seekBar.progress = 0
                        mediaPlayer?.seekTo(0)
                        playButton.setBackgroundResource(R.drawable.ic_play)
                        return
                    }
                    handler.postDelayed(this, 50)
                }
            }
        }
    }

    init {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun togglePlayPause(audioResId: Int) {
        Log.d("AudioPlayerHelper","togglePlayPause: ${mediaPlayer}")
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, audioResId).apply {
                setOnPreparedListener {
                    seekBar.max = it.duration
                    it.start()
                    handler.post(updateSeekbarRunnable)
                }
                setOnCompletionListener {
                    handler.removeCallbacks(updateSeekbarRunnable)
                    seekBar.progress = 0
                    seekTo(0)
                    playButton.setBackgroundResource(R.drawable.ic_play)
                }
            }
            playButton.setBackgroundResource(R.drawable.ic_pause)
        } else if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            playButton.setBackgroundResource(R.drawable.ic_play)
        } else {
            mediaPlayer?.start()
            handler.post(updateSeekbarRunnable)
            playButton.setBackgroundResource(R.drawable.ic_pause)
        }
    }

    fun togglePlayPause(audioPath: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                Log.d("AudioPlayerHelper", "togglePlayPause: $mediaPlayer")
                setDataSource(audioPath)
                setOnPreparedListener {
                    seekBar.max = it.duration
                    it.start()
                    handler.post(updateSeekbarRunnable)
                    playButton.setBackgroundResource(R.drawable.ic_pause)
                }
                prepareAsync()
                setOnCompletionListener {
                    handler.removeCallbacks(updateSeekbarRunnable)
                    seekBar.progress = 0
                    seekTo(0)
                    playButton.setBackgroundResource(R.drawable.ic_play)
                }
            }
        } else if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            playButton.setBackgroundResource(R.drawable.ic_play)
        } else {
            mediaPlayer?.start()
            handler.post(updateSeekbarRunnable)
            playButton.setBackgroundResource(R.drawable.ic_pause)
        }
    }




    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun release() {
        handler.removeCallbacks(updateSeekbarRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
