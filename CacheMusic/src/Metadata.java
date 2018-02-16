import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Metadata {
    public enum FormatName {NONE, ID3v1, ID3v2}

    private Metadata() {};

    private String tag;
    private String version;
    private String artist;
    private String title;

    public String getTag() {
        return tag;
    }

    public String getVersion() {
        return version;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Checks for metadata at file.
     *
     * @param filePath Path to file
     * @return <code>true</code> - if contains metadata<br><code>false</code> - if not found
     */
    public static boolean contains(String filePath) {
        return contains(Paths.get(filePath));
    }

    /**
     * Checks for metadata at file.
     *
     * @param filePath Path to file
     * @return <code>true</code> - if contains metadata<br><code>false</code> - if not found
     */
    public static boolean contains(Path filePath) {
        return containsAtStart(filePath) || containsAtEnd(filePath);
    }

    /**
     * Checks for metadata at file.
     *
     * @param filePath Path to file
     * @return Found metadata format
     */
    public static FormatName getFormat(Path filePath) {
        FormatName formatAtStart = getFormatAtStart(filePath);
        if (formatAtStart != FormatName.NONE) {
            return formatAtStart;
        } else {
            FormatName formatAtEnd = getFormatAtStart(filePath);
            if (formatAtEnd != FormatName.NONE) {
                return formatAtEnd;
            }
        }
        return FormatName.NONE;
    }

    /**
     * Checks for metadata at start of file.
     *
     * @param filePath Path to file
     * @return <code>true</code> - if contains metadata<br><code>false</code> - if not found
     */
    public static boolean containsAtStart(Path filePath) {
        return getFormatAtStart(filePath) != FormatName.NONE;

    }
    /**
     * Checks for metadata at start of file.
     *
     * @param filePath Path to file
     * @return Found metadata format
     */
    public static FormatName getFormatAtStart(Path filePath) {
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) return FormatName.NONE;
        String fileAbsolutePath = filePath.toAbsolutePath().toString();

        final int MAX_TAG_LENGTH = 4; // ID3v1 tag is 4 bytes (TAG+)
        final String META_TAG_ID3v1 = "TAG";
        final String META_TAG_ID3v2 = "ID3";

        try (FileInputStream file = new FileInputStream(fileAbsolutePath)) {
            byte[] header = new byte[MAX_TAG_LENGTH]; // 3 for ID3, 4 for ID3v1

            int readBytes = file.read(header);
            if (readBytes > 0) {
                String tag = new String(header).toUpperCase();
                if (tag.contains(META_TAG_ID3v1)) {
                    return FormatName.ID3v1;
                } else if (tag.contains(META_TAG_ID3v2)) {
                    return FormatName.ID3v2;
                }
            }
        } catch (Exception e) {
            return FormatName.NONE;
        }

        return FormatName.NONE;
    }

    /**
     * Checks for metadata at end of file.
     *
     * @param filePath Path to file
     * @return <code>true</code> - if contains metadata<br><code>false</code> - if not found
     */
    public static boolean containsAtEnd(Path filePath) {
        return getFormatAtEnd(filePath) != FormatName.NONE;
    }

    /**
     * Checks for metadata at end of file.
     *
     * @param filePath Path to file
     * @return Found metadata format
     */
    public static FormatName getFormatAtEnd(Path filePath) {
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) return FormatName.NONE;
        String fileAbsolutePath = filePath.toAbsolutePath().toString();

        final int TAG_POST_PENDED_LENGTH = 10;
        final String META_TAG_ID3v2_REVERSED = "3DI";
        int fileSizeInBytes = 0;
        try {
            fileSizeInBytes = (int) Files.size(filePath);
        } catch (IOException e) {
            return FormatName.NONE;
        }
        try (RandomAccessFile file = new RandomAccessFile(fileAbsolutePath, "r")) {
            file.skipBytes(fileSizeInBytes - TAG_POST_PENDED_LENGTH);
            byte[] header = new byte[TAG_POST_PENDED_LENGTH];

            file.readFully(header, 0, TAG_POST_PENDED_LENGTH);
            String tag = new String(header).toUpperCase();
            if (tag.contains(META_TAG_ID3v2_REVERSED)) {
                return FormatName.ID3v2;
            }
        } catch (IOException e) {
            return FormatName.NONE;
        }

        return FormatName.NONE;
    }
}

class ID3v1 {
    public static final String ARTIST = "TPE1";
    public static final String TITLE = "TIT2";
    public static final String ALBUM = "TALB";
    public static final String YEAR = "TYER";
    public static final String COMMENT = "vCOMM";
    public static final String GENRE = "TCON";
}