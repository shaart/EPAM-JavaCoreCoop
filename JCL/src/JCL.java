import interfaces.Commandable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JCL {
    private JCL() {
    }

    private static final String JCL_PREFIX = "JCL: ";
    private static final String JCL_POSTFIX = " > ";
    private static Map<String, Commandable> commands;
    private static Path currentPath = Paths.get("").toAbsolutePath();

    public static void setCurrentPath(Path newPath) {
        currentPath = newPath;
    }

    public static Path getCurrentPath() {
        return currentPath;
    }

    public static void main(String[] args) {
        commands = CommandConnector.getCommands();
        if (commands == null || commands.isEmpty()) {
            System.err.println("Something gone wrong: commands were not loaded!");
            return;
        }

        final String[] BLANK = new String[0];
        Scanner input = new Scanner(System.in);
        String userInput;

        String currentCommand;
        String[] commandArgs;

        final String SINGLE_WORD_OR_PHRASE_IN_QUOTES_PATTERN = "\"([^\"]*)\"|'([^']*)'|[^\\s]+";
        Pattern regex = Pattern.compile(SINGLE_WORD_OR_PHRASE_IN_QUOTES_PATTERN);
        Matcher regexMatcher;
        List<String> matchList = new ArrayList<>();

        boolean working = true;
        while (working) {
            commandArgs = BLANK;
            matchList.clear();

            System.out.print(JCL_PREFIX + currentPath.toAbsolutePath().toString() + JCL_POSTFIX);
            userInput = input.nextLine();
            regexMatcher = regex.matcher(userInput);

            while (regexMatcher.find()) {
                if (regexMatcher.group(1) != null) {
                    // Add double-quoted string without the quotes
                    matchList.add(regexMatcher.group(1));
                } else if (regexMatcher.group(2) != null) {
                    // Add single-quoted string without the quotes
                    matchList.add(regexMatcher.group(2));
                } else {
                    // Add unquoted word
                    matchList.add(regexMatcher.group());
                }
            }
            if (matchList.size() > 0) {
                currentCommand = matchList.get(0);
                matchList.remove(0);
                commandArgs = matchList.toArray(commandArgs);

                if (currentCommand.equalsIgnoreCase("exit") || currentCommand.equalsIgnoreCase("close")) {
                    working = false;
                    break;
                }

                if (commands.containsKey(currentCommand)) {
                    commands.get(currentCommand).run(commandArgs);
                } else {
                    System.out.println("Unrecognized command: " + currentCommand);
                }
            }
        }
    }
}
