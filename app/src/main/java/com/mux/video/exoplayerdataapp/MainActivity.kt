package com.mux.video.exoplayerdataapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.KEEP_SCREEN_ON
import android.widget.Toast
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView.SHOW_BUFFERING_WHEN_PLAYING
import com.mux.stats.sdk.core.model.CustomerData
import com.mux.stats.sdk.core.model.CustomerPlayerData
import com.mux.stats.sdk.core.model.CustomerVideoData
import com.mux.stats.sdk.core.model.CustomerViewData
import com.mux.stats.sdk.muxstats.MuxStatsExoPlayer
import com.mux.stats.sdk.muxstats.monitorWithMuxData
import com.mux.video.exoplayerdataapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var viewBinding: ActivityMainBinding
  private var muxStats: MuxStatsExoPlayer? = null
  private var player: ExoPlayer? = null;

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    viewBinding.playerView.apply {
      setShowBuffering(SHOW_BUFFERING_WHEN_PLAYING)
    }
    window.addFlags(KEEP_SCREEN_ON)
  }

  override fun onStart() {
    super.onStart()
    startPlaying(Constants.VOD_TEST_URL_BIG_BUCK_BUNNY)
  }

  private fun startPlaying(mediaUrl: String) {
    stopPlaying()

    player = createPlayer().also { newPlayer ->
      muxStats = monitorPlayer(newPlayer)
      viewBinding.playerView.player = newPlayer
      newPlayer.setMediaItem(createMediaItem(mediaUrl))
      newPlayer.prepare()
      newPlayer.playWhenReady = true
    }
  }
  override fun onStop() {

    stopPlaying()
    super.onStop()
  }

  private fun createMediaItem(mediaUrl: String): MediaItem {
    return mediaUrl.toMediaItem()
  }

  private fun stopPlaying() {
    player?.let { oldPlayer ->
      oldPlayer.stop()
      oldPlayer.release()
    }
    // Make sure to release() your muxStats whenever the user is done with the player
    muxStats?.release()
  }

  private fun monitorPlayer(player: ExoPlayer): MuxStatsExoPlayer {
    // You can add your own data to a View, which will override any data we collect
    val customerData = CustomerData(
      CustomerPlayerData().apply { },
      CustomerVideoData().apply {
        videoId = "A Custom ID"
        videoTitle = "Mux ExoPlayer minimal integration - 2.16.1"
      },
      CustomerViewData().apply { }
    )

    return player.monitorWithMuxData(
      context = this,
      envKey = Constants.MUX_DATA_ENV_KEY,
      customerData = customerData,
      playerView = viewBinding.playerView
    )
  }

  private fun createPlayer(): ExoPlayer {
    return ExoPlayer.Builder(this)
      .build().apply {
        addListener(object : Player.Listener {
          override fun onPlayerError(error: PlaybackException) {
            Log.e(javaClass.simpleName, "player error!", error)
            Toast.makeText(this@MainActivity, error.localizedMessage, Toast.LENGTH_SHORT)
              .show()
          }
        })
      }
  }

}