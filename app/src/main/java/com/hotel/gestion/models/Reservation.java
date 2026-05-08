package com.hotel.gestion.models;

public class Reservation {
    private long id;
    private long clientId;
    private long chambreId;
    private String dateDebut;
    private String dateFin;
    private int nombrePersonnes;
    private String statut;
    private double montantTotal;
    private String dateReservation;

    public Reservation() {
    }

    public Reservation(long id, long clientId, long chambreId, String dateDebut, String dateFin,
                       int nombrePersonnes, String statut, double montantTotal, String dateReservation) {
        this.id = id;
        this.clientId = clientId;
        this.chambreId = chambreId;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nombrePersonnes = nombrePersonnes;
        this.statut = statut;
        this.montantTotal = montantTotal;
        this.dateReservation = dateReservation;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public long getChambreId() {
        return chambreId;
    }

    public void setChambreId(long chambreId) {
        this.chambreId = chambreId;
    }

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
    }

    public int getNombrePersonnes() {
        return nombrePersonnes;
    }

    public void setNombrePersonnes(int nombrePersonnes) {
        this.nombrePersonnes = nombrePersonnes;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public String getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(String dateReservation) {
        this.dateReservation = dateReservation;
    }
}
