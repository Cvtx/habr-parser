import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Writes articles in .csv file.
 */
public class CSVWriter {
    static void writeCSV(String filename, LinkedList<Article> articles){
        File csvFile = new File(filename);
        try (PrintWriter printWriter = new PrintWriter(csvFile, StandardCharsets.UTF_8)) {
            printWriter.write('\ufeff');
            for (Article article : articles) {
                List<String> row = new ArrayList<>();
                row.add("\"" + article.getTitle() + "\"");
                row.add("\"" + article.getAuthor() + "\"");
                row.add("\"" + article.getViews() + "\"");
                row.add("\"" + article.getDate() + "\"");
                row.add("\"" + article.getTags() + "\"");
                row.add("\"" + article.getURL() + "\"");
                printWriter.println(String.join(",", row));
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

}
