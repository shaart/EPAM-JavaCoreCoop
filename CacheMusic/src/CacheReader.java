import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CacheReader {
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
     * Searches at <code>partsPaths</code> for name in MP3 metadata
     *
     * @param partsPaths List with file names
     * @return Name of file or <code>null</code> if not found
     */
    public static String searchSongName(List<String> partsPaths) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks for metadata at file.
     *
     * @param filePath Path to file
     * @return <code>true</code> - if contains metadata<br><code>false</code> - if not found
     */
    public static boolean containsMetadata(String filePath) {
        return containsMetadata(Paths.get(filePath));
    }

    /**
     * Checks for metadata at file.
     *
     * @param filePath Path to file
     * @return <code>true</code> - if contains metadata<br><code>false</code> - if not found
     */
    public static boolean containsMetadata(Path filePath) {
        return containsMetadataAtStart(filePath) || containsMetadataAtEnd(filePath);
    }

    /**
     * Checks for metadata at start of file.
     *
     * @param filePath Path to file
     * @return <code>true</code> - if contains metadata<br><code>false</code> - if not found
     */
    private static boolean containsMetadataAtStart(Path filePath) {
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) return false;
        String fileAbsolutePath = filePath.toAbsolutePath().toString();

        final int MAX_TAG_LENGTH = 4; // ID3v1 tag is 4 bytes (TAG+)
        final String META_TAG_ID3v1 = "TAG";
        final String META_TAG_ID3v2 = "ID3";

        try (FileInputStream file = new FileInputStream(fileAbsolutePath)) {
            byte[] header = new byte[MAX_TAG_LENGTH]; // 3 for ID3, 4 for ID3v1

            int readBytes = file.read(header);
            if (readBytes > 0) {
                String tag = new String(header).toUpperCase();
                if (tag.contains(META_TAG_ID3v1) || tag.contains(META_TAG_ID3v2)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    /**
     * Checks for metadata at end of file.
     *
     * @param filePath Path to file
     * @return <code>true</code> - if contains metadata<br><code>false</code> - if not found
     */
    private static boolean containsMetadataAtEnd(Path filePath) {
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) return false;
        String fileAbsolutePath = filePath.toAbsolutePath().toString();

        final int TAG_POST_PENDED_LENGTH = 10;
        final String META_TAG_ID3v2_REVERSED = "3DI";
        int fileSizeInBytes = 0;
        try {
            fileSizeInBytes = (int) Files.size(filePath);
        } catch (IOException e) {
            return false;
        }
        try (RandomAccessFile file = new RandomAccessFile(fileAbsolutePath, "r")) {
            file.skipBytes(fileSizeInBytes - TAG_POST_PENDED_LENGTH);
            byte[] header = new byte[TAG_POST_PENDED_LENGTH];

            file.readFully(header, 0, TAG_POST_PENDED_LENGTH);
            String tag = new String(header).toUpperCase();
            if (tag.contains(META_TAG_ID3v2_REVERSED)) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    /**
     * Seaching for cache path by user's OS.
     *
     * @return Path to Media Cache
     */
    private static String detectCachePath() {
        String userOS = System.getProperty("os.name").toLowerCase();

        if (userOS.contains("win")) {
            // WINDOWS
            // <ROOT>:\Users\<USERNAME>\AppData\Local\Google\Chrome\User Data\Default\Media Cache
            String googleChromeMusicCache = Paths.get(System.getProperty("user.home") , "AppData", "Local", "Google", "Chrome", "User Data", "Default", "Media Cache")
                    .toAbsolutePath().toString();
            return googleChromeMusicCache;
        } else if (userOS.contains("mac")) {
            // MAC OS

        } else if (userOS.contains("nix") || userOS.contains("nux") || userOS.contains("aix")) {
            // UNIX
            // $Home/.cache/google-chrome/Default/Media\ Cache/

        } else if (userOS.contains("sunos")) {
            // SOLARIS

        } else {
            // UNKNOWN OS
            throw new UnsupportedOperationException("Unknown OS!");
        }

        throw new UnsupportedOperationException();
    }

    /**
     * Default program's entry point
     *
     * @param args Program arguments
     */
    public static void main(String[] args) {
        String musicCachePath = "";
        try {
            musicCachePath = detectCachePath();
        } catch (UnsupportedOperationException e) {
            e.getMessage();
            return;
        }

        List<List<String>> songs = scan(musicCachePath);
        long filesAnalysed = 0;
        for (List<String> songParts : songs) {
            String fileName = searchSongName(songParts);
            if (fileName == null) {
                FileBuilder.build(songParts);
            } else {
                FileBuilder.build(songParts, fileName);
            }
            filesAnalysed += songParts.size();
        }
        System.out.format("Cache was successfully read!\n== Statistics:\n  Analysed part files: %d.\n  Found songs: %d.",
                filesAnalysed, songs.size());
    }
}
