import java.util.Objects;

public class proto_RemoteFile {
    private String name;
    private String fullPath;

    public proto_RemoteFile() {

    }

    public proto_RemoteFile(String name, String fullPath) {
        this.name = name;
        this.fullPath = fullPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        proto_RemoteFile that = (proto_RemoteFile) o;
        return Objects.equals(name, that.name) && Objects.equals(fullPath, that.fullPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fullPath);
    }
}
