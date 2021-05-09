import java.io.File;
import java.io.IOException;

public interface IFtpService {

    public void open() throws IOException;

    public void close() throws IOException;

    public String[] ls() throws IOException;

    public void cwd();

    public void cdup();

    public void retr(String name) throws IOException;

    public void stor(String name) throws IOException;
}
