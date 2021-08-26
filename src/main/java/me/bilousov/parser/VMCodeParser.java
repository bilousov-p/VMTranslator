package me.bilousov.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VMCodeParser {

    private static final String COMMENT_IDENTIFIER = "//";

    public List<String> parseVMFile(String fileName){
        List<String> vmInstructions = new ArrayList<>();
        File vmFile = new File(fileName);

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(vmFile));
            String line = bufferedReader.readLine();

            while (line != null) {
                if (lineIsInstruction(line)) {
                    vmInstructions.add(line);
                }

                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return vmInstructions;
    }

    private boolean lineIsInstruction(String line){
        String trimmedLine = line.trim();

        return !trimmedLine.equals("") && !trimmedLine.startsWith(COMMENT_IDENTIFIER);
    }
}
