package general;

import interfaces.Commandable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CommandConnector {
    private static String functionFolderPath = "commands";
    private static String packageFolder = "commands";

    public static Map<String, Commandable> getCommands() {
        Map<String, Commandable> commands = new HashMap<>();
        Path functionFolder = Paths.get(functionFolderPath);
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(functionFolder)) {
            for (Path classPath : directoryStream) {
                String className = classPath.getFileName().toString();
                if (className.contains(".")) {
                    className = className.substring(0, className.lastIndexOf("."));
                }
                className = packageFolder + "." + className;
                Class running = classLoader.loadClass(className);

                Class[] interfaces = running.getInterfaces();
                for (Class cInterface : interfaces) {
                    if (cInterface == Commandable.class) {
                        Object obj = running.newInstance();
                        Commandable command = (Commandable) obj;

                        String commandName = command.getCommandName();
                        if (commands.containsKey(commandName)) {
                            commandName = running.getClass().getSimpleName();
                            if (commands.containsKey(commandName)) {
                                commandName = running.getClass().getName();
                                if (commands.containsKey(commandName)) {
                                    // skip this command
                                    break;
                                }
                            }
                        }

                        commands.put(commandName, command);
                        break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        return commands;
    }
}
