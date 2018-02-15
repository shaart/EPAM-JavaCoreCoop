import java.util.List;

public class FileBuilder {
    private FileBuilder() {}

    public static void build(List<String> files) {
        build(files, "default");
    }

    public static void build(List<String> files, String fileName) {
        throw new UnsupportedOperationException();
    }
}
