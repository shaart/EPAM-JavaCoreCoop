package commands;//package functions;


import interfaces.Commandable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip implements Commandable {

    private final String commandName = "zip";


    @Override
    public String getUsage() {
        return null;
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
        String compressionIndex = args[0];
       // Path folderToZip = Paths.get(args[0]);
        String fileName = args[1];

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

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            for (File childFile : children != null ? children : new File[0]) {
                zipFile(childFile, fileName + "\\" + childFile.getName(), zipOut);
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
}

