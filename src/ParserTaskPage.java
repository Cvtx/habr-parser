/**
 * Represents parser task for parsing all articles on the single page.
 */
public class ParserTaskPage extends ParserTask{

    public ParserTaskPage(String url) throws InstantiationException {
        super(url);
    }

    @Override
    public boolean isValidURL(String url) {
        return URLParser.isPageUrl(url);
    }
}
