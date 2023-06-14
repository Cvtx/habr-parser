# habr-parser

Save articles from habr.com to the SQLite database and .csv files. Also perform fuzzy search in database.

## Usage

```shell
$ habr-parser -h

usage: habr-parser TASK [OPTIONS]
 -h,--help          Prints this usage hint.
 -parse,--parse     Perform parsing. For usage instruction type
                    habr-parser -parse -h
 -search,--search   Perform fuzzy-search in database. For usage
                    instruction type habr-parser -search -h
```

## Parser Usage

```shell
$ habr-parser -parse -h

usage: habr-parser -parse [OPTIONS]
 -f,--filename <filename>   File name of the output .csv file.
 -p,--pages <pages>         If -u contains only one page URL, this can be
                            specified to download N amount of pages after
                            specified page. This should be >= 0.
 -parse,--parse             Perform parsing. For usage instruction type
                            habr-parser -parse -h.
 -u,--urls <urls>           URLs for parsing e.g.
                            `https://habr.com/en/all/`,
                            `https://habr.com/en/all/page5/`,
                            `https://habr.com/en/articles/127197/`
 -ve,--verbose-errors       If specified, errors would be more verbose.
```

## Searcher Usage

```shell
$ habr-parser -search -h

usage: habr-parser -search [OPTIONS]
 -f,--filename <filename>         File name of the output .csv file.
 -k,--keyword <keyword>           Starts fuzzy-search with provided
                                  keyword.
 -s,--minscore <minscore>         Minimum search score to be indexed by
                                  searcher.
 -search,--search                 Perform fuzzy-search in database. For
                                  usage instruction type habr-parser
                                  -search -h
 -t,--ntopresults <ntopresults>   Number of keyword occurrences to find.
```

## Examples

```shell
$ habr-parser -parse -u https://habr.com/en/flows/develop/page2/
$ habr-parser -parse -u https://habr.com/en/all/page1/ -p 3
$ habr-parser -parse -u https://habr.com/en/all/page5/ https://habr.com/en/articles/127197/
```

```shell
$ habr-parser -search "machine learning"
$ habr-parser -search Habr -t 100
$ habr-parser -search kubernetes -t 30 -s 85
```