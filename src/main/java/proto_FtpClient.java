import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.Util;

import java.io.*;
import java.nio.file.Paths;
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

    public proto_FtpClient() {
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


    public void retrieve(String name) throws IOException {
        String fullPath = workingDirectory.concat(name);
        InputStream in = ftp.retrieveFileStream(fullPath);
        /*if(!FTPReply.isPositiveIntermediate(ftp.getReplyCode())) {
            close();
            throw new IOException(ftp.getReplyString());
        }*/
        OutputStream out = new FileOutputStream(name);
        out.write(in.readAllBytes());
        out.flush();
        in.close();
        out.close();
        boolean completed = this.ftp.completePendingCommand();
        if (completed) {
            System.out.println("Downloaded file from FTP: " + name);
        } else {
            throw new IOException("Can not download file from FTP: " + name);
        }
    }

    public void store(String localName) throws IOException {
        InputStream inputStream = new FileInputStream(localName);
        String fullPath = workingDirectory.concat(localName);
        OutputStream outputStream = ftp.storeFileStream(fullPath);
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
            System.out.println("Uploaded file to FTP: " + localName);
        } else {
            throw new IOException("Can not upload file to FTP: " + localName);
        }
    }

    public void close() throws IOException {
        ftp.logout();
        ftp.disconnect();
    }

}