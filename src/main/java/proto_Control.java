import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;

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

    public ArrayList<String> listFiles(String path) {
        ArrayList<String> files = new ArrayList<>();
        try {
            for (FTPFile file : ftp.listFiles(path)) {
                files.add(file.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    public void retrieve(String retrievePath, String storePath) {
        try {
            ftp.retrieve(retrievePath, storePath);
        } catch (IOException e) {
            System.err.println("Cannot retrieve file from " + retrievePath + " (" + e.getLocalizedMessage() + ")");
        }
    }
}
