package it.comune.biblioteca.enums;

public enum ExceptionCodeEnum {

    G_000("G_000", "Qualcosa è andato storto. Se l'errore persiste contattare l'amministratore"),
    G_001("G_001", "Application Error"),
    G_002("G_002", "Credenziali non valide"),
    G_003("G_003", "Utente non autorizzato"),
    C_001("C_001", "Catrgory already exists"),
    C_002("C_002", "Impossibile cancellare la categoria: la categoria contiene libri"),
    B_001("B_001", "Il seriale esiste già"),
    B_002("B_002", "ISBN esiste già"),
    B_003("B_003", "Serial Code is mandatory"),
    B_004("B_004", "Serial Code does not match the pattern"),
    B_005("B_005", "Book title is mandatory");

    private final String code;
    private final String description;

    ExceptionCodeEnum(String code, String description) {
	this.code = code;
	this.description = description;
    }

    public String getCode() {
	return code;
    }

    public String getDescription() {
	return description;
    }

    public String toString() {
	return code + ": " + description;
    }
    }
