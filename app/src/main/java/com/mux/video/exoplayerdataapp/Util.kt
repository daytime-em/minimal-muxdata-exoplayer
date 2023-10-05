package com.mux.video.exoplayerdataapp

import android.net.Uri
import com.google.android.exoplayer2.MediaItem


fun String.toUri(): Uri = Uri.parse(this)
fun String.toMediaItem() = MediaItem.fromUri(toUri())
