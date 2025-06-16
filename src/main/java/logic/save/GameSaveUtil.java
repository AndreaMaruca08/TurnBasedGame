package logic.save;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class GameSaveUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void salvaSuFile(GameSave save, String filePath) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), save);
    }

    public static GameSave caricaDaFile(String filePath) throws IOException {
        return mapper.readValue(new File(filePath), GameSave.class);
    }

}