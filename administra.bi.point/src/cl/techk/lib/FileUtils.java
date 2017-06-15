package cl.techk.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.json.JSONObject;

public class FileUtils {

    public static final String TEMP_FILE_FOLDER = "/temp_files_point/";

    public static void deleteFile(String file_path) {

        try {
            File fichero = new File(file_path);
            if (fichero.exists()) {
                fichero.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static InputStream getTemporalFile(JSONObject data) {

        InputStream temp_file = null;
        
        try {
            String file_path = data.getString("file_path");
            File file = new File(file_path);
            temp_file = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return temp_file;
    }
}
