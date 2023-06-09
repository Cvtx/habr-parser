import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Writes articles in SQLite database.
 */
public class DBWriter implements AutoCloseable {

    public DBWriter(){
        try {
            SQLiteDB.connect();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            SQLiteDB.createDB();
        } catch (SQLException e) {
            // most likely error about table is already created so ignore it
        }
    }

    public void writeDB(LinkedList<Article> articles) throws SQLException{
        for (Article a: articles){
            // TODO: Better do batch insert
            PreparedStatement statement = SQLiteDB.connection.prepareStatement("INSERT OR REPLACE INTO \"Articles\" (\"ID\", \"Title\", \"Author\", \"URL\", \"Text\", \"Tags\", \"Date\", \"DateTime\", \"Views\") VALUES (?,?,?,?,?,?,?,?,?);");
            statement.setInt(1, a.getID());
            statement.setString(2, a.getTitle());
            statement.setString(3, a.getAuthor());
            statement.setString(4, a.getURL());
            statement.setString(5, a.getText());
            statement.setString(6, a.getTags());
            statement.setString(7, a.getDate());
            statement.setString(8, a.getDateTime());
            statement.setString(9, a.getViews());
            statement.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        SQLiteDB.closeDB();
    }
}
