import org.apache.commons.cli.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static String csvFilename = "articles.csv";

    public static void main(String[] args) {
        Options options = getOptions();
        ParserJob job = parseArgs(options, args);
        Parser parser = new Parser();
        parser.parse(job);
        save(parser.getArticles());
        System.out.println("Completed. See the result in " + csvFilename + " or in articles.db");
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
        Option help = Option.builder("h").longOpt("help")
                .argName("help")
                .hasArg(false)
                .required(false)
                .desc("Prints this usage hint.")
                .build();
        Option urls = Option.builder("u").longOpt("urls")
                .argName("urls")
                .hasArgs()
                .required(false)
                .desc("URLs for parsing e.g. `https://habr.com/en/all/`, `https://habr.com/en/all/page5/`, `https://habr.com/en/articles/127197/`")
                .build();
        Option filename = Option.builder("f").longOpt("filename")
                .argName("filename")
                .hasArg(true)
                .required(false)
                .desc("File name of the output .csv file.")
                .build();
        Option verboseErrors = Option.builder("ve").longOpt("verbose-errors")
                .argName("verbose-errors")
                .hasArg(false)
                .required(false)
                .desc("If specified, errors would be more verbose.")
                .build();
        Option pagesAmount = Option.builder("p").longOpt("pages")
                .argName("pages")
                .hasArg(true)
                .required(false)
                .desc("If -u contains only one page URL, this can be specified to download N amount of pages after specified page. This should be >= 0.")
                .build();

        options.addOption(help);
        options.addOption(urls);
        options.addOption(filename);
        options.addOption(verboseErrors);
        options.addOption(pagesAmount);
        return options;
    }

    private static void printHelp(HelpFormatter helper, Options options){
        helper.printHelp("habr-parser -u [URLs] [OPTIONS]", options);
    }

    private static void checkForInvalidUrls(List<String> invalidUrls, boolean verboseErrors) throws ParseException {
        if (!invalidUrls.isEmpty()) {
            System.out.println(String.format("Some (%d) of the provided URLs are invalid, therefore they can't be parsed.", invalidUrls.size()));
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

    /**
     * Try to construct ParserJob to run.
     * @param options
     * @param args
     * @return ParserJob based on passed options.
     */
    private static ParserJob parseArgs(Options options, String[] args){
        CommandLine cmd;
        CommandLineParser parser = new BasicParser();
        HelpFormatter helper = new HelpFormatter();
        ParserJob job = new ParserJob();
        ParserConfig config = new ParserConfig();
        try {
            cmd = parser.parse(options, args);
            boolean verboseErrors = false;
            if(cmd.hasOption("h")) { printHelp(helper, options); System.exit(0); }
            if(cmd.hasOption("ve")) { verboseErrors = true; }
            if(cmd.hasOption("f")) { csvFilename = cmd.getOptionValue("filename");}
            if (cmd.hasOption("l")) {
                String language = cmd.getOptionValue("language");
                config.setLanguage(language);
            }
            if (cmd.hasOption("u")) {
                var urls = cmd.getOptionValues("urls");
                // Split valid and invalid urls
                Map<Boolean, List<String>> partitionedUrl = Arrays.stream(urls)
                        .collect(Collectors.partitioningBy(URLParser::isValidUrl));

                // Check if we got some invalid urls
                List<String> invalidUrls = partitionedUrl.get(false);
                checkForInvalidUrls(invalidUrls, verboseErrors);

                List<String> validUrls = partitionedUrl.get(true);
                // Edge case: Replace main pages like https://habr.com/en/hub/git/ -> to first pages like https://habr.com/en/hub/git/page1/
                // TODO: This should be handled by ParserTaskPage
                validUrls.parallelStream()
                        .filter(url -> URLParser.isMainPageUrl(url))
                        .forEach(url -> {validUrls.set(validUrls.indexOf(url), url + "page1/");});

                // Convert valid urls into ParserTasks
                LinkedList<ParserTask> validParserTasks = makeParserTasksFromUrls(validUrls);
                job.addTasks(validParserTasks);
            }
            if (cmd.hasOption("p")){
                var urls = cmd.getOptionValues("urls");
                int urlsAmount = urls.length;
                if (urlsAmount == 1) {
                    String url = urls[0];
                    if (URLParser.isPageUrl(url)) {
                        String pagesAmountStr = cmd.getOptionValue("pages");
                        int pagesAmount = Integer.parseInt(pagesAmountStr);
                        job.reset();
                        job.addTask(new ParserTaskConsecutivePages(url, pagesAmount));
                    }
                    else {throw new ParseException("Option -p provided but specified URL is not a page URL.");}
                } else {throw new ParseException("Option -p should not be specified if more than one URL is provided.");}
            }
            if (cmd.getOptions().length == 0){
                printHelp(helper, options);
                System.exit(0);
            }
        } catch (ParseException e) {
            System.out.println("[Argument Error] " + e.getMessage());
            System.out.println("Type habr-parser --help to see a list of all options.");
            System.exit(1);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        return job;
    }
}
