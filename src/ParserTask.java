/**
 * A base class for representing a parser task.
 */
public class ParserTask {
    private String _url;

    protected ParserTask(String url) throws InstantiationException{
        setUrl(url);
    }

    public String getUrl(){return _url;}

    public void setUrl(String url) throws InstantiationException {
        if (!isValidURL(url)){
            throw new InstantiationException("Invalid URL provided, couldn't construct object. URL: " + url);
        }
        _url = url;
    }

    /**
     * This should be overriden by inheritors.
     * @param url Url to check.
     * @return True, if url is valid for object instantiation, False otherwise.
     */
    public boolean isValidURL(String url){
        return URLParser.isValidUrl(url);
    }
}
