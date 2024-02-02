import java.io.IOException;

public class ContentTypeHelper {
    public static ContentType GetContentType(String file) throws IOException{
        String[] fileSplitted = file.split("\\.");
        String fileExtension = fileSplitted[fileSplitted.length - 1];
        return getContentTypeFromFileExtension(fileExtension);
    }

    private static ContentType getContentTypeFromFileExtension(String fileExtension) throws IOException{
        for (ContentType type : ContentType.values()) {
            if (type.name().equalsIgnoreCase(fileExtension)) {
                return type;
            }
        }

        throw new IOException("file extension not supported");
    }

    public enum ContentType {
        html("text/html"),
        jpg("image/jpg"),
        png("image/png"),
        bmp("image/bmp"),
        gif("image/gif"),
        ico("image/x-icon");

        private String description;

        ContentType(String description) {
            this.description = description;
        }

        public String GetDescription() {
            return description;
        }
    }
}
