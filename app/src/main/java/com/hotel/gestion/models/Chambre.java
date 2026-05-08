package com.hotel.gestion.models;

public class Chambre {
    private long id;
    private String numero;
    private String type;
    private double prixNuit;
    private int capacite;
    private String equipements;
    private boolean disponible;
    private String statut;

    public Chambre() {
    }

    public Chambre(long id, String numero, String type, double prixNuit, int capacite,
                   String equipements, boolean disponible, String statut) {
        this.id = id;
        this.numero = numero;
        this.type = type;
        this.prixNuit = prixNuit;
        this.capacite = capacite;
        this.equipements = equipements;
        this.disponible = disponible;
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

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
