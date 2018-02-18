import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class FileBuilder {

    //    private static String cashFolderPath = "";
    private final static Path DEFAULT_OUTPUT_FOLDER = Paths.get(".", "Tracks");
    private final static String DEFAULT_SONG_NAME = "Unnamed";

    private static final String MP3 = ".mp3";

    /**
     * Make an unique name for every track
     *
     * @param outputFolder folder which will contain file with current <code>fileName</code>
     * @param fileName     default file name
     * @return a path contains an unique name for track
     */
    private static Path songEnumerator(String outputFolder, String fileName) {
        int number = 1;
        while (Files.exists(Paths.get(outputFolder, fileName + number + MP3))) {
            number++;
        }
        return Paths.get(outputFolder, fileName + number);
    }

    /**
     * Collect track parts to full track; Additional parameters;
     *
     * @param files track parts
     * @throws IOException
     */
    public static void build(List<String> files) throws IOException {

        build(files, DEFAULT_OUTPUT_FOLDER, DEFAULT_SONG_NAME);
    }


    /**
     * Collect track parts to full track; Additional parameters;
     *
     * @param files    track parts
     * @param fileName name of full track which we get finally
     * @throws IOException
     */
    public static void build(List<String> files, String fileName) throws IOException {

        build(files, DEFAULT_OUTPUT_FOLDER, fileName);
    }

    /**
     * Collect track parts to full track; Additional parameters;
     *
     * @param files        track parts
     * @param outputFolder folder that will contain final track
     * @throws IOException
     */
    public static void build(List<String> files, Path outputFolder) throws IOException {

        build(files, outputFolder, "Unnamed");
    }

    /**
     * Collect track parts to full track; Additional parameters;
     *
     * @param files        track parts
     * @param fileName     name of full track which we get finally
     * @param outputFolder folder that will contain final track
     * @throws IOException
     */
    public static void build(List<String> files, Path outputFolder, String fileName) throws IOException {

        if (Files.notExists(outputFolder))
            Files.createDirectories(outputFolder);

        Path outSong = Paths.get(outputFolder.toAbsolutePath().toString(), fileName + MP3);

        if (Files.notExists(outSong))
            Files.createFile(outSong);
        else
            outSong = Files.createFile(Paths.get(songEnumerator(outputFolder.toAbsolutePath().toString(), fileName) + MP3));


        for (String str : files) {

            Path tmp = Paths.get(str);

            if (Files.isWritable(outSong))
                try {
                    Files.write(outSong, Files.readAllBytes(tmp), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Error while song building " + fileName);
                }
        }
    }
}
