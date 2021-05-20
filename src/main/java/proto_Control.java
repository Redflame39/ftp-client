import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class proto_Control {
    proto_FtpClient ftp;

    public proto_Control(String host, String username, String password) {
        ftp = new proto_FtpClient(host, 0, username, password);
    }

    public void connect() {
        try {
            ftp.connect();
        } catch (IOException e) {
            System.err.println("Cannot connect to server.");
        }
    }

    public void disconnect() {
        try {
            ftp.close();
        } catch (IOException e) {
            System.err.println("Cannot close connection with server.");
        }
    }

    public ArrayList<String> listDirectories(String path) {
        ArrayList<String> directories = new ArrayList<>();
        try {
            for (FTPFile file : ftp.listDirectories(path)) {
                directories.add(file.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return directories;
    }

    public ArrayList<FTPFile> listFiles(String path) {
        try {
            return new ArrayList<>(Arrays.asList(ftp.listFiles(path)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void retrieve(String retrievePath, String storePath) {
        try {
            ftp.retrieve(retrievePath, storePath);
        } catch (IOException e) {
            System.err.println("Cannot retrieve file from " + retrievePath + " (" + e.getLocalizedMessage() + ")");
        }
    }

    public void store(File toStore, String storePath) {
        try {
            ftp.store(toStore, storePath);
        } catch (IOException e) {
            System.err.println("Cannot upload file to FTP: " + storePath);
        }
    }

    public boolean deleteFile(String path) {
        try {
            return ftp.deleteFile(path);
        } catch (IOException e) {
            System.err.println("Cannot delete file from FTP: " + path);
            return false;
        }
    }

    public boolean deleteDirectory(String path) {
        try {
            return ftp.deleteDirectory(path);
        } catch (IOException e) {
            System.err.println("Cannot delete directory from FTP: " + path);
            return false;
        }
    }
}
