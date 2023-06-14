/**
 * Represents parser task for parsing all articles on the single page.
 */
public class ParserTaskPage extends ParserTask{

    public ParserTaskPage(String url) throws InstantiationException {
        super(url);
        // Edge case: Replace main pages like https://habr.com/en/hub/git/ -> to first pages like https://habr.com/en/hub/git/page1/
        if (URLParser.isMainPageUrl(url)){
            url = url + "page1/";
            setUrl(url);
        }
    }

    @Override
    public boolean isValidURL(String url) {
        return URLParser.isPageUrl(url) || URLParser.isMainPageUrl(url);
    }
}
