import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteTmp {
    private final String writeSentence;

    public WriteTmp(String writeSentence) {
        this.writeSentence = writeSentence;
    }

    public void write() {
        try {
            settings setting = new settings();
            File file = new File(setting.getFilePath());
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(this.writeSentence);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
