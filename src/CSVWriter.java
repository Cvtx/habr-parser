import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;


/**
 * Writes articles in .csv file.
 */
public class CSVWriter {
    static void writeCSV(String filename, Collection<? extends Object> objects){
        File csvFile = new File(filename);
        try (PrintWriter printWriter = new PrintWriter(csvFile, StandardCharsets.UTF_8)) {
            printWriter.write('\ufeff');
            for (Object object : objects) {
                printWriter.println(object.toString().replace("\n", ""));
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

}
