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

    /**
     * Command name in JCL console.
     */
    private final String commandName = "zip";

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


    /**
     *
     * @param args
     * args[0] - String compressionIndex, can be a - archive or u - unarchive
     * args[1] - String contains path to file or folder should be zipped
     * or unzipped.
     * run uses zip or unzip.
     */
    @Override
    public void run(String args[]) {

        if (args == null || args.length == 0 || args[1]==null){
            System.out.println(getUsage());
            return;
        }

        String compressionIndex = args[0];
        File check = new File(args[1]).getAbsoluteFile();

        if (compressionIndex.trim().equals("a")) {
            if (check.exists()) {
                    zip(check.getAbsolutePath().toString());
            }
            else
                System.out.println("File " + args[1] + " doesn't exist");
        }
        else if (compressionIndex.trim().equals("u")){
            if (check.exists() )
                    unzip(check.getAbsolutePath().toString());
            else
                System.out.println("File "+ args[1] +" doesn't exist");
        }
        else {
            System.out.println("Wrong argument " + compressionIndex);
            System.out.println(getUsage());
        }
    }

    /**
     * Turn chosen file to zip archive in file parent folder.
     *
     * @param fileName - file or folder to zip.
     */
    private void zip (String fileName){

        File toZip = new File(fileName);
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

    /**
     * Zip helper method.
     * Recursively archives folder with files it contains.
     * @param fileToZip - absolute way to file
     * @param fileName - name of file
     * @param zipOut - output folder
     * @throws IOException
     */
    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            for (File childFile : children != null ? children : new File[0]) {
                zipFile(childFile, fileName + File.separator + childFile.getName(), zipOut);
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

    /**
     * Unzip archive file if file parent folder.
     *
     * @param fileZip - zip file to unarchive
     */
    private void unzip(String fileZip){

        String outputZip = (new File(new File(fileZip).getParentFile().getAbsolutePath())).toString();

        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip))) {

            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {

                String fileName = zipEntry.getName();
                File newFile = new File(outputZip + File.separator + fileName);

                System.out.println("file unzip : "+ newFile.getAbsoluteFile());

                //create all non exists folders
                new File(newFile.getParent()).mkdirs();

                if (zipEntry.isDirectory()){
                    newFile.mkdir();
                }
                else {
                    newFile.createNewFile();

                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

