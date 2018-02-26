package commands;//package functions;


import interfaces.Commandable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zip implements Commandable {

    private final String commandName = "zipIO";

    private static final String commandUsage = "zipIO <argument> <source> \n" +
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
    public void run(String args[]) {

        if (args == null || args.length == 0 || args[1]==null){
            System.out.println(getUsage());
            return;
        }

        String compressionIndex = args[0];

        if (compressionIndex.trim().equals("a")) {
            if (new File(args[1]).exists())
                zip(args[1]);
            else
                System.out.println("File " + args[1] + " doesn't exist");
        }
        else if (compressionIndex.trim().equals("u")){
            if (new File(args[1].concat(".zip")).exists() )
                unzip (args[1]);
            else
                System.out.println("File "+ args[1] +" doesn't exist");
        }
        else {
            System.out.println("Wrong argument " + compressionIndex);
            System.out.println(getUsage());
        }
        // Path folderToZip = Paths.get(args[0]);
    }

    private void zip (String fileName){
        //String fileName = args[1];

        File toZip = new File(fileName);
        //FileOutputStream fos = null;
        try (FileOutputStream fos = new FileOutputStream(fileName +".zip")){

            try(ZipOutputStream zipOut = new ZipOutputStream(fos)) {
                zipFile(toZip, toZip.getName(), zipOut);
            }catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("file " + fileName + ".zip has been successfully created");
    }

    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            for (File childFile : children != null ? children : new File[0]) {
                zipFile(childFile, fileName + "\\" + childFile.getName(), zipOut);
                System.out.println("file zipped : "+ childFile.getAbsoluteFile());
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }


    private void unzip(String fileZip){
        fileZip += ".zip";
        System.out.println(fileZip);
        //String outputZip = fileZip.substring(0, fileZip.lastIndexOf(""));

        //System.out.println(outputZip);
        //System.out.println(fileZip);

        File outputZip = new File(new File(fileZip).getParentFile().getName());
        //outputZip = outputZip.getParentFile();

        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip))) {

            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File newFile = new File(String.valueOf(outputZip + File.separator + fileName));

                System.out.println("file unzip : "+ newFile.getAbsoluteFile());

                //create all non exists folders
                new File(newFile.getParent()).mkdirs();


                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

