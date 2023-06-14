public class ArticleInfo {

    private String title;
    private String author;
    private String url;
    int id;

    ArticleInfo(int id, String title, String author, String url){
        setID(id);
        setTitle(title);
        setURl(url);
        setAuthor(author);
    }

    public int getID(){ return id; }

    public void setID(int id){ this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) {
        this.title = title;
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

    @Override
    public String toString() {
        return "ArticleInfo(" +
                "title: " + title + ", " +
                "id: " + id + ", " +
                "url: " + url +
                ')';
    }
}
