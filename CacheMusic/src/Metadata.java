import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Metadata {
    static enum Name { ID3v1, ID3v2 };
    String Tag;
    String Version;

    public static final String ARTIST = "TPE1";
    public static final String TITLE = "TIT2";
    public static final String ALBUM = "TALB";
    public static final String YEAR = "TYER";
    public static final String COMMENT = "vCOMM";
    public static final String GENRE = "TCON";

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
     * Checks for metadata at start of file.
     *
     * @param filePath Path to file
     * @return <code>true</code> - if contains metadata<br><code>false</code> - if not found
     */
    public static boolean containsAtStart(Path filePath) {
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
    public static boolean containsAtEnd(Path filePath) {
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
}