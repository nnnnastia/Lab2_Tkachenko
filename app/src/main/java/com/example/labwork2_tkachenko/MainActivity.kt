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

    // Оголошення змінних для елементів інтерфейсу та файлу з фото
    private lateinit var imageView: ImageView      // Для відображення селфі
    private lateinit var imageUri: Uri             // URI для доступу до фото
    private lateinit var photoFile: File           // Сам файл із селфі

    private val REQUEST_IMAGE_CAPTURE = 1          // Код запиту для перевірки результату з камери

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ініціалізація елементів інтерфейсу
        imageView = findViewById(R.id.imageView)
        val btnTakeSelfie = findViewById<Button>(R.id.btnTakeSelfie)
        val btnSendSelfie = findViewById<Button>(R.id.btnSendSelfie)

        // Обробка натискання кнопки "Зробити селфі"
        btnTakeSelfie.setOnClickListener {
            dispatchTakePictureIntent()
        }

        // Обробка натискання кнопки "Відіслати селфі"
        btnSendSelfie.setOnClickListener {
            sendEmailWithPhoto()
        }
    }

    // Метод для запуску камери і створення файлу для збереження зображення
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Створення унікального файлу для фото
        photoFile = createImageFile()

        // Отримання URI через FileProvider — необхідно для доступу до файлу іншим додаткам
        imageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",  // Авторитет FileProvider — повинен збігатися з AndroidManifest
            photoFile
        )

        // Передаємо URI у камеру як місце для збереження фото
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        // Запуск активності камери
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    // Метод для створення файлу з унікальним іменем на основі дати і часу
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)  // Папка для зображень
        return File.createTempFile("SELFIE_${timeStamp}_", ".jpg", storageDir)  // Створення тимчасового файлу
    }

    // Обробка результату після зйомки селфі
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Встановлення зробленого фото в ImageView
            imageView.setImageURI(imageUri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // Метод для створення листа з фото та надсиланням його через поштовий додаток
    private fun sendEmailWithPhoto() {
        // Перевірка, чи файл із фото справді існує
        if (!photoFile.exists()) {
            Toast.makeText(this, "Фото не знайдено", Toast.LENGTH_SHORT).show()
            return
        }

        // Створення наміру на надсилання e-mail з фото
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"  // Тип контенту — фото
            putExtra(Intent.EXTRA_EMAIL, arrayOf("10328016@stud.op.edu.ua"))  // Адреса отримувача
            putExtra(Intent.EXTRA_SUBJECT, "ANDROID [Прізвище Ім’я]")         // Тема листа
            putExtra(
                Intent.EXTRA_TEXT,
                "Надсилаю селфі та посилання на репозиторій проєкту:\nhttps://github.com/nnnnastia/Lab2_Tkachenko.git"
            )
            putExtra(Intent.EXTRA_STREAM, imageUri)  // Додаємо фото як вкладення
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Дозвіл на доступ до URI для інших додатків
        }

        // Перевірка, чи є поштовий клієнт, який може надіслати лист
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        } else {
            Toast.makeText(this, "Немає доступного поштового додатку", Toast.LENGTH_SHORT).show()
        }
    }

}
