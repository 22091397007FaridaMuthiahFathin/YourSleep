package com.farrrmf.yoursleep; // Mendefinisikan paket tempat file ini berada

import androidx.appcompat.app.AppCompatActivity; // Import class AppCompatActivity dari AndroidX

import android.os.Bundle; // Import class Bundle dari Android
import android.view.ViewGroup; // Import class ViewGroup dari Android
import android.app.Activity; // Import class Activity dari Android
import android.content.DialogInterface; // Import class DialogInterface dari Android
import android.view.LayoutInflater; // Import class LayoutInflater dari Android
import android.view.View; // Import class View dari Android
import android.widget.Button; // Import class Button dari Android
import android.widget.EditText; // Import class EditText dari Android
import android.widget.Toast; // Import class Toast dari Android
import androidx.fragment.app.Fragment; // Import class Fragment dari AndroidX

import androidx.annotation.NonNull; // Import class NonNull dari AndroidX untuk anotasi
import androidx.annotation.Nullable; // Import class Nullable dari AndroidX untuk anotasi

import com.google.android.material.bottomsheet.BottomSheetDialogFragment; // Import class BottomSheetDialogFragment dari Material Design

public class RegisterActivity extends BottomSheetDialogFragment { // Deklarasi kelas RegisterActivity yang merupakan turunan dari BottomSheetDialogFragment

    private DatabaseHelperLogin db; // Deklarasi variabel db untuk mengakses DatabaseHelperLogin

    public static final String TAG = "Register"; // Deklarasi konstanta TAG untuk logging

    public static RegisterActivity newInstance() { // Metode untuk membuat instance baru dari RegisterActivity
        return new RegisterActivity(); // Mengembalikan instance baru dari RegisterActivity
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // Metode yang dipanggil saat fragment membuat tampilan
        return inflater.inflate(R.layout.activity_register, container, false); // Meng-inflate layout activity_register untuk tampilan fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { // Metode yang dipanggil setelah tampilan fragment dibuat
        super.onViewCreated(view, savedInstanceState); // Memanggil metode superclass

        // Mengambil referensi dari elemen UI
        EditText username = view.findViewById(R.id.etUsername); // Referensi EditText untuk username
        EditText password = view.findViewById(R.id.etPassword); // Referensi EditText untuk password
        EditText repassword = view.findViewById(R.id.etRepeatPassword); // Referensi EditText untuk konfirmasi password
        Button daftar = view.findViewById(R.id.btnRegister); // Referensi Button untuk mendaftar

        db = new DatabaseHelperLogin(requireActivity()); // Inisialisasi DatabaseHelperLogin

        // Menetapkan aksi yang dilakukan saat tombol daftar diklik
        daftar.setOnClickListener(v -> {
            String inUsername = username.getText().toString().trim(); // Mengambil teks dari EditText username dan menghapus spasi berlebih
            String inPassword = password.getText().toString().trim(); // Mengambil teks dari EditText password dan menghapus spasi berlebih
            String inRePassword = repassword.getText().toString().trim(); // Mengambil teks dari EditText konfirmasi password dan menghapus spasi berlebih

            if (!inRePassword.equals(inPassword)) { // Memeriksa apakah password dan konfirmasi password sama
                repassword.setError("Password Tidak Sama"); // Menampilkan pesan error jika password tidak sama
            } else {
                boolean isRegistered = db.simpanUser(inUsername, inPassword); // Menyimpan user ke database dan menyimpan hasilnya ke isRegistered
                if (isRegistered) { // Memeriksa apakah pendaftaran berhasil
                    Toast.makeText(getActivity(), "Daftar Berhasil", Toast.LENGTH_LONG).show(); // Menampilkan pesan toast bahwa pendaftaran berhasil
                } else {
                    Toast.makeText(getActivity(), "Daftar Gagal", Toast.LENGTH_LONG).show(); // Menampilkan pesan toast bahwa pendaftaran gagal
                }
                dismiss(); // Menutup dialog bottom sheet
            }
        });
    }
}
