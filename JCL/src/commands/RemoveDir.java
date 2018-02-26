package commands;

import general.Console;
import interfaces.Commandable;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RemoveDir implements Commandable {
    private static final String commandName = "rmdir";
    private static final String commandUsage = "rmdir [-a] <path>\n" +
            "  [-a]\tRemove file or directory with content. Without this parameter \n" +
            "      \tcommand removes only empty directory or single file.\n" +
            "  <path>\tPath to file or directory.\n";
    private static final String commandDescription = "Remove file or directory by path.";

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
            boolean removeTree = false;
            int pathIndex;
            if (args.length == 1) {
                pathIndex = 0;
            } else {
                if (args[0].equalsIgnoreCase("-a")) {
                    removeTree = true;
                } else {
                    System.out.println("Error: incorrect argument - " + args[0]);
                    return;
                }
                pathIndex = 1;
            }
            Path directory;
            try {
                directory = Console.getCurrentPath().resolve(Paths.get(args[pathIndex]));
            } catch (Exception e) {
                System.out.println("Error: incorrect path.");
                return;
            }

            if (Files.notExists(directory)) {
                System.out.println("File or directory by this path not exists.");
                return;
            }

            try {
                if (removeTree) {
                    Files.walkFileTree(directory, new RemoveFileTree());
                } else {
                    Files.deleteIfExists(directory);
                }
                System.out.println("\"" + directory + "\" removed.");
            } catch (IOException e) {
                System.out.format("Can't remove %s at path \"%s\".\nError: %s\n",
                        Files.isDirectory(directory) ? "directory" : "file",
                        directory,
                        (e instanceof DirectoryNotEmptyException) ? "Directory not empty" : e.getClass().getSimpleName());
            }
        }
    }

    class RemoveFileTree implements FileVisitor<Path> {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            System.out.format("Failed to delete \"%s\".\nError: %s\n", file, exc.getClass().getSimpleName());
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
