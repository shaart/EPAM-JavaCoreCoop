import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class FileBuilder {

    private static String cashFolderPath = "";
    private static String outputFolder = "D:\\Music";

    private static final String MP3 = ".mp3";

   /*
    public String getCashFolderPath() {
        return cashFolderPath;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    private FileBuilder() {}

    */



    private static String songEnumerator(String outputFolder, String fileName){
        int number =1;
        while (Files.exists(Paths.get(outputFolder + "\\" + fileName + number + MP3))){
            number++;
            //fileName = fileName + "(" + number + ")";
        }
        return outputFolder + "\\" + fileName + number;
    }



    public static void build(List<String> files) throws IOException{

        build(files, "Default", outputFolder );
    }





    public static void build(List<String> files, String fileName) throws IOException {

        build(files,fileName, outputFolder);
    }






    public static void build(List<String> files, String fileName, String outputFolder) throws IOException {

        Path outSong = Paths.get(outputFolder + "\\" + fileName+ MP3);

        if (Files.notExists(outSong))
            Files.createFile(outSong);
        else
            outSong = Files.createFile( Paths.get(songEnumerator(outputFolder,fileName) + MP3));

       // OutputStream out = Files.newOutputStream(outSong, CREATE, APPEND);


            for (String str: files){

                Path tmp = Paths.get(str);
                System.out.println(tmp.toString());

                if (Files.isWritable(outSong))
                    try {
                        Files.write(outSong, Files.readAllBytes(tmp),StandardOpenOption.APPEND);
                        //Files.copy(outSong, tmp, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e){
                        e.printStackTrace();
                        System.out.println("не вышло");
                }
            }
    }
}
