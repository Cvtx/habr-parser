public class ParserTask {
    private String _url;

    public ParserTask(String url) throws InstantiationException{
        setUrl(url);
    }
    public String getUrl(){return _url;}

    public void setUrl(String url) throws InstantiationException {
        if (!isValidURL(url)){
            throw new InstantiationException("Invalid URL provided.");
        }
        _url = url;
    }

    public boolean isValidURL(String url){
        return URLParser.isValidUrl(url);
    }

}
