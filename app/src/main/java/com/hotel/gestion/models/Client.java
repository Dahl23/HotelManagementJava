package com.hotel.gestion.models;

public class Client {
    private long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String numeroCarteIdentite;
    private String dateNaissance;
    private String statut;

    public Client() {
    }

    public Client(long id, String nom, String prenom, String email, String telephone,
                  String adresse, String numeroCarteIdentite, String dateNaissance, String statut) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.numeroCarteIdentite = numeroCarteIdentite;
        this.dateNaissance = dateNaissance;
        this.statut = statut;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNumeroCarteIdentite() {
        return numeroCarteIdentite;
    }

    public void setNumeroCarteIdentite(String numeroCarteIdentite) {
        this.numeroCarteIdentite = numeroCarteIdentite;
    }

    @Deprecated
    public String getNumeroPasseport() {
        return numeroCarteIdentite;
    }

    @Deprecated
    public void setNumeroPasseport(String numeroPasseport) {
        this.numeroCarteIdentite = numeroPasseport;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNomComplet() {
        return (prenom == null ? "" : prenom.trim()) + " " + (nom == null ? "" : nom.trim());
    }

    public String getInitiales() {
        String p = prenom != null && !prenom.isBlank() ? prenom.substring(0, 1).toUpperCase() : "";
        String n = nom != null && !nom.isBlank() ? nom.substring(0, 1).toUpperCase() : "";
        String initials = p + n;
        return initials.isBlank() ? "CL" : initials;
    }
}
