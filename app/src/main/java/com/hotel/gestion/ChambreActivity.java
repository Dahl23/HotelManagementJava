package com.hotel.gestion;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.hotel.gestion.adapters.ChambreAdapter;
import com.hotel.gestion.db.DatabaseHelper;
import com.hotel.gestion.models.Chambre;

import java.util.List;

public class ChambreActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ChambreAdapter adapter;
    private ProgressBar progressBar;
    private TextView textOccupation;
    private TextView chipAvailable;
    private TextView chipOccupied;
    private TextView chipMaintenance;
    private String selectedFilter = DatabaseHelper.FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chambre);

        dbHelper = new DatabaseHelper(this);
        adapter = new ChambreAdapter(new ChambreAdapter.OnChambreActionListener() {
            @Override
            public void onEditChambre(Chambre chambre) {
                showRoomDialog(chambre);
            }

            @Override
            public void onDeleteChambre(Chambre chambre) {
                confirmDeleteRoom(chambre);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerRooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        progressBar = findViewById(R.id.progressOccupation);
        textOccupation = findViewById(R.id.textOccupationValue);
        chipAvailable = findViewById(R.id.chipAvailable);
        chipOccupied = findViewById(R.id.chipOccupied);
        chipMaintenance = findViewById(R.id.chipMaintenance);

        FloatingActionButton fab = findViewById(R.id.fabAddRoom);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        fab.setOnClickListener(v -> showRoomDialog(null));
        chipAvailable.setOnClickListener(v -> selectFilter(DatabaseHelper.FILTER_AVAILABLE));
        chipOccupied.setOnClickListener(v -> selectFilter(DatabaseHelper.FILTER_OCCUPIED));
        chipMaintenance.setOnClickListener(v -> selectFilter(DatabaseHelper.FILTER_MAINTENANCE));

        setupBottomNavigation(bottomNavigationView, R.id.nav_chambres);
        applyChipStyles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshRoomData();
    }

    private void selectFilter(String filter) {
        if (filter.equals(selectedFilter)) {
            selectedFilter = DatabaseHelper.FILTER_ALL;
        } else {
            selectedFilter = filter;
        }
        applyChipStyles();
        refreshRoomData();
    }

    private void applyChipStyles() {
        updateChip(chipAvailable, DatabaseHelper.FILTER_AVAILABLE.equals(selectedFilter));
        updateChip(chipOccupied, DatabaseHelper.FILTER_OCCUPIED.equals(selectedFilter));
        updateChip(chipMaintenance, DatabaseHelper.FILTER_MAINTENANCE.equals(selectedFilter));
    }

    private void updateChip(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chip.setTextColor(getColor(selected ? R.color.white : R.color.text_primary));
    }

    private void refreshRoomData() {
        DatabaseHelper.DashboardStats stats = dbHelper.getDashboardStats();
        progressBar.setProgress(stats.occupationPercent);
        textOccupation.setText(getString(R.string.room_occupied_percent_format, stats.occupationPercent));

        List<Chambre> chambres = dbHelper.getChambres(selectedFilter);
        adapter.setChambres(chambres);
    }

    private void showRoomDialog(Chambre existingRoom) {
        try {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_chambre_form, null, false);
            AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                    .setView(view)
                    .create();

            TextInputLayout layoutNumber = view.findViewById(R.id.layoutRoomNumber);
            TextInputLayout layoutType = view.findViewById(R.id.layoutRoomType);
            TextInputLayout layoutPrice = view.findViewById(R.id.layoutRoomPrice);
            TextInputLayout layoutCapacity = view.findViewById(R.id.layoutRoomCapacity);
            TextInputLayout layoutEquipment = view.findViewById(R.id.layoutRoomEquipment);

            EditText editNumber = view.findViewById(R.id.editRoomNumber);
            MaterialAutoCompleteTextView inputRoomType = view.findViewById(R.id.inputRoomType);
            EditText editPrice = view.findViewById(R.id.editRoomPrice);
            EditText editCapacity = view.findViewById(R.id.editRoomCapacity);
            EditText editEquipment = view.findViewById(R.id.editRoomEquipment);
            MaterialSwitch switchAvailable = view.findViewById(R.id.switchRoomAvailable);
            Button buttonSave = view.findViewById(R.id.buttonSaveRoom);

            if (layoutNumber == null || layoutType == null || layoutPrice == null || layoutCapacity == null
                    || layoutEquipment == null || editNumber == null || inputRoomType == null
                    || editPrice == null || editCapacity == null || editEquipment == null
                    || switchAvailable == null || buttonSave == null) {
                Toast.makeText(this, getString(R.string.message_room_dialog_failed), Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    new String[]{"Simple", "Double", "Suite"}
            );
            inputRoomType.setAdapter(typeAdapter);
            inputRoomType.setText("Simple", false);

            if (existingRoom != null) {
                editNumber.setText(existingRoom.getNumero());
                inputRoomType.setText(existingRoom.getType(), false);
                editPrice.setText(String.valueOf(existingRoom.getPrixNuit()));
                editCapacity.setText(String.valueOf(existingRoom.getCapacite()));
                editEquipment.setText(existingRoom.getEquipements());
                switchAvailable.setChecked(Chambre.STATUS_LIBRE.equals(existingRoom.getStatut()));
                buttonSave.setText(R.string.update_room);
            }

            buttonSave.setOnClickListener(v -> {
                try {
                    clearErrors(layoutNumber, layoutType, layoutPrice, layoutCapacity, layoutEquipment);

                    boolean valid = true;
                    valid &= validateRequired(layoutNumber, editNumber);
                    valid &= validateRequired(layoutType, inputRoomType);
                    valid &= validatePositiveDecimal(layoutPrice, editPrice);
                    valid &= validatePositiveInteger(layoutCapacity, editCapacity);

                    if (!valid) {
                        return;
                    }

                    Chambre chambre = new Chambre();
                    chambre.setNumero(editNumber.getText().toString().trim());
                    chambre.setType(inputRoomType.getText().toString().trim());
                    chambre.setPrixNuit(Double.parseDouble(editPrice.getText().toString().trim()));
                    chambre.setCapacite(Integer.parseInt(editCapacity.getText().toString().trim()));
                    chambre.setEquipements(editEquipment.getText().toString().trim());
                    chambre.setDisponible(switchAvailable.isChecked());

                    boolean success;
                    if (existingRoom == null) {
                        success = dbHelper.insertChambre(chambre) > 0;
                    } else {
                        chambre.setId(existingRoom.getId());
                        success = dbHelper.updateChambre(chambre);
                    }

                    if (success) {
                        Toast.makeText(this, getString(existingRoom == null
                                ? R.string.message_room_saved
                                : R.string.message_room_updated), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        refreshRoomData();
                    } else {
                        layoutNumber.setError(getString(R.string.message_room_duplicate));
                        Toast.makeText(this, getString(R.string.message_room_save_failed), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception exception) {
                    Toast.makeText(this, getString(R.string.message_room_save_failed), Toast.LENGTH_SHORT).show();
                }
            });

            dialog.setOnShowListener(dialogInterface -> configureDialogWindow(dialog));
            dialog.show();
        } catch (Exception exception) {
            Toast.makeText(this, getString(R.string.message_room_dialog_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteRoom(Chambre chambre) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_room_title)
                .setMessage(getString(R.string.delete_room_message, chambre.getNumero()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    if (dbHelper.deleteChambre(chambre.getId())) {
                        Toast.makeText(this, R.string.message_room_deleted, Toast.LENGTH_SHORT).show();
                        refreshRoomData();
                    } else {
                        Toast.makeText(this, R.string.message_room_delete_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void configureDialogWindow(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95f);
        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private boolean validateRequired(TextInputLayout layout, TextView field) {
        String value = field.getText() == null ? "" : field.getText().toString().trim();
        if (!value.isEmpty()) {
            layout.setError(null);
            return true;
        }
        layout.setError(getString(R.string.error_required));
        return false;
    }

    private boolean validatePositiveDecimal(TextInputLayout layout, EditText field) {
        String value = field.getText() == null ? "" : field.getText().toString().trim();
        try {
            if (!value.isEmpty() && Double.parseDouble(value) > 0) {
                layout.setError(null);
                return true;
            }
        } catch (NumberFormatException ignored) {
        }
        layout.setError(getString(R.string.error_invalid_price));
        return false;
    }

    private boolean validatePositiveInteger(TextInputLayout layout, EditText field) {
        String value = field.getText() == null ? "" : field.getText().toString().trim();
        try {
            if (!value.isEmpty() && Integer.parseInt(value) > 0) {
                layout.setError(null);
                return true;
            }
        } catch (NumberFormatException ignored) {
        }
        layout.setError(getString(R.string.error_invalid_capacity));
        return false;
    }

    private void clearErrors(TextInputLayout... layouts) {
        for (TextInputLayout layout : layouts) {
            layout.setError(null);
        }
    }

    private void setupBottomNavigation(BottomNavigationView bottomNavigationView, int selectedId) {
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
        finish();
    }
}
