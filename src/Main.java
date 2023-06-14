import org.apache.commons.cli.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static String csvFilename = "result.csv";

    public static void main(String[] args) {
        Options options = getOptions();
        parseArgs(options, args);
    }

    private static void runParser(ParserJob job){
        Parser parser = new Parser();
        parser.parse(job);
        save(parser.getArticles());
        System.out.printf("Completed. See the result in %s or in articles.db.%n", csvFilename);
    }

    private static void runSearcher(SearcherTask task){
        Searcher searcher = new Searcher();
        searcher.search(task);
        var results = searcher.getResults();
        System.out.printf("Found %d articles.%n", results.size());
        CSVWriter.writeCSV(csvFilename, results.entrySet());
        System.out.printf("Completed. See the result in %s.%n", csvFilename);
    }

    private static void save(LinkedList<Article> articles){
        CSVWriter.writeCSV(csvFilename, articles);
        try(DBWriter writer = new DBWriter()) {
            writer.writeDB(articles);
        } catch (SQLException e) {
        }
    }

    private static Options getOptions() {
        Options options = new Options();

        Option help = Option.builder("h")
                .longOpt("help")
                .argName("help")
                .hasArg(false)
                .required(false)
                .desc("Prints this usage hint.")
                .build();
        Option runSearch = Option.builder("search")
                .longOpt("search")
                .argName("search")
                .hasArg(false)
                .required(false)
                .desc("Perform fuzzy-search in database. For usage instruction type habr-parser -search -h")
                .build();
        Option runParser = Option.builder("parse")
                .longOpt("parse")
                .argName("parse")
                .hasArg(false)
                .required(false)
                .desc("Perform parsing. For usage instruction type habr-parser -parse -h")
                .build();

        options.addOption(help);
        options.addOption(runSearch);
        options.addOption(runParser);

        return options;
    }

    private static Options getParserOptions() {
        Options options = new Options();

        Option runParser = Option.builder("parse")
                .longOpt("parse")
                .argName("parse")
                .hasArg(false)
                .required(true)
                .desc("Perform parsing. For usage instruction type habr-parser -parse -h.")
                .build();
        Option urls = Option.builder("u").longOpt("urls")
                .argName("urls")
                .hasArgs()
                .required(true)
                .desc("URLs for parsing e.g. `https://habr.com/en/all/`, `https://habr.com/en/all/page5/`, `https://habr.com/en/articles/127197/`")
                .build();
        Option filename = Option.builder("f").longOpt("filename")
                .argName("filename")
                .hasArg(true)
                .required(false)
                .desc("File name of the output .csv file.")
                .build();
        Option pagesAmount = Option.builder("p").longOpt("pages")
                .argName("pages")
                .hasArg(true)
                .required(false)
                .desc("If -u contains only one page URL, this can be specified to download N amount of pages after specified page. This should be >= 0.")
                .build();
        Option verboseErrors = Option.builder("ve").longOpt("verbose-errors")
                .argName("verbose-errors")
                .hasArg(false)
                .required(false)
                .desc("If specified, errors would be more verbose.")
                .build();

        options.addOption(runParser);
        options.addOption(urls);
        options.addOption(verboseErrors);
        options.addOption(filename);
        options.addOption(pagesAmount);

        return options;
    }

    private static Options getSearcherOptions() {
        Options options = new Options();

        Option runSearch = Option.builder("search")
                .longOpt("search")
                .argName("search")
                .hasArg(false)
                .required(true)
                .desc("Perform fuzzy-search in database. For usage instruction type habr-parser -search -h")
                .build();
        Option searchKeyword = Option.builder("k")
                .longOpt("keyword")
                .argName("keyword")
                .hasArg(true)
                .required(true)
                .desc("Starts fuzzy-search with provided keyword.")
                .build();
        Option nTopResults = Option.builder("t")
                .longOpt("ntopresults")
                .argName("ntopresults")
                .hasArg(true)
                .required(false)
                .desc("Number of keyword occurrences to find. ")
                .build();
        Option minScore = Option.builder("s")
                .longOpt("minscore")
                .argName("minscore")
                .hasArg(true)
                .required(false)
                .desc("Minimum search score to be indexed by searcher.")
                .build();
        Option filename = Option.builder("f")
                .longOpt("filename")
                .argName("filename")
                .hasArg(true)
                .required(false)
                .desc("File name of the output .csv file.")
                .build();

        options.addOption(runSearch);
        options.addOption(searchKeyword);
        options.addOption(nTopResults);
        options.addOption(minScore);
        options.addOption(filename);

        return options;
    }

    private static void printHelp(HelpFormatter helper, Options options, String taskName){
        helper.printHelp(String.format("habr-parser %s [OPTIONS]", taskName), options);
    }

    private static void checkForInvalidUrls(List<String> invalidUrls, boolean verboseErrors) throws ParseException {
        if (!invalidUrls.isEmpty()) {
            System.out.printf("Some (%d) of the provided URLs are invalid, therefore they can't be parsed.%n", invalidUrls.size());
            if (verboseErrors) {
                System.out.println("Invalid URLs are:");
                invalidUrls.parallelStream().forEach(System.out::println);
            }
            else {
                System.out.println("Use -ve to output invalid URLs.");
            }
            throw new ParseException("Invalid URLs provided.");
        }
    }

    private static LinkedList<ParserTask> makeParserTasksFromUrls(List<String> validUrls){
        LinkedList<ParserTask> tasks = validUrls.parallelStream()
                .map(str -> {
                    try {
                        if (URLParser.isArticleUrl(str)) {
                            return new ParserTaskArticle(str);
                        } else if (URLParser.isPageUrl(str)) {
                            return new ParserTaskPage(str);
                        } else {
                            throw new RuntimeException("Parsing of this kind URL is not implemented. URL: " + str);
                        }
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));
        return tasks;
    }

    private static void parseExceptionExit(ParseException e, String message){
        System.out.println("[Argument Error] " + e.getMessage());
        System.out.println(message);
        System.exit(1);
    }

    /**
     * Try to construct ParserJob to run.
     * @param options Options.
     * @param args Args.
     * @return ParserJob based on passed options.
     */
    private static void parseArgs(Options options, String[] args){
        CommandLine cmd;
        CommandLineParser parser = new RelaxedParser();
        HelpFormatter helper = new HelpFormatter();
        try {
            cmd = parser.parse(options, args);
            if (args.length == 0){
                printHelp(helper, options, "TASK");
                System.exit(0);
            }
            if (cmd.hasOption("h")) {
                if (args.length == 1){
                    printHelp(helper, options, "TASK");
                    System.exit(0);
                } else{
                    if (cmd.hasOption("search")) {
                        printHelp(helper, getSearcherOptions(), "-search");
                        System.exit(0);
                    }
                    if (cmd.hasOption("parse")) {
                        printHelp(helper, getParserOptions(), "-parse");
                        System.exit(0);
                    }
                }
            }
            if (cmd.hasOption("search")) {
                parseSearcherArgs(getSearcherOptions(), args);
            }
            if (cmd.hasOption("parse")) {
                parseParserArgs(getParserOptions(), args);
            }
        }
        catch (ParseException e) {
            parseExceptionExit(e, "Type habr-parser --help to see a list of all options.");
        }
    }

    private static void parseParserArgs(Options options, String[] args) {
        CommandLine cmd;
        CommandLineParser parser = new BasicParser();
        HelpFormatter helper = new HelpFormatter();
        ParserJob job = new ParserJob();
        ParserConfig config = new ParserConfig();
        boolean verboseErrors = false;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("ve")) { verboseErrors = true; }
            if (cmd.hasOption("f")) {
                csvFilename = cmd.getOptionValue("filename");
            } else {
                csvFilename = "articles.csv";
            }
            if (cmd.hasOption("l")) {
                String language = cmd.getOptionValue("language");
                config.setLanguage(language);
            }
            if (cmd.hasOption("u")) {
                var urls = cmd.getOptionValues("urls");
                // Split valid and invalid urls
                Map<Boolean, List<String>> partitionedUrl = Arrays.stream(urls)
                        .collect(Collectors.partitioningBy(URLParser::isValidUrl));

                // Check if we got any invalid urls
                List<String> invalidUrls = partitionedUrl.get(false);
                checkForInvalidUrls(invalidUrls, verboseErrors);

                // Convert valid urls into ParserTasks
                List<String> validUrls = partitionedUrl.get(true);
                LinkedList<ParserTask> validParserTasks = makeParserTasksFromUrls(validUrls);
                job.addTasks(validParserTasks);
            }
            if (cmd.hasOption("p")){
                var urls = cmd.getOptionValues("urls");
                int urlsAmount = urls.length;

                if (urlsAmount != 1) {
                    throw new ParseException("Option -p should not be specified if more than one URL is provided.");
                }

                String url = urls[0];
                if (!URLParser.isPageUrl(url)) {
                    throw new ParseException("Option -p provided but specified URL is not a page URL.");
                }

                String pagesAmountStr = cmd.getOptionValue("pages");
                int pagesAmount = Integer.parseInt(pagesAmountStr);
                job.reset();
                job.addTask(new ParserTaskConsecutivePages(url, pagesAmount));
            }
        } catch (ParseException e) {
            parseExceptionExit(e, "Type habr-parser -parse --help to see a list of all options.");

        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }

        runParser(job);
    }

    private static void parseSearcherArgs(Options options, String[] args) {
        CommandLine cmd;
        CommandLineParser parser = new BasicParser();
        HelpFormatter helper = new HelpFormatter();
        SearcherTask task = new SearcherTask();

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("k")) {
                String keyword = cmd.getOptionValue("k");
                task.setKeyword(keyword);
            }
            if (cmd.hasOption("s")) {
                int minScore = Integer.parseInt(cmd.getOptionValue("s"));
                task.setMinScore(minScore);
            }
            if (cmd.hasOption("t")) {
                int nTopResults = Integer.parseInt(cmd.getOptionValue("t"));
                task.setNTopResults(nTopResults);
            }
            if (cmd.hasOption("f")) {
                csvFilename = cmd.getOptionValue("filename");
            } else {
                csvFilename = "search_result.csv";
            }
        }
        catch (ParseException e) {
            parseExceptionExit(e, "Type habr-parser -search --help to see a list of all options.");
        }

        runSearcher(task);
    }
}
