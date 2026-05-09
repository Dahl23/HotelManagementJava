package com.hotel.gestion;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.hotel.gestion.adapters.ClientAdapter;
import com.hotel.gestion.db.DatabaseHelper;
import com.hotel.gestion.models.Client;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ClientActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ClientAdapter adapter;
    private SearchView searchView;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        dbHelper = new DatabaseHelper(this);
        adapter = new ClientAdapter(new ClientAdapter.OnClientActionListener() {
            @Override
            public void onEditClient(Client client) {
                showClientDialog(client);
            }

            @Override
            public void onDeleteClient(Client client) {
                confirmDeleteClient(client);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerClients);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.searchClients);
        FloatingActionButton fab = findViewById(R.id.fabAddClient);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        fab.setOnClickListener(v -> showClientDialog(null));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadClients(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadClients(newText);
                return true;
            }
        });

        setupBottomNavigation(bottomNavigationView, R.id.nav_clients);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClients(searchView.getQuery() == null ? "" : searchView.getQuery().toString());
    }

    private void loadClients(String query) {
        List<Client> clients = dbHelper.searchClients(query);
        adapter.setClients(clients);
    }

    private void showClientDialog(Client existingClient) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_client_form, null, false);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();

        TextInputLayout layoutNom = view.findViewById(R.id.layoutClientNom);
        TextInputLayout layoutPrenom = view.findViewById(R.id.layoutClientPrenom);
        TextInputLayout layoutEmail = view.findViewById(R.id.layoutClientEmail);
        TextInputLayout layoutTelephone = view.findViewById(R.id.layoutClientTelephone);
        TextInputLayout layoutAdresse = view.findViewById(R.id.layoutClientAdresse);
        TextInputLayout layoutPassport = view.findViewById(R.id.layoutClientPassport);
        TextInputLayout layoutBirthDate = view.findViewById(R.id.layoutClientBirthDate);

        EditText editNom = view.findViewById(R.id.editClientNom);
        EditText editPrenom = view.findViewById(R.id.editClientPrenom);
        EditText editEmail = view.findViewById(R.id.editClientEmail);
        EditText editTelephone = view.findViewById(R.id.editClientTelephone);
        EditText editAdresse = view.findViewById(R.id.editClientAdresse);
        EditText editPassport = view.findViewById(R.id.editClientPassport);
        EditText editBirthDate = view.findViewById(R.id.editClientBirthDate);
        Button buttonSave = view.findViewById(R.id.buttonSaveClient);

        if (existingClient != null) {
            editNom.setText(existingClient.getNom());
            editPrenom.setText(existingClient.getPrenom());
            editEmail.setText(existingClient.getEmail());
            editTelephone.setText(existingClient.getTelephone());
            editAdresse.setText(existingClient.getAdresse());
            editPassport.setText(existingClient.getNumeroPasseport());
            editBirthDate.setText(existingClient.getDateNaissance());
            buttonSave.setText(R.string.update_client);
        }

        editBirthDate.setOnClickListener(v -> openDatePicker(editBirthDate));

        buttonSave.setOnClickListener(v -> {
            clearErrors(layoutNom, layoutPrenom, layoutEmail, layoutTelephone, layoutAdresse, layoutPassport, layoutBirthDate);

            boolean valid = true;
            valid &= validateRequired(layoutNom, editNom);
            valid &= validateRequired(layoutPrenom, editPrenom);
            valid &= validateRequired(layoutTelephone, editTelephone);
            valid &= validateRequired(layoutPassport, editPassport);
            valid &= validateRequired(layoutBirthDate, editBirthDate);
            valid &= validateOptionalEmail(layoutEmail, editEmail);

            if (!valid) {
                return;
            }

            Client client = new Client();
            client.setNom(editNom.getText().toString().trim());
            client.setPrenom(editPrenom.getText().toString().trim());
            client.setEmail(editEmail.getText().toString().trim());
            client.setTelephone(editTelephone.getText().toString().trim());
            client.setAdresse(editAdresse.getText().toString().trim());
            client.setNumeroPasseport(editPassport.getText().toString().trim());
            client.setDateNaissance(editBirthDate.getText().toString().trim());

            boolean success;
            if (existingClient == null) {
                success = dbHelper.insertClient(client) > 0;
            } else {
                client.setId(existingClient.getId());
                success = dbHelper.updateClient(client);
            }

            if (success) {
                Toast.makeText(this, getString(existingClient == null
                        ? R.string.message_client_saved
                        : R.string.message_client_updated), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadClients(searchView.getQuery() == null ? "" : searchView.getQuery().toString());
            } else {
                Toast.makeText(this, getString(R.string.message_client_save_failed), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setOnShowListener(dialogInterface -> configureDialogWindow(dialog));
        dialog.show();
    }

    private void confirmDeleteClient(Client client) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_client_title)
                .setMessage(getString(R.string.delete_client_message, client.getNomComplet().trim()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    if (dbHelper.deleteClient(client.getId())) {
                        Toast.makeText(this, R.string.message_client_deleted, Toast.LENGTH_SHORT).show();
                        loadClients(searchView.getQuery() == null ? "" : searchView.getQuery().toString());
                    } else {
                        Toast.makeText(this, R.string.message_client_delete_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void openDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    target.setText(dateFormat.format(selected.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
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

    private boolean validateRequired(TextInputLayout layout, EditText field) {
        String value = field.getText() == null ? "" : field.getText().toString().trim();
        if (!value.isEmpty()) {
            layout.setError(null);
            return true;
        }
        layout.setError(getString(R.string.error_required));
        return false;
    }

    private boolean validateOptionalEmail(TextInputLayout layout, EditText field) {
        String value = field.getText() == null ? "" : field.getText().toString().trim();
        if (value.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            layout.setError(null);
            return true;
        }
        layout.setError(getString(R.string.error_invalid_email));
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
        finish();
    }
}
