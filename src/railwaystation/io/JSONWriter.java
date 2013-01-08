/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.io;

import java.io.*;
import java.util.zip.GZIPOutputStream;
import org.json.JSONObject;

/**
 *
 * @author artur
 */
public class JSONWriter {
    public static void write(File file, JSONObject json) {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            write(stream, json);
        } catch (FileNotFoundException ex) {
            System.err.println("file not found");
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {}
        }
    }

    public static void write(OutputStream stream, JSONObject json) {
        OutputStreamWriter outputWriter = null;

        try {
            outputWriter = new OutputStreamWriter(stream);
            outputWriter.write(json.toString());
        } catch (IOException ex) {
            System.err.println("error writing json stream");
        } finally {
            try {
                if (outputWriter != null) {
                    outputWriter.close();
                }
            } catch (IOException ex) {}
        }
    }
    
    public static void makeGzip(String jsonFileName, String gzipFileName) {
        BufferedWriter bufferedWriter = null;
        BufferedReader bufferedReader = null;

        try {

            //Construct the BufferedWriter object
            bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(
                    new GZIPOutputStream(new FileOutputStream(gzipFileName))));

            //Construct the BufferedReader object
            bufferedReader = new BufferedReader(new FileReader(jsonFileName));

            String line = null;

            // from the input file to the GZIP output file
            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Close the BufferedWrter
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //Close the BufferedReader
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}