import java.time.LocalDate;

public class Paciente extends Persona {
    private String contrasenia;
    public Paciente(String nif, String nombre, LocalDate fNac, String contrasenia) {
        super(nif, nombre, fNac);
        this.contrasenia = contrasenia;
    }
}
