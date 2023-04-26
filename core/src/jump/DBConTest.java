package jump;
import java.sql.*;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jump.actors.BotActor;
import jump.geneticAlgorithm.Genotype;
import jump.geneticAlgorithm.Population;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.*;
public abstract class DBConTest {
    private static Connection db_connection = null;
    private static String sql = "INSERT INTO bot_info (date, generation, fitness, neuralnetwork) VALUES (?, ?, ?, ?)";
    private static PreparedStatement pstmt = null;
    public static void main( String args[] ) {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:jump.db");
            System.out.println("Connection to SQLite has been established.");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        jooqExample();
    }

    private static void jooqExample(){
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:jump.db");
            DSLContext context = DSL.using(connection, SQLDialect.SQLITE);
            Result<Record> result = context.select().from("your_table").fetch();
            for (Record r : result) {
                System.out.println(r);
            }
//            Record record = context.newRecord("your_table");
//
//            record.set().setDate(Date.valueOf("2023-04-24")); // Set the date
//            record.setText("Some text"); // Set the text
//
//            // Insert the record into the database
//            record.store();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    public static void init( ) {
        try {
            Class.forName("org.sqlite.JDBC");
            db_connection = DriverManager.getConnection("jdbc:sqlite:jump.db");
            System.out.println("Connection to SQLite has been established.");
            pstmt = db_connection.prepareStatement(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    public static void saveGeneration(Population population, int generation){
        Date date = Date.valueOf(java.time.LocalDate.now());
        LocalDate lDate= java.time.LocalDate.now();
        LocalDateTime lDateTime= java.time.LocalDateTime.now().withNano(0);
        String dateString = lDateTime.toString();
        try {
            for (Genotype gene : population.genomes) {
                BotActor bot = gene.getBot();
                pstmt.setString(1, dateString);
                pstmt.setInt(2, generation);
                pstmt.setFloat(3, gene.getFitness());
                pstmt.setString(4, bot.getNeuralNetwork().flatten().toString());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
