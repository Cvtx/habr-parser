import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite database support.
 */
public class SQLiteDB {
    public static Connection connection;

    public static void conn() throws ClassNotFoundException, SQLException {
        connection = null;
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:articles.db");
    }

    public static void createDB() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE if not exists \"Articles\" (\n" +
                "\t\"ID\"\tINTEGER,\n" +
                "\t\"Title\"\tTEXT NOT NULL,\n" +
                "\t\"Author\"\tTEXT NOT NULL,\n" +
                "\t\"URL\"\tTEXT NOT NULL,\n" +
                "\t\"Text\"\tTEXT,\n" +
                "\t\"Tags\"\tTEXT,\n" +
                "\t\"Date\"\tTEXT,\n" +
                "\t\"DateTime\"\tTEXT,\n" +
                "\t\"Views\"\tTEXT,\n" +
                "\tPRIMARY KEY(\"ID\" AUTOINCREMENT)\n" +
                ")");
        statement.close();
    }

    public static void printDB() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resSet = statement.executeQuery("SELECT * FROM 'Articles'");

        while(resSet.next())
        {
            int id = resSet.getInt("ID");
            String  title = resSet.getString("Title");
            String  author = resSet.getString("Author");
            String  url = resSet.getString("URL");
            String  text = resSet.getString("Text");
            System.out.println( "ID = " + id );
            System.out.println( "author = " + author );
            System.out.println( "url = " + url );
            System.out.println( "text = " + text );
            System.out.println();
        }
    }

    public static void closeDB() throws SQLException {
        connection.close();
    }
}
