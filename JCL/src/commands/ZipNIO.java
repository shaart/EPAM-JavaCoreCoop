package commands;

import interfaces.Commandable;

import java.nio.file.Paths;


public class ZipNIO implements Commandable {

    private final String commandName = "zip";

    private static final String commandUsage = "zip <argument> <source> \n" +
            "  <argument>\tchoose wisely: \n" +
            "\t\ta - archive, turn current file to zip archive \n" +
            "\t\tu - unarchive, unzip source file \n"+
            "  <source>\tFile or directory to zip / unzip\n";


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
        return null;
    }

    @Override
    public void run(String[] args) {

        if (args == null || args.length == 0 || args[1]==null){
            System.out.println(getUsage());
            return;
        }

        String compressionIndex = args[0];

        if (compressionIndex.trim().equals("a")) {

            if(true)
                System.out.println("zip method: ");
            // zip

            else
                System.out.println("File " + args[1] + " doesn't exist");
        }
        else if (compressionIndex.trim().equals("u")){

            if(true)
                System.out.println("unzip method: ");
            //unzip
            else
                System.out.println("File "+ args[1] +" doesn't exist");
        }
        else {
            System.out.println("Wrong argument " + compressionIndex);
            System.out.println(getUsage());
        }

    }
}
