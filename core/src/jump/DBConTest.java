package jump;
import java.sql.*;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.*;
public class DBConTest {
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
}
