import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Searcher {

    private SearcherConfig _config;

    Searcher(){
        _config = new SearcherConfig();
    }

    Searcher(SearcherConfig config){
        _config = config;
    }

    private Map<ArticleInfo, List<ExtractedResult>> searchResults;
    public void search(SearcherTask task) {
        searchResults = new HashMap<>();
        System.out.printf("Searching for '%s' keyword.%n", task.getKeyword());
        try {
            SQLiteDB.connect();
            ResultSet resultSet = SQLiteDB.readDB();
            while(resultSet.next())
            {
                ArticleInfo articleInfo = getArticleInfoFromResult(resultSet);
                List<String> wordList = getWordListFromResult(resultSet);
                var fuzzySearchResult = FuzzySearch.extractTop(task.getKeyword(), wordList, task.getNTopResults());
                var topResults = fuzzySearchResult
                        .stream()
                        .filter(result -> result.getScore() > task.getMinScore())
                        .toList();
                if (topResults.size() > 0) {
                    searchResults.put(articleInfo, topResults);
                }
            }
            SQLiteDB.closeDB();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> getWordListFromResult(ResultSet resultSet) throws SQLException {
        String  text = resultSet.getString("Text");
        String[] textWords = text.split(" ");
        List<String> wordList = Arrays.asList(textWords);
        return wordList;
    }
    private static ArticleInfo getArticleInfoFromResult(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("ID");
        String  title = resultSet.getString("Title");
        String  author = resultSet.getString("Author");
        String  url = resultSet.getString("URL");
        ArticleInfo articleInfo = new ArticleInfo(id, title, author, url);
        return articleInfo;
    }

    public Map<ArticleInfo, List<ExtractedResult>> getResults() {
        return searchResults;
    }
}
