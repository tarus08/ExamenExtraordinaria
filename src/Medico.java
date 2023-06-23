import java.time.LocalDate;

public class Medico extends Persona{

    private String especialidad;
    private int anios;
    private int valoracion;

    public Medico(String nif, String nombre, LocalDate fNac, String especialidad, int anios, int valoracion) {
        super(nif, nombre, fNac);
        this.anios = anios;
        this.valoracion = valoracion;
        this.especialidad = especialidad;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public int getAnios() {
        return anios;
    }

    public void setAnios(int anios) {
        this.anios = anios;
    }

    public int getValoracion() {
        return valoracion;
    }

    public void setValoracion(int valoracion) {
        this.valoracion = valoracion;
    }
}
