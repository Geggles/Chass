import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface Controller {
    Path savegamePath = Paths.get(new File("").toURI());
    void setSavegamePath(Path path);
}
