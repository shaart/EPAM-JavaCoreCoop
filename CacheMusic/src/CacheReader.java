import java.util.List;

public class CacheReader {
    private CacheReader() {}
    /**
     * Scans folder and make list of part files for build
     * @param folderPath
     * @return
     */
    public static List<String> scan(String folderPath) {
        throw new UnsupportedOperationException();
    }

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
