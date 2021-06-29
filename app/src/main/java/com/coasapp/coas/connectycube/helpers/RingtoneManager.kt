package com.connectycube.messenger.helpers

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RawRes
import timber.log.Timber
import java.io.IOException
import java.io.Serializable

class RingtoneManager(val context: Context) :Serializable{

    private var uri: Uri? = null

    private val vibratePattern = longArrayOf(0, 1000, 500)

    private var vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private var player: MediaPlayer? = null

    constructor(context: Context, @RawRes resource: Int) : this(context) {
        uri = Uri.parse("android.resource://" + context.packageName + "/" + resource)
    }

    fun start(looping: Boolean = true, vibrate: Boolean = false) {
        player?.release()
        if (uri == null) {
            uri = notificationUri()
            uri?.let { player = createMediaPlayer(AudioManager.STREAM_RING) }
        } else {
            uri?.let { player = createMediaPlayer(AudioManager.STREAM_VOICE_CALL) }
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        player?.isLooping = looping

        val ringerMode = audioManager.ringerMode

        if (isNeedVibrate(context, player, ringerMode, vibrate)) {
            Timber.d("vibrator vibrate")
            vibrator.vibrate(vibratePattern, 1)
        }

        player?.let {
            try {
                if (!it.isPlaying) {
                    it.prepare()
                    it.start()
                    Timber.d("play ringtone")
                } else {
                    Timber.d("Ringtone is playing already")
                }
            } catch (e: IllegalStateException) {
                Timber.d("e= $e")
                player = null
            }

        }
    }

    fun startOut(looping: Boolean = true, vibrate: Boolean = false) {
        player?.release()
        if (uri == null) {
            uri = notificationUri()
            uri?.let { player = createMediaPlayer(AudioManager.STREAM_RING) }
        } else {
            uri?.let { player = createMediaPlayer(AudioManager.STREAM_VOICE_CALL) }
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
        player?.isLooping = looping

        val ringerMode = audioManager.ringerMode

       /* if (isNeedVibrate(context, player, ringerMode, vibrate)) {
            Timber.d("vibrator vibrate")
            vibrator.vibrate(vibratePattern, 1)
        }*/

        player?.let {
            try {
                if (!it.isPlaying) {
                    it.prepare()
                    it.start()
                    Timber.d("play ringtone")
                } else {
                    Timber.d("Ringtone is playing already")
                }
            } catch (e: IllegalStateException) {
                Timber.d("e= $e")
                player = null
            }

        }
    }

    fun stop() {
        if (player != null) {
            Timber.d("stop ringer")
            player?.release()
            player = null
        }
        vibrator.cancel()
    }

    private fun createMediaPlayer(streamType: Int): MediaPlayer? {
        return try {
            val mediaPlayer = MediaPlayer()

            mediaPlayer.setOnErrorListener { _, _, _ ->
                player = null
                false
            }
            mediaPlayer.setDataSource(context, uri!!)

            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val volume = audioManager.getStreamVolume(AudioManager.STREAM_RING)

            mediaPlayer.setAudioStreamType(streamType)
            if(streamType.equals(AudioManager.STREAM_RING)){
                mediaPlayer.setVolume(volume.toFloat(), volume.toFloat())
            }
            mediaPlayer
        } catch (e: IOException) {
            Timber.e(e)
            null
        }
    }

    private fun isNeedVibrate(context: Context,
                              player: MediaPlayer?,
                              ringerMode: Int,
                              vibrate: Boolean
    ): Boolean {
        return if (player == null) {
            true
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            if (!vibrator.hasVibrator()) {
                return false
            }
            return if (vibrate) {
                Log.i("VibNeed1",""+( ringerMode != AudioManager.RINGER_MODE_SILENT))

                ringerMode != AudioManager.RINGER_MODE_SILENT
            } else {
                Log.i("VibNeed2",""+( ringerMode == AudioManager.RINGER_MODE_VIBRATE))
                ringerMode == AudioManager.RINGER_MODE_VIBRATE
            }
        }
    }

    private fun notificationUri(): Uri? {
        var uri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        if (uri == null) {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
        }
        return uri
    }
}