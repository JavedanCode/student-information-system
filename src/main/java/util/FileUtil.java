package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    public static List<String> readFile(String path) {
        List<String> lines = new ArrayList<>();

        File file = new File(path);

        try {
            // Create file if it doesn't exist
            if (!file.exists()) {
                file.getParentFile().mkdirs(); // ensure folder exists
                file.createNewFile();
                return lines; // empty file
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }

            br.close();

        } catch (IOException e) {
            System.out.println("File read error: " + path);
        }

        return lines;
    }

    public static void writeFile(String path, List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("File write error: " + path);
        }
    }
}