

//import sun.reflect.Reflection;

import interfaces.Commandable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import commands.*;
import interfaces.Commandable;

public class Console {

    private static String functionFolderPath = ".//src//commands//";

    public static void main(String[] args) {

        Map<String, Commandable> commands = new HashMap<>();

        Path functionFolder = Paths.get(functionFolderPath);

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();


        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(functionFolder)) {


            for (Path classPath : directoryStream) {
                //System.out.println(classPath);
                String className = classPath.getFileName().toString();
                // System.out.println(className);
                className = className.substring(0, className.lastIndexOf("."));
                className = "commands." + className;
                //System.out.println(className);
                Class running = classLoader.loadClass(className);

                //Class[] interfaces = running.getInterfaces();
                //System.out.println(running.getName());
                //for (Class cInterface : interfaces) {
                Object obj = running.newInstance();
                if (obj instanceof Commandable) {
                    Commandable command = (Commandable) obj;
                    String commandName = command.getCommandName();
                    commands.put(commandName, (Commandable) running.newInstance());


                    System.out.println("commandName: " + commandName);
                    System.out.println("command: " +commands.get(commandName));
                    System.out.println("size: " +commands.size());




                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}

