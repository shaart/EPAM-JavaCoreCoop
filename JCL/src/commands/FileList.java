package commands;

import general.Console;
import interfaces.Commandable;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileList implements Commandable {
    private static final String commandName = "dir";
    private static final String commandUsage = "dir [path] [-s:one of sorting fields] [-o:one of sorting orders] [-i:additional info]\n" +
            "  [path]\tPath to directory.\n" +
            "  [-s]\tSorting fields:\n" +
            "  \t\tN  by file name (default)\n" +
            "  \t\tS  by size\n" +
            "  \t\tD  by date\n" +
            "  [-o]\tSorting orders:\n" +
            "  \t\tA  Ascending (default)\n" +
            "  \t\tD  Descending\n" +
            "  [-i]\tAdditional information:\n" +
            "  \t\tS  size\n" +
            "  \t\tD  date\n";
    private static final String commandDescription = "Prints directory contents.";

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
            if (args.length > 0 && !args[0].contains("-s:") && !args[0].contains("-o:") && !args[0].contains("-i:")) {
                directory = Console.getCurrentPath().resolve(Paths.get(args[0]));

                if (Files.notExists(directory)) {
                    System.out.println(args[0] + " not found!");
                    return;
                }
                if (!Files.exists(directory)) {
                    System.out.println("No access to " + args[0] + "!");
                    return;
                }
            } else {
                directory = Console.getCurrentPath();
            }

            Comparator<Path> comparator = null;
            boolean isDescendingSort = false;
            String info = "";
            for (int i = 0; i < args.length; i++) {
                String curArg = args[i].toLowerCase();
                if (curArg.startsWith("-s:") && curArg.length() >= 4) {
                    switch (curArg.charAt(3)) {
                        case 'n':
                            comparator = new NameComparator();
                            break;
                        case 's':
                            comparator = new SizeComparator();
                            break;
                        case 'd':
                            comparator = new DateComparator();
                            break;
                        default:
                            System.out.println("Unknown sort field: " + args[i].substring(3));
                            break;
                    }
                } else if (curArg.startsWith("-o:")) {
                    switch (curArg.charAt(3)) {
                        case 'a':
                            isDescendingSort = false;
                            break;
                        case 'd':
                            isDescendingSort = true;
                            break;
                        default:
                            System.out.println("Unknown sort order: " + args[i].substring(3));
                            break;
                    }
                } else if (curArg.startsWith("-i:")) {
                    for (int c = 3; c < curArg.length(); c++) {
                        switch (curArg.charAt(c)) {
                            case 's':
                            case 'd':
                                info += curArg.charAt(c);
                                break;
                        }
                    }
                }
            }
            if (comparator == null) {
                comparator = new NameComparator(); // default
            }

            if (!Files.isDirectory(directory)) {
                showInfo(directory, info);
            } else {
                List<Path> folders = new ArrayList<>();
                List<Path> files = new ArrayList<>();
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
                    for (Path p : stream) {
                        if (p.toFile().isDirectory()) {
                            folders.add(p);
                        } else {
                            files.add(p);
                        }
                    }
                } catch (IOException | DirectoryIteratorException e) {
                    // IOException can never be thrown by the iteration.
                    // In this snippet, it can only be thrown by newDirectoryStream.
                    System.err.println(e);
                }

                Collections.sort(folders, comparator);
                Collections.sort(files, comparator);
                if (isDescendingSort) {
                    Collections.reverse(folders);
                    Collections.reverse(files);
                }

                for (Path folder : folders) {
                    showInfo(folder, info);
                }
                for (Path file : files) {
                    showInfo(file, info);
                }
            }
        }
    }

    private void showInfo(Path fileOrFolder, String info) {
        System.out.print(fileOrFolder.getFileName());
        if (info.contains("s")) {
            long sizeBytes;
            String sizeOutFormat = " (%s)";
            try {
                sizeBytes = Files.size(fileOrFolder);
                System.out.format(sizeOutFormat, fileSizeOutput(sizeBytes));
            } catch (Exception e) {
                System.out.format(sizeOutFormat, "size: error on read");
            }
        }
        if (info.contains("d")) {
            String dateOutFormat = " [%s]";
            try {
                FileTime time = Files.getLastModifiedTime(fileOrFolder);
                DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                System.out.format(dateOutFormat, df.format(time.toMillis()).toString());
            } catch (Exception e) {
                System.out.format(dateOutFormat, "date: error on read");
            }
        }
        System.out.println();
    }

    private String fileSizeOutput(long bytes) {
        String format = "%d %s";
        if (bytes < 1024) {
            return String.format(format, bytes, "B");
        } else {
            long kilobytes = (bytes / 1024);
            return String.format(format, kilobytes, "KB");
        }
    }
}

class DateComparator implements Comparator<Path> {
    public int compare(java.nio.file.Path o1, java.nio.file.Path o2) {
        try {
            return Files.getLastModifiedTime(o1).compareTo(Files.getLastModifiedTime(o2));
        } catch (IOException e) {
            // handle exception
            return 0;
        }
    }
}

class SizeComparator implements Comparator<Path> {
    public int compare(java.nio.file.Path o1, java.nio.file.Path o2) {
        try {
            return Long.compare(Files.size(o1), Files.size(o2));
        } catch (IOException e) {
            // handle exception
            return 0;
        }
    }
}

class NameComparator implements Comparator<Path> {
    public int compare(java.nio.file.Path o1, java.nio.file.Path o2) {
        return o1.getFileName().compareTo(o2.getFileName());
    }
}