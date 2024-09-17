package com.farrrmf.yoursleep;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

// Kelas DatabaseHelperLogin meng-extend SQLiteOpenHelper untuk menangani database
public class DatabaseHelperLogin extends SQLiteOpenHelper {
    // Mendefinisikan nama dan versi database
    private static final String DATABASE_NAME = "DB_LOGIN";
    private static final int DATABASE_VERSION = 1;

    // Konstruktor untuk DatabaseHelperLogin, menerima FragmentActivity sebagai konteks
    public DatabaseHelperLogin(FragmentActivity context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Metode onCreate akan dipanggil saat database pertama kali dibuat
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Membuat tabel session dengan kolom id dan login
        sqLiteDatabase.execSQL("CREATE TABLE session (id integer PRIMARY KEY, login text)");
        // Membuat tabel user dengan kolom id, username, dan password
        sqLiteDatabase.execSQL("CREATE TABLE user (id integer PRIMARY KEY AUTOINCREMENT, username text, password text)");
        // Menambahkan satu record ke tabel session dengan nilai default 'kosong'
        sqLiteDatabase.execSQL("INSERT INTO session(id, login) VALUES (1, 'kosong')");
    }

    // Metode onUpgrade akan dipanggil saat database di-upgrade
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Menghapus tabel session jika ada
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS session");
        // Menghapus tabel user jika ada
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS user");
        // Membuat ulang tabel dengan memanggil onCreate
        onCreate(sqLiteDatabase);
    }

    // Metode untuk memeriksa sesi
    public boolean checkSession(String value) {
        // Mendapatkan database yang bisa dibaca
        SQLiteDatabase db = this.getReadableDatabase();
        // Menjalankan query untuk memeriksa apakah ada baris di tabel session dengan nilai login yang diberikan
        try (Cursor cursor = db.rawQuery("SELECT * FROM session WHERE login = ?", new String[]{value})) {
            // Mengembalikan true jika ada satu atau lebih baris yang ditemukan, selain itu false
            return cursor.getCount() > 0;
        }
    }

    // Metode untuk meng-upgrade sesi
    public boolean upgradeSession(String value, int id) {
        // Mendapatkan database yang bisa ditulis
        SQLiteDatabase db = this.getWritableDatabase();
        // Membuat objek ContentValues untuk menyimpan nilai baru dari kolom login
        ContentValues values = new ContentValues();
        values.put("login", value);
        // Meng-upgrade nilai login di tabel session untuk id yang diberikan
        int update = db.update("session", values, "id = ?", new String[]{String.valueOf(id)});
        // Mengembalikan true jika update berhasil, selain itu false
        return update != -1;
    }

    // Metode untuk menyimpan pengguna baru
    public boolean simpanUser(String username, String password) {
        // Mendapatkan database yang bisa ditulis
        SQLiteDatabase db = this.getWritableDatabase();
        // Membuat objek ContentValues untuk menyimpan nilai username dan password
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        // Menyimpan nilai baru ke tabel user
        long insert = db.insert("user", null, values);
        // Mengembalikan true jika insert berhasil, selain itu false
        return insert != -1;
    }

    // Metode untuk memeriksa login
    public boolean checkLogin(String username, String password) {
        // Mendapatkan database yang bisa dibaca
        SQLiteDatabase db = this.getReadableDatabase();
        // Menjalankan query untuk memeriksa apakah ada baris di tabel user dengan username dan password yang diberikan
        try (Cursor cursor = db.rawQuery("SELECT * FROM user WHERE username = ? AND password = ?", new String[]{username, password})) {
            // Mengembalikan true jika ada satu atau lebih baris yang ditemukan, selain itu false
            return cursor.getCount() > 0;
        }
    }
}
