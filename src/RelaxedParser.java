import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Same as DefaultParser but it doesn't throw ParseException when encounters Unrecognized option.
 */
public class RelaxedParser extends DefaultParser {

    @Override
    public CommandLine parse(final Options options, final String[] arguments) throws ParseException {
        final List<String> knownArgs = new ArrayList<>();
        for (int i = 0; i < arguments.length; i++) {
            if (options.hasOption(arguments[i])) {
                knownArgs.add(arguments[i]);
                if (i + 1 < arguments.length && options.getOption(arguments[i]).hasArg()) {
                    knownArgs.add(arguments[i + 1]);
                }
            }
        }
        return super.parse(options, knownArgs.toArray(new String[0]));
    }
}
