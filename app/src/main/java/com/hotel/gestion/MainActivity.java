package com.hotel.gestion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hotel.gestion.db.DatabaseHelper;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView textAvailableRooms;
    private TextView textActiveReservations;
    private TextView textClients;
    private TextView textCheckouts;
    private TextView textOccupationSummary;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        textAvailableRooms = findViewById(R.id.textStatAvailableRooms);
        textActiveReservations = findViewById(R.id.textStatReservations);
        textClients = findViewById(R.id.textStatClients);
        textCheckouts = findViewById(R.id.textStatCheckouts);
        textOccupationSummary = findViewById(R.id.textOccupationSummary);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        Button buttonClients = findViewById(R.id.buttonQuickClients);
        Button buttonRooms = findViewById(R.id.buttonQuickRooms);
        Button buttonReservations = findViewById(R.id.buttonQuickReservations);

        buttonClients.setOnClickListener(v -> openActivity(ClientActivity.class));
        buttonRooms.setOnClickListener(v -> openActivity(ChambreActivity.class));
        buttonReservations.setOnClickListener(v -> openActivity(ReservationActivity.class));

        setupBottomNavigation(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboard();
    }

    private void refreshDashboard() {
        DatabaseHelper.DashboardStats stats = dbHelper.getDashboardStats();
        textAvailableRooms.setText(String.valueOf(stats.availableRooms));
        textActiveReservations.setText(String.valueOf(stats.activeReservations));
        textClients.setText(String.valueOf(stats.clients));
        textCheckouts.setText(String.valueOf(stats.checkoutsToday));
        textOccupationSummary.setText(getString(
                R.string.dashboard_occupation_format,
                stats.occupationPercent,
                stats.totalRooms
        ));
    }

    private void setupBottomNavigation(int selectedId) {
        bottomNavigationView.setSelectedItemId(selectedId);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == selectedId) {
                return true;
            }
            if (itemId == R.id.nav_home) {
                openActivity(MainActivity.class);
                return true;
            }
            if (itemId == R.id.nav_clients) {
                openActivity(ClientActivity.class);
                return true;
            }
            if (itemId == R.id.nav_chambres) {
                openActivity(ChambreActivity.class);
                return true;
            }
            if (itemId == R.id.nav_reservations) {
                openActivity(ReservationActivity.class);
                return true;
            }
            return false;
        });
    }

    private void openActivity(Class<?> target) {
        startActivity(new Intent(this, target));
        overridePendingTransition(0, 0);
        if (target != MainActivity.class) {
            finish();
        }
    }
}
