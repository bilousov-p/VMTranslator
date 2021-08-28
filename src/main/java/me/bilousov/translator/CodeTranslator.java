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
        String[] commandParts = codeLine.split(" ");
        StringBuilder instructions = new StringBuilder();
        instructions.append("// ").append(codeLine).append(LINE_SEPARATOR);

        return switch (commandParts[0]) {
            case "push" -> translatePushCommand(instructions, commandParts, fileName);
            case "pop" -> translatePopCommand(instructions, commandParts, fileName);
            case "add", "sub", "or", "and" -> translateArthCommWithTwoOperands(instructions, codeLine);
            case "neg", "not" -> translateLogicalCommand(instructions, codeLine);
            default -> translateCompareCommand(instructions, codeLine);
        };
    }

    private String translatePushCommand(StringBuilder instructions, String[] commandParts, String fileName){
        selectMemorySegment(instructions, commandParts, fileName);
        getValueFromMemorySegment(instructions, commandParts);

        pushValueToStack(instructions);
        increaseStackPointer(instructions);

        return instructions.toString();
    }

    private String translatePopCommand(StringBuilder instructions, String[] commandParts, String fileName){
        getProperMemorySegment(commandParts, instructions, fileName);
        decreaseStackPointer(instructions);
        pushValueToMemSegment(commandParts, instructions);

        return instructions.toString();
    }

    private String translateArthCommWithTwoOperands(StringBuilder instructions, String arthCommand){
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append(arthCommand.equals("sub")? "D=M-D" : "D=D" + getOperator(arthCommand) + "M").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("M=M-1").append(LINE_SEPARATOR);

        return instructions.toString();
    }

    private String translateLogicalCommand(StringBuilder instructions, String logCommand){
        instructions.append("// ").append(logCommand).append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("M=" + (logCommand.equals("neg")? "-" : "!") + "M").append(LINE_SEPARATOR);

        return instructions.toString();
    }

    private String translateCompareCommand(StringBuilder instructions, String compCommand){
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("D=D-M").append(LINE_SEPARATOR);
        instructions.append("@POS_RESULT_" + labelId).append(LINE_SEPARATOR);
        instructions.append("D;" + getJumpInstr(compCommand)).append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("M=0").append(LINE_SEPARATOR);
        instructions.append("@NEG_RESULT_" + labelId).append(LINE_SEPARATOR);
        instructions.append("0;JMP").append(LINE_SEPARATOR);
        instructions.append("(POS_RESULT_" + labelId + ")").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("M=-1").append(LINE_SEPARATOR);
        instructions.append("(NEG_RESULT_" + labelId + ")").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("M=M-1").append(LINE_SEPARATOR);

        labelId++;

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

    private String getOperator(String arthCommand){
        return switch (arthCommand) {
            case "add" -> "+";
            case "sub" -> "-";
            case "and" -> "&";
            default -> "|";
        };
    }

    private String getJumpInstr(String logCommand){
        return switch (logCommand) {
            case "eq" -> "JEQ";
            case "gt" -> "JLT";
            default -> "JGT";
        };
    }
}