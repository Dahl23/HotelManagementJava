package com.hotel.gestion.models;

public class ConsommationService {
    private long id;
    private long reservationId;
    private long serviceId;
    private int quantite;
    private String dateConsommation;
    private double prixUnitaire;
    private String nomService;

    public ConsommationService() {
    }

    public ConsommationService(long id, long reservationId, long serviceId, int quantite,
                               String dateConsommation, double prixUnitaire, String nomService) {
        this.id = id;
        this.reservationId = reservationId;
        this.serviceId = serviceId;
        this.quantite = quantite;
        this.dateConsommation = dateConsommation;
        this.prixUnitaire = prixUnitaire;
        this.nomService = nomService;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getDateConsommation() {
        return dateConsommation;
    }

    public void setDateConsommation(String dateConsommation) {
        this.dateConsommation = dateConsommation;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public String getNomService() {
        return nomService;
    }

    public void setNomService(String nomService) {
        this.nomService = nomService;
    }

    public double getSousTotal() {
        return quantite * prixUnitaire;
    }
}
