package com.example.sheikhaudio

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    companion object {
        const val PLAY = "com.example.sheikhaudio.PLAY"
    }

    private var player: ExoPlayer? = null
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this).build()

        val openAppIntent = Intent(this, MainActivity::class.java)
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        session = MediaSession.Builder(this, player!!)
            .setSessionActivity(sessionActivity)
            .build()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        if (intent?.action == PLAY) {
            val title = intent.getStringExtra("title") ?: "محاضرة"
            val series = intent.getStringExtra("series")
                ?: "الشيخ د. محمد الأمين إسماعيل"
            val url = intent.getStringExtra("url")
                ?: return START_NOT_STICKY

            val artwork = BitmapFactory.decodeResource(resources, R.drawable.sheikh)
            val output = ByteArrayOutputStream()
            artwork.compress(Bitmap.CompressFormat.JPEG, 85, output)

            val mediaItem = MediaItem.Builder()
                .setUri(url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(series)
                        .setAlbumTitle("الشيخ د. محمد الأمين إسماعيل")
                        .setArtworkData(
                            output.toByteArray(),
                            MediaMetadata.PICTURE_TYPE_FRONT_COVER
                        )
                        .build()
                )
                .build()

            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()
        }

        return START_STICKY
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (player?.isPlaying != true) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        session?.release()
        player?.release()
        session = null
        player = null
        super.onDestroy()
    }
}
