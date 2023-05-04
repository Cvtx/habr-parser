public class ParserTaskArticle extends  ParserTask{
    public ParserTaskArticle(String url) throws InstantiationException {
        super(url);
    }

    @Override
    public boolean isValidURL(String url) {
        return URLParser.isArticleUrl(url);
    }
}
