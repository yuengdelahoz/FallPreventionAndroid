package fallprevention.usf.neuralnetwork.tfclassification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
    public void mSaveData(String file, String line, File albumStorageDir){
        try {
            File mFile = new File(albumStorageDir, file);
            FileWriter writer = new FileWriter(mFile, true);
            BufferedWriter output = new BufferedWriter(writer);
            output.append(line);
            output.newLine();  // This is safer than using '\n'
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
