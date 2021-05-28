package by.bsuir.ftp;

import org.apache.commons.net.ftp.FTPFile;

import java.util.Objects;

public class proto_RemoteFile {
    private String name;
    private String fullPath;
    private FTPFile file;

    public proto_RemoteFile() {

    }

    public proto_RemoteFile(FTPFile file, String fullPath) {
        this.file = file;
        this.name = file.getName();
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

    public FTPFile getFile() {
        return file;
    }

    public void setFile(FTPFile file) {
        this.file = file;
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
