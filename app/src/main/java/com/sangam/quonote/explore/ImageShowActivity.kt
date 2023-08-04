package com.sangam.quonote.explore

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.sangam.quonote.R
import java.io.File

class ImageShowActivity : AppCompatActivity() {
    lateinit var imageUrl: String
    lateinit var imageSearch: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_show)
        imageUrl = intent.extras?.getString("ImageUrl").toString()
        imageSearch = intent.extras?.getString("SearchImage").toString()
        val imageView = findViewById<ImageView>(R.id.imagePexelsApi)
        val textView = findViewById<TextView>(R.id.textCloseImage)
        val button = findViewById<Button>(R.id.btnDownloadImage)
        Glide.with(this).load(imageUrl).into(imageView)
        textView.setOnClickListener {
            finish()
        }
        button.setOnClickListener {

            downloadImage(this, imageUrl, imageSearch.toString())

        }
    }

    private fun downloadImage(context: Context, imageUrl: String, fileName: String) {

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val request = DownloadManager.Request(Uri.parse(imageUrl))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Image Download")
        request.setDescription("Downloading Image")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_PICTURES,
            File.separator + fileName + ".jpg"
        )

        downloadManager.enqueue(request)
        Toast.makeText(this, "Image Downloaded", Toast.LENGTH_SHORT).show()

    }

}