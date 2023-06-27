import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

public class Metodos {
    private static final Scanner scanner = new Scanner(System.in);
    private static String passwordUser = null;
    static String username = "root";
    static String passwordDB = "admin1234_";
    static String url = "jdbc:mysql://localhost/clinica";
    static Connection connection = null;

    private static int counter = 0;
    private static String currentSpecialty;
    private static String currentDoctor;
    private static LocalTime currentTime;
    private static LocalDate currentDate;
    private static LocalDate newDate;
    private static LocalTime newTime;
    private static String patientName;
    private static String chooseSpecialty;
    private static String chooseDoctor;

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

                    if (AskForCredentials()) {
                        MenuSecundario();
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
                    patientName = scanner.nextLine();
                    System.out.println("Introduzca su fecha de nacimiento (formato: YYYY-MM-DD)");
                    int anio = scanner.nextInt();
                    int mes = scanner.nextInt();
                    int dia = scanner.nextInt();
                    LocalDate fnac = LocalDate.of(anio, mes, dia);
                    scanner.nextLine();

                    do {
                        System.out.println("Introduzca una contraseña para su registro (debe ser >= 8 caracteres):");
                        passwordUser = scanner.nextLine();
                        if (passwordUser.length() < 8) {
                            System.out.println("Por favor, inténtelo de nuevo.");
                        }
                    } while (passwordUser.length() < 8);

                    String sql = "SELECT * FROM PACIENTE WHERE NIF_PAC = '" + nif + "'";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet rs = statement.executeQuery();
                    if (rs.next()) {
                        System.out.println("Ya hay un usuario con ese nif");
                    } else {
                        String sqlInsertPaciente = "INSERT INTO PACIENTE (NIF_PAC, NOM_PAC, FECH_NAC_PAC, PWD) VALUES (?, ?, ?, ?)";
                        PreparedStatement statement02 = connection.prepareStatement(sqlInsertPaciente);
                        statement02.setString(1, nif);
                        statement02.setString(2, patientName);
                        statement02.setDate(3, Date.valueOf(fnac));
                        statement02.setString(4, passwordUser);
                        statement02.executeUpdate();

                        System.out.println("\nBienvenido/a " + patientName + "!");
                    }
                }
            }
        } while (opcion != 3);
    }

    private static boolean AskForCredentials() throws SQLException {

        boolean exist;
        scanner.nextLine();
        do {
            System.out.println("\nIntroduzca su nombre (Usuario):");
            patientName = scanner.nextLine();
            System.out.println(patientName + ", introduzca su contrasenia:");
            passwordUser = scanner.nextLine();
            String sql = "SELECT * FROM PACIENTE WHERE NOM_PAC = ? AND PWD = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, patientName);
            statement.setString(2, passwordUser);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                System.out.println("\nBienvenido/a " + patientName + "!");
                exist = true;
            } else {
                exist = false;
                System.out.println("Este usuario no exite!");
            }
        } while (!exist);
        return true;
    }

    private static void MenuSecundario() throws SQLException, IOException {

        int opcion02;
        do {
            System.out.println("""
                                \nElija una de las siguientes opciones:\s
                                1. Darse de baja de la App.
                                2. Acceder al apartado de citas.
                                3. Volver al menu principal.""");
            opcion02 = scanner.nextInt();

            switch (opcion02) {
                case 1 -> DeletePatient();
                case 2 -> MenuAppointments();
            }
        }
        while (opcion02 != 3);
    }

    private static void DeletePatient() throws SQLException {
        conectarBase();

        String sqlSelectUser = "SELECT NIF_PAC, PWD FROM PACIENTE WHERE PWD = '" + passwordUser + "'";
        String sqlDelete = "DELETE FROM PACIENTE WHERE PWD = '" + passwordUser + "'";
        PreparedStatement statement = connection.prepareStatement(sqlSelectUser);
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            String nifDelete = rs.getString(1);
            PreparedStatement deleteStatement = connection.prepareStatement(sqlDelete);
            int resultSet = deleteStatement.executeUpdate();
            if (resultSet > 0) {
                System.out.println("El paciente con NIF: " + nifDelete + " ha sido borrado correctamente de la BBDD");
            }
        }
    }

    private static void MenuAppointments() throws SQLException, IOException {

        int opcionCitas;

        do {

            System.out.println("""
                    \nIntroduce:
                    1. Para crear una nueva cita.
                    2. Para modificar una cita existente.
                    3. Para cancelar una cita.
                    4. Para crear el recordatorio de citas.
                    5. Para salir al menu anterior.""");
            opcionCitas = scanner.nextInt();

        } while (opcionCitas < 1 || opcionCitas > 5);

        switch (opcionCitas)
        {
            // create a new appointment

            case 1 ->
            {

                // ESPECIALIDAD

                ElijeEspecialidad();

                //MEDICOS

                ElijeMedico();

                if (AskForDate()) {
                    AskForHours(newDate);
                }

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

                    String sqlSelect = "SELECT * FROM PACIENTE WHERE PWD = ?";
                    PreparedStatement statementNif = connection.prepareStatement(sqlSelect);
                    statementNif.setString(1, passwordUser);
                    ResultSet rs = statementNif.executeQuery();
                    String nifFinal = "";

                    if (rs.next()) {
                        nifFinal = rs.getString("NIF_PAC");
                    }

                    String sqlInsert = "INSERT INTO CITA (NUM_CITA, NIF_CITA_PAC, NOM_CITA_PAC, ESPECIALIDAD_CITA, NOM_CITA_MED, FECHA_CITA, HORA_CITA) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement statementInsert = connection.prepareStatement(sqlInsert);
                    statementInsert.setInt(1, nextNumCita);
                    statementInsert.setString(2, nifFinal);
                    statementInsert.setString(3, patientName);
                    statementInsert.setString(4, chooseSpecialty);
                    statementInsert.setString(5, chooseDoctor);
                    statementInsert.setDate(6, Date.valueOf(newDate));
                    statementInsert.setTime(7, Time.valueOf(newTime));
                    statementInsert.executeUpdate();
                    System.out.println("La cita ha sido creada con exito.");
                }
            }

            // modify an appointment

        case 2 -> {

            boolean valid = false;

            System.out.println("\nA continuacion le pedire la fecha y la hora de la cita...");

            do {

                AskForDateAndTime();

                String sqlCheck = "SELECT * FROM CITA WHERE FECHA_CITA = ? AND HORA_CITA = ? AND NOM_CITA_PAC = ?";
                PreparedStatement statementCheck = connection.prepareStatement(sqlCheck);
                statementCheck.setDate(1, Date.valueOf(currentDate));
                statementCheck.setTime(2, Time.valueOf(currentTime));
                statementCheck.setString(3, patientName);
                ResultSet rsDataCitas = statementCheck.executeQuery();

                if (rsDataCitas.next())
                {
                    valid = true;
                    currentDate = rsDataCitas.getDate("FECHA_CITA").toLocalDate();
                    currentTime = rsDataCitas.getTime("HORA_CITA").toLocalTime();
                    currentSpecialty = rsDataCitas.getString("ESPECIALIDAD_CITA");
                    currentDoctor = rsDataCitas.getString("NOM_CITA_MED");
                    System.out.println("\n" + patientName + ", la cita para el dia " + currentDate + " a las " + currentTime + " para la especialidad de " + currentSpecialty + " con el medico " + currentDoctor + " ha sido encontrada en la base de datos.");
                    ModifyAppointments();
                }
                else {
                    System.out.println("Los datos introducidos no se corresponden con los de la base de datos, vuelva a intentarlo.");
                }

            } while(!valid);
        }

        // cancel an appointment

        case 3 -> {

            boolean delete;

            do {

                AskForDateAndTime();

                String sqlDelete = "DELETE FROM CITA WHERE FECHA_CITA = ? AND HORA_CITA = ? AND NOM_CITA_PAC = ?";
                PreparedStatement statementDelete = connection.prepareStatement(sqlDelete);
                statementDelete.setDate(1, Date.valueOf(currentDate));
                statementDelete.setTime(2, Time.valueOf(currentTime));
                statementDelete.setString(3, patientName);
                int deleteAppointment = statementDelete.executeUpdate();

                if (deleteAppointment > 0) {
                    delete = true;
                    System.out.println(patientName + ", su cita para el dia " + currentDate + " a la/s " + currentTime + " ha sido cancelada.");
                } else {
                    delete = false;
                    System.out.println("No se pudo cancelar la cita.");
                }
            } while (!delete);
        }

        // create the reminder

        case 4 -> {
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
                statementAppoinment.setString(1, patientName);
                ResultSet rsAppoinment = statementAppoinment.executeQuery();

                int numCitas = 0;

                List<Citas> citasList = new ArrayList<>();

                while (rsAppoinment.next()) {
                    numCitas++;
                    LocalDate dateAppointment = rsAppoinment.getDate("FECHA_CITA").toLocalDate();
                    LocalTime time = rsAppoinment.getTime("HORA_CITA").toLocalTime();
                    String specialty = rsAppoinment.getString("ESPECIALIDAD_CITA");
                    String nameDoc = rsAppoinment.getString("NOM_CITA_MED");
                    Citas cita = new Citas(String.valueOf(dateAppointment), String.valueOf(time), numCitas, specialty, nameDoc);
                    citasList.add(cita);
                }

                Collections.sort(citasList);

                // header text
                String nombreTtx = "Nombre del Paciente: " + patientName;
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
                    numeroCita = "Numero de cita: " + numCitas;
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
            }
        }
        case 5 -> MenuSecundario();
    }
}

    private static void AskForDateAndTime() {

        System.out.println("\n" +patientName+ ", introduzca la fecha de la cita (YYYY-MM-DD)");
        int anio = scanner.nextInt();
        int mes = scanner.nextInt();
        int dia = scanner.nextInt();
        scanner.nextLine();
        currentDate = LocalDate.of(anio, mes, dia);

        System.out.println("\nAhora introuduzca la hora (HH:mm): ");
        int hour = scanner.nextInt();
        int min = scanner.nextInt();
        scanner.nextLine();
        currentTime = LocalTime.of(hour, min);
    }

    private static void ModifyAppointments() throws SQLException, IOException {

        List<String> optionsList = new ArrayList<>();
        optionsList.add("fecha");
        optionsList.add("hora");
        optionsList.add("especialidad");
        optionsList.add("medico");

        String chooseChange;
        do {

            System.out.println("""
                            \nIntroduzca:
                            'Fecha' para cambiar la fecha de la cita.
                            'Hora' para cambiar la hora de la cita.
                            'Especialidad' para cambiar la especialidad de la cita.
                            'Medico' para cambiar el medico.
                            'Salir' para volver al menu anterior.""");
            chooseChange = scanner.nextLine().toUpperCase();

            switch (chooseChange) {

                case "FECHA" -> {

                    AskForDate();

                    String sqlUpdateFecha = "UPDATE CITA SET FECHA_CITA = ? WHERE FECHA_CITA = ? AND HORA_CITA = ? AND NOM_CITA_PAC = ?";
                    PreparedStatement statementChangeFecha = connection.prepareStatement(sqlUpdateFecha);
                    statementChangeFecha.setDate(1, Date.valueOf(newDate));
                    statementChangeFecha.setDate(2, Date.valueOf(currentDate));
                    statementChangeFecha.setTime(3, Time.valueOf(currentTime));
                    statementChangeFecha.setString(4, patientName);
                    int fe = statementChangeFecha.executeUpdate();

                    if (fe > 0) {
                        System.out.println("La fecha de la cita se ha cambiado con exito.");
                    } else {
                        System.out.println("No se ha podido cambiar la fecha de la cita.");
                    }
                }

                case "HORA" -> {

                    AskForHours(currentDate);
                    String sqlupdateHour = "UPDATE CITA SET HORA_CITA = ? WHERE FECHA_CITA = ? AND HORA_CITA = ? AND NOM_CITA_PAC = ?";
                    PreparedStatement statementChangeHour = connection.prepareStatement(sqlupdateHour);
                    statementChangeHour.setTime(1, Time.valueOf(newTime));
                    statementChangeHour.setDate(2, Date.valueOf(currentDate));
                    statementChangeHour.setTime(3, Time.valueOf(currentTime));
                    statementChangeHour.setString(4, patientName);
                    int ho = statementChangeHour.executeUpdate();

                    if (ho > 1) {
                        System.out.println("La hora de la cita se ha cambiado con exito.");
                    } else {
                        System.out.println("No se ha podido cambiar la hora de la cita.");
                    }
                }
                case "ESPECIALIDAD" -> {

                    int es = 0;
                     System.out.println("\nLa especialidad actual de la cita es: " + currentSpecialty);
                     ElijeEspecialidad();

                    if (chooseSpecialty.equals(currentSpecialty)) {
                        System.out.print("pero esta repetida, vuelva a intentarlo.");
                        ElijeEspecialidad();
                    } else {
                        counter++;
                        ElijeMedico();
                        counter--;
                        String sqlUpdateEspecialidad = "UPDATE CITA SET ESPECIALIDAD_CITA = ?, NOM_CITA_MED = ? WHERE FECHA_CITA = ? AND HORA_CITA = ? AND NOM_CITA_PAC = ?";
                        PreparedStatement statementChangeEspecialidad = connection.prepareStatement(sqlUpdateEspecialidad);
                        statementChangeEspecialidad.setString(1, chooseSpecialty);
                        statementChangeEspecialidad.setString(2, chooseDoctor);
                        statementChangeEspecialidad.setDate(3, Date.valueOf(currentDate));
                        statementChangeEspecialidad.setTime(4, Time.valueOf(currentTime));
                        statementChangeEspecialidad.setString(5, patientName);
                        es = statementChangeEspecialidad.executeUpdate();
                    }

                    if (es > 0) {
                        System.out.println("La especialidad de la cita se ha cambiado con exito a " + chooseSpecialty);
                    } else {
                        System.out.println("No se ha podido cambiar la especialidad de la cita.");
                    }
                }
                case "MEDICO" ->
                {
                    int me;
                    ElijeMedico();

                    if (currentDoctor.equals(chooseDoctor)) {
                        System.out.println("Ha introducido al mismo medico, vuelva a intentarlo.");
                        ElijeMedico();
                    }

                    String sqlUpdateMedico = "UPDATE CITA SET NOM_CITA_MED = ? WHERE FECHA_CITA = ? AND HORA_CITA = ? AND NOM_CITA_PAC = ?";
                    PreparedStatement statementChangeMedico = connection.prepareStatement(sqlUpdateMedico);
                    statementChangeMedico.setString(1, chooseDoctor);
                    statementChangeMedico.setDate(2, Date.valueOf(currentDate));
                    statementChangeMedico.setTime(3, Time.valueOf(currentTime));
                    statementChangeMedico.setString(4, patientName);
                    me = statementChangeMedico.executeUpdate();

                    if (me > 0) {
                        System.out.println("El medico de la cita se ha cambiado con exito.");
                    } else {
                        System.out.println("No se ha podido cambiar el medico de la cita.");
                    }
                }
            }
        } while (optionsList.contains(chooseChange.toUpperCase()));
        MenuAppointments();
    }

    private static void ElijeMedico() throws SQLException {

        Map<String, Integer> doctorMap = new HashMap<>();

        String sql01 = "SELECT NOM_MED, VALORACION_PAC FROM MEDICOS WHERE ESPECIALIDAD = ? ORDER BY VALORACION_PAC DESC";
        PreparedStatement statement01 = connection.prepareStatement(sql01);
        if (counter == 1) statement01.setString(1, chooseSpecialty);
        else statement01.setString(1, currentSpecialty);

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

                chooseDoctor = scanner.nextLine();

                if (doctorMap.containsKey(chooseDoctor)) {
                    System.out.println("Ha elegido el medico: " + chooseDoctor);
                } else {
                    System.out.println("¡Ese no es un medico válido! Por favor, inténtelo de nuevo.");
                }
            } while (!doctorMap.containsKey(chooseDoctor));
        }

    private static void ElijeEspecialidad() throws SQLException {
        List<String> especialidadList = new ArrayList<>();

        String sqlEspecialidad = "SELECT DISTINCT ESPECIALIDAD FROM MEDICOS";
        PreparedStatement statementEspecialidad = connection.prepareStatement(sqlEspecialidad);
        ResultSet rsEspecialidad = statementEspecialidad.executeQuery();

        while (rsEspecialidad.next()) {
            especialidadList.add(rsEspecialidad.getString("ESPECIALIDAD"));
        }

        int opcion;
        do {
            System.out.println("\nElija entre las siguientes especialidades medicas:");
            for (int i = 0; i < especialidadList.size(); i++) {
                System.out.println((i + 1) + ". " + especialidadList.get(i));
            }

            opcion = scanner.nextInt();
            scanner.nextLine();

            if (opcion > 0 && opcion <= especialidadList.size()) {
                chooseSpecialty = especialidadList.get(opcion - 1);
                System.out.println("\nLa especialidad " + chooseSpecialty + " es valida ");
                counter ++;
            } else {
                System.out.println("La opción no es válida. Por favor, inténtelo de nuevo.");
            }
        } while (opcion <= 0 || opcion > especialidadList.size());
    }

    private static boolean AskForDate() {

        boolean valid = false;
        LocalDate today;

        do {
            System.out.println("\n" + patientName + ", introduzca la fecha en la que quiere ser citado (Formato: YYYY-MM-DD). Las fechas deben ser posteriores al día de hoy:");
            int anio = scanner.nextInt();
            int mes = scanner.nextInt();
            int dia = scanner.nextInt();
            newDate = LocalDate.of(anio, mes, dia);
            scanner.nextLine();

            today = LocalDate.now();

            if (newDate.isAfter(today))
            {
                System.out.println("La fecha es valida.");
                valid = true;
            }
            else
            {
                System.out.println("Fecha invalida. Intentelo de nuevo.");
            }
        } while (!valid);
        return true;
    }

    private static void AskForHours(LocalDate fechaCita) throws SQLException {

        conectarBase();
        List<String> hoursList = new ArrayList<>();

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

                newTime = LocalTime.of(hour, min);

            } while (hoursList.contains(String.valueOf(newTime)));
    }
}