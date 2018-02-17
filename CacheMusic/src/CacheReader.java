import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/* TODO
    Encoding UTF-8/UTF-16
    - find byte where stores information about encoding
    - read meta info with correct encoding
    - parse required data to song name with extension
 */

public class CacheReader {

    private final static String CACHE_FILE_NAME_PREFIX = "f_";
    private final static long CACHE_PART_SIZE_IN_BYTES = 1024 * 1024;

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
                    if (songPart.getFileName().toString().startsWith(CACHE_FILE_NAME_PREFIX)
                            && Files.size(songPart) <= CACHE_PART_SIZE_IN_BYTES) {
                        if (Metadata.contains(songPart) && songParts.size() > 0) { // new .mp3 file
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
     * Searches at <code>partsPaths</code> for name in MP3 metadata.
     *
     * @param partsPaths List with file names
     * @return First found name of file or <code>null</code> if not found
     */
    public static String searchSongName(List<String> partsPaths) {
        String songName = null;
        for (String partPath : partsPaths) {
            songName = searchSongName(partPath);
            if (songName != null) break;
        }

        return songName;
    }

    /**
     * Searches at <code>filePath</code> for name in MP3 metadata
     *
     * @param filePath Path to file
     * @return Name of file or <code>null</code> if not found
     */
    public static String searchSongName(String filePath) {
        String songName = null;
        Path path = Paths.get(filePath);
        String fileAbsolutePath = path.toAbsolutePath().toString();

        Metadata.FormatName format = Metadata.getFormatAtStart(path);
        File file = new File(filePath);
        String songArtist = null;
        String songTitle = null;
        switch (format) {
            case ID3v1:
                try (FileInputStream fileStream = new FileInputStream(file)) {
                    byte[] buffer = new byte[128];
                    fileStream.read(buffer);
                    songName = new String(buffer);
                    throw new UnsupportedOperationException("ID3v1 headers not realized");
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
                break;
            case ID3v2:
                Metadata data = Metadata.readID3v23(filePath);
                songArtist = data.getArtist();
                songTitle = data.getTitle();
                break;
            case NONE:
            default:
                songName = null;
                break;
        }

        if (songArtist != null) {
            songName = songArtist;
        }

        if (songTitle != null) {
            if (songArtist != null) songName += " - ";
            songName += songTitle;
        }

        if (songName == null) {
            if (Metadata.getFormatAtEnd(path) != Metadata.FormatName.NONE) {
                throw new UnsupportedOperationException("Post-pended metadata formats not supported");
            }
        }

        if (songName != null)
            songName += ".mp3";
        System.out.println("Result song name: " + songName);

        return songName;
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
            String googleChromeMusicCache = Paths.get(System.getProperty("user.home"), "AppData", "Local", "Google", "Chrome", "User Data", "Default", "Media Cache")
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

        } catch (UnsupportedOperationException e) {
            System.out.println("Error: " + e.getMessage() + " at");
            e.printStackTrace();
            return;
        }
    }
}
