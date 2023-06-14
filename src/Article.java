/**
 * Data-only class that holds all information about single article.
 */
public class Article {
    private String title;
    private String author;
    private String url;
    private String quoteText;
    int id;
    private String tags;
    private String date;
    private String dateTime;
    private String views;

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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", url='" + url + '\'' +
                ", tags='" + tags + '\'' +
                ", date='" + dateTime + '\'' +
                '}';
    }
}
