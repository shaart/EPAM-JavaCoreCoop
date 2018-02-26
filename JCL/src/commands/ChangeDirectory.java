package commands;

import general.Console;
import interfaces.Commandable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChangeDirectory implements Commandable {
    private static final String commandName = "cd";
    private static final String commandUsage = "cd <path>\n" +
            "  <path>\tPath to new directory.\n";
    private static final String commandDescription = "Change console's current path.";

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
        if (args == null || args.length == 0 || args.length >= 1 && args[0] != null &&
                (args[0].equalsIgnoreCase("/?") || args[0].equalsIgnoreCase("help"))) {
            System.out.println(commandUsage);
        } else {
            try {
                Path arg = Paths.get(args[0]);
                Path newPath = Console.getCurrentPath().resolve(arg).normalize();
                if (Files.notExists(newPath) || !Files.isDirectory(newPath)) {
                    System.out.println("Error: Incorrect path \"" + newPath + "\". The path must be an existing directory.");
                    return;
                }
                if (!Files.exists(newPath)) {
                    System.out.println("Error: No access to path \"" + newPath + "\".");
                    return;
                }

                Console.setCurrentPath(newPath);
            } catch (Exception e) {
                System.out.println("Error. Incorrect argument: " + args[0]);
            }
        }
    }
}
