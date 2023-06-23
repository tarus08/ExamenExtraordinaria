import java.time.LocalDate;

public class Persona {
    private String nif;
    private String nombre;
    private LocalDate fNac;

    public Persona(String nif, String nombre, LocalDate fNac) {
        this.nif = nif;
        this.nombre = nombre;
        this.fNac = fNac;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getfNac() {
        return fNac;
    }

    public void setfNac(LocalDate fNac) {
        this.fNac = fNac;
    }
}
