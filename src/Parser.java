import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * The parser processes links in ParserJobs by visiting and scraping pages and then storing data about encountered articles.
 */
public class Parser {

    public Parser(){
        _config = new ParserConfig();
    }

    public Parser(ParserConfig config){
        _config = config;
    }

    /**
     * @param job Job for Parser to perform. If completed, results would be added to `articles` field which can be accessed with `getArticles()` method.
     */
    public void parse(ParserJob job){
        articles.clear();
        articlesParsed = 0;
        Set<ParserTask> tasks = job.getTasks();
        long articlesAmount = Parser.getArticlesAmountInTaskSet(tasks);
        System.out.printf("Downloading %d articles.%n", articlesAmount);

        tasks.stream()
            .forEach(task -> {
                try {
                    // Whack polymorphism
                    if (task.getClass() == ParserTaskArticle.class) {
                        articles.add(parseArticle((ParserTaskArticle) task));
                    } else if (task.getClass() == ParserTaskPage.class) {
                        articles.addAll(parsePage((ParserTaskPage) task));
                    } else if (task.getClass() == ParserTaskConsecutivePages.class) {
                        articles.addAll(batchPageParse((ParserTaskConsecutivePages) task, _config));
                    } else {
                        throw new RuntimeException("Parsing of this kind URL is not implemented. URL: " + task.getUrl());
                    }
                    articlesParsed = getParsedArticlesAmount();
                } catch (InstantiationException e){
                    System.out.println(e.getMessage());
                    System.out.println("Failed to create a valid URL for the parser task. Did URL format changed? Did page format changed/redesigned?");
                }

                if (articlesParsed % articlesOnPage == 0 || articlesParsed == articlesAmount){
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    System.out.printf("Downloaded [%d / %d] articles.%n", articlesParsed, articlesAmount);
                    if (articlesParsed < articlesAmount) {
                        halt(_config);
                    }
                }
            });
    }

    public LinkedList<Article> getArticles(){
        return articles;
    }

    public void setConfig(ParserConfig config) {
        _config = config;
    }

    public ParserConfig getConfig() {
        return _config;
    }

    public int getParsedArticlesAmount(){
        return articles.size();
    }

    /**
     * Parses all pages in the batch and returns all Articles encountered.
     * @param task Batch of pages to parse.
     * @param config Parser configuration.
     * @return List of parsed Articles that reside on all pages in the batch.
     * @throws InstantiationException If unable to construct proper tasks for parser.
     */
    private static LinkedList<Article> batchPageParse(ParserTaskConsecutivePages task, ParserConfig config) throws InstantiationException {
        int pagesToDownload = task.getPagesAmount();
        int startUrlPageNumber = URLParser.getUrlPageNumber(task.getUrl());
        String dir = URLParser.getUrlDir(task.getUrl());
        LinkedList<Article> articles = new LinkedList<>();
        System.out.printf("Pages to download: %d %n", pagesToDownload + 1);
        for (int pageNumber = startUrlPageNumber; pageNumber <= startUrlPageNumber + pagesToDownload; pageNumber++) {
            String pageUrl = URLParser.makeUrlFromPageNumber(config.getLanguage(), dir, pageNumber);
            System.out.printf("Downloading from %s%n", pageUrl);
            ParserTaskPage pageTask = new ParserTaskPage(pageUrl);
            articles.addAll(parsePage(pageTask));
            halt(config);
        }
        return articles;
    }

    /**
     * Parses single article url of passed task and returns Article.
     * @param task Task to perform.
     * @return Parsed Article.
     */
    private static Article parseArticle(ParserTaskArticle task){
        DocumentLoader documentLoader = new DocumentLoader();
        Document d = documentLoader.getDoc(task.getUrl());
        String authorUsername = getTextFromElement(d.select("a.tm-user-info__username").first());
        String title = getTextFromElement(d.select("h1.tm-title").first());
        String articleText = getTextFromElement(d.select("div.article-formatted-body").first());
        LinkedList<String> tags = getTextFromElements(d.select("div.tm-article-snippet__hubs > span.tm-article-snippet__hubs-item"));
        String tagsStr = tags.stream().collect(Collectors.joining(","));
        String views = getTextFromElement(d.select("span.tm-icon-counter__value").first());
        String time = getTextFromElement(d.select("span.tm-article-datetime-published > time").first());
        Element timeTag = d.select("span.tm-article-datetime-published > time").first();
        String timeDatetime = null;
        if (timeTag != null) {
            timeDatetime = timeTag.attr("datetime");
        }
        int id = URLParser.getUrlID(task.getUrl());
        Article a = new Article(id, title, authorUsername, task.getUrl(), articleText);
        a.setTags(tagsStr);
        a.setDate(time);
        a.setDateTime(timeDatetime);
        a.setViews(views);
        return a;
    }

    /**
     Retrieves the text from a collection of elements and returns them as a LinkedList.
     @param e The Elements collection from which to extract the text.
     @return A LinkedList containing the text extracted from the elements.
     */
    private static LinkedList<String>  getTextFromElements(Elements e){
        LinkedList<String> texts = e.parallelStream()
                .map(str -> {return getTextFromElement(str);})
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));
        return texts;
    }

    /**
     * Parses all articles URLs on the page url and returns all Articles on the page.
     * @param task Task to perform
     * @return All Parsed Articles of page url.
     * @throws InstantiationException If unable to construct proper tasks for parser.
     */
    private static LinkedList<Article> parsePage(ParserTaskPage task) throws InstantiationException {
        DocumentLoader documentLoader = new DocumentLoader();
        Document doc = documentLoader.getDoc(task.getUrl());
        Elements elements = doc.getElementsByTag("article");
        return getArticlesFromElements(elements);
    }

    /**
     * Try to find article url in the HTML element and parse that url.
     * @param element Element to search.
     * @return Parsed Article.
     * @throws InstantiationException If unable to construct proper task for parser.
     */
    private static Article getArticleFromElement(Element element) throws InstantiationException {
        String relativeUrl = element.getElementsByTag("a").get(2).attr("href");
        String completeUrl = baseUrl + relativeUrl;
        ParserTaskArticle task = new ParserTaskArticle(completeUrl);
        return parseArticle(task);
    }

    /**
     * Same as getArticleFromElement() but it iterates over multiple Elements.
     * @param elements Elements to search.
     * @return All parsed Articles whose URLs are presented in HTML elements.
     * @throws InstantiationException If unable to construct proper task for parser.
     */
    private static LinkedList<Article> getArticlesFromElements(Elements elements) throws InstantiationException {
        LinkedList<Article> articles = new LinkedList<>();
        for(Element element : elements)
        {
            articles.add(getArticleFromElement(element));
        }
        return articles;
    }

    /**
     Halts the execution for a specified duration based on the provided ParserConfig.
     @param config The ParserConfig object containing the halt time configuration.
     */
    private static void halt(ParserConfig config){
        sleep(config.getHaltTime());
    }

    /**
     Pauses the execution of the current thread for a specified number of milliseconds.
     @param milliseconds The duration in milliseconds for which the thread should sleep.
     @throws RuntimeException if the thread sleep is interrupted.
     */
    private static void sleep(int milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     Retrieves the total number of articles in a given ParserJob.
     @param job The ParserJob containing the tasks from which to extract the article count.
     @return The total number of articles found in the ParserJob.
     */
    public static long getArticlesAmountInJob(ParserJob job){
        var parserTaskSet = job.getTasks();
        return getArticlesAmountInTaskSet(parserTaskSet);
    }

    /**
     * Calculate beforehand amount of articles in Task Set that can be obtained with ParserJob::getTasks();
     * @param parserTaskSet Task Set to calculate.
     * @return Amount of articles in passed task set.
     */
    public static long getArticlesAmountInTaskSet(Set<ParserTask> parserTaskSet){
        long numArticles = parserTaskSet.parallelStream()
                .filter(task -> task.getClass() == ParserTaskArticle.class || task.getClass() == ParserTask.class)
                .count();
        long numPages = parserTaskSet.parallelStream()
                .filter(task -> task.getClass() == ParserTaskPage.class)
                .count();
        int numPagesBatches = parserTaskSet.parallelStream()
                .filter(task -> task.getClass() == ParserTaskConsecutivePages.class)
                .mapToInt(task -> ((ParserTaskConsecutivePages)task).getPagesAmount() + 1)
                .sum();
        return numArticles + numPages * articlesOnPage + numPagesBatches * articlesOnPage;
    }

    private static String getTextFromElement(Element e){
        if (e != null){
            return e.text();
        }
        return "";
    }

    private static final String baseUrl = "https://habr.com";
    private static final int articlesOnPage = 20;
    private LinkedList<Article> articles = new LinkedList<>();
    private long articlesParsed = 0;
    private ParserConfig _config;
}
