package by.bsuir.ftp;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.Objects;

public class proto_FtpClient {
    private String server;
    private int port;
    private String user;
    private String password;
    private FTPClient ftp;
    private String workingDirectory;

    public proto_FtpClient(String server, int port, String user, String password) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
        this.workingDirectory = "/";
    }

    public void connect() throws IOException {
        ftp = new FTPClient();

        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        //ftp.connect(server, port);
        ftp.connect(server);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }
        ftp.login(user, password);
    }

    public FTPFile[] listDirectories(String path) throws IOException {
        return ftp.listDirectories(path);
    }

    public FTPFile[] listFiles(String path) throws IOException {
        return ftp.listFiles(path);
    }

    public void changeWorkingDirectory(String dir) {
        this.workingDirectory = this.workingDirectory.concat(dir).concat("/");
    }

    public String printWorkingDirectory() {
        return workingDirectory;
    }

    public void retrieve(String retrievePath, String storePath) throws IOException {
        InputStream in = ftp.retrieveFileStream(retrievePath);
        /*if(!FTPReply.isPositiveIntermediate(ftp.getReplyCode())) {
            close();
            throw new IOException(ftp.getReplyString());
        }*/
        OutputStream out = new FileOutputStream(storePath);
        out.write(in.readAllBytes());
        out.flush();
        in.close();
        out.close();
        boolean completed = this.ftp.completePendingCommand();
        if (completed) {
            System.out.println("Downloaded file from FTP: " + retrievePath);
        } else {
            throw new IOException("Can not download file from FTP: " + retrievePath);
        }
    }

    public InputStream retrieve(String retrievePath) throws IOException {
        InputStream in = ftp.retrieveFileStream(retrievePath);
        /*if(!FTPReply.isPositiveIntermediate(ftp.getReplyCode())) {
            close();
            throw new IOException(ftp.getReplyString());
        }*/
        boolean completed = this.ftp.completePendingCommand();
        if (completed) {
            System.out.println("Downloaded file from FTP: " + retrievePath);
            return in;
        } else {
            throw new IOException("Can not download file from FTP: " + retrievePath);
        }
    }

    public void store(File toStore, String storePath) throws IOException {
        if (!ftp.allocate(toStore.length())) {
            throw new IOException("Not enough space on server");
        }
        InputStream inputStream = new FileInputStream(toStore);
        OutputStream outputStream = ftp.storeFileStream(storePath + toStore.getName());
        if (Objects.isNull(outputStream)) {
            throw new IOException(ftp.getReplyString());
        }
        int sizeBytes = 4096;
        byte[] bytesIn = new byte[sizeBytes];
        int read = 0;
        while ((read = inputStream.read(bytesIn)) != -1) {
            outputStream.write(bytesIn, 0, read);
        }
        inputStream.close();
        outputStream.close();
        boolean completed = this.ftp.completePendingCommand();
        if (completed) {
            System.out.println("Uploaded file to FTP: " + storePath);
        } else {
            throw new IOException("Can not upload file to FTP: " + storePath);
        }
    }

    public boolean deleteFile(String path) throws IOException {
        return ftp.deleteFile(path);
    }

    public boolean makeDirectory(String path) throws IOException {
        return ftp.makeDirectory(path);
    }

    public boolean deleteDirectory(String path) throws IOException {
        return ftp.removeDirectory(path);
    }

    public void close() throws IOException {
        ftp.logout();
        ftp.disconnect();
    }

}