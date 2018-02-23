package general;

import interfaces.Commandable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Console {
    private Console() {
    }

    private static final String JCL_PREFIX = "JCL# ";
    private static final String JCL_POSTFIX = "> ";
    private static Map<String, Commandable> commands;
    private static Path currentPath = Paths.get("").toAbsolutePath();

    public static void setCurrentPath(Path newPath) {
        currentPath = newPath;
    }

    public static Path getCurrentPath() {
        return currentPath;
    }

    public static String getCommandDescription(String commandName) {
        Commandable command = commands.get(commandName);
        String description;
        if (command == null) {
            description = null;
        } else {
            description = command.getDescription();
        }

        return description;
    }

    public static String getCommandUsage(String commandName) {
        Commandable command = commands.get(commandName);
        String usage;
        if (command == null) {
            usage = null;
        } else {
            usage = command.getUsage();
        }

        return usage;
    }

    public static boolean containsCommand(String command) {
        return commands.containsKey(command);
    }

    public static List<String> getCommands() {
        Set<String> commandsSet = commands.keySet();
        List<String> knownCommands = new ArrayList<>(commandsSet.size());
        for (String commandName : commandsSet) {
            knownCommands.add(commandName);
        }

        return knownCommands;
    }

    public static void run() {
        commands = CommandConnector.getCommands();
        if (commands == null || commands.isEmpty()) {
            System.err.println("Something gone wrong: commands were not loaded!");
            return;
        }
        commands.put("help", new HelpCommand());

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
                    working = false; // for clarity
                    break;
                }

                if (commands.containsKey(currentCommand)) {
                    commands.get(currentCommand).run(commandArgs);
                    System.out.println();
                } else {
                    System.out.println("Unrecognized command: " + currentCommand);
                }
            }
        }
    }
}


class HelpCommand implements interfaces.Commandable {
    public static final int RECOMMEND_COMMAND_NAME_LENGTH = 16;

    @Override
    public String getUsage() {
        return "help <command name>";
    }

    @Override
    public String getCommandName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Show all or specified command's information";
    }

    @Override
    public void run(String[] args) {
        if (args == null || args.length == 0) {
            List<String> commands = Console.getCommands();
            Collections.sort(commands);
            for (String command : commands) {
                String description = Console.getCommandDescription(command);
                if (description == null || description.isEmpty()) {
                    description = "No description";
                }
                String format;
                if (command.length() < RECOMMEND_COMMAND_NAME_LENGTH) {
                    format = "%s" + padRight(" ", RECOMMEND_COMMAND_NAME_LENGTH - command.length()) + "%s\n";
                } else {
                    format = "%s\n   | %s\n";
                }
                System.out.printf(format, command, description);
            }
        } else {
            String command = args[0];
            if (Console.containsCommand(command)) {
                String usage = Console.getCommandUsage(command);
                if (usage == null || usage.isEmpty()) {
                    usage = "No usage description";
                }
                System.out.printf("Command's \"%s\" usage:\n%s\n", command, usage);
            } else {
                System.out.println("Unrecognized command: " + command);
            }
        }
    }

    private static String padRight(String s, int n) {
        if (n <= 0) {
            return "";
        }
        return String.format("%1$-" + n + "s", s);
    }
}