/**
 * Represents parser task for parsing all articles on the all pages.
 */
public class ParserTaskConsecutivePages extends ParserTaskPage{
    private int _pagesAmount;

    public ParserTaskConsecutivePages(String url, int pagesAmount) throws InstantiationException {
        super(url);
        if (pagesAmount < 0){
            throw new InstantiationException("Pages amount should be a positive number");
        }
        _pagesAmount = pagesAmount;
    }

    public int getPagesAmount(){ return _pagesAmount;}
}
