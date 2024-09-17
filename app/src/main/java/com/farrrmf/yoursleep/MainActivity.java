package com.farrrmf.yoursleep;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Deklarasi variabel untuk UI components dan database helper
    private Button btnKeluar;
    private DatabaseHelperLogin db;
    public static final String SHARED_PREF_NAME = "myPref";
    private SharedPreferences sharedPreferences;
    private Button btnTambahData;
    private TextView tvHasilInput;
    private TextView tvJamMulai;
    private TextView tvJamBangun;
    private BarChart chartPolaTidur;
    private List<Integer> jamTidurList;
    private List<Integer> jamBangunList;
    private List<Integer> totalJamTidurList;
    public static final String JAM_TIDUR_KEY = "jamTidur";
    public static final String JAM_BANGUN_KEY = "jamBangun";
    public static final String TOTAL_JAM_TIDUR_KEY = "totalJamTidur";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menghubungkan activity dengan layout activity_main.xml
        setContentView(R.layout.activity_main);

        // Menginisialisasi variabel UI dengan komponen dari layout
        btnKeluar = findViewById(R.id.btnKeluar);
        db = new DatabaseHelperLogin(this);
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        btnTambahData = findViewById(R.id.btnTambahData);
        tvHasilInput = findViewById(R.id.tvHasilInput);
        tvJamMulai = findViewById(R.id.tvJamMulai);
        tvJamBangun = findViewById(R.id.tvJamBangun);
        chartPolaTidur = findViewById(R.id.chartPolaTidur);

        // Menginisialisasi tombol reset data dan menambahkan event listener
        Button btnResetData = findViewById(R.id.btnResetData);
        btnResetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetData();
            }
        });

        // Menginisialisasi list untuk menyimpan data jam tidur
        jamTidurList = new ArrayList<>();
        jamBangunList = new ArrayList<>();
        totalJamTidurList = new ArrayList<>();

        // Menambahkan event listener untuk tombol tambah data
        btnTambahData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tampilkanDialogJamMulai();
            }
        });

        btnTambahData.setOnClickListener(v -> tampilkanDialogJamMulai());

        // Memeriksa apakah sesi login masih ada
        boolean checkSession = db.checkSession("ada");
        if (!checkSession) {
            // Jika sesi tidak ada, arahkan ke LoginActivity
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        // Menambahkan event listener untuk tombol keluar
        btnKeluar.setOnClickListener(view -> {
            boolean updateSession = db.upgradeSession("kosong", 1);
            if (updateSession) {
                Toast.makeText(this, "Berhasil Keluar", Toast.LENGTH_LONG).show();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("masuk", false);
                editor.apply();

                // Arahkan ke LoginActivity
                Intent logoutIntent = new Intent(this, LoginActivity.class);
                startActivity(logoutIntent);
                finish();
            }
        });

        // Memuat data yang disimpan dan menampilkan grafik
        loadData();
        tampilkanChart();
    }

    // Fungsi untuk mereset data yang disimpan
    private void resetData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(JAM_TIDUR_KEY); // Menghapus data jam tidur
        editor.remove(JAM_BANGUN_KEY); // Menghapus data jam bangun
        editor.remove(TOTAL_JAM_TIDUR_KEY); // Menghapus data total jam tidur
        editor.apply();

        // Mengosongkan list data jam tidur
        jamTidurList.clear();
        jamBangunList.clear();
        totalJamTidurList.clear();

        // Menghapus tampilan dari TextView
        tvHasilInput.setVisibility(View.GONE);
        tvJamMulai.setVisibility(View.GONE);
        tvJamBangun.setVisibility(View.GONE);

        // Menghapus data dari grafik
        chartPolaTidur.clear();
    }

    // Fungsi untuk menampilkan grafik pola tidur
    private void tampilkanChart() {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < totalJamTidurList.size(); i++) {
            entries.add(new BarEntry(i, totalJamTidurList.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Total Jam Tidur");
        BarData barData = new BarData(dataSet);
        chartPolaTidur.setData(barData);

        // Mengatur sumbu X dan Y pada grafik
        XAxis xAxis = chartPolaTidur.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getHariLabels()));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisLeft = chartPolaTidur.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setAxisMaximum(18f);

        chartPolaTidur.getAxisRight().setEnabled(false);
        chartPolaTidur.invalidate();
    }

    // Fungsi untuk mendapatkan label hari untuk sumbu X
    private List<String> getHariLabels() {
        List<String> labels = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            labels.add("Hari ke-" + i);
        }
        return labels;
    }

    // Fungsi untuk menghitung total jam tidur
    private int hitungTotalJamTidur(int jamMulai, int menitMulai, int jamBangun, int menitBangun) {
        int totalMenitMulai = jamMulai * 60 + menitMulai;
        int totalMenitBangun = jamBangun * 60 + menitBangun;
        int totalMenitTidur = (totalMenitBangun - totalMenitMulai + 24 * 60) % (24 * 60);
        return totalMenitTidur / 60;
    }

    // Fungsi untuk memuat data yang disimpan di SharedPreferences
    private void loadData() {
        String jamTidurString = sharedPreferences.getString(JAM_TIDUR_KEY, "");
        String jamBangunString = sharedPreferences.getString(JAM_BANGUN_KEY, "");
        String totalJamTidurString = sharedPreferences.getString(TOTAL_JAM_TIDUR_KEY, "");

        if (!jamTidurString.isEmpty() && !jamBangunString.isEmpty() && !totalJamTidurString.isEmpty()) {
            jamTidurList = stringToList(jamTidurString);
            jamBangunList = stringToList(jamBangunString);
            totalJamTidurList = stringToList(totalJamTidurString);
        }
    }

    // Fungsi untuk mengkonversi list menjadi string
    private String listToString(List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (Integer i : list) {
            sb.append(i).append(",");
        }
        return sb.toString();
    }

    // Fungsi untuk mengkonversi string menjadi list
    private List<Integer> stringToList(String str) {
        List<Integer> list = new ArrayList<>();
        String[] items = str.split(",");
        for (String item : items) {
            if (!item.isEmpty()) {
                list.add(Integer.parseInt(item));
            }
        }
        return list;
    }

    // Fungsi untuk menampilkan dialog waktu mulai tidur
    private void tampilkanDialogJamMulai() {
        MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Pilih Jam Mulai Tidur")
                .build();

        materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int jamMulai = materialTimePicker.getHour();
                int menitMulai = materialTimePicker.getMinute();
                tampilkanDialogJamBangun(jamMulai, menitMulai);
            }
        });

        materialTimePicker.show(getSupportFragmentManager(), "JamMulaiPicker");
    }

    // Fungsi untuk menampilkan dialog waktu bangun tidur
    private void tampilkanDialogJamBangun(final int jamMulai, final int menitMulai) {
        MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Pilih Jam Bangun Tidur")
                .build();

        materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int jamBangun = materialTimePicker.getHour();
                int menitBangun = materialTimePicker.getMinute();
                int totalJamTidur = hitungTotalJamTidur(jamMulai, menitMulai, jamBangun, menitBangun);
                jamTidurList.add(jamMulai);
                jamBangunList.add(jamBangun);
                totalJamTidurList.add(totalJamTidur);

                simpanData();

                // Menampilkan hasil input waktu tidur
                tvJamMulai.setText(String.format(Locale.getDefault(), "Jam Mulai: %02d:%02d", jamMulai, menitMulai));
                tvJamBangun.setText(String.format(Locale.getDefault(), "Jam Bangun: %02d:%02d", jamBangun, menitBangun));
                tvHasilInput.setText(String.format(Locale.getDefault(), "Total Jam Tidur: %d jam", totalJamTidur));
                tvHasilInput.setVisibility(View.VISIBLE);
                tvJamMulai.setVisibility(View.VISIBLE);
                tvJamBangun.setVisibility(View.VISIBLE);

                tampilkanChart();
            }
        });

        materialTimePicker.show(getSupportFragmentManager(), "JamBangunPicker");
    }

    // Fungsi untuk menyimpan data ke SharedPreferences
    private void simpanData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(JAM_TIDUR_KEY, listToString(jamTidurList));
        editor.putString(JAM_BANGUN_KEY, listToString(jamBangunList));
        editor.putString(TOTAL_JAM_TIDUR_KEY, listToString(totalJamTidurList)); // Menyimpan data total jam tidur
        editor.apply();
    }

    // Fungsi untuk menampilkan hasil input waktu tidur di TextView
    private void tampilkanHasil(int jamMulai, int menitMulai, int jamBangun, int menitBangun, int totalJamTidur) {
        String jamMulaiFormatted = String.format(Locale.getDefault(), "%02d:%02d", jamMulai, menitMulai);
        String jamBangunFormatted = String.format(Locale.getDefault(), "%02d:%02d", jamBangun, menitBangun);

        tvHasilInput.setText("Total Jam Tidur: " + totalJamTidur + " jam");
        tvJamMulai.setText("Jam Mulai Tidur: " + jamMulaiFormatted);
        tvJamBangun.setText("Jam Bangun Tidur: " + jamBangunFormatted);
        tvHasilInput.setVisibility(View.VISIBLE);
        tvJamMulai.setVisibility(View.VISIBLE);
        tvJamBangun.setVisibility(View.VISIBLE);
    }
}
