package me.bilousov.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeTranslator {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String A_REGISTER = "A";
    private static final String M_REGISTER = "M";

    private Map<String, String> memoryMapping;
    private int labelId;

    public CodeTranslator() {
        labelId = 0;
        memoryMapping = new HashMap<>();
        memoryMapping.put("local", "@LCL");
        memoryMapping.put("argument", "@ARG");
        memoryMapping.put("this", "@THIS");
        memoryMapping.put("that", "@THAT");
        memoryMapping.put("temp", "@5");
    }

    public List<String> translateCodeToAssembly(List<String> vmCodeLines, String fileName){
        List<String> translated = new ArrayList<>();

        for (String line : vmCodeLines){
            translated.add(translateLine(line, fileName));
        }

        return translated;
    }

    private String translateLine(String codeLine, String fileName){
        if(codeLine.startsWith("push")){
            return translatePushCommand(codeLine, fileName);
        }

        if(codeLine.startsWith("add")){
            return translateAddCommand(codeLine);
        }

        if(codeLine.startsWith("eq")){
            return translateEqCommand(codeLine);
        }

        if(codeLine.startsWith("lt")){
            return translateLtCommand(codeLine);
        }

        if(codeLine.startsWith("gt")){
            return translateGtCommand(codeLine);
        }

        if(codeLine.startsWith("sub")){
            return translateSubCommand(codeLine);
        }

        if(codeLine.startsWith("neg")){
            return translateNegCommand(codeLine);
        }

        if(codeLine.startsWith("and")){
            return translateAndCommand(codeLine);
        }

        if(codeLine.startsWith("or")){
            return translateOrCommand(codeLine);
        }

        if(codeLine.startsWith("not")){
            return translateNotCommand(codeLine);
        }

        return translatePopCommand(codeLine, fileName);
    }

    private String translatePushCommand(String pushCommand, String fileName){
        String[] commandParts = pushCommand.split(" ");
        StringBuilder instructions = new StringBuilder();
        instructions.append("// ").append(pushCommand).append(LINE_SEPARATOR);

        selectMemorySegment(instructions, commandParts, fileName);
        getValueFromMemorySegment(instructions, commandParts);

        pushValueToStack(instructions);
        increaseStackPointer(instructions);

        return instructions.toString();
    }

    private String translatePopCommand(String popCommand, String fileName){
        String[] commandParts = popCommand.split(" ");
        StringBuilder instructions = new StringBuilder();
        instructions.append("// ").append(popCommand).append(LINE_SEPARATOR);

        getProperMemorySegment(commandParts, instructions, fileName);
        decreaseStackPointer(instructions);
        pushValueToMemSegment(commandParts, instructions);

        return instructions.toString();
    }

    private void getValueFromMemorySegment(StringBuilder prevCommands, String[] commandParts){
        if(!commandParts[1].equals("constant") && !commandParts[1].equals("static") && !commandParts[1].equals("pointer")){
            prevCommands.append(memoryMapping.get(commandParts[1])).append(LINE_SEPARATOR);
            prevCommands.append("D=D+" + getProperMemoryValue(commandParts[1])).append(LINE_SEPARATOR);
            prevCommands.append("A=D").append(LINE_SEPARATOR);
            prevCommands.append("D=M").append(LINE_SEPARATOR);
        }
    }

    private String getProperMemoryValue(String memorySegment){
        if(memorySegment.equals("temp")){
            return A_REGISTER;
        }

        return M_REGISTER;
    }

    private void selectMemorySegment(StringBuilder prevCommands, String[] vmCommand, String fileName){
        String memorySegmentSelection = "@";

        if(vmCommand[1].equals("pointer")){
            memorySegmentSelection+=vmCommand[2].equals("0")? "THIS" : "THAT";
        } else {
            memorySegmentSelection+=(vmCommand[1].equals("static")? fileName + "." : "") + vmCommand[2];
        }

        prevCommands.append(memorySegmentSelection).append(LINE_SEPARATOR);
        prevCommands.append("D=" + (vmCommand[1].equals("static") || vmCommand[1].equals("pointer")? M_REGISTER : A_REGISTER)).append(LINE_SEPARATOR);
    }

    private void pushValueToStack(StringBuilder prevCommands){
        prevCommands.append("@SP").append(LINE_SEPARATOR);
        prevCommands.append("A=M").append(LINE_SEPARATOR);
        prevCommands.append("M=D").append(LINE_SEPARATOR);
    }

    private void increaseStackPointer(StringBuilder prevCommands){
        prevCommands.append("@SP").append(LINE_SEPARATOR);;
        prevCommands.append("M=M+1").append(LINE_SEPARATOR);;
    }

    private void decreaseStackPointer(StringBuilder prevCommands){
        prevCommands.append("@SP").append(LINE_SEPARATOR);
        prevCommands.append("M=M-1").append(LINE_SEPARATOR);
        prevCommands.append("A=M").append(LINE_SEPARATOR);
        prevCommands.append("D=M").append(LINE_SEPARATOR);
    }

    private void pushValueToMemSegment(String[] commandParts, StringBuilder prevCommands){
        if(commandParts[1].equals("pointer")){
            prevCommands.append(commandParts[2].equals("0") ? "@THIS" : "@THAT").append(LINE_SEPARATOR);
        } else {
            prevCommands.append("@R13").append(LINE_SEPARATOR);
            prevCommands.append("A=M").append(LINE_SEPARATOR);
        }

        prevCommands.append("M=D").append(LINE_SEPARATOR);
    }

    private void getProperMemorySegment(String[] commandParts, StringBuilder prevCommands, String fileName){
        if(commandParts[1].equals("pointer")){
            return;
        }

        prevCommands.append("@" + (commandParts[1].equals("static")? fileName + "." : "") + commandParts[2]).append(LINE_SEPARATOR);
        prevCommands.append("D=A").append(LINE_SEPARATOR);

        if(!commandParts[1].equals("static")){
            prevCommands.append(memoryMapping.get(commandParts[1])).append(LINE_SEPARATOR);
            prevCommands.append("D=D+" + getProperMemoryValue(commandParts[1])).append(LINE_SEPARATOR);
        }

        prevCommands.append("@R13").append(LINE_SEPARATOR);
        prevCommands.append("M=D").append(LINE_SEPARATOR);
    }

    private String translateAddCommand(String addCommand){
        StringBuilder instructions = new StringBuilder();

        instructions.append("// ").append(addCommand).append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=D+M").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("M=M-1").append(LINE_SEPARATOR);

        return instructions.toString();
    }

    private String translateSubCommand(String subCommand){
        StringBuilder instructions = new StringBuilder();

        instructions.append("// ").append(subCommand).append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=M-D").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("M=M-1").append(LINE_SEPARATOR);

        return instructions.toString();
    }

    private String translateAndCommand(String andCommand){
        StringBuilder instructions = new StringBuilder();

        instructions.append("// ").append(andCommand).append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=D&M").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("M=M-1").append(LINE_SEPARATOR);

        return instructions.toString();
    }

    private String translateOrCommand(String orCommand){
        StringBuilder instructions = new StringBuilder();

        instructions.append("// ").append(orCommand).append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=D|M").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("M=M-1").append(LINE_SEPARATOR);

        return instructions.toString();
    }

    private String translateNotCommand(String notCommand){
        StringBuilder instructions = new StringBuilder();

        instructions.append("// ").append(notCommand).append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("M=!M").append(LINE_SEPARATOR);

        return instructions.toString();
    }

    private String translateNegCommand(String negCommand){
        StringBuilder instructions = new StringBuilder();

        instructions.append("// ").append(negCommand).append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("M=-M").append(LINE_SEPARATOR);

        return instructions.toString();
    }

    private String translateEqCommand(String eqCommand){
        StringBuilder instructions = new StringBuilder();

        instructions.append("// ").append(eqCommand).append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=D-M").append(LINE_SEPARATOR);
        instructions.append("@EQ_" + labelId).append(LINE_SEPARATOR);
        instructions.append("D;JEQ").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("M=0").append(LINE_SEPARATOR);
        instructions.append("@EQ_END_" + labelId).append(LINE_SEPARATOR);
        instructions.append("0;JMP").append(LINE_SEPARATOR);
        instructions.append("(EQ_" + labelId + ")").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("M=-1").append(LINE_SEPARATOR);
        instructions.append("(EQ_END_" + labelId + ")").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("M=M-1").append(LINE_SEPARATOR);

        labelId++;

        return instructions.toString();
    }

    private String translateLtCommand(String ltCommand){
        StringBuilder instructions = new StringBuilder();

        instructions.append("// ").append(ltCommand).append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=D-M").append(LINE_SEPARATOR);
        instructions.append("@LT_" + labelId).append(LINE_SEPARATOR);
        instructions.append("D;JGT").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("M=0").append(LINE_SEPARATOR);
        instructions.append("@LT_END_" + labelId).append(LINE_SEPARATOR);
        instructions.append("0;JMP").append(LINE_SEPARATOR);
        instructions.append("(LT_" + labelId + ")").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("M=-1").append(LINE_SEPARATOR);
        instructions.append("(LT_END_" + labelId + ")").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("M=M-1").append(LINE_SEPARATOR);

        labelId++;

        return instructions.toString();
    }

    private String translateGtCommand(String gtCommand){
        StringBuilder instructions = new StringBuilder();

        instructions.append("// ").append(gtCommand).append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=D-M").append(LINE_SEPARATOR);
        instructions.append("@GT_" + labelId).append(LINE_SEPARATOR);
        instructions.append("D;JLT").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("M=0").append(LINE_SEPARATOR);
        instructions.append("@GT_END_" + labelId).append(LINE_SEPARATOR);
        instructions.append("0;JMP").append(LINE_SEPARATOR);
        instructions.append("(GT_" + labelId + ")").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("M=-1").append(LINE_SEPARATOR);
        instructions.append("(GT_END_" + labelId + ")").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("M=M-1").append(LINE_SEPARATOR);

        labelId++;

        return instructions.toString();
    }

}