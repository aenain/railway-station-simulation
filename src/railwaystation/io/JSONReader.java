/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author artur
 */
public class JSONReader {
    public static JSONObject read(File file) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            return read(stream);
        } catch (FileNotFoundException ex) {
            System.err.println("file not found");
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {}
        }
        return null;
    }

    public static JSONObject read(InputStream stream) {
        InputStreamReader inputReader = null;
        BufferedReader bufferedReader = null;
        JSONObject json = null;

        try {
            String line;
            StringBuilder builder = new StringBuilder();
            inputReader = new InputStreamReader(stream);
            bufferedReader = new BufferedReader(inputReader);

            while((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }

            try {
                json = new JSONObject(builder.toString());
            } catch (JSONException ex) {
                System.err.println("error reading json");
            }

        } catch (IOException ex) {
            System.err.println("error reading json stream");
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputReader != null) {
                    inputReader.close();
                }
            } catch (IOException ex) {}
        }

        return json;
    } 
}
