public class ParserTaskBatchPages extends ParserTaskPage{
    private int _pagesAmount;
    public ParserTaskBatchPages(String url, int pagesAmount) throws InstantiationException {
        super(url);
        if (pagesAmount < 0){
            throw new InstantiationException("Pages amount should be a positive number");
        }
        _pagesAmount = pagesAmount;
    }

    public int getPagesAmount(){ return _pagesAmount;}
}
