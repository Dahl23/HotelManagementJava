package com.hotel.gestion.models;

public class Chambre {
    public static final String STATUS_LIBRE = "libre";
    public static final String STATUS_RESERVEE = "reservee";
    public static final String STATUS_OCCUPEE = "occupee";
    public static final String STATUS_MAINTENANCE = "maintenance";

    private long id;
    private String numero;
    private String type;
    private double prixNuit;
    private int capacite;
    private String equipements;
    private String statut;

    public Chambre() {
    }

    public Chambre(long id, String numero, String type, double prixNuit, int capacite,
                   String equipements, String statut) {
        this.id = id;
        this.numero = numero;
        this.type = type;
        this.prixNuit = prixNuit;
        this.capacite = capacite;
        this.equipements = equipements;
        this.statut = statut;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrixNuit() {
        return prixNuit;
    }

    public void setPrixNuit(double prixNuit) {
        this.prixNuit = prixNuit;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getEquipements() {
        return equipements;
    }

    public void setEquipements(String equipements) {
        this.equipements = equipements;
    }

    @Deprecated
    public boolean isDisponible() {
        return STATUS_LIBRE.equals(statut);
    }

    @Deprecated
    public void setDisponible(boolean disponible) {
        this.statut = disponible ? STATUS_LIBRE : STATUS_MAINTENANCE;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
