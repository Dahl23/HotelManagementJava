package com.hotel.gestion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hotel.gestion.adapters.ServiceAdapter;
import com.hotel.gestion.db.DatabaseHelper;
import com.hotel.gestion.models.Service;
import com.hotel.gestion.utils.MoneyUtils;

public class ServiceActivity extends AppCompatActivity {
    public static final String EXTRA_RESERVATION_ID = "reservation_id";

    private DatabaseHelper dbHelper;
    private ServiceAdapter adapter;
    private long reservationId;
    private TextView textReservationInfo;
    private TextView textServiceTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        reservationId = getIntent().getLongExtra(EXTRA_RESERVATION_ID, -1L);
        if (reservationId <= 0) {
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        textReservationInfo = findViewById(R.id.textServiceReservationInfo);
        textServiceTotal = findViewById(R.id.textServiceTotalValue);
        Button buttonInvoice = findViewById(R.id.buttonViewInvoice);

        RecyclerView recyclerView = findViewById(R.id.recyclerServices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServiceAdapter(this::addServiceToReservation);
        recyclerView.setAdapter(adapter);

        buttonInvoice.setOnClickListener(v -> {
            Intent intent = new Intent(this, FactureActivity.class);
            intent.putExtra(FactureActivity.EXTRA_RESERVATION_ID, reservationId);
            startActivity(intent);
        });

        loadServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadServices();
    }

    private void loadServices() {
        DatabaseHelper.ReservationInvoice invoice = dbHelper.getReservationInvoice(reservationId);
        if (invoice == null) {
            Toast.makeText(this, getString(R.string.message_reservation_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textReservationInfo.setText(getString(
                R.string.service_reservation_format,
                invoice.client.getNomComplet().trim(),
                invoice.chambre.getNumero()
        ));
        adapter.setServices(dbHelper.getAllServices(), dbHelper.getServiceQuantitiesForReservation(reservationId));
        updateServiceTotal();
    }

    private void addServiceToReservation(Service service) {
        int quantity = dbHelper.addServiceToReservation(reservationId, service.getId());
        if (quantity > 0) {
            adapter.updateQuantity(service.getId(), quantity);
            updateServiceTotal();
        }
    }

    private void updateServiceTotal() {
        double total = dbHelper.getReservationServiceTotal(reservationId);
        textServiceTotal.setText(MoneyUtils.formatMoney(total));
    }
}
