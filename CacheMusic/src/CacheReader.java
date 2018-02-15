import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CacheReader {
    private CacheReader() {}
    private final static String CACHE_FILE_NAME_PREFIX = "f_";
    private final static long CACHE_PART_SIZE_IN_BYTES = 1024;

    private CacheReader() {
    }

    /**
     * Scans folder and make lists of song parts for build.<br>
     * Each <code>List&lt;String&gt;</code> contains paths to parts of song, that must be assembled.
     *
     * @param cacheFolderPath Path to cache folder
     * @return List of songs that must be assembled. Each song presented as list of part files.<br>
     * If received <code>cacheFolderPath</code> not exists or is not a directory - returns <code>null</code>.
     */
    public static List<List<String>> scan(String cacheFolderPath) {
        Path cacheFolder = Paths.get(cacheFolderPath);
        if (Files.exists(cacheFolder) && Files.isDirectory(cacheFolder)) {
            List<List<String>> songs = new ArrayList<>();

            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(cacheFolder)) {
                List<String> songParts = new ArrayList<>();
                for (Path songPart : directoryStream) {
                    if (songPart.getFileName().startsWith(CACHE_FILE_NAME_PREFIX)
                            && Files.size(songPart) <= CACHE_PART_SIZE_IN_BYTES) {
                        if (containsMetadata(songPart) && songParts.size() > 0) { // new .mp3 file
                            songs.add(songParts); // save prev list of parts
                            songParts = new ArrayList<>(); // and create new list of parts
                        }
                        songParts.add(songPart.toAbsolutePath().toString());
                    }
                }
                if (songParts.size() > 0) { // if last read part was separate song
                    songs.add(songParts);
                }
            } catch (IOException ex) {
                System.err.println("Something went wrong when trying to scan a cache folder!\n" + ex.getMessage());
            }

            return songs;
        } else {
            return null;
        }
    }

    /**
     * Search at files for name in MP3 metadata
     * @param files List with file names
     * @return Name of file or <code>null</code> if not found
     */
    public static String searchForName(List<String> files) {
        boolean found = true;

        if (found)
            return "name";

        return null;
    }

    public static void main(String[] args) {
        System.out.println("a".compareTo("9"));
        System.out.println("a9f".compareTo("aaf"));

        String windowsLocalAppData = "%localappdata%";
        String googleChromeMusicCache = windowsLocalAppData + "\\Google\\Chrome\\User Data\\Default\\Media Cache";

        List<String> files = scan(googleChromeMusicCache);
        String fileName = searchForName(files);
        FileBuilder.build(files, fileName);
    }
}
