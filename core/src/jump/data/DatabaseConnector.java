package jump.data;
import java.sql.*;
import java.time.LocalDateTime;

import jump.WorldMisc;
import jump.actors.BotActor;
import jump.evolutionaryAlgorithm.Genotype;
import jump.evolutionaryAlgorithm.Population;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.*;
public abstract class DatabaseConnector {
    private static Connection db_connection = null;
    private final static String sql_save_bot_str =
            "INSERT INTO agents (generation, level, score, fitness, NN_parameters, EA_parameters, created_at, NN_weights) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static PreparedStatement pstmt_save_bot = null;
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
            pstmt_save_bot = db_connection.prepareStatement(sql_save_bot_str);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    public static void saveGeneration(Population population, int generation, int eaType, int nnType){
        LocalDateTime lDateTime= java.time.LocalDateTime.now().withNano(0);
        String dateString = lDateTime.toString();
        try {
            for (Genotype gene : population.genomes) {
                BotActor bot = gene.getBot();
                // (generation, level, score, fitness, NN_parameters, EA_parameters, created_at, NN_weights)
                pstmt_save_bot.setInt(1, generation);
                pstmt_save_bot.setInt(2, WorldMisc.level);
                pstmt_save_bot.setFloat(3, bot.getScore());
                pstmt_save_bot.setFloat(4, gene.getFitness());
                pstmt_save_bot.setInt(5, eaType);
                pstmt_save_bot.setInt(6, nnType);
                pstmt_save_bot.setString(7, dateString);
                pstmt_save_bot.setString(8, bot.getNeuralNetwork().flatten().weightsToString());
                pstmt_save_bot.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    enum ParameterType{
        EA,
        NN
    }
    public static ResultSet loadParametersByID(int id, ParameterType type) {
        String query = "SELECT * FROM " + type.toString() + "_parameters WHERE id = ?";
        try {
            PreparedStatement statement = db_connection.prepareStatement(query);
            statement.setInt(1, id);
            return statement.executeQuery();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }
}
