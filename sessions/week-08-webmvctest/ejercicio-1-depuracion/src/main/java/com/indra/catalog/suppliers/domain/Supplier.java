package com.indra.catalog.suppliers.domain;

public class Supplier {

    private String id;
    private String name;
    private String taxId;
    private String email;
    private String internalNotes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public void normalize() {
        if (this.name != null) {
            this.name = this.name.trim().toUpperCase();
        }
        if (this.taxId != null) {
            this.taxId = this.taxId.replace(".", "").trim();
        }
    }
}
