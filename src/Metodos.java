import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

public class Metodos {
    private static final Scanner scanner = new Scanner(System.in);
    private static String contrasenia = null;
    static String username = "root";
    static String passwordDB = "admin1234_";
    static String url = "jdbc:mysql://localhost/clinica";
    static Connection connection = null;

    private static LocalDate fechaCita;
    private static LocalDate dateAppointment;
    private static String nombrePaciente;
    private static String especialidadElije;
    private static String doctorName;

    public static void conectarBase() throws SQLException {
        connection = DriverManager.getConnection(url, username, passwordDB);
    }

    public static void MenuPrincipal () throws SQLException, IOException {
        int opcion;
        do {
            System.out.println("""
                    \nElija una de las siguientes opciones:
                    1. Iniciar sesion.
                    2. Registrarse.
                    3. Salir.""");

            opcion = scanner.nextInt();

            switch (opcion) {

                case 1 -> {
                    conectarBase();

                    if (AskForCredentials())
                    {
                        MenuSecundario();
                    }
                    else {
                        AskForCredentials();
                    }
                }
                case 2 -> {
                    conectarBase();
                    String nif;
                    do {
                        scanner.nextLine();
                        System.out.println("Introduzca su NIF (8 numeros seguidos de una letra):");
                        nif = scanner.nextLine();
                        if (nif.length() != 9) {
                            System.out.println("Por favor, inténtelo de nuevo.");
                        }
                    } while (nif.length() != 9);

                    System.out.println("Introduzca su nombre (Sera tambien su nombre de ususario):");
                    nombrePaciente = scanner.nextLine();
                    System.out.println("Introduzca su fecha de nacimiento (formato: YYYY-MM-DD)");
                    int anio = scanner.nextInt();
                    int mes = scanner.nextInt();
                    int dia = scanner.nextInt();
                    LocalDate fnac = LocalDate.of(anio, mes, dia);
                    scanner.nextLine();

                    do {
                        System.out.println("Introduzca una contraseña para su registro (debe ser >= 8 caracteres):");
                        contrasenia = scanner.nextLine();
                        if (contrasenia.length() < 8) {
                            System.out.println("Por favor, inténtelo de nuevo.");
                        }
                    } while (contrasenia.length() < 8);

                    String sql = "SELECT * FROM PACIENTE WHERE NIF_PAC = '" + nif + "'";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet rs = statement.executeQuery();
                    if (rs.next()) {
                        System.out.println("Ya hay un usuario con ese nif");
                    } else {
                        String sqlInsertPaciente = "INSERT INTO PACIENTE (NIF_PAC, NOM_PAC, FECH_NAC_PAC, PWD) VALUES (?, ?, ?, ?)";
                        PreparedStatement statement02 = connection.prepareStatement(sqlInsertPaciente);
                        statement02.setString(1, nif);
                        statement02.setString(2, nombrePaciente);
                        statement02.setDate(3, Date.valueOf(fnac));
                        statement02.setString(4, contrasenia);
                        statement02.executeUpdate();

                        System.out.println("\nBienvenido/a " + nombrePaciente + "!");
                        MenuSecundario();
                    }
                }
                case 3 -> {
                    return;
                }
            }
        } while(opcion != 1 && opcion != 2);
    }

    private static boolean AskForCredentials() throws SQLException {

        boolean exist;
        scanner.nextLine();
        do {
            System.out.println("\nIntroduzca su nombre (Usuario):");
            nombrePaciente = scanner.nextLine();
            System.out.println(nombrePaciente + ", introduzca su contrasenia:");
            contrasenia = scanner.nextLine();
            String sql = "SELECT * FROM PACIENTE WHERE NOM_PAC = ? AND PWD = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nombrePaciente);
            statement.setString(2, contrasenia);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                System.out.println("\nBienvenido/a " + nombrePaciente + "!");
                exist = true;
            } else {
                exist = false;
                System.out.println("Este usuario no exite!");
            }
        } while (!exist);
        return exist;
    }

    private static void MenuSecundario() throws SQLException, IOException {

        int opcion02;
        do {
            System.out.println("""
                                \nElija una de las siguientes opciones:\s
                                1. Darse de baja de la App.
                                2. Solicitar nueva cita.
                                3. Crear recordatorio de citas.
                                4. Volver al menu principal.""");
            opcion02 = scanner.nextInt();

            switch (opcion02) {

                case 1 -> {
                    conectarBase();
                    String sqlSelect = "SELECT * FROM PACIENTE WHERE PWD = '" + contrasenia + "'";
                    String sqlDelete = "DELETE FROM PACIENTE WHERE PWD = '" + contrasenia + "'";
                    PreparedStatement statement = connection.prepareStatement(sqlSelect);
                    ResultSet rs = statement.executeQuery();
                    if (rs.next()) {
                        String nifDelete = rs.getString(1);
                        PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
                        int resultSet = deleteStatement.executeUpdate();
                        if (resultSet > 0) {
                            System.out.println("El paciente con NIF: " + nifDelete + " ha sido borrado correctamente de la BBDD");
                            MenuSecundario();
                        }
                    }
                }
                case 2 -> {

                    //ESPECIALIDADES

                    Set<String> especialidadSet = new HashSet<>();
                    String sqlEspecialidad = "SELECT DISTINCT ESPECIALIDAD FROM MEDICOS";
                    PreparedStatement statementEspecialidad = connection.prepareStatement(sqlEspecialidad);
                    ResultSet rsEspecialidad = statementEspecialidad.executeQuery();

                    while (rsEspecialidad.next()) {
                        especialidadSet.add(rsEspecialidad.getString("ESPECIALIDAD"));
                    }

                    Scanner scanner = new Scanner(System.in);

                    do {
                        StringBuilder menu = new StringBuilder("\nElija entre las siguientes especialidades medicas:");
                        int i = 1;
                        for (String especialidad : especialidadSet) {
                            menu.append("\n").append(i).append(". ").append(especialidad);
                            i++;
                        }
                        System.out.println(menu);
                        especialidadElije = scanner.nextLine();

                        if (especialidadSet.contains(especialidadElije)) {
                            String sql = "SELECT NOM_MED, VALORACION_PAC FROM MEDICOS WHERE ESPECIALIDAD = ? ORDER BY VALORACION_PAC DESC";
                            PreparedStatement statement = connection.prepareStatement(sql);
                            statement.setString(1, especialidadElije);
                            ResultSet rs = statement.executeQuery();
                            if (rs.next()) {
                                System.out.println("La especialidad " + especialidadElije + " es valida.");
                            }
                        } else {
                            System.out.println("¡Esa no es una especialidad válida! Por favor, inténtelo de nuevo.");
                        }
                    } while (!especialidadSet.contains(especialidadElije));

                    //MEDICOS

                    Map<String, Integer> doctorMap = new HashMap<>();

                    String sql01 = "SELECT NOM_MED, VALORACION_PAC FROM MEDICOS WHERE ESPECIALIDAD = ? ORDER BY VALORACION_PAC DESC";
                    PreparedStatement statement01 = connection.prepareStatement(sql01);
                    statement01.setString(1, especialidadElije);
                    ResultSet rs01 = statement01.executeQuery();

                    while (rs01.next()) {
                        doctorMap.put(rs01.getString("NOM_MED"), rs01.getInt("VALORACION_PAC"));
                    }

                    do {
                        System.out.println("\nElija entre los medicos:");

                        System.out.println("Nombre\t\t\tPuntuacion");
                        for (Map.Entry<String, Integer> entry : doctorMap.entrySet()) {
                            String name = entry.getKey();
                            Integer number = entry.getValue();
                            System.out.println(name + "\t\t" + number);
                        }

                        doctorName = scanner.nextLine();

                        if (doctorMap.containsKey(doctorName)) {
                            System.out.println("Ha elegido el medico: " + doctorName);
                        } else {
                            System.out.println("¡Ese no es un medico válido! Por favor, inténtelo de nuevo.");
                        }

                    } while (!doctorMap.containsKey(doctorName));

                    if(AskForDate())
                    {
                        AskForHours(fechaCita);
                    }
                }
                case 3 -> {

                    File file = new File("Recordatorio Citas.txt");
                    try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file))) {
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        //borramos el contenido del fichero txt
                        writer.write("".getBytes());

                        String sqlAppoinment = "SELECT * FROM CITA WHERE NOM_CITA_PAC = ?";
                        PreparedStatement statementAppoinment = connection.prepareStatement(sqlAppoinment);
                        statementAppoinment.setString(1, nombrePaciente);
                        ResultSet rsAppoinment = statementAppoinment.executeQuery();

                        int numCitas = 0;

                        List<Citas> citasList = new ArrayList<>();

                        while (rsAppoinment.next()) {
                            numCitas++;
                            dateAppointment = rsAppoinment.getDate("FECHA_CITA").toLocalDate();
                            LocalTime time = rsAppoinment.getTime("HORA_CITA").toLocalTime();
                            String specialty = rsAppoinment.getString("ESPECIALIDAD_CITA");
                            String nameDoc = rsAppoinment.getString("NOM_CITA_MED");
                            Citas cita = new Citas(String.valueOf(dateAppointment), String.valueOf(time), numCitas, specialty, nameDoc);
                            citasList.add(cita);
                        }

                        Collections.sort(citasList);

                        // header text
                        String nombreTtx = "Nombre del Paciente: " + nombrePaciente;
                        String fechaTxt = "\nFecha de Creacion: " + LocalDate.now();
                        String numCitasTxt = "\nNumero de Citas del Paciente: " + numCitas;

                        writer.write(nombreTtx.getBytes());
                        writer.write(fechaTxt.getBytes());
                        writer.write(numCitasTxt.getBytes());

                        // body of the txt
                        String encabezadoTxt = "\n\nCitas del Paciente:";
                        writer.write(encabezadoTxt.getBytes());

                        // Comma
                        String comma = ", ";

                        // Cita{
                        String citaTxt = "\nCita{ ";

                        // Fecha Cita:
                        String fechaCitaString = "Fecha Cita: ";

                        // Hora Cita:
                        String horaCita = "Hora Cita: ";

                        // Numero de Cita:
                        String numeroCita;

                        // Especialidad:
                        String specialtyTxt = "Especialidad: ";

                        // Nombre Medico:
                        String nombreMedico = "Nombre Medico: ";

                        for (Citas citas : citasList) {
                            writer.write(citaTxt.getBytes());
                            writer.write(fechaCitaString.getBytes());
                            writer.write(citas.getDate().getBytes());
                            writer.write(comma.getBytes());
                            writer.write(horaCita.getBytes());
                            writer.write(citas.getTime().getBytes());
                            writer.write(comma.getBytes());
                            numeroCita =  "Numero de cita: " +numCitas;
                            writer.write(numeroCita.getBytes());
                            writer.write(comma.getBytes());
                            writer.write(specialtyTxt.getBytes());
                            writer.write(citas.getSpecialty().getBytes());
                            writer.write(comma.getBytes());
                            writer.write(nombreMedico.getBytes());
                            writer.write(citas.getDoctorName().getBytes());
                            writer.write("}".getBytes());
                            numCitas--;
                        }
                        System.out.println("Se ha creado el recordatorio de las citas.");
                        MenuSecundario();
                    }
                }
                case 4 -> MenuPrincipal();
            }
        }
        while (opcion02 < 0 || opcion02 > 5);
    }

    private static boolean AskForDate() {

        boolean valid = false;
        LocalDate today;

        do {
            System.out.println("\n" + nombrePaciente + ", introduzca la fecha en la que quiere ser citado (Formato: YYYY-MM-DD). Las fechas deben ser posteriores al día de hoy:");
            int anio = scanner.nextInt();
            int mes = scanner.nextInt();
            int dia = scanner.nextInt();
            fechaCita = LocalDate.of(anio, mes, dia);
            scanner.nextLine();

            today = LocalDate.now();

            if (fechaCita.isAfter(today))
            {
                System.out.println("La fecha es valida.");
                valid = true;
            }
            else
            {
                System.out.println("Fecha invalida. Intentelo de nuevo.");
            }
        } while (!valid);
        return valid;
    }

    private static void AskForHours(LocalDate fechaCita) throws SQLException, IOException {

        conectarBase();
        List<String> hoursList = new ArrayList<>();
        String enterHour;
        LocalTime time;

            System.out.println("Estas son las horas ocupadas para el dia " + fechaCita + ":");
            String sqlHours = "SELECT HORA_CITA FROM CITA WHERE FECHA_CITA = ?";
            PreparedStatement statement = connection.prepareStatement(sqlHours);
            statement.setDate(1, Date.valueOf(fechaCita));
            ResultSet rsHours = statement.executeQuery();
            while (rsHours.next()) {
                hoursList.add(rsHours.getString("HORA_CITA"));
            }

            for (String h : hoursList) {
                System.out.println(h);
            }

            do {

                int hour;
                do {
                    System.out.println("\nIntroduzca una hora (entre las 12 y las 14)");
                    hour = scanner.nextInt();
                } while (hour < 12 || hour > 14);

                int min;
                do {
                System.out.println("Introduzca el minuto (00, o 30)");
                min = scanner.nextInt();
                } while (min != 0 && min != 30);
                scanner.nextLine();

                time = LocalTime.of(hour, min);
                enterHour = String.valueOf(time);
                System.out.println("Cita{Especialidad: " + especialidadElije + ", con el medico " + doctorName + ", el dia " + fechaCita + " a la/s " + enterHour);

            } while (hoursList.contains(enterHour));

        System.out.println("Si quieres confirmar la cita, pulsa 's'.");
        String confirmation = scanner.nextLine();
        if (confirmation.equalsIgnoreCase("s")) {

            String sqlNumCita = "SELECT MAX(NUM_CITA) AS MAX_NUM_CITA FROM CITA";
            PreparedStatement statementNumCita = connection.prepareStatement(sqlNumCita);
            ResultSet rsNumCita = statementNumCita.executeQuery();
            int nextNumCita = 1;
            if (rsNumCita.next()) {
                nextNumCita = rsNumCita.getInt("MAX_NUM_CITA") + 1;
            }

            String sqlSelect = "SELECT * FROM PACIENTE WHERE PWD = '" + contrasenia + "'";
            PreparedStatement statementNif = connection.prepareStatement(sqlSelect);
            ResultSet rs = statementNif.executeQuery();
            String nifFinal = "";
            if (rs.next()) {
                nifFinal = rs.getString("NIF_PAC");
            }

            String sqlInsert = "INSERT INTO CITA (NUM_CITA, NIF_CITA_PAC, NOM_CITA_PAC, ESPECIALIDAD_CITA, NOM_CITA_MED, FECHA_CITA, HORA_CITA) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statementInsert = connection.prepareStatement(sqlInsert);
            statementInsert.setInt(1, nextNumCita);
            statementInsert.setString(2, nifFinal);
            statementInsert.setString(3, nombrePaciente);
            statementInsert.setString(4, especialidadElije);
            statementInsert.setString(5, doctorName);
            statementInsert.setDate(6, Date.valueOf(fechaCita));
            assert time != null;
            statementInsert.setTime(7, Time.valueOf(time));
            statementInsert.executeUpdate();
            System.out.println("La cita ha sido creada con exito.");
            MenuSecundario();
        }
    }
}

