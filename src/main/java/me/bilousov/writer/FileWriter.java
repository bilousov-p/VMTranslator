package me.bilousov.writer;

import java.io.*;
import java.util.List;

public class FileWriter {

    private static final String INPUT_FILE_EXTENSION = ".vm";
    private static final String OUTPUT_FILE_EXTENSION = ".asm";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static void writeBinaryFileWithLines(List<String> binaryLines, String path) throws IOException {
        File vmFile = new File(path);
        String outputFilePath;

        if(vmFile.isDirectory()){
            outputFilePath = path + "\\" + vmFile.getName() + OUTPUT_FILE_EXTENSION;
        } else {
            outputFilePath = path.replace(INPUT_FILE_EXTENSION, OUTPUT_FILE_EXTENSION);
        }

        FileOutputStream fos = new FileOutputStream(outputFilePath);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for (String line : binaryLines) {
            bw.write(line + LINE_SEPARATOR);
        }

        bw.close();
    }
}
