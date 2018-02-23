package commands;

import general.Console;
import interfaces.Commandable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Rename implements Commandable {
    private static final String commandName = "rename";
    private static final String commandUsage = "rename <source> <new name>\n" +
            "  <source>\tFile with extension or directory.\n" +
            "  <new name>\tNew name of file (with extension) or directory.\n";
    private static final String commandDescription = "Renames directory or file";

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
        if (args == null || args.length < 2) {
            if (args != null && (args.length < 1 || args.length == 1 && args[0] != null &&
                    (args[0].equalsIgnoreCase("/?") || args[0].equalsIgnoreCase("help")))) {
                System.out.println(commandUsage);
            } else
                System.out.println("Incorrect arguments. Command usage:\n" + commandUsage);
        } else {
            Path source = Console.getCurrentPath().resolve(Paths.get(args[0]));
            if (!Files.exists(source)) {
                System.out.println("File or directory with name '" + args[0] + "' not found!");
                return;
            }

            try {
                Path newName = source.resolveSibling(args[1]);
                if (Files.notExists(newName)) {
                    Files.move(source, newName);
                    System.out.println("'" + args[0] + "' successfully renamed to '" + args[1] + "'");
                } else {
                    System.out.println("File or directory with name '" + args[1] + "' already exists!");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}
