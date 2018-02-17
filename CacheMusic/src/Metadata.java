import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Metadata {
    public enum FormatName {NONE, ID3v1, ID3v2, ID3v23}

    private Metadata() {
    }

    private FormatName format = FormatName.NONE;
    private String tag = null;
    private String version = null;
    private String artist = null;
    private String title = null;

    /**
     * Converts integer's bytes to integer.<br>
     * Auxiliary method for parsing metadata's information to numbers.
     *
     * @param b Bytes of number
     * @return Number equals these bytes
     */
    private static int byteArrayToInt(byte[] b) {
        int value = 0;
        for (int i = 0; i < b.length; i++) {
            int shift = (b.length - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }

    public static Metadata readID3v23(String file) {
        Metadata meta = new Metadata();
        meta.format = FormatName.ID3v23;
        final String TAGS_ENCODING = "UTF-8";
        byte[] buffer;

        final int BYTES_TAG_LENGTH = 3;
        final int BYTES_VERSION_LENGTH = 1;
        final int BYTES_SUBVERSION_LENGTH = 1;
        final int BYTES_FLAGS_LENGTH = 1;
        final int BYTES_HEADER_SIZE_LENGTH = 4;

        try (FileInputStream fileStream = new FileInputStream(file)) {
            buffer = new byte[BYTES_TAG_LENGTH];
            fileStream.read(buffer);
            meta.tag = new String(buffer, TAGS_ENCODING);

            buffer = new byte[BYTES_VERSION_LENGTH];
            fileStream.read(buffer);
            meta.version = Integer.toString(buffer[0]);

            buffer = new byte[BYTES_SUBVERSION_LENGTH];
            fileStream.read(buffer);
            int tagSubVersion = buffer[0];

            buffer = new byte[BYTES_FLAGS_LENGTH];
            fileStream.read(buffer);
            int flags = byteArrayToInt(buffer);
            byte flagUnsync = (byte) (buffer[0] & 0b100_000);
            byte flagExtendedHeader = (byte) (buffer[0] & 0b010_000);
            byte flagExperIndicator = (byte) (buffer[0] & 0b001_000);

            buffer = new byte[BYTES_HEADER_SIZE_LENGTH];
            fileStream.read(buffer);
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = (byte) (buffer[i] & 0b011_1111); // unset 7 bit
            }

            final int headerSize = byteArrayToInt(buffer);
            System.out.printf("== Read\n  %s v%s.%s\n  flags = %d (a:%d b:%d c:%d)\n  headerSize = %d\n",
                    meta.tag, meta.version, tagSubVersion,
                    flags, flagUnsync, flagExtendedHeader, flagExperIndicator,
                    headerSize);

            byte[] header = new byte[headerSize];
            int bytesRead = fileStream.read(header);
            System.out.println("Read header: " + bytesRead + " bytes");

            int frameRead;
            for (int i = 0; i < bytesRead; i += 10 + frameRead) {
                byte[] frameIDBytes = new byte[4]; // 4 letter TAGS
                System.arraycopy(header, i, frameIDBytes, 0, frameIDBytes.length);// buffer = Arrays.copyOf(header, 4, 4);
                String frameID = new String(frameIDBytes, TAGS_ENCODING);
                System.out.println("\nFrame id: " + frameID);
                byte[] frameSizeBytes = new byte[4];
                System.arraycopy(header, i + 4, frameSizeBytes, 0, frameSizeBytes.length);
                final int frameSize = byteArrayToInt(frameSizeBytes);
                System.out.println("Frame size: " + frameSize);

                byte[] frameFlags = new byte[2];
                System.arraycopy(header, i + 8, frameFlags, 0, frameFlags.length);
                byte[] encodingByte = new byte[1];
                System.arraycopy(header, i + 10, encodingByte, 0, encodingByte.length);
                int encoding = encodingByte[0];
                String codingName;
                switch (encoding) {
                    case 0:
                        codingName = "ISO_8859_1";
                        break;
                    case 1:
                        codingName = "UTF_16";
                        break;
                    case 2:
                        codingName = "UTF_16BE";
                        break;
                    case 3:
                        codingName = "UTF-8";
                        break;
                    default:
                        codingName = TAGS_ENCODING;
                        break;
                }
                final int remainedFrameSize = frameSize - encodingByte.length;
                if (remainedFrameSize > 0) {
                    buffer = new byte[remainedFrameSize];
                    if (header.length >= i + 11 + buffer.length) {
                        System.arraycopy(header, i + 11, buffer, 0, buffer.length);
                    }
                    String frameData = new String(buffer, codingName);
                    System.out.println("Frame data: " + frameData);

                    switch (frameID.toUpperCase()) {
                        case "TIT2":
                            meta.title = frameData;
                            break;
                        case "TPE1":
                            meta.artist = frameData;
                            break;
                        default:
                            break;
                    }
                }
                frameRead = frameSize;
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return meta;
    }

    public String getTag() {
        return tag;
    }

    public FormatName getFormat() {
        return format;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public static final int MAX_PRE_TAG_LENGTH_BYTES = 4; // ID3v1 tag is 4 bytes (TAG+)
    public static final String META_TAG_ID3v1 = "TAG";
    public static final String META_TAG_ID3v2 = "ID3";
    public static final String TAG_ID3v2_REVERSED = "3DI";
    public static final int TAG_POST_PENDED_LENGTH_BYTES = 10;

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

        try (FileInputStream file = new FileInputStream(fileAbsolutePath)) {
            byte[] header = new byte[MAX_PRE_TAG_LENGTH_BYTES]; // 3 for ID3, 4 for ID3v1

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

        int fileSizeInBytes = 0;
        try {
            fileSizeInBytes = (int) Files.size(filePath);
        } catch (IOException e) {
            return FormatName.NONE;
        }
        try (RandomAccessFile file = new RandomAccessFile(fileAbsolutePath, "r")) {
            file.skipBytes(fileSizeInBytes - TAG_POST_PENDED_LENGTH_BYTES);
            byte[] header = new byte[TAG_POST_PENDED_LENGTH_BYTES];

            file.readFully(header, 0, TAG_POST_PENDED_LENGTH_BYTES);
            String tag = new String(header).toUpperCase();
            if (tag.contains(TAG_ID3v2_REVERSED)) {
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

class ID3v23 {

}