import org.jsoup.*;
import org.jsoup.nodes.*;

import java.io.IOException;

public class DocumentLoader {
    public Document getDoc(String url) {

        try {
            Document doc = Jsoup
                    .connect(url)
                    .referrer("http://www.google.com")
                    .followRedirects(true)
                    .timeout(5000) // 5 seconds timeout
                    //.userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .get();
            return doc;
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
        return new Document(url);
    }
}
