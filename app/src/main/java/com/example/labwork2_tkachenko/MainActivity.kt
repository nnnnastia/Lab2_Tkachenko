package com.example.labwork2_tkachenko

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var imageUri: Uri
    private lateinit var photoFile: File

    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        val btnTakeSelfie = findViewById<Button>(R.id.btnTakeSelfie)
        val btnSendSelfie = findViewById<Button>(R.id.btnSendSelfie)

        btnTakeSelfie.setOnClickListener {
            dispatchTakePictureIntent()
        }

        btnSendSelfie.setOnClickListener {
            sendEmailWithPhoto()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Створення файлу для фото
        photoFile = createImageFile()
        imageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            photoFile
        )

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("SELFIE_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            imageView.setImageURI(imageUri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun sendEmailWithPhoto() {
        if (!photoFile.exists()) {
            Toast.makeText(this, "Фото не знайдено", Toast.LENGTH_SHORT).show()
            return
        }

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("10328016@stud.op.edu.ua"))
            putExtra(Intent.EXTRA_SUBJECT, "ANDROID [Прізвище Ім’я]")
            putExtra(
                Intent.EXTRA_TEXT,
                "Надсилаю селфі та посилання на репозиторій проєкту:\nhttps://github.com/yourusername/yourproject"
            )
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        } else {
            Toast.makeText(this, "Немає доступного поштового додатку", Toast.LENGTH_SHORT).show()
        }
    }

}
