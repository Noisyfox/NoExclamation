package org.foxteam.noisyfox.noexclamation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Noisyfox on 2017/7/1.
 */

public class Utils {

    public static String cmdExecSu(String cmd) {
        Process p = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            p = new ProcessBuilder("su").start();
            writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            InputStream is = p.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            writer.write(cmd);
            writer.write("\n");
            writer.write("exit\n");
            writer.flush();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            p.waitFor();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (p != null) {
                p.destroy();
            }
        }
        return null;
    }

}
