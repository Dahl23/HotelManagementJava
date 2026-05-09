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
    public static final int DB_VERSION = 4;

    public static final long RESULT_ROOM_UNAVAILABLE = -10L;
    public static final long RESULT_INVALID_DATES = -11L;
    public static final long RESULT_CAPACITY_EXCEEDED = -12L;
    public static final long RESULT_ROOM_MAINTENANCE = -13L;
    public static final long RESULT_UNKNOWN_ERROR = -14L;

    public static final String FILTER_ALL = "ALL";
    public static final String FILTER_AVAILABLE = Chambre.STATUS_LIBRE;
    public static final String FILTER_RESERVED = Chambre.STATUS_RESERVEE;
    public static final String FILTER_OCCUPIED = Chambre.STATUS_OCCUPEE;
    public static final String FILTER_MAINTENANCE = Chambre.STATUS_MAINTENANCE;

    private static final String RESERVATION_STATUS_CONFIRMED = "confirmee";
    private static final String RESERVATION_STATUS_COMPLETED = "terminee";
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
                "numero_carte_identite TEXT NOT NULL," +
                "date_naissance TEXT NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_CHAMBRES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "numero TEXT NOT NULL UNIQUE," +
                "type TEXT NOT NULL," +
                "prix_nuit REAL NOT NULL," +
                "capacite INTEGER NOT NULL," +
                "equipements TEXT," +
                "statut TEXT NOT NULL DEFAULT '" + Chambre.STATUS_LIBRE + "'" +
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
        insertRoomSeed(db, "101", "Simple", 65000, 1, "Wi-Fi, TV, Douche", Chambre.STATUS_LIBRE);
        insertRoomSeed(db, "102", "Double", 90000, 2, "Wi-Fi, TV, Balcon", Chambre.STATUS_LIBRE);
        insertRoomSeed(db, "103", "Suite", 150000, 4, "Wi-Fi, TV, Salon, Jacuzzi", Chambre.STATUS_LIBRE);
        insertRoomSeed(db, "201", "Simple", 70000, 1, "Wi-Fi, TV, Bureau", Chambre.STATUS_LIBRE);
        insertRoomSeed(db, "202", "Double", 95000, 2, "Wi-Fi, TV, Mini-bar", Chambre.STATUS_LIBRE);
        insertRoomSeed(db, "203", "Suite", 165000, 4, "Wi-Fi, TV, Salon, Vue ville", Chambre.STATUS_LIBRE);
        insertRoomSeed(db, "301", "Double", 100000, 3, "Wi-Fi, TV, Balcon, Coffre", Chambre.STATUS_LIBRE);
        insertRoomSeed(db, "305", "Suite", 180000, 4, "Jacuzzi, Terrasse, Vue panoramique", Chambre.STATUS_MAINTENANCE);

        insertServiceSeed(db, "Petit-dejeuner", "Buffet continental servi en chambre ou au restaurant", 12000);
        insertServiceSeed(db, "Blanchisserie", "Nettoyage et repassage express", 9000);
        insertServiceSeed(db, "Navette aeroport", "Transfert simple depuis ou vers l aeroport", 25000);
        insertServiceSeed(db, "Spa", "Acces spa et massage relaxant", 40000);
        insertServiceSeed(db, "Diner gastronomique", "Menu complet du chef", 30000);
    }

    private void insertRoomSeed(SQLiteDatabase db, String numero, String type, double prixNuit,
                                int capacite, String equipements, String statut) {
        ContentValues values = new ContentValues();
        values.put("numero", numero);
        values.put("type", type);
        values.put("prix_nuit", prixNuit);
        values.put("capacite", capacite);
        values.put("equipements", equipements);
        values.put("statut", normalizeRoomStatus(statut));
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
        values.put("nom", safeText(client.getNom()));
        values.put("prenom", safeText(client.getPrenom()));
        values.put("email", safeText(client.getEmail()));
        values.put("telephone", safeText(client.getTelephone()));
        values.put("adresse", safeText(client.getAdresse()));
        values.put("numero_carte_identite", safeText(client.getNumeroCarteIdentite()));
        values.put("date_naissance", safeText(client.getDateNaissance()));
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
                    "nom LIKE ? OR prenom LIKE ? OR telephone LIKE ? OR numero_carte_identite LIKE ?",
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
            values.put("numero", safeText(chambre.getNumero()));
            values.put("type", safeText(chambre.getType()));
            values.put("prix_nuit", chambre.getPrixNuit());
            values.put("capacite", chambre.getCapacite());
            values.put("equipements", safeText(chambre.getEquipements()));
            values.put("statut", normalizeRoomStatus(chambre.getStatut()));
            return db.insert(TABLE_CHAMBRES, null, values);
        } catch (Exception exception) {
            return -1L;
        }
    }

    public List<Chambre> getChambres(String statusFilter) {
        return getChambres(statusFilter, null, null, null);
    }

    public List<Chambre> getChambres(String statusFilter, String typeFilter, Double maxPrice, Integer minCapacity) {
        SQLiteDatabase db = getWritableDatabase();
        synchronizeRoomStatuses(db);

        List<Chambre> chambres = new ArrayList<>();
        Cursor cursor = db.query(TABLE_CHAMBRES, null, null, null, null, null, "numero ASC");

        while (cursor.moveToNext()) {
            Chambre chambre = mapChambre(cursor);
            if (matchesRoomFilters(chambre, statusFilter, typeFilter, maxPrice, minCapacity)) {
                chambres.add(chambre);
            }
        }
        cursor.close();
        return chambres;
    }

    public List<Chambre> getReservableChambres() {
        return getReservableChambres(null, null, null);
    }

    public List<Chambre> getReservableChambres(String typeFilter, Double maxPrice, Integer minCapacity) {
        return getChambres(FILTER_AVAILABLE, typeFilter, maxPrice, minCapacity);
    }

    public List<Client> getSelectableClients() {
        return getAllClients();
    }

    public DashboardStats getDashboardStats() {
        SQLiteDatabase db = getWritableDatabase();
        synchronizeRoomStatuses(db);

        DashboardStats stats = new DashboardStats();
        String today = getCurrentDate();

        stats.totalRooms = getCount(db, "SELECT COUNT(*) FROM " + TABLE_CHAMBRES, null);
        stats.availableRooms = getCount(
                db,
                "SELECT COUNT(*) FROM " + TABLE_CHAMBRES + " WHERE statut = ?",
                new String[]{Chambre.STATUS_LIBRE}
        );
        stats.occupiedRooms = getCount(
                db,
                "SELECT COUNT(*) FROM " + TABLE_CHAMBRES + " WHERE statut = ?",
                new String[]{Chambre.STATUS_OCCUPEE}
        );
        stats.clients = getCount(db, "SELECT COUNT(*) FROM " + TABLE_CLIENTS, null);
        stats.activeReservations = getCount(
                db,
                "SELECT COUNT(*) FROM " + TABLE_RESERVATIONS +
                        " WHERE statut = ? AND date_fin > ?",
                new String[]{RESERVATION_STATUS_CONFIRMED, today}
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
            synchronizeRoomStatuses(db);

            if (!isValidDateRange(reservation.getDateDebut(), reservation.getDateFin())) {
                return RESULT_INVALID_DATES;
            }

            Cursor roomCursor = db.query(
                    TABLE_CHAMBRES,
                    new String[]{"id", "statut", "capacite", "prix_nuit"},
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

            long chambreId = roomCursor.getLong(roomCursor.getColumnIndexOrThrow("id"));
            String roomStatus = normalizeRoomStatus(roomCursor.getString(roomCursor.getColumnIndexOrThrow("statut")));
            int capacite = roomCursor.getInt(roomCursor.getColumnIndexOrThrow("capacite"));
            double prixNuit = roomCursor.getDouble(roomCursor.getColumnIndexOrThrow("prix_nuit"));
            roomCursor.close();

            if (Chambre.STATUS_MAINTENANCE.equals(roomStatus)) {
                return RESULT_ROOM_MAINTENANCE;
            }
            if (!Chambre.STATUS_LIBRE.equals(roomStatus)) {
                return RESULT_ROOM_UNAVAILABLE;
            }
            if (reservation.getNombrePersonnes() > capacite) {
                return RESULT_CAPACITY_EXCEEDED;
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
            values.put("statut", RESERVATION_STATUS_CONFIRMED);
            values.put("montant_total", roomTotal);
            values.put("date_reservation", getCurrentDate());

            long reservationId = db.insert(TABLE_RESERVATIONS, null, values);
            if (reservationId <= 0) {
                return RESULT_UNKNOWN_ERROR;
            }

            ContentValues roomUpdate = new ContentValues();
            roomUpdate.put("statut", isStayStarted(reservation.getDateDebut()) ? Chambre.STATUS_OCCUPEE : Chambre.STATUS_RESERVEE);
            db.update(TABLE_CHAMBRES, roomUpdate, "id = ?", new String[]{String.valueOf(chambreId)});

            db.setTransactionSuccessful();
            return reservationId;
        } catch (Exception exception) {
            return RESULT_UNKNOWN_ERROR;
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
        SQLiteDatabase db = getWritableDatabase();
        synchronizeRoomStatuses(db);

        Cursor cursor = db.rawQuery(
                "SELECT r.id, r.client_id, r.chambre_id, r.date_debut, r.date_fin, r.nombre_personnes, " +
                        "r.statut, r.montant_total, r.date_reservation, " +
                        "c.nom, c.prenom, c.email, c.telephone, c.adresse, c.numero_carte_identite, c.date_naissance, " +
                        "ch.numero, ch.type, ch.prix_nuit, ch.capacite, ch.equipements, ch.statut " +
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
        client.setNumeroCarteIdentite(cursor.getString(cursor.getColumnIndexOrThrow("numero_carte_identite")));
        client.setDateNaissance(cursor.getString(cursor.getColumnIndexOrThrow("date_naissance")));

        Chambre chambre = new Chambre();
        chambre.setId(reservation.getChambreId());
        chambre.setNumero(cursor.getString(cursor.getColumnIndexOrThrow("numero")));
        chambre.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
        chambre.setPrixNuit(cursor.getDouble(cursor.getColumnIndexOrThrow("prix_nuit")));
        chambre.setCapacite(cursor.getInt(cursor.getColumnIndexOrThrow("capacite")));
        chambre.setEquipements(cursor.getString(cursor.getColumnIndexOrThrow("equipements")));
        chambre.setStatut(normalizeRoomStatus(cursor.getString(cursor.getColumnIndexOrThrow("statut"))));
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
            reservationUpdate.put("statut", RESERVATION_STATUS_COMPLETED);
            db.update(TABLE_RESERVATIONS, reservationUpdate, "id = ?", new String[]{String.valueOf(reservationId)});

            ContentValues roomUpdate = new ContentValues();
            roomUpdate.put("statut", Chambre.STATUS_LIBRE);
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
        client.setNumeroCarteIdentite(cursor.getString(cursor.getColumnIndexOrThrow("numero_carte_identite")));
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
        chambre.setStatut(normalizeRoomStatus(cursor.getString(cursor.getColumnIndexOrThrow("statut"))));
        return chambre;
    }

    private String resolveClientStatus(SQLiteDatabase db, long clientId) {
        String today = getCurrentDate();
        Cursor activeCursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_RESERVATIONS +
                        " WHERE client_id = ? AND statut = ? AND date_debut <= ? AND date_fin > ? LIMIT 1",
                new String[]{String.valueOf(clientId), RESERVATION_STATUS_CONFIRMED, today, today}
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

    private boolean matchesRoomFilters(Chambre chambre, String statusFilter, String typeFilter,
                                       Double maxPrice, Integer minCapacity) {
        if (statusFilter != null && !FILTER_ALL.equals(statusFilter)
                && !normalizeRoomStatus(statusFilter).equals(chambre.getStatut())) {
            return false;
        }

        if (typeFilter != null && !typeFilter.trim().isEmpty() && !"TOUS".equalsIgnoreCase(typeFilter.trim())
                && !typeFilter.equalsIgnoreCase(chambre.getType())) {
            return false;
        }

        if (maxPrice != null && chambre.getPrixNuit() > maxPrice) {
            return false;
        }

        return minCapacity == null || chambre.getCapacite() >= minCapacity;
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

    private boolean hasOverlappingReservation(SQLiteDatabase db, long chambreId, String startDate, String endDate) {
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_RESERVATIONS +
                        " WHERE chambre_id = ? AND statut = ? AND date_debut < ? AND date_fin > ? LIMIT 1",
                new String[]{String.valueOf(chambreId), RESERVATION_STATUS_CONFIRMED, endDate, startDate}
        );
        boolean overlaps = cursor.moveToFirst();
        cursor.close();
        return overlaps;
    }

    private boolean isValidDateRange(String startDate, String endDate) {
        return countNights(startDate, endDate) > 0;
    }

    private boolean isStayStarted(String startDate) {
        return getCurrentDate().compareTo(startDate) >= 0;
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

    private void synchronizeRoomStatuses(SQLiteDatabase db) {
        List<RoomStatusUpdate> updates = new ArrayList<>();
        Cursor cursor = db.query(TABLE_CHAMBRES, new String[]{"id", "statut"}, null, null, null, null, null);

        while (cursor.moveToNext()) {
            long roomId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String currentStatus = normalizeRoomStatus(cursor.getString(cursor.getColumnIndexOrThrow("statut")));
            String nextStatus = currentStatus;

            if (!Chambre.STATUS_MAINTENANCE.equals(currentStatus)) {
                if (hasCurrentConfirmedReservation(db, roomId)) {
                    nextStatus = Chambre.STATUS_OCCUPEE;
                } else if (hasFutureConfirmedReservation(db, roomId)) {
                    nextStatus = Chambre.STATUS_RESERVEE;
                } else {
                    nextStatus = Chambre.STATUS_LIBRE;
                }
            }

            if (!nextStatus.equals(currentStatus)) {
                updates.add(new RoomStatusUpdate(roomId, nextStatus));
            }
        }
        cursor.close();

        for (RoomStatusUpdate update : updates) {
            ContentValues values = new ContentValues();
            values.put("statut", update.status);
            db.update(TABLE_CHAMBRES, values, "id = ?", new String[]{String.valueOf(update.roomId)});
        }
    }

    private boolean hasCurrentConfirmedReservation(SQLiteDatabase db, long chambreId) {
        String today = getCurrentDate();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_RESERVATIONS +
                        " WHERE chambre_id = ? AND statut = ? AND date_debut <= ? AND date_fin > ? LIMIT 1",
                new String[]{String.valueOf(chambreId), RESERVATION_STATUS_CONFIRMED, today, today}
        );
        boolean hasCurrent = cursor.moveToFirst();
        cursor.close();
        return hasCurrent;
    }

    private boolean hasFutureConfirmedReservation(SQLiteDatabase db, long chambreId) {
        String today = getCurrentDate();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_RESERVATIONS +
                        " WHERE chambre_id = ? AND statut = ? AND date_debut > ? LIMIT 1",
                new String[]{String.valueOf(chambreId), RESERVATION_STATUS_CONFIRMED, today}
        );
        boolean hasFuture = cursor.moveToFirst();
        cursor.close();
        return hasFuture;
    }

    private String normalizeRoomStatus(String status) {
        if (status == null) {
            return Chambre.STATUS_LIBRE;
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if (Chambre.STATUS_RESERVEE.equals(normalized)
                || Chambre.STATUS_OCCUPEE.equals(normalized)
                || Chambre.STATUS_MAINTENANCE.equals(normalized)) {
            return normalized;
        }
        return Chambre.STATUS_LIBRE;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private static class RoomStatusUpdate {
        final long roomId;
        final String status;

        RoomStatusUpdate(long roomId, String status) {
            this.roomId = roomId;
            this.status = status;
        }
    }

    public static class DashboardStats {
        public int availableRooms;
        public int activeReservations;
        public int clients;
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
