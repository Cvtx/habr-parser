# habr-parser

Save articles from habr.com to the SQLite database and .csv files.

## Usage

```shell
$ habr-parser -h

usage: habr-parser -u [URLs] [OPTIONS]
 -f,--filename <filename>   File name of the output .csv file.
 -h,--help                  Prints this usage hint.
 -p,--pages <pages>         If -u contains only one page URL, this can be
                            specified to download N amount of pages after
                            specified page. This should be >= 0.
 -u,--urls <urls>           URLs for parsing e.g.
                            `https://habr.com/en/all/`,
                            `https://habr.com/en/all/page5/`,
                            `https://habr.com/en/articles/127197/`
 -ve,--verbose-errors       If specified, errors would be more verbose.
```

## Examples

```shell
$ habr-parser -u https://habr.com/en/flows/develop/page2/
$ habr-parser -u https://habr.com/en/all/page1/ -p 3
$ habr-parser -u https://habr.com/en/all/page5/ https://habr.com/en/articles/127197/
```