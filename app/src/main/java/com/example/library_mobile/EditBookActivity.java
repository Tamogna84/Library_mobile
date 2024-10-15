package com.example.library_mobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditBookActivity extends AppCompatActivity {

    private EditText titleEditField, authorEditField, descriptionEditField;
    private ImageView editCoverImageView;
    private Button editLoadCoverBtn, saveEditBookBtn;
    private byte[] editCoverImage;

    private BookDatabaseHelper dbHelper;
    private int bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        titleEditField = findViewById(R.id.title_edit_field);
        authorEditField = findViewById(R.id.author_edit_field);
        descriptionEditField = findViewById(R.id.description_edit_field);
        editCoverImageView = findViewById(R.id.edit_cover_image_view);
        editLoadCoverBtn = findViewById(R.id.edit_cover_load_btn);
        saveEditBookBtn = findViewById(R.id.save_edit_book_btn);

        dbHelper = new BookDatabaseHelper(this);


        // Получаем bookId из Intent
        bookId = getIntent().getIntExtra("book_id", -1);
        if (bookId == -1) {
            // Если bookId не был передан, закрываем активность
            finish();
            return;
        }

        // Загрузите данные книги из базы данных
        Book book = dbHelper.getBookById(bookId);
        if (book != null) {
            // Заполняем поля
            titleEditField.setText(book.getTitle());
            authorEditField.setText(book.getAuthor());
            descriptionEditField.setText(book.getDescription());

            // Устанавливаем обложку книги
            if (book.getCover() != null) {
                Bitmap coverBitmap = BitmapFactory.decodeByteArray(book.getCover(), 0, book.getCover().length);
                editCoverImageView.setImageBitmap(coverBitmap);
            }
        } else {
            // Если книга не найдена, можно завершить активность
            finish();
        }

        editLoadCoverBtn.setOnClickListener(v -> openImageChooser());
        saveEditBookBtn.setOnClickListener(v -> saveBookToDb());
    }

    // Выбор изображения из галереи и получение результата
    private final ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri selectedImageUri = result.getData().getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                editCoverImageView.setImageBitmap(bitmap);
                editCoverImage = getBytesFromBitmap(bitmap);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    });

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectImageLauncher.launch(intent);
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
        return stream.toByteArray();
    }

    private void saveBookToDb() {
        String title = titleEditField.getText().toString();
        String author = authorEditField.getText().toString();
        String description = descriptionEditField.getText().toString();

        if (title.isEmpty() || author.isEmpty() || description.isEmpty()) {
            Toast.makeText(EditBookActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.getWritableDatabase().execSQL("UPDATE books SET title=?, author=?, description=?, cover=? WHERE id=?",
                new Object[]{title, author, description, editCoverImage, bookId});

        setResult(RESULT_OK);
        finish();
    }

}