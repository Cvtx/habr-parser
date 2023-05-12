/**
 * Data-only class that holds all information about single article.
 */
public class Article {
    private String title;
    private String author;
    private String url;
    private String quoteText;
    int id;

    Article(int id, String title, String author, String url, String quoteText){
        setID(id);
        setTitle(title);
        setURl(url);
        setAuthor(author);
        setText(quoteText);
    }

    public int getID(){ return id; }

    public void setID(int id){ this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() { return quoteText; }

    public void setText(String quoteText) {
        this.quoteText = quoteText;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getURL() {
        return url;
    }

    public void setURl(String url) {
        this.url = url;
    }
}
