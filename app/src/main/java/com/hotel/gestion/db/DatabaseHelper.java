package com.hotel.gestion.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hotel.gestion.models.Chambre;
import com.hotel.gestion.models.Client;
import com.hotel.gestion.models.ConsommationService;
import com.hotel.gestion.models.Reservation;
import com.hotel.gestion.models.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "hotel_manager.db";
    public static final int DB_VERSION = 3;

    public static final long RESULT_ROOM_UNAVAILABLE = -10L;
    public static final long RESULT_INVALID_DATES = -11L;
    public static final long RESULT_CAPACITY_EXCEEDED = -12L;
    public static final long RESULT_ROOM_MAINTENANCE = -13L;
    public static final long RESULT_UNKNOWN_ERROR = -14L;

    public static final String FILTER_ALL = "ALL";
    public static final String FILTER_AVAILABLE = "Disponible";
    public static final String FILTER_OCCUPIED = "Occupee";
    public static final String FILTER_MAINTENANCE = "Maintenance";

    private static final String STATUS_CONFIRMED = "Confirmee";
    private static final String STATUS_COMPLETED = "Terminee";
    private static final String CLIENT_STATUS_STAYING = "En sejour";
    private static final String CLIENT_STATUS_ARCHIVED = "Archive";
    private static final String CLIENT_STATUS_ACTIVE = "Actif";

    private static final String TABLE_CLIENTS = "clients";
    private static final String TABLE_CHAMBRES = "chambres";
    private static final String TABLE_RESERVATIONS = "reservations";
    private static final String TABLE_SERVICES = "services";
    private static final String TABLE_CONSOMMATIONS = "consommation_services";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CLIENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT NOT NULL," +
                "prenom TEXT NOT NULL," +
                "email TEXT," +
                "telephone TEXT NOT NULL," +
                "adresse TEXT," +
                "numero_passeport TEXT NOT NULL," +
                "date_naissance TEXT NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_CHAMBRES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "numero TEXT NOT NULL UNIQUE," +
                "type TEXT NOT NULL," +
                "prix_nuit REAL NOT NULL," +
                "capacite INTEGER NOT NULL," +
                "equipements TEXT," +
                "disponible INTEGER NOT NULL DEFAULT 1" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_RESERVATIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "client_id INTEGER NOT NULL," +
                "chambre_id INTEGER NOT NULL," +
                "date_debut TEXT NOT NULL," +
                "date_fin TEXT NOT NULL," +
                "nombre_personnes INTEGER NOT NULL," +
                "statut TEXT NOT NULL," +
                "montant_total REAL NOT NULL DEFAULT 0," +
                "date_reservation TEXT NOT NULL," +
                "FOREIGN KEY(client_id) REFERENCES clients(id) ON DELETE CASCADE," +
                "FOREIGN KEY(chambre_id) REFERENCES chambres(id) ON DELETE CASCADE" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_SERVICES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT NOT NULL," +
                "description TEXT," +
                "prix REAL NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_CONSOMMATIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "reservation_id INTEGER NOT NULL," +
                "service_id INTEGER NOT NULL," +
                "quantite INTEGER NOT NULL," +
                "date_consommation TEXT NOT NULL," +
                "prix_unitaire REAL NOT NULL," +
                "FOREIGN KEY(reservation_id) REFERENCES reservations(id) ON DELETE CASCADE," +
                "FOREIGN KEY(service_id) REFERENCES services(id) ON DELETE CASCADE" +
                ")");

        seedDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONSOMMATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESERVATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAMBRES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENTS);
        onCreate(db);
    }

    private void seedDefaultData(SQLiteDatabase db) {
        insertRoomSeed(db, "101", "Simple", 65000, 1, "Wi-Fi, TV, Douche", true);
        insertRoomSeed(db, "102", "Double", 90000, 2, "Wi-Fi, TV, Balcon", true);
        insertRoomSeed(db, "103", "Suite", 150000, 4, "Wi-Fi, TV, Salon, Jacuzzi", true);
        insertRoomSeed(db, "201", "Simple", 70000, 1, "Wi-Fi, TV, Bureau", true);
        insertRoomSeed(db, "202", "Double", 95000, 2, "Wi-Fi, TV, Mini-bar", true);
        insertRoomSeed(db, "203", "Suite", 165000, 4, "Wi-Fi, TV, Salon, Vue ville", true);
        insertRoomSeed(db, "301", "Double", 100000, 3, "Wi-Fi, TV, Balcon, Coffre", true);
        insertRoomSeed(db, "305", "Suite", 180000, 4, "Jacuzzi, Terrasse, Vue panoramique", false);

        insertServiceSeed(db, "Petit-dejeuner", "Buffet continental servi en chambre ou au restaurant", 12000);
        insertServiceSeed(db, "Blanchisserie", "Nettoyage et repassage express", 9000);
        insertServiceSeed(db, "Navette aeroport", "Transfert simple depuis ou vers l aeroport", 25000);
        insertServiceSeed(db, "Spa", "Acces spa et massage relaxant", 40000);
        insertServiceSeed(db, "Diner gastronomique", "Menu complet du chef", 30000);
    }

    private void insertRoomSeed(SQLiteDatabase db, String numero, String type, double prixNuit,
                                int capacite, String equipements, boolean disponible) {
        ContentValues values = new ContentValues();
        values.put("numero", numero);
        values.put("type", type);
        values.put("prix_nuit", prixNuit);
        values.put("capacite", capacite);
        values.put("equipements", equipements);
        values.put("disponible", disponible ? 1 : 0);
        db.insert(TABLE_CHAMBRES, null, values);
    }

    private void insertServiceSeed(SQLiteDatabase db, String nom, String description, double prix) {
        ContentValues values = new ContentValues();
        values.put("nom", nom);
        values.put("description", description);
        values.put("prix", prix);
        db.insert(TABLE_SERVICES, null, values);
    }

    public long insertClient(Client client) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nom", client.getNom());
        values.put("prenom", client.getPrenom());
        values.put("email", client.getEmail());
        values.put("telephone", client.getTelephone());
        values.put("adresse", client.getAdresse());
        values.put("numero_passeport", client.getNumeroPasseport());
        values.put("date_naissance", client.getDateNaissance());
        return db.insert(TABLE_CLIENTS, null, values);
    }

    public List<Client> getAllClients() {
        return searchClients("");
    }

    public List<Client> searchClients(String query) {
        SQLiteDatabase db = getReadableDatabase();
        List<Client> clients = new ArrayList<>();
        String trimmed = query == null ? "" : query.trim();
        Cursor cursor;

        if (trimmed.isEmpty()) {
            cursor = db.query(TABLE_CLIENTS, null, null, null, null, null, "nom ASC, prenom ASC");
        } else {
            String likeValue = "%" + trimmed + "%";
            cursor = db.query(
                    TABLE_CLIENTS,
                    null,
                    "nom LIKE ? OR prenom LIKE ? OR telephone LIKE ? OR numero_passeport LIKE ?",
                    new String[]{likeValue, likeValue, likeValue, likeValue},
                    null,
                    null,
                    "nom ASC, prenom ASC"
            );
        }

        while (cursor.moveToNext()) {
            Client client = mapClient(cursor);
            client.setStatut(resolveClientStatus(db, client.getId()));
            clients.add(client);
        }
        cursor.close();
        return clients;
    }

    public long insertChambre(Chambre chambre) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("numero", chambre.getNumero());
            values.put("type", chambre.getType());
            values.put("prix_nuit", chambre.getPrixNuit());
            values.put("capacite", chambre.getCapacite());
            values.put("equipements", chambre.getEquipements());
            values.put("disponible", chambre.isDisponible() ? 1 : 0);
            return db.insert(TABLE_CHAMBRES, null, values);
        } catch (Exception exception) {
            return -1L;
        }
    }

    public List<Chambre> getChambres(String filter) {
        SQLiteDatabase db = getReadableDatabase();
        List<Chambre> chambres = new ArrayList<>();
        Cursor cursor = db.query(TABLE_CHAMBRES, null, null, null, null, null, "numero ASC");

        while (cursor.moveToNext()) {
            Chambre chambre = mapChambre(cursor);
            chambre.setStatut(resolveRoomStatus(db, chambre.getId(), chambre.isDisponible()));
            if (matchesFilter(chambre, filter)) {
                chambres.add(chambre);
            }
        }
        cursor.close();
        return chambres;
    }

    public List<Chambre> getReservableChambres() {
        SQLiteDatabase db = getReadableDatabase();
        List<Chambre> chambres = new ArrayList<>();
        Cursor cursor = db.query(TABLE_CHAMBRES, null, "disponible = 1", null, null, null, "numero ASC");

        while (cursor.moveToNext()) {
            Chambre chambre = mapChambre(cursor);
            chambre.setStatut(resolveRoomStatus(db, chambre.getId(), chambre.isDisponible()));
            chambres.add(chambre);
        }
        cursor.close();
        return chambres;
    }

    public List<Client> getSelectableClients() {
        return getAllClients();
    }

    public DashboardStats getDashboardStats() {
        SQLiteDatabase db = getReadableDatabase();
        DashboardStats stats = new DashboardStats();
        String today = getCurrentDate();

        stats.totalRooms = getCount(db, "SELECT COUNT(*) FROM " + TABLE_CHAMBRES, null);
        stats.occupiedRooms = getCount(
                db,
                "SELECT COUNT(*) FROM " + TABLE_CHAMBRES + " c " +
                        "WHERE EXISTS (" +
                        "SELECT 1 FROM " + TABLE_RESERVATIONS + " r " +
                        "WHERE r.chambre_id = c.id AND r.statut = ? " +
                        "AND r.date_debut <= ? AND r.date_fin >= ?)",
                new String[]{STATUS_CONFIRMED, today, today}
        );
        stats.availableRooms = getCount(
                db,
                "SELECT COUNT(*) FROM " + TABLE_CHAMBRES + " c " +
                        "WHERE c.disponible = 1 AND NOT EXISTS (" +
                        "SELECT 1 FROM " + TABLE_RESERVATIONS + " r " +
                        "WHERE r.chambre_id = c.id AND r.statut = ? " +
                        "AND r.date_debut <= ? AND r.date_fin >= ?)",
                new String[]{STATUS_CONFIRMED, today, today}
        );
        stats.clients = getCount(db, "SELECT COUNT(*) FROM " + TABLE_CLIENTS, null);
        stats.activeReservations = getCount(
                db,
                "SELECT COUNT(*) FROM " + TABLE_RESERVATIONS +
                        " WHERE statut = ? AND date_debut <= ? AND date_fin >= ?",
                new String[]{STATUS_CONFIRMED, today, today}
        );
        stats.checkoutsToday = getCount(
                db,
                "SELECT COUNT(*) FROM " + TABLE_RESERVATIONS +
                        " WHERE statut = ? AND date_fin = ?",
                new String[]{STATUS_CONFIRMED, today}
        );

        if (stats.totalRooms > 0) {
            stats.occupationPercent = (int) Math.round((stats.occupiedRooms * 100.0) / stats.totalRooms);
        }
        return stats;
    }

    public long createReservationTransaction(Reservation reservation) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            if (!isValidDateRange(reservation.getDateDebut(), reservation.getDateFin())) {
                return RESULT_INVALID_DATES;
            }

            Cursor roomCursor = db.query(
                    TABLE_CHAMBRES,
                    null,
                    "id = ?",
                    new String[]{String.valueOf(reservation.getChambreId())},
                    null,
                    null,
                    null
            );
            if (!roomCursor.moveToFirst()) {
                roomCursor.close();
                return RESULT_UNKNOWN_ERROR;
            }

            boolean roomDisponible = roomCursor.getInt(roomCursor.getColumnIndexOrThrow("disponible")) == 1;
            int capacite = roomCursor.getInt(roomCursor.getColumnIndexOrThrow("capacite"));
            double prixNuit = roomCursor.getDouble(roomCursor.getColumnIndexOrThrow("prix_nuit"));
            long chambreId = roomCursor.getLong(roomCursor.getColumnIndexOrThrow("id"));
            roomCursor.close();

            if (reservation.getNombrePersonnes() > capacite) {
                return RESULT_CAPACITY_EXCEEDED;
            }
            if (!roomDisponible && !hasActiveReservationToday(db, chambreId)) {
                return RESULT_ROOM_MAINTENANCE;
            }
            if (hasOverlappingReservation(db, chambreId, reservation.getDateDebut(), reservation.getDateFin())) {
                return RESULT_ROOM_UNAVAILABLE;
            }

            int nights = countNights(reservation.getDateDebut(), reservation.getDateFin());
            double roomTotal = nights * prixNuit;

            ContentValues values = new ContentValues();
            values.put("client_id", reservation.getClientId());
            values.put("chambre_id", chambreId);
            values.put("date_debut", reservation.getDateDebut());
            values.put("date_fin", reservation.getDateFin());
            values.put("nombre_personnes", reservation.getNombrePersonnes());
            values.put("statut", STATUS_CONFIRMED);
            values.put("montant_total", roomTotal);
            values.put("date_reservation", getCurrentDate());

            long reservationId = db.insert(TABLE_RESERVATIONS, null, values);
            if (reservationId <= 0) {
                return RESULT_UNKNOWN_ERROR;
            }

            if (isStayActiveOnCurrentDate(reservation.getDateDebut(), reservation.getDateFin())) {
                ContentValues roomUpdate = new ContentValues();
                roomUpdate.put("disponible", 0);
                db.update(TABLE_CHAMBRES, roomUpdate, "id = ?", new String[]{String.valueOf(chambreId)});
            }

            db.setTransactionSuccessful();
            return reservationId;
        } finally {
            db.endTransaction();
        }
    }

    public List<Service> getAllServices() {
        SQLiteDatabase db = getReadableDatabase();
        List<Service> services = new ArrayList<>();
        Cursor cursor = db.query(TABLE_SERVICES, null, null, null, null, null, "nom ASC");

        while (cursor.moveToNext()) {
            Service service = new Service();
            service.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
            service.setNom(cursor.getString(cursor.getColumnIndexOrThrow("nom")));
            service.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            service.setPrix(cursor.getDouble(cursor.getColumnIndexOrThrow("prix")));
            services.add(service);
        }
        cursor.close();
        return services;
    }

    public Map<Long, Integer> getServiceQuantitiesForReservation(long reservationId) {
        SQLiteDatabase db = getReadableDatabase();
        Map<Long, Integer> quantities = new HashMap<>();
        Cursor cursor = db.query(
                TABLE_CONSOMMATIONS,
                new String[]{"service_id", "quantite"},
                "reservation_id = ?",
                new String[]{String.valueOf(reservationId)},
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            quantities.put(
                    cursor.getLong(cursor.getColumnIndexOrThrow("service_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("quantite"))
            );
        }
        cursor.close();
        return quantities;
    }

    public int addServiceToReservation(long reservationId, long serviceId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor serviceCursor = db.query(
                    TABLE_SERVICES,
                    new String[]{"prix"},
                    "id = ?",
                    new String[]{String.valueOf(serviceId)},
                    null,
                    null,
                    null
            );
            if (!serviceCursor.moveToFirst()) {
                serviceCursor.close();
                return 0;
            }
            double prixUnitaire = serviceCursor.getDouble(serviceCursor.getColumnIndexOrThrow("prix"));
            serviceCursor.close();

            int updatedQuantity;
            Cursor cursor = db.query(
                    TABLE_CONSOMMATIONS,
                    new String[]{"id", "quantite"},
                    "reservation_id = ? AND service_id = ?",
                    new String[]{String.valueOf(reservationId), String.valueOf(serviceId)},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                long consommationId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                updatedQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantite")) + 1;
                ContentValues update = new ContentValues();
                update.put("quantite", updatedQuantity);
                update.put("date_consommation", getCurrentDate());
                db.update(TABLE_CONSOMMATIONS, update, "id = ?", new String[]{String.valueOf(consommationId)});
            } else {
                ContentValues insert = new ContentValues();
                insert.put("reservation_id", reservationId);
                insert.put("service_id", serviceId);
                insert.put("quantite", 1);
                insert.put("date_consommation", getCurrentDate());
                insert.put("prix_unitaire", prixUnitaire);
                db.insert(TABLE_CONSOMMATIONS, null, insert);
                updatedQuantity = 1;
            }
            cursor.close();

            refreshReservationTotal(db, reservationId);
            db.setTransactionSuccessful();
            return updatedQuantity;
        } finally {
            db.endTransaction();
        }
    }

    public double getReservationServiceTotal(long reservationId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(quantite * prix_unitaire), 0) FROM " + TABLE_CONSOMMATIONS +
                        " WHERE reservation_id = ?",
                new String[]{String.valueOf(reservationId)}
        );
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public ReservationInvoice getReservationInvoice(long reservationId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT r.id, r.client_id, r.chambre_id, r.date_debut, r.date_fin, r.nombre_personnes, " +
                        "r.statut, r.montant_total, r.date_reservation, " +
                        "c.nom, c.prenom, c.email, c.telephone, c.adresse, c.numero_passeport, c.date_naissance, " +
                        "ch.numero, ch.type, ch.prix_nuit, ch.capacite, ch.equipements, ch.disponible " +
                        "FROM " + TABLE_RESERVATIONS + " r " +
                        "JOIN " + TABLE_CLIENTS + " c ON c.id = r.client_id " +
                        "JOIN " + TABLE_CHAMBRES + " ch ON ch.id = r.chambre_id " +
                        "WHERE r.id = ?",
                new String[]{String.valueOf(reservationId)}
        );

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        Reservation reservation = new Reservation();
        reservation.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        reservation.setClientId(cursor.getLong(cursor.getColumnIndexOrThrow("client_id")));
        reservation.setChambreId(cursor.getLong(cursor.getColumnIndexOrThrow("chambre_id")));
        reservation.setDateDebut(cursor.getString(cursor.getColumnIndexOrThrow("date_debut")));
        reservation.setDateFin(cursor.getString(cursor.getColumnIndexOrThrow("date_fin")));
        reservation.setNombrePersonnes(cursor.getInt(cursor.getColumnIndexOrThrow("nombre_personnes")));
        reservation.setStatut(cursor.getString(cursor.getColumnIndexOrThrow("statut")));
        reservation.setMontantTotal(cursor.getDouble(cursor.getColumnIndexOrThrow("montant_total")));
        reservation.setDateReservation(cursor.getString(cursor.getColumnIndexOrThrow("date_reservation")));

        Client client = new Client();
        client.setId(reservation.getClientId());
        client.setNom(cursor.getString(cursor.getColumnIndexOrThrow("nom")));
        client.setPrenom(cursor.getString(cursor.getColumnIndexOrThrow("prenom")));
        client.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        client.setTelephone(cursor.getString(cursor.getColumnIndexOrThrow("telephone")));
        client.setAdresse(cursor.getString(cursor.getColumnIndexOrThrow("adresse")));
        client.setNumeroPasseport(cursor.getString(cursor.getColumnIndexOrThrow("numero_passeport")));
        client.setDateNaissance(cursor.getString(cursor.getColumnIndexOrThrow("date_naissance")));

        Chambre chambre = new Chambre();
        chambre.setId(reservation.getChambreId());
        chambre.setNumero(cursor.getString(cursor.getColumnIndexOrThrow("numero")));
        chambre.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
        chambre.setPrixNuit(cursor.getDouble(cursor.getColumnIndexOrThrow("prix_nuit")));
        chambre.setCapacite(cursor.getInt(cursor.getColumnIndexOrThrow("capacite")));
        chambre.setEquipements(cursor.getString(cursor.getColumnIndexOrThrow("equipements")));
        chambre.setDisponible(cursor.getInt(cursor.getColumnIndexOrThrow("disponible")) == 1);
        cursor.close();

        List<ConsommationService> consommations = getConsommationsForReservation(reservationId);
        int nights = countNights(reservation.getDateDebut(), reservation.getDateFin());
        double roomTotal = nights * chambre.getPrixNuit();
        double serviceTotal = getReservationServiceTotal(reservationId);

        ReservationInvoice invoice = new ReservationInvoice();
        invoice.reservation = reservation;
        invoice.client = client;
        invoice.chambre = chambre;
        invoice.consommations = consommations;
        invoice.nights = nights;
        invoice.roomTotal = roomTotal;
        invoice.serviceTotal = serviceTotal;
        invoice.grandTotal = roomTotal + serviceTotal;
        return invoice;
    }

    public List<ConsommationService> getConsommationsForReservation(long reservationId) {
        SQLiteDatabase db = getReadableDatabase();
        List<ConsommationService> items = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT cs.id, cs.reservation_id, cs.service_id, cs.quantite, cs.date_consommation, cs.prix_unitaire, " +
                        "s.nom FROM " + TABLE_CONSOMMATIONS + " cs " +
                        "JOIN " + TABLE_SERVICES + " s ON s.id = cs.service_id " +
                        "WHERE cs.reservation_id = ? ORDER BY s.nom ASC",
                new String[]{String.valueOf(reservationId)}
        );

        while (cursor.moveToNext()) {
            ConsommationService item = new ConsommationService();
            item.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
            item.setReservationId(cursor.getLong(cursor.getColumnIndexOrThrow("reservation_id")));
            item.setServiceId(cursor.getLong(cursor.getColumnIndexOrThrow("service_id")));
            item.setQuantite(cursor.getInt(cursor.getColumnIndexOrThrow("quantite")));
            item.setDateConsommation(cursor.getString(cursor.getColumnIndexOrThrow("date_consommation")));
            item.setPrixUnitaire(cursor.getDouble(cursor.getColumnIndexOrThrow("prix_unitaire")));
            item.setNomService(cursor.getString(cursor.getColumnIndexOrThrow("nom")));
            items.add(item);
        }
        cursor.close();
        return items;
    }

    public boolean closeStayTransaction(long reservationId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor cursor = db.query(
                    TABLE_RESERVATIONS,
                    new String[]{"chambre_id"},
                    "id = ?",
                    new String[]{String.valueOf(reservationId)},
                    null,
                    null,
                    null
            );
            if (!cursor.moveToFirst()) {
                cursor.close();
                return false;
            }
            long chambreId = cursor.getLong(cursor.getColumnIndexOrThrow("chambre_id"));
            cursor.close();

            refreshReservationTotal(db, reservationId);

            ContentValues reservationUpdate = new ContentValues();
            reservationUpdate.put("statut", STATUS_COMPLETED);
            db.update(TABLE_RESERVATIONS, reservationUpdate, "id = ?", new String[]{String.valueOf(reservationId)});

            ContentValues roomUpdate = new ContentValues();
            roomUpdate.put("disponible", 1);
            db.update(TABLE_CHAMBRES, roomUpdate, "id = ?", new String[]{String.valueOf(chambreId)});

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public int countNights(String dateDebut, String dateFin) {
        try {
            Date start = dateFormat.parse(dateDebut);
            Date end = dateFormat.parse(dateFin);
            if (start == null || end == null) {
                return 0;
            }
            long diffMillis = end.getTime() - start.getTime();
            int nights = (int) (diffMillis / (1000L * 60L * 60L * 24L));
            return Math.max(nights, 0);
        } catch (ParseException exception) {
            return 0;
        }
    }

    public String getCurrentDate() {
        return dateFormat.format(new Date());
    }

    private Client mapClient(Cursor cursor) {
        Client client = new Client();
        client.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        client.setNom(cursor.getString(cursor.getColumnIndexOrThrow("nom")));
        client.setPrenom(cursor.getString(cursor.getColumnIndexOrThrow("prenom")));
        client.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        client.setTelephone(cursor.getString(cursor.getColumnIndexOrThrow("telephone")));
        client.setAdresse(cursor.getString(cursor.getColumnIndexOrThrow("adresse")));
        client.setNumeroPasseport(cursor.getString(cursor.getColumnIndexOrThrow("numero_passeport")));
        client.setDateNaissance(cursor.getString(cursor.getColumnIndexOrThrow("date_naissance")));
        return client;
    }

    private Chambre mapChambre(Cursor cursor) {
        Chambre chambre = new Chambre();
        chambre.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        chambre.setNumero(cursor.getString(cursor.getColumnIndexOrThrow("numero")));
        chambre.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
        chambre.setPrixNuit(cursor.getDouble(cursor.getColumnIndexOrThrow("prix_nuit")));
        chambre.setCapacite(cursor.getInt(cursor.getColumnIndexOrThrow("capacite")));
        chambre.setEquipements(cursor.getString(cursor.getColumnIndexOrThrow("equipements")));
        chambre.setDisponible(cursor.getInt(cursor.getColumnIndexOrThrow("disponible")) == 1);
        return chambre;
    }

    private String resolveClientStatus(SQLiteDatabase db, long clientId) {
        String today = getCurrentDate();
        Cursor activeCursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_RESERVATIONS +
                        " WHERE client_id = ? AND statut = ? AND date_debut <= ? AND date_fin >= ? LIMIT 1",
                new String[]{String.valueOf(clientId), STATUS_CONFIRMED, today, today}
        );
        boolean isStaying = activeCursor.moveToFirst();
        activeCursor.close();
        if (isStaying) {
            return CLIENT_STATUS_STAYING;
        }

        Cursor historyCursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_RESERVATIONS + " WHERE client_id = ? LIMIT 1",
                new String[]{String.valueOf(clientId)}
        );
        boolean hasHistory = historyCursor.moveToFirst();
        historyCursor.close();
        return hasHistory ? CLIENT_STATUS_ARCHIVED : CLIENT_STATUS_ACTIVE;
    }

    private String resolveRoomStatus(SQLiteDatabase db, long chambreId, boolean disponible) {
        if (hasActiveReservationToday(db, chambreId)) {
            return FILTER_OCCUPIED;
        }
        if (!disponible) {
            return FILTER_MAINTENANCE;
        }
        return FILTER_AVAILABLE;
    }

    private boolean matchesFilter(Chambre chambre, String filter) {
        if (filter == null || FILTER_ALL.equals(filter)) {
            return true;
        }
        return filter.equals(chambre.getStatut());
    }

    private int getCount(SQLiteDatabase db, String query, String[] args) {
        Cursor cursor = db.rawQuery(query, args);
        int value = 0;
        if (cursor.moveToFirst()) {
            value = cursor.getInt(0);
        }
        cursor.close();
        return value;
    }

    private boolean hasActiveReservationToday(SQLiteDatabase db, long chambreId) {
        String today = getCurrentDate();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_RESERVATIONS +
                        " WHERE chambre_id = ? AND statut = ? AND date_debut <= ? AND date_fin >= ? LIMIT 1",
                new String[]{String.valueOf(chambreId), STATUS_CONFIRMED, today, today}
        );
        boolean hasActive = cursor.moveToFirst();
        cursor.close();
        return hasActive;
    }

    private boolean hasOverlappingReservation(SQLiteDatabase db, long chambreId, String startDate, String endDate) {
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_RESERVATIONS +
                        " WHERE chambre_id = ? AND statut = ? AND date_debut < ? AND date_fin > ? LIMIT 1",
                new String[]{String.valueOf(chambreId), STATUS_CONFIRMED, endDate, startDate}
        );
        boolean overlaps = cursor.moveToFirst();
        cursor.close();
        return overlaps;
    }

    private boolean isValidDateRange(String startDate, String endDate) {
        return countNights(startDate, endDate) > 0;
    }

    private boolean isStayActiveOnCurrentDate(String startDate, String endDate) {
        String today = getCurrentDate();
        return today.compareTo(startDate) >= 0 && today.compareTo(endDate) <= 0;
    }

    private void refreshReservationTotal(SQLiteDatabase db, long reservationId) {
        Cursor cursor = db.rawQuery(
                "SELECT r.date_debut, r.date_fin, c.prix_nuit FROM " + TABLE_RESERVATIONS + " r " +
                        "JOIN " + TABLE_CHAMBRES + " c ON c.id = r.chambre_id WHERE r.id = ?",
                new String[]{String.valueOf(reservationId)}
        );

        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        String dateDebut = cursor.getString(cursor.getColumnIndexOrThrow("date_debut"));
        String dateFin = cursor.getString(cursor.getColumnIndexOrThrow("date_fin"));
        double prixNuit = cursor.getDouble(cursor.getColumnIndexOrThrow("prix_nuit"));
        cursor.close();

        double roomTotal = countNights(dateDebut, dateFin) * prixNuit;
        double serviceTotal = getReservationServiceTotal(reservationId);

        ContentValues update = new ContentValues();
        update.put("montant_total", roomTotal + serviceTotal);
        db.update(TABLE_RESERVATIONS, update, "id = ?", new String[]{String.valueOf(reservationId)});
    }

    public static class DashboardStats {
        public int availableRooms;
        public int activeReservations;
        public int clients;
        public int checkoutsToday;
        public int occupiedRooms;
        public int totalRooms;
        public int occupationPercent;
    }

    public static class ReservationInvoice {
        public Reservation reservation;
        public Client client;
        public Chambre chambre;
        public List<ConsommationService> consommations;
        public int nights;
        public double roomTotal;
        public double serviceTotal;
        public double grandTotal;
    }
}
