package com.hotel.gestion;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.hotel.gestion.db.DatabaseHelper;
import com.hotel.gestion.models.Chambre;
import com.hotel.gestion.models.Client;
import com.hotel.gestion.models.Reservation;
import com.hotel.gestion.utils.MoneyUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReservationActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private Spinner spinnerClients;
    private Spinner spinnerRooms;
    private TextView textArrival;
    private TextView textDeparture;
    private EditText editPeople;
    private TextInputLayout layoutPeople;
    private TextView textSummaryNights;
    private TextView textSummaryRoomTotal;
    private TextView textSummaryServiceTotal;
    private TextView textSummaryEstimatedTotal;
    private final List<Client> clients = new ArrayList<>();
    private final List<Chambre> chambres = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        dbHelper = new DatabaseHelper(this);

        spinnerClients = findViewById(R.id.spinnerClients);
        spinnerRooms = findViewById(R.id.spinnerRooms);
        textArrival = findViewById(R.id.textArrivalDate);
        textDeparture = findViewById(R.id.textDepartureDate);
        editPeople = findViewById(R.id.editReservationPeople);
        layoutPeople = findViewById(R.id.layoutReservationPeople);
        textSummaryNights = findViewById(R.id.textSummaryNightsValue);
        textSummaryRoomTotal = findViewById(R.id.textSummaryRoomValue);
        textSummaryServiceTotal = findViewById(R.id.textSummaryServiceValue);
        textSummaryEstimatedTotal = findViewById(R.id.textSummaryEstimatedValue);

        Button buttonConfirm = findViewById(R.id.buttonConfirmReservation);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        Calendar arrival = Calendar.getInstance();
        Calendar departure = Calendar.getInstance();
        departure.add(Calendar.DAY_OF_MONTH, 1);
        textArrival.setText(dateFormat.format(arrival.getTime()));
        textDeparture.setText(dateFormat.format(departure.getTime()));

        textArrival.setOnClickListener(v -> openDatePicker(textArrival));
        textDeparture.setOnClickListener(v -> openDatePicker(textDeparture));
        buttonConfirm.setOnClickListener(v -> createReservation());

        editPeople.addTextChangedListener(new SimpleTextWatcher(() -> {
            layoutPeople.setError(null);
            updateSummary();
        }));
        setupBottomNavigation(bottomNavigationView, R.id.nav_reservations);
        safeLoadSelectionData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        safeLoadSelectionData();
    }

    private void safeLoadSelectionData() {
        try {
            loadSelectionData();
        } catch (Exception exception) {
            resetSummary();
            showErrorToast(getString(R.string.message_reservation_screen_error));
        }
    }

    private void loadSelectionData() {
        clients.clear();
        clients.addAll(dbHelper.getSelectableClients());
        chambres.clear();
        chambres.addAll(dbHelper.getReservableChambres());

        ArrayAdapter<String> clientAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                buildClientLabels()
        );
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClients.setAdapter(clientAdapter);

        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                buildRoomLabels()
        );
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRooms.setAdapter(roomAdapter);

        spinnerRooms.setOnItemSelectedListener(new SimpleItemSelectedListener(this::updateSummary));
        spinnerClients.setOnItemSelectedListener(new SimpleItemSelectedListener(this::updateSummary));
        updateSummary();
    }

    private List<String> buildClientLabels() {
        List<String> labels = new ArrayList<>();
        if (clients.isEmpty()) {
            labels.add(getString(R.string.no_clients_available));
            return labels;
        }
        for (Client client : clients) {
            labels.add(client.getNomComplet().trim() + " - " + client.getNumeroPasseport());
        }
        return labels;
    }

    private List<String> buildRoomLabels() {
        List<String> labels = new ArrayList<>();
        if (chambres.isEmpty()) {
            labels.add(getString(R.string.no_rooms_available));
            return labels;
        }
        for (Chambre chambre : chambres) {
            labels.add("Ch. " + chambre.getNumero() + " - " + chambre.getType() + " - "
                    + MoneyUtils.formatMoney(chambre.getPrixNuit()));
        }
        return labels;
    }

    private void openDatePicker(TextView target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    target.setText(dateFormat.format(selected.getTime()));
                    updateSummary();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateSummary() {
        if (chambres.isEmpty()) {
            resetSummary();
            return;
        }

        int roomIndex = spinnerRooms.getSelectedItemPosition();
        if (roomIndex < 0 || roomIndex >= chambres.size()) {
            resetSummary();
            return;
        }

        Chambre chambre = chambres.get(roomIndex);
        int nights = dbHelper.countNights(textArrival.getText().toString(), textDeparture.getText().toString());
        double roomTotal = nights * chambre.getPrixNuit();

        textSummaryNights.setText(getString(R.string.nights_format, nights));
        textSummaryRoomTotal.setText(MoneyUtils.formatMoney(roomTotal));
        textSummaryServiceTotal.setText(MoneyUtils.formatMoney(0));
        textSummaryEstimatedTotal.setText(MoneyUtils.formatMoney(roomTotal));
    }

    private void createReservation() {
        try {
            if (clients.isEmpty()) {
                Toast.makeText(this, getString(R.string.message_reservation_needs_client), Toast.LENGTH_SHORT).show();
                return;
            }
            if (chambres.isEmpty()) {
                Toast.makeText(this, getString(R.string.message_reservation_needs_room), Toast.LENGTH_SHORT).show();
                return;
            }

            String peopleValue = editPeople.getText() == null ? "" : editPeople.getText().toString().trim();
            if (peopleValue.isEmpty()) {
                layoutPeople.setError(getString(R.string.message_reservation_missing_people));
                return;
            }
            layoutPeople.setError(null);

            int clientIndex = spinnerClients.getSelectedItemPosition();
            int roomIndex = spinnerRooms.getSelectedItemPosition();
            if (clientIndex < 0 || roomIndex < 0) {
                Toast.makeText(this, getString(R.string.message_invalid_selection), Toast.LENGTH_SHORT).show();
                return;
            }

            int peopleCount;
            try {
                peopleCount = Integer.parseInt(peopleValue);
                if (peopleCount <= 0) {
                    layoutPeople.setError(getString(R.string.error_invalid_capacity));
                    return;
                }
            } catch (NumberFormatException exception) {
                layoutPeople.setError(getString(R.string.error_invalid_capacity));
                return;
            }

            Reservation reservation = new Reservation();
            reservation.setClientId(clients.get(clientIndex).getId());
            reservation.setChambreId(chambres.get(roomIndex).getId());
            reservation.setDateDebut(textArrival.getText().toString());
            reservation.setDateFin(textDeparture.getText().toString());
            reservation.setNombrePersonnes(peopleCount);

            long result = dbHelper.createReservationTransaction(reservation);
            if (result > 0) {
                Toast.makeText(this, getString(R.string.message_reservation_saved), Toast.LENGTH_SHORT).show();
                showReservationSuccessDialog(result);
                return;
            }

            if (result == DatabaseHelper.RESULT_ROOM_UNAVAILABLE) {
                showErrorToast(getString(R.string.message_room_unavailable));
            } else if (result == DatabaseHelper.RESULT_INVALID_DATES) {
                showErrorToast(getString(R.string.message_invalid_departure));
            } else if (result == DatabaseHelper.RESULT_CAPACITY_EXCEEDED) {
                showErrorToast(getString(R.string.message_capacity_exceeded));
            } else if (result == DatabaseHelper.RESULT_ROOM_MAINTENANCE) {
                showErrorToast(getString(R.string.message_room_maintenance));
            } else {
                showErrorToast(getString(R.string.message_reservation_failed));
            }
        } catch (Exception exception) {
            showErrorToast(getString(R.string.message_unexpected_error));
        }
    }

    private void showReservationSuccessDialog(long reservationId) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.reservation_success_title)
                .setMessage(R.string.reservation_success_message)
                .setPositiveButton(R.string.reservation_add_services, (dialog, which) -> openServiceActivity(reservationId))
                .setNegativeButton(R.string.reservation_later, (dialog, which) -> {
                })
                .show();
    }

    private void openServiceActivity(long reservationId) {
        try {
            Intent intent = new Intent(this, ServiceActivity.class);
            intent.putExtra(ServiceActivity.EXTRA_RESERVATION_ID, reservationId);
            startActivity(intent);
        } catch (Exception exception) {
            showErrorToast(getString(R.string.message_unexpected_error));
        }
    }

    private void showErrorToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        View view = toast.getView();
        if (view != null) {
            view.setBackgroundResource(R.drawable.bg_badge_red);
        }
        toast.show();
    }

    private void resetSummary() {
        textSummaryNights.setText(getString(R.string.nights_format, 0));
        textSummaryRoomTotal.setText(MoneyUtils.formatMoney(0));
        textSummaryServiceTotal.setText(MoneyUtils.formatMoney(0));
        textSummaryEstimatedTotal.setText(MoneyUtils.formatMoney(0));
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
                openActivity(ChambreActivity.class);
                return true;
            }
            if (itemId == R.id.nav_reservations) {
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

    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable onChange;

        SimpleTextWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onChange.run();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
