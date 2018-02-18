import javax.print.attribute.standard.MediaPrintableArea;
import java.io.File;
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
    private int version;
    private int subversion;
    private String artist = null;
    private String title = null;

    private boolean flagUnsync = false;
    private boolean flagExtendedHeader = false;
    private boolean flagExperIndicator = false;

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

    private static Metadata parseID3v1(byte[] header) {
        Metadata meta = null;
        if (header.length > 0) {
            meta = new Metadata();
            final String TAGS_ENCODING = "UTF-8";
            final String DATA_ENCODING = "ISO_8859_1";

            byte[] buffer;
            try {
                int offset = 0;
                buffer = new byte[ID3v1.TAG_LENGTH_BYTES];
                System.arraycopy(header, offset, buffer, 0, buffer.length);
                meta.tag = new String(buffer, TAGS_ENCODING);
                offset += ID3v1.TAG_LENGTH_BYTES;
                if (meta.tag.toUpperCase().equals("TAG")) {
                    meta.format = FormatName.ID3v1;

                    buffer = new byte[ID3v1.TITLE_LENGTH_BYTES];
                    System.arraycopy(header, offset, buffer, 0, buffer.length);
                    meta.title = new String(buffer, DATA_ENCODING);
                    offset += ID3v1.TITLE_LENGTH_BYTES;

                    buffer = new byte[ID3v1.ARTIST_LENGTH_BYTES];
                    System.arraycopy(header, offset, buffer, 0, buffer.length);
                    meta.artist = new String(buffer, DATA_ENCODING);
                    offset += ID3v1.ARTIST_LENGTH_BYTES;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return meta;
    }

    public static Metadata readID3v1(String file) {
        Metadata meta = null;

        final String TAGS_ENCODING = "UTF-8";
        byte[] buffer;
        byte[] header = new byte[ID3v1.HEADER_LENGTH];
        try (FileInputStream fileStream = new FileInputStream(file)) {
            int readBytes = fileStream.read(header);
            if (readBytes > 0) {
                buffer = new byte[ID3v1.TAG_LENGTH_BYTES];
                System.arraycopy(header, 0, buffer, 0, buffer.length);
                if (new String(buffer, TAGS_ENCODING).equals("TAG")) {
                    meta = parseID3v1(header);
                }
            }
            if (meta == null) {
                File f = new File(file);
                fileStream.skip(f.length() - readBytes - header.length);
                readBytes = fileStream.read(header);
                if (readBytes > 0) {
                    buffer = new byte[ID3v1.TAG_LENGTH_BYTES];
                    System.arraycopy(header, 0, buffer, 0, buffer.length);
                    if (new String(buffer, TAGS_ENCODING).equals("TAG")) {
                        meta = parseID3v1(header);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return meta;
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

        final int BYTES_FRAME_ID_LENGTH = 4;
        final int BYTES_FRAME_SIZE_LENGTH = 4;
        final int BYTES_FRAME_FLAGS_LENGTH = 2;
        final int BYTES_ENCODING_LENGTH = 1;

        try (FileInputStream fileStream = new FileInputStream(file)) {
            buffer = new byte[BYTES_TAG_LENGTH];
            fileStream.read(buffer);
            meta.tag = new String(buffer, TAGS_ENCODING);

            buffer = new byte[BYTES_VERSION_LENGTH];
            fileStream.read(buffer);
            meta.version = buffer[0];

            buffer = new byte[BYTES_SUBVERSION_LENGTH];
            fileStream.read(buffer);
            meta.subversion = buffer[0];

            buffer = new byte[BYTES_FLAGS_LENGTH];
            fileStream.read(buffer);
            int flags = byteArrayToInt(buffer);
            int unsyncBit = 0b100_0000;
            int extendedHeaderBit = 0b010_0000;
            int experimentalBit = 0b001_0000;
            meta.flagUnsync = (buffer[0] & unsyncBit) == unsyncBit;
            meta.flagExtendedHeader = (buffer[0] & extendedHeaderBit) == extendedHeaderBit;
            meta.flagExperIndicator = (buffer[0] & experimentalBit) == experimentalBit;

            buffer = new byte[BYTES_HEADER_SIZE_LENGTH];
            fileStream.read(buffer);
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = (byte) (buffer[i] & 0b011_1111); // unset 7 bit
            }

            final int headerSize = byteArrayToInt(buffer);

            byte[] header = new byte[headerSize];
            int bytesRead = fileStream.read(header);

            int offset;
            byte[] frameIDBytes;
            String frameID;
            byte[] frameSizeBytes;
            int frameDataSize;
            byte[] frameFlagBytes;
            byte[] encodingByte;
            int encoding;
            int remainedFrameSize;
            for (long pos = 0; pos < bytesRead; pos += offset) {
                if (pos < 0) break; // overflow

                offset = 0;

                frameIDBytes = new byte[BYTES_FRAME_ID_LENGTH];
                System.arraycopy(header, (int) pos, frameIDBytes, 0, frameIDBytes.length);
                frameID = new String(frameIDBytes, TAGS_ENCODING);
                offset += BYTES_FRAME_ID_LENGTH;

                frameSizeBytes = new byte[BYTES_FRAME_SIZE_LENGTH];
                System.arraycopy(header, (int) pos + offset, frameSizeBytes, 0, frameSizeBytes.length);

                frameDataSize = byteArrayToInt(frameSizeBytes);
                offset += BYTES_FRAME_SIZE_LENGTH;

                frameFlagBytes = new byte[BYTES_FRAME_FLAGS_LENGTH];
                System.arraycopy(header, (int) pos + offset, frameFlagBytes, 0, frameFlagBytes.length);
                offset += BYTES_FRAME_FLAGS_LENGTH;

                encodingByte = new byte[BYTES_ENCODING_LENGTH];
                System.arraycopy(header, (int) pos + offset, encodingByte, 0, encodingByte.length);
                offset += frameDataSize;

                encoding = encodingByte[0];
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
                remainedFrameSize = frameDataSize - encodingByte.length;
                if (remainedFrameSize > 0) {
                    buffer = new byte[remainedFrameSize];
                    System.arraycopy(header, (int) pos + 11, buffer, 0, buffer.length);
                    String frameData = new String(buffer, codingName);

                    switch (frameID.toUpperCase()) {
                        case ID3v23.TITLE:
                            meta.title = frameData;
                            break;
                        case ID3v23.ARTIST:
                            meta.artist = frameData;
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Data ended suddenly or header was incorrect
            // Nothing to do - end read header
//            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return meta;
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

        try (RandomAccessFile file = new RandomAccessFile(fileAbsolutePath, "r")) {
            file.skipBytes(fileSizeInBytes - ID3v1.HEADER_LENGTH);
            byte[] header = new byte[ID3v1.HEADER_LENGTH];

            file.readFully(header, 0, header.length);
            String tag = new String(header).toUpperCase();
            if (tag.contains("TAG")) {
                return FormatName.ID3v1;
            }
        } catch (IOException e) {
            return FormatName.NONE;
        }

        return FormatName.NONE;
    }
}

class ID3v1 {
    public static final int HEADER_LENGTH = 128;

    public static final int TAG_LENGTH_BYTES = 3;
    public static final int TITLE_LENGTH_BYTES = 30;
    public static final int ARTIST_LENGTH_BYTES = 30;
    public static final int ALBUM_LENGTH_BYTES = 30;
    public static final int YEAR_LENGTH_BYTES = 4;
    public static final int COMMENT_LENGTH_BYTES = 30; // here is also Track # at last 2 bytes
    public static final int GENRE_LENGTH_BYTES = 1;

    public static final String ARTIST = "TPE1";
    public static final String TITLE = "TIT2";
    public static final String ALBUM = "TALB";
    public static final String YEAR = "TYER";
    public static final String COMMENT = "vCOMM";
    public static final String GENRE = "TCON";
}

class ID3v23 {
    public static final String ARTIST = "TPE1";
    public static final String TITLE = "TIT2";
}