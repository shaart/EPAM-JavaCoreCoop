package commands;

import general.Console;
import interfaces.Commandable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateFolder implements Commandable {
    private static final String commandName = "mkdir";
    private static final String commandUsage = "mkdir <path>\n" +
            "  <path>\tPath to creating directory.\n";
    private static final String commandDescription = "Creates directory by path.";

    @Override
    public String getUsage() {
        return commandUsage;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public String getDescription() {
        return commandDescription;
    }

    @Override
    public void run(String[] args) {
        if (args == null || args.length == 1 && args[0] != null &&
                (args[0].equalsIgnoreCase("/?") || args[0].equalsIgnoreCase("help"))) {
            System.out.println(commandUsage);
        } else {
            Path directory;
            try {
                directory = Console.getCurrentPath().resolve(Paths.get(args[0]));
            } catch (Exception e) {
                System.out.println("Error: incorrect path.");
                return;
            }

            if (!Files.notExists(directory)) {
                System.out.println("File or directory by this path already exists.");
                return;
            }

            try {
                Files.createDirectories(directory);
                System.out.println("Directory \"" + directory + "\" created.");
            } catch (IOException e) {
                System.out.println("Can't create directory at path \"" + directory + "\".\nError: " + e.getMessage());
            }
        }
    }
}
