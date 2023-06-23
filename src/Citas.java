import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;

public class Citas implements Comparable<Citas> {

    private String date;
    private String time;
    private int nCita;
    private String specialty;
    private String doctorName;

    public Citas (String date, String time, int nCita, String specialty, String doctorName) {
        this.date = date;
        this.time = time;
        this.nCita = nCita;
        this.specialty = specialty;
        this.doctorName = doctorName;
    }

    @Override
    public int compareTo(Citas other) {
        LocalDate date1 = LocalDate.parse(this.date);
        LocalDate date2 = LocalDate.parse(other.date);

        int dateComparison = date1.compareTo(date2);

        if (dateComparison != 0) {
            return dateComparison;
        } else {
            LocalTime time1 = LocalTime.parse(this.time);
            LocalTime time2 = LocalTime.parse(other.time);

            return time1.compareTo(time2);
        }
    }

    public String getDate() {
        return date;
    }
    public String getTime() {
        return time;
    }
    public String getSpecialty() {
        return specialty;
    }
    public int getnCita(){return nCita;}
    public String getDoctorName() {
        return doctorName;
    }
}
