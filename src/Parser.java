import java.util.LinkedList;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Parser {

    private ParserConfig _config;
    public Parser(){
        _config = new ParserConfig();
    }
    public Parser(ParserConfig config){
        _config = config;
    }
    public void parse(ParserJob job){
        articles.clear();
        articlesParsed = 0;
        Set<ParserTask> tasks = job.getTasks();
        long articlesAmount = Parser.getArticlesAmountInTaskSet(tasks);
        System.out.println(String.format("Downloading %d articles.", articlesAmount));

        tasks.stream()
            .forEach(task -> {
                if (task.getClass() == ParserTask.class) {
                    articles.add(parseArticle(task.getUrl()));
                    updateParsedArticlesAmount(1);
                } else if (task.getClass() == ParserTaskPage.class) {
                    articles.addAll(parsePage(task.getUrl()));
                    updateParsedArticlesAmount(articlesOnPage);
                } else if (task.getClass() == ParserTaskBatchPages.class){
                    articles.addAll(batchPageParse((ParserTaskBatchPages) task, _config));
                    updateParsedArticlesAmount(articlesOnPage * ((ParserTaskBatchPages) task).getPagesAmount() + 1);
                } else {
                    throw new RuntimeException("Parsing of this kind URL is not implemented. URL: " + task.getUrl());
                }

                if (articlesParsed % articlesOnPage == 0 || articlesParsed == articlesAmount){
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    System.out.println(String.format("Downloaded [%d / %d] articles.", articlesParsed, articlesAmount));
                    if (articlesParsed < articlesAmount) {
                        sleep(5000); // Halt after every 20 downloaded articles so that we don't get ip-banned
                    }
                }
            });
    }

    public LinkedList<Article> getArticles(){
        return articles;
    }

    private static LinkedList<Article> batchPageParse(ParserTaskBatchPages task, ParserConfig config){
        int pagesToDownload = task.getPagesAmount();
        int startUrlPageNumber = URLParser.getUrlPageNumber(task.getUrl());
        String dir = URLParser.getUrlDir(task.getUrl());
        LinkedList<Article> articles = new LinkedList<>();
        System.out.println(String.format("Pages to download: %d ", pagesToDownload + 1));
        for (int pageNumber = startUrlPageNumber; pageNumber <= startUrlPageNumber + pagesToDownload; pageNumber++) {
            String pageUrl = URLParser.makeUrlFromPageNumber(config.getLanguage(), dir, pageNumber);
            System.out.println(String.format("Downloading from %s", pageUrl));
            articles.addAll(parsePage(pageUrl));
            System.out.println(String.format("Articles %d", articles.size()));
            sleep(5000);
        }
        return articles;
    }
    private static Article parseArticle(String url){
        DocumentLoader documentLoader = new DocumentLoader();
        Document d = documentLoader.getDoc(url);
        String authorUsername = d.select("a.tm-user-info__username").first().text();
        String title = d.select("h1.tm-title").first().text();
        String articleText = d.select("div.article-formatted-body > div").first().text();
        int id = URLParser.getUrlID(url);
        return new Article(id, title, authorUsername, url, articleText);
    }
    private static LinkedList<Article> parsePage(String url){
        DocumentLoader documentLoader = new DocumentLoader();
        Document doc = documentLoader.getDoc(url);
        Elements elements = doc.getElementsByTag("article");
        return getArticlesFromElements(elements);
    }
    private static Article getArticleFromElement(Element element){
        String relativeUrl = element.getElementsByTag("a").get(2).attr("href");
        String completeUrl = baseUrl + relativeUrl;
        return parseArticle(completeUrl);
    }
    private static LinkedList<Article> getArticlesFromElements(Elements elements)  {
        LinkedList<Article> articles = new LinkedList<>();
        for(Element element : elements)
        {
            articles.add(getArticleFromElement(element));
        }
        return articles;
    }

    private static void sleep(int milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateParsedArticlesAmount(int amount){
        articlesParsed += amount;
    }

    public static long getArticlesAmountInConfig(ParserJob config){
        var parserTaskSet = config.getTasks();
        return getArticlesAmountInTaskSet(parserTaskSet);
    }

    public static long getArticlesAmountInTaskSet(Set<ParserTask> parserTaskSet){
        long numArticles = parserTaskSet.stream()
                .filter(task -> task.getClass() == ParserTaskArticle.class || task.getClass() == ParserTask.class)
                .count();
        long numPages = parserTaskSet.stream()
                .filter(task -> task.getClass() == ParserTaskPage.class)
                .count();
        int numPagesBatches = parserTaskSet.stream()
                .filter(task -> task.getClass() == ParserTaskBatchPages.class)
                .mapToInt(task -> ((ParserTaskBatchPages)task).getPagesAmount() + 1)
                .sum();
        return numArticles + numPages * articlesOnPage + numPagesBatches * articlesOnPage;
    }
    private static String baseUrl = "https://habr.com";
    private static final int articlesOnPage = 20;
    private LinkedList<Article> articles = new LinkedList<>();
    private long articlesParsed = 0;
}
