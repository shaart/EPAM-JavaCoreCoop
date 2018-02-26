package general;

import interfaces.Commandable;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CommandConnector {
    private static String commandsDirectory = "commands";
    private static String packageFolder = "commands";

    /**
     * Trying to load command to map with commands by specified path of .class.
     *
     * @param commands Map with commands
     * @param classLoader Application's class loader
     * @param classPath Path to command's .class
     * @return Command's name or <code>null</code> if command was not loaded
     */
    private static String loadCommandToMap(Map<String, Commandable> commands, ClassLoader classLoader, Path classPath) {
        if (commands == null || classLoader == null || Files.isDirectory(classPath)) return null;

        try {
            String className = classPath.getFileName().toString();
            if (className.contains(".")) {
                className = className.substring(0, className.lastIndexOf("."));
            }
            className = packageFolder + "." + className;
            Class loadingClass = classLoader.loadClass(className);

            Class[] interfaces = loadingClass.getInterfaces();
            for (Class cInterface : interfaces) {
                if (cInterface == Commandable.class) {
                    Object obj = loadingClass.newInstance();
                    Commandable command = (Commandable) obj;

                    String commandName = command.getCommandName();
                    if (commands.containsKey(commandName)) {
                        commandName = loadingClass.getSimpleName();
                        if (commands.containsKey(commandName)) {
                            // skip this command
                            return null;
                        }
                    }

                    commands.put(commandName, command);
                    return commandName;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            return null;
        }

        return null;
    }

    public static Map<String, Commandable> getCommands() {
        Map<String, Commandable> commands = new HashMap<>();
        Path functionFolder = Paths.get(commandsDirectory);
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        final File jarFile = new File(CommandConnector.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath());
        if (jarFile.isFile()) {  // Run with JAR file
            try (final JarFile jar = new JarFile(jarFile)) {
                final Enumeration<JarEntry> entryEnumeration = jar.entries();
                while (entryEnumeration.hasMoreElements()) {
                    String fileName = entryEnumeration.nextElement().getName();
                    if (fileName.startsWith(commandsDirectory + "/")) { // filter according to the path
                        Path classPath = Paths.get(fileName);
                        if (!fileName.endsWith(".class")) {
                            continue;
                        }

                        loadCommandToMap(commands, classLoader, classPath);
                    }
                }
            } catch (Exception e) {
                System.out.println("Fail loading commands from jar!");
            }
        }

        if (Files.exists(functionFolder)) {
            System.out.println("=== Loading external commands...");
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(functionFolder)) {
                List<String> loaded = new ArrayList<>();
                List<String> failed = new ArrayList<>();
                for (Path classPath : directoryStream) {
                    if (Files.isDirectory(classPath)) continue;

                    String loadedCommand = loadCommandToMap(commands, classLoader, classPath);
                    if (loadedCommand == null) {
                        failed.add(classPath.getFileName().toString());
                    } else {
                        loaded.add(loadedCommand);
                    }
                }
                if (!failed.isEmpty()) {
                    System.out.println("Failed to load:");
                    for (String failedCommand : failed) {
                        System.out.println("   " + failedCommand);
                    }
                }
                System.out.println("Loaded:");
                for (String loadedCommand : loaded) {
                    System.out.println("   " + loadedCommand);
                }
                System.out.println("=== Commands successfully loaded!");
            } catch (IOException e) {
                System.out.println("=== Loading external commands failed!");
                e.printStackTrace();
            }
        }
        return commands;
    }
}
