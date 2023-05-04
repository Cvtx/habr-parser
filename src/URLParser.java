import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLParser {
    public static int getUrlPageNumber(String url) throws RuntimeException {
        return getIntFromRegexGroup(habrPageRegex, url, 2);
    }

    public static int getUrlID(String url){
        return getIntFromRegexGroup(habrArticleURLRegex, url, 3);
    }

    public static String getUrlLanguage(String url) {
        return getStringFromRegexGroup(habrPageLanguageRegex, url, 1);
    }

    public static String getUrlDir(String url) {
        return getStringFromRegexGroup(habrUrlDir, url, 2);
    }

    public static String makeUrlFromPageNumber(String language, String dir, int pageNumber){
        return String.format("https://habr.com/%s/%spage%d/", language, dir, pageNumber);
    }

    public static boolean isValidUrl(String url){
        return isMatchesRegex(habrPageRegex, url) || isMatchesRegex(habrMainPageRegex, url) || isMatchesRegex(habrArticleURLRegex, url);
    }

    public static boolean isMainPageUrl(String url){return isMatchesRegex(habrMainPageRegex, url);}

    public static boolean isPageUrl(String url){
        return isMatchesRegex(habrPageRegex, url);
    }
    public static boolean isArticleUrl(String url){
        return isMatchesRegex(habrArticleURLRegex, url);
    }
    private static boolean isMatchesRegex(String regex, String str){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
    private static int getIntFromRegexGroup(String regex, String str, int group) throws RuntimeException, NumberFormatException{
        String intStr = getStringFromRegexGroup(regex, str, group);
        int parsedInt = Integer.parseInt(intStr);
        return parsedInt;
    }

    private static String getStringFromRegexGroup(String regex, String str, int group) throws RuntimeException{
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            String strGroup = matcher.group(group);
            return strGroup;
        } else {
            throw new RuntimeException("Can't match string with regex. String:" + str + " Regex: " + regex);
        }
    }

    // Matches https://habr.com/<lang>/companies/<company>/articles/<id>/, https://habr.com/<lang>/articles/<id>/
    private static String habrArticleURLRegex = "https:\\/\\/habr\\.com\\/([a-z]{2})\\/(?:companies\\/([a-zA-Z0-9_-]+)\\/)?articles\\/(\\d+)\\/?";

    // Matches https://habr.com/<lang>/<something>/page{n}/
    // e.g. https://habr.com/en/all/page25/, https://habr.com/en/flows/develop/page3, https://habr.com/en/hub/git/page3/
    private static String habrPageRegex = "https:\\/\\/habr\\.com\\/([a-z]{2})\\/(?:[a-z_]+\\/)+page(\\d+)\\/?";

    // Matches https://habr.com/<language>/<something>/
    // e.g. https://habr.com/en/all/, https://habr.com/en/flows/develop/, https://habr.com/en/hub/git/
    private static String habrMainPageRegex =  "https:\\/\\/habr\\.com\\/([a-z]{2})\\/(?:[a-z_]+\\/?)+";

    // Matches https://habr.com/<language>/
    private static String  habrPageLanguageRegex =  "https:\\/\\/habr\\.com\\/([a-z]{2})\\/?";


    // e.g. https://habr.com/en/hub/artificial_intelligence/page3/, https://habr.com/en/hub/whatever/, https://habr.com/en/all/page4/
    private static String  habrUrlDir =  "https:\\/\\/habr\\.com\\/([a-z]{2})\\/((?:[a-z_]+\\/)+)(?:page\\d+\\/)?";
}
