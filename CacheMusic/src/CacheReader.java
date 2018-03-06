import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
     * @throws IllegalArgumentException Cache folder not exists or is not directory
     */
    public static List<List<String>> scan(String cacheFolderPath) throws IllegalArgumentException {
        Path cacheFolder = Paths.get(cacheFolderPath);
        if (!Files.exists(cacheFolder) || !Files.isDirectory(cacheFolder)) {
            throw new IllegalArgumentException("Received path cache folder not exist or is not folder");
        } else {
            List<List<String>> songs = new ArrayList<>();
            String lastFoundSongName;
            String currentSongName;
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(cacheFolder)) {
                List<String> songParts = new ArrayList<>();
                for (Path songPart : directoryStream) {
                    if (songPart.getFileName().toString().startsWith(CACHE_FILE_NAME_PREFIX)
                            && Files.size(songPart) <= CACHE_PART_SIZE_IN_BYTES) {
                        if (Metadata.contains(songPart) && songParts.size() > 0) { // new .mp3 file
                            lastFoundSongName = searchSongName(songParts);
                            currentSongName = searchSongName(songPart.toAbsolutePath().toString());
                            if (lastFoundSongName == null || lastFoundSongName != null && !lastFoundSongName.equals(currentSongName)) {
                                songs.add(songParts); // save prev list of parts
                                songParts = new ArrayList<>(); // and create new list of parts
                            }
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
     * Searches at <code>filePath</code> for name in MP3 metadata.
     *
     * @param filePath Path to file
     * @return Name of file or <code>null</code> if not found
     */
    public static String searchSongName(String filePath) {
        String songName = null;

        try {
            Metadata metadata = Metadata.read(filePath);
            if (metadata != null) {
                String songArtist = metadata.getArtist();
                if (songArtist != null && !songArtist.trim().isEmpty()) {
                    songName = songArtist.trim();
                }

                String songTitle = metadata.getTitle();
                if (songTitle != null && !songTitle.trim().equals("")) {
                    if (songName != null) {
                        songName += " - " + songTitle.trim();
                    } else {
                        songName = songTitle.trim();
                    }
                }
            }
        } catch (UnsupportedOperationException e) {
            // Nothing to do
        }

        return songName;
    }

    /**
     * Seaching for cache path by user's OS.
     *
     * @return Path to Media Cache
     */
    private static String detectCachePath() {
        String userOS = System.getProperty("os.name").toLowerCase();
        String cachePath = "";

        if (userOS.contains("win")) {
            // WINDOWS
            // <ROOT>:\Users\<USERNAME>\AppData\Local\Google\Chrome\User Data\Default\Media Cache
            String googleChromeMusicCache = Paths.get(System.getProperty("user.home"), "AppData", "Local",
                    "Google", "Chrome", "User Data", "Default", "Media Cache")
                    .toAbsolutePath().toString();
            cachePath = googleChromeMusicCache;
        } else if (userOS.contains("mac")) {
            // MAC OS
            throw new UnsupportedOperationException();
        } else if (userOS.contains("nix") || userOS.contains("nux") || userOS.contains("aix")) {
            // UNIX
            // $Home/.cache/google-chrome/Default/Media\ Cache/
            throw new UnsupportedOperationException();
        } else if (userOS.contains("sunos")) {
            // SOLARIS
            throw new UnsupportedOperationException();
        } else {
            // UNKNOWN OS
            throw new UnsupportedOperationException("Unknown OS!");
        }

        return cachePath;
    }

    /**
     * Default program's entry point
     *
     * @param args Program arguments
     */
    public static void main(String[] args) {
        final String PROGRAM_USAGE = "Program usage: CacheReader [-o <output_folder>] [-c <cache_folder>]\nParameters:\n" +
                "   -o <output_folder>\tDestination folder for found songs. Default path: " +
                FileBuilder.DEFAULT_OUTPUT_FOLDER + "\n" +
                "   -c <cache_folder>\tPath to folder with cache files\n";

        String arg0 = args.length > 0 ? args[0].toLowerCase() : "";
        if (args.length > 0 && (arg0.equals("help") || arg0.equals("?") || arg0.equals("/?"))) {
            System.out.println(PROGRAM_USAGE);
            return;
        }

        Path outputFolder = null;
        String musicCacheFolder = null;

        String lastArg = "";
        final String ARG_OUTPUT = "-o";
        final String ARG_CACHE = "-c";
        List<String> argPrefixes = new ArrayList<String>() {
            {
                add(ARG_OUTPUT);
                add(ARG_CACHE);
            }
        };
        try {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i].toLowerCase();
                if (lastArg.isEmpty() && argPrefixes.contains(arg)) {
                    lastArg = arg;
                } else {
                    if (argPrefixes.contains(args[i])) {
                        System.err.format("Error: Waited for value of '%s' parameter, but found parameter: %s\n",
                                lastArg, args[i]);
                        return;
                    }
                    switch (lastArg) {
                        case ARG_OUTPUT:
                            outputFolder = Paths.get(args[i]);
                            lastArg = "";
                            break;
                        case ARG_CACHE:
                            musicCacheFolder = args[i];
                            Path cachePath = Paths.get(musicCacheFolder);
                            if (!Files.exists(cachePath)) {
                                System.err.println(
                                        "Error: Received <cache_folder> not exists or program don't have access to folder!");
                                return;
                            }
                            if (!Files.isDirectory(cachePath)) {
                                System.err.println("Error: Received <cache_folder> is not folder!");
                                return;
                            }
                            lastArg = "";
                            break;
                        default:
                            System.err.println("Found unknown parameter: " + args[i]);
                            System.out.println(PROGRAM_USAGE);
                            return;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Incorrect parameters");
            return;
        }
        System.out.format("Starting program with parameters:\n\tOutput folder: %s\n",
                outputFolder == null ? "[default] " + FileBuilder.DEFAULT_OUTPUT_FOLDER : outputFolder);
        try {
            if (musicCacheFolder == null) {
                musicCacheFolder = detectCachePath();
                System.out.format("\tCache folder: [default] %s\n", musicCacheFolder);
            } else {
                System.out.format("\tCache folder: %s\n", musicCacheFolder);
            }
            List<List<String>> songs = scan(musicCacheFolder);
            long filesAnalysed = 0;
            int statsSongsProcessed = 0;
            for (List<String> songParts : songs) {
                String fileName = searchSongName(songParts);
                try {
                    if (fileName == null) {
                        if (outputFolder == null) {
                            FileBuilder.build(songParts);
                        } else {
                            FileBuilder.build(songParts, outputFolder);
                        }
                    } else {
                        if (outputFolder == null) {
                            FileBuilder.build(songParts, fileName);
                        } else {
                            FileBuilder.build(songParts, outputFolder, fileName);
                        }
                    }
                    filesAnalysed += songParts.size();
                } catch (IOException e) {
                    System.err.format("An error occurred while creating %s song from parts!\nError: %s\n",
                            fileName == null ? "unknown" : fileName,
                            e.getMessage());
                }
                statsSongsProcessed++;
                System.out.format("\rProgress: %d%% (%d/%d)",
                        statsSongsProcessed * 100 / songs.size(),
                        statsSongsProcessed,
                        songs.size());
            }
            System.out.format(
                    "\nCache was successfully read!\n== Statistics:\n  Analysed part files: %d.\n  Found songs: %d.",
                    filesAnalysed, songs.size());
        } catch (UnsupportedOperationException e) {
            System.err.println("Error: " + e.getMessage() + " at");
            e.printStackTrace();
            return;
        }
    }
}
