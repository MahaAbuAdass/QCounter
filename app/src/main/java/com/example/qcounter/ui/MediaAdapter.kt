package com.example.qcounter.ui

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.qcounter.databinding.ItemMediaBinding
import com.example.qcounter.dataclass.FileURL

class MediaAdapter(
    private val context: Context,
    private val mediaList: List<FileURL>,
    private val viewPager: ViewPager2, // Pass the ViewPager2
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val file = mediaList[position]

        if (file.fileName != null) {
            if (isVideo(file.fileName!!)) {
                holder.binding.videoView.apply {
                    visibility = View.VISIBLE
                    setVideoURI(Uri.parse(file.fileName))

                    setOnPreparedListener { mediaPlayer ->
                        mediaPlayer.isLooping = false
                        mediaPlayer.start()

                        val videoDuration = mediaPlayer.duration.toLong()
                        MediaAdapter.videoDurations[file.fileName!!] = videoDuration

                        Log.d("VideoDuration", "Video prepared at position: $position, Duration: ${videoDuration} ms")

                        setOnCompletionListener {
                            handler?.post {
                                val nextPosition = (position + 1) % mediaList.size
                                viewPager.setCurrentItem(nextPosition, true)
                            }
                        }
                    }
                }
                holder.binding.imageView.visibility = View.GONE
            } else {
                holder.binding.imageView.apply {
                    visibility = View.VISIBLE
                    Glide.with(context)
                        .load(file.fileName)
                        .into(this)
                }
                holder.binding.videoView.visibility = View.GONE
            }
        }
    }
    override fun getItemCount(): Int = mediaList.size

    private fun isVideo(fileName: String): Boolean {
        val videoExtensions = listOf(".mp4", ".mov", ".avi", ".mkv")
        return videoExtensions.any { fileName.endsWith(it, ignoreCase = true) }
    }
    companion object {
        val videoDurations = mutableMapOf<String, Long>()
    }

}