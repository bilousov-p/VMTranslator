package me.bilousov.parser;

import me.bilousov.translator.CodeTranslator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VMCodeParser {

    private static final String COMMENT_IDENTIFIER = "//";
    private String fileName;

    private List<String> translatedLines = new ArrayList<>();
    private CodeTranslator codeTranslator = new CodeTranslator();


    public List<String> parseVMFiles(String path){
        File vmFile = new File(path);

        if(vmFile.isDirectory()){
            return parseVMDirectory(vmFile);
        }

        return parseVMFile(vmFile);
    }

    public List<String> parseAndTranslateVMFiles(String path){
        File vmFile = new File(path);

        if (vmFile.isDirectory()){
            return parseAndTranslateVMDirectory(vmFile);
        }

        return parseAndTranslateVMFile(vmFile);
    }

    private List<String> parseVMDirectory(File directory){
        List<String> vmInstructions = new ArrayList<>();

        for(File file : directory.listFiles()){
            if(file.getName().endsWith(".vm")) {
                vmInstructions.addAll(parseVMFile(file));
            }
        }

        return vmInstructions;
    }

    private List<String> parseAndTranslateVMDirectory(File directory){
        for(File file : directory.listFiles()){
            if(file.getName().endsWith(".vm")) {
                translatedLines.addAll(parseAndTranslateVMFile(file));
            }
        }

        return translatedLines;
    }

    private List<String> parseAndTranslateVMFile(File vmFile){
        List<String> vmInstructions = new ArrayList<>();
        this.fileName = vmFile.getName();

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

        return codeTranslator.translateCodeToAssembly(vmInstructions, vmFile.getName());
    }

    private List<String> parseVMFile(File vmFile){
        List<String> vmInstructions = new ArrayList<>();
        this.fileName = vmFile.getName();

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

    public String getFileName(){
        return fileName;
    }

    private boolean lineIsInstruction(String line){
        String trimmedLine = line.trim();

        return !trimmedLine.equals("") && !trimmedLine.startsWith(COMMENT_IDENTIFIER);
    }
}
