public class SearcherTask {
    private String _keyword;
    private int _nTopResults = 10;
    private int _minScore = 90;

    public void setKeyword(String keyword) { _keyword = keyword; }

    public String getKeyword() { return _keyword; }

    public void setNTopResults(int nTopResults) {
        if (nTopResults <= 0){
            nTopResults = 1;
        }
        _nTopResults = nTopResults;
    }

    public int getNTopResults() { return _nTopResults; }

    public void setMinScore(int minScore) {
        if (minScore <= 0){
            minScore = 1;
        }
        _minScore = minScore;
    }

    public int getMinScore() { return _minScore; }
}
