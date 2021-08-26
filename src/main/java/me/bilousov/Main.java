package me.bilousov;

import me.bilousov.parser.VMCodeParser;
import me.bilousov.translator.CodeTranslator;
import me.bilousov.writer.FileWriter;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        VMCodeParser parser = new VMCodeParser();

        CodeTranslator codeTranslator = new CodeTranslator();

        FileWriter.writeBinaryFileWithLines(codeTranslator.translateCodeToAssembly(parser.parseVMFile(args[0])), args[0]);
    }
}
