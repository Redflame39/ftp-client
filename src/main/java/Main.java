import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        proto_FtpClient ftp = new proto_FtpClient("localhost", 1, "redflame", "070218");
        try {
            ftp.open();

            ftp.changeWorkingDirectory("folder1");
            ftp.store("hello.txt");

            ftp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
