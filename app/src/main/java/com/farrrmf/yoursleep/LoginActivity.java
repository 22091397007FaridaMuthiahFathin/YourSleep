package com.farrrmf.yoursleep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Mendefinisikan kelas LoginActivity yang merupakan bagian dari aplikasi
public class LoginActivity extends AppCompatActivity {

    // Mendeklarasikan variabel untuk UI components dan database helper
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private DatabaseHelperLogin db;

    // Mendefinisikan SharedPreferences untuk menyimpan data login
    public static final String SHARED_PREF_NAME = "myPref";
    private SharedPreferences sharedPreferences;

    // Method onCreate() dipanggil saat activity dibuat.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menghubungkan activity dengan layout activity_login.xml
        setContentView(R.layout.activity_login);

        // Menginisialisasi variabel UI dengan komponen dari layout
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // Menambahkan event listener untuk teks "Register" yang membuka RegisterActivity
        // Jadi jika tulisan register diklik, maka akan terbuka halaman Register
        tvRegister.setOnClickListener(view ->
                RegisterActivity.newInstance().show(getSupportFragmentManager(), RegisterActivity.TAG)
        );

        // Menginisialisasi DatabaseHelperLogin
        db = new DatabaseHelperLogin(this);
        // Menginisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        // Menambahkan event listener untuk tombol "Login"
        btnLogin.setOnClickListener(view -> {
            // Mengambil teks dari EditText username dan password
            String getUsername = etUsername.getText().toString();
            String getPassword = etPassword.getText().toString();

            boolean isLoginSuccessful = false;
            // Memeriksa apakah username atau password kosong
            if (getUsername.isEmpty() || getPassword.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Username atau password tidak boleh kosong!", Toast.LENGTH_LONG).show();
            } else {
                // Memeriksa login menggunakan DatabaseHelperLogin
                isLoginSuccessful = db.checkLogin(getUsername, getPassword);
                if (isLoginSuccessful) {
                    // Jika login berhasil, maka sesi diupgrade
                    boolean isSessionUpdated = db.upgradeSession("ada", 1);
                    if (isSessionUpdated) {
                        // Menampilkan pesan berhasil login
                        Toast.makeText(getApplicationContext(), "Berhasil Masuk", Toast.LENGTH_LONG).show();
                        // Menyimpan status login  ke SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("masuk", true);
                        editor.apply();

                        // Membuka MainActivity dan menutup LoginActivity
                        Intent dashboardIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(dashboardIntent);
                        finish();
                    }
                } else {
                    // Jika login gagal maka akan ditampilkan pesan gagal login
                    Toast.makeText(getApplicationContext(), "Gagal Masuk", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
