package com.example.library_mobile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "library.db";
    private static final int DATABASE_VERSION = 1;

    public BookDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists books (id integer primary key autoincrement, title text, author text, description text, cover blob)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists books");
    }

    public Book getBookById(int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM books WHERE id=?", new String[]{String.valueOf(bookId)});

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String author = cursor.getString(cursor.getColumnIndexOrThrow("author"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            byte[] coverImage = cursor.getBlob(cursor.getColumnIndexOrThrow("cover"));

            cursor.close();

            return new Book(bookId, title, author, description, coverImage); // Предположим, у вас есть класс Book с соответствующими полями
        }

        return null; // Если книга не найдена
    }
}
