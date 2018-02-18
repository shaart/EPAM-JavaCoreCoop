import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class ID3v1 {
    public static final String META_TAG = "TAG";
    public static final int HEADER_LENGTH = 128;

    public static final int TAG_LENGTH_BYTES = 3;
    public static final int TITLE_LENGTH_BYTES = 30;
    public static final int ARTIST_LENGTH_BYTES = 30;
    public static final int ALBUM_LENGTH_BYTES = 30;
    public static final int YEAR_LENGTH_BYTES = 4;
    public static final int COMMENT_LENGTH_BYTES = 30; // here is also "Track's number at album" at last 2 bytes
    public static final int GENRE_LENGTH_BYTES = 1;
}

class ID3v23 {
    public static final String META_TAG = "ID3";
    public static final String TAG_ID3v2_REVERSED = "3DI";
    public static final int POST_PENDED_HEADER_LENGTH_BYTES = 10;

    public static final int TAG_LENGTH_BYTES = 3;
    public static final int VERSION_LENGTH_BYTES = 1;
    public static final int SUBVERSION_LENGTH_BYTES = 1;
    public static final int FLAGS_LENGTH_BYTES = 1;
    public static final int HEADER_SIZE_LENGTH_BYTES = 4;

    public static final int FRAME_ID_LENGTH_BYTES = 4;
    public static final int FRAME_SIZE_LENGTH_BYTES = 4;
    public static final int FRAME_FLAGS_LENGTH_BYTES = 2;
    public static final int ENCODING_LENGTH_BYTES = 1;

    public static final String ARTIST = "TPE1";
    public static final String TITLE = "TIT2";
    public static final String ALBUM = "TALB";
    public static final String YEAR = "TYER";
    public static final String COMMENT = "COMM";
    public static final String GENRE = "TCON";
}

/**
 * Class for working with ID3 metadata
 */
public class Metadata {
    public enum FormatName {NONE, ID3v1, ID3v2, ID3v23}

    // FIELDS
    private FormatName format = FormatName.NONE;
    private String tag = null;
    private int version;
    private int subversion;
    private String artist = null;
    private String title = null;

    private boolean flagUnsync = false;
    private boolean flagExtendedHeader = false;
    private boolean flagExperIndicator = false;

    private Metadata() {
    }

    // CONSTANTS
    private static final int MAX_PRE_TAG_LENGTH_BYTES = 3;

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

    /**
     * Reads metadata from file.
     *
     * @param filePath Path to file
     * @return Metadata information. If metadata not found returns <code>null</code>.
     */
    public static Metadata read(String filePath) {
        Metadata metadata = null;
        Path path = Paths.get(filePath);
        Metadata.FormatName format = Metadata.getFormatAtStart(path);
        switch (format) {
            case ID3v1:
                metadata = Metadata.readID3v1(filePath);
                break;
            case ID3v2:
            case ID3v23:
                metadata = Metadata.readID3v23(filePath);
                break;
            case NONE:
            default:
                break;
        }

        if (metadata == null) {
            format = Metadata.getFormatAtEnd(path);
            switch (format) {
                case ID3v1:
                    metadata = Metadata.readID3v1(filePath);
                    break;
                default:
                    throw new UnsupportedOperationException("Only ID3v1 post-pended format supported");
            }
        }

        return metadata;
    }

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

    /**
     * Trying to parse 128 bytes header as ID3v1.
     *
     * @param header Header with data
     * @return Metadata with header's information or <code>null</code> if ID3v1 information not found
     */
    private static Metadata parseID3v1(byte[] header) {
        Metadata meta = null;
        if (header.length > 0) {
            meta = new Metadata();
            final String TAGS_ENCODING = "UTF-8";

            byte[] buffer;
            try {
                int offset = 0;
                buffer = new byte[ID3v1.TAG_LENGTH_BYTES];
                System.arraycopy(header, offset, buffer, 0, buffer.length);
                meta.tag = new String(buffer, TAGS_ENCODING);
                offset += ID3v1.TAG_LENGTH_BYTES;
                if (meta.tag.toUpperCase().equals("TAG")) {
                    meta.format = FormatName.ID3v1;
                    final String DATA_ENCODING = "ISO_8859_1";

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

    /**
     * Trying to read ID3v1 header from file.
     *
     * @param file        Path to file
     * @param readFromEnd Should the title search be at the end of the file
     * @return Read Metadata or <code>null</code> if ID3v1 metadata not found
     */
    public static Metadata readID3v1(String file, boolean readFromEnd) {
        Metadata meta = null;

        final String TAGS_ENCODING = "UTF-8";
        byte[] buffer;
        byte[] header = new byte[ID3v1.HEADER_LENGTH];
        try (FileInputStream fileStream = new FileInputStream(file)) {
            if (readFromEnd) {
                File f = new File(file);
                fileStream.skip(f.length() - header.length);
            }
            int readBytes = fileStream.read(header);
            if (readBytes > 0) {
                buffer = new byte[ID3v1.TAG_LENGTH_BYTES];
                System.arraycopy(header, 0, buffer, 0, buffer.length);
                if (new String(buffer, TAGS_ENCODING).equals("TAG")) {
                    meta = parseID3v1(header);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return meta;
    }

    /**
     * Trying to read ID3v1 header from file.
     *
     * @param file Path to file
     * @return Read Metadata or <code>null</code> if ID3v1 metadata not found
     */
    public static Metadata readID3v1(String file) {
        Metadata meta = readID3v1(file, false);

        if (meta == null) {
            meta = readID3v1(file, true);
        }

        return meta;
    }

    /**
     * Trying to read ID3v2.3 header from file.
     *
     * @param file Path to file
     * @return Read Metadata
     */
    public static Metadata readID3v23(String file) {
        Metadata meta = new Metadata();
        final String TAGS_ENCODING = "UTF-8";
        byte[] buffer;

        try (FileInputStream fileStream = new FileInputStream(file)) {
            buffer = new byte[ID3v23.TAG_LENGTH_BYTES];
            fileStream.read(buffer);
            meta.tag = new String(buffer, TAGS_ENCODING);
            if (meta.tag.equals("ID3")) {
                meta.format = FormatName.ID3v23;

                buffer = new byte[ID3v23.VERSION_LENGTH_BYTES];
                fileStream.read(buffer);
                meta.version = buffer[0];

                buffer = new byte[ID3v23.SUBVERSION_LENGTH_BYTES];
                fileStream.read(buffer);
                meta.subversion = buffer[0];

                buffer = new byte[ID3v23.FLAGS_LENGTH_BYTES];
                fileStream.read(buffer);
                int flags = byteArrayToInt(buffer);
                int unsyncBit = 0b100_0000;
                int extendedHeaderBit = 0b010_0000;
                int experimentalBit = 0b001_0000;
                meta.flagUnsync = (buffer[0] & unsyncBit) == unsyncBit;
                meta.flagExtendedHeader = (buffer[0] & extendedHeaderBit) == extendedHeaderBit;
                meta.flagExperIndicator = (buffer[0] & experimentalBit) == experimentalBit;

                buffer = new byte[ID3v23.HEADER_SIZE_LENGTH_BYTES];
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

                    frameIDBytes = new byte[ID3v23.FRAME_ID_LENGTH_BYTES];
                    System.arraycopy(header, (int) pos, frameIDBytes, 0, frameIDBytes.length);
                    frameID = new String(frameIDBytes, TAGS_ENCODING);
                    offset += ID3v23.FRAME_ID_LENGTH_BYTES;

                    frameSizeBytes = new byte[ID3v23.FRAME_SIZE_LENGTH_BYTES];
                    System.arraycopy(header, (int) pos + offset, frameSizeBytes, 0, frameSizeBytes.length);

                    frameDataSize = byteArrayToInt(frameSizeBytes);
                    offset += ID3v23.FRAME_SIZE_LENGTH_BYTES;

                    frameFlagBytes = new byte[ID3v23.FRAME_FLAGS_LENGTH_BYTES];
                    System.arraycopy(header, (int) pos + offset, frameFlagBytes, 0, frameFlagBytes.length);
                    offset += ID3v23.FRAME_FLAGS_LENGTH_BYTES;

                    encodingByte = new byte[ID3v23.ENCODING_LENGTH_BYTES];
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
            byte[] header = new byte[MAX_PRE_TAG_LENGTH_BYTES];

            int readBytes = file.read(header);
            if (readBytes > 0) {
                String tag = new String(header).toUpperCase();
                switch (tag) {
                    case ID3v1.META_TAG:
                        return FormatName.ID3v1;
                    case ID3v23.META_TAG:
                        return FormatName.ID3v23;
                    default:
                        break;
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
     * @return <code>true</code> - Contains metadata<br><code>false</code> - Header not found
     */
    public static boolean containsAtEnd(Path filePath) {
        return getFormatAtEnd(filePath) != FormatName.NONE;
    }

    /**
     * Checks for metadata at end of file.
     *
     * @param filePath Path to file
     * @return Metadata format
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
            file.skipBytes(fileSizeInBytes - ID3v23.POST_PENDED_HEADER_LENGTH_BYTES);
            byte[] header = new byte[ID3v23.POST_PENDED_HEADER_LENGTH_BYTES];

            file.readFully(header, 0, ID3v23.POST_PENDED_HEADER_LENGTH_BYTES);
            String tag = new String(header).toUpperCase();
            if (tag.contains(ID3v23.TAG_ID3v2_REVERSED)) {
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