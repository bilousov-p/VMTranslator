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
    private int returnAddressId = 0;

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
        addBootstrapCode(translated, fileName);

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
            case "label" -> translateLabelCommand(instructions, codeLine);
            case "goto" -> translateGotoCommand(instructions, codeLine);
            case "if-goto" -> translateIfGotoCommand(instructions, codeLine);
            case "call" -> translateCallCommand(instructions, codeLine, fileName);
            case "function" -> translateFunctionCommand(instructions, codeLine, fileName);
            case "return" -> translateReturnCommand(instructions);
            default -> translateCompareCommand(instructions, codeLine);
        };
    }

    private void addBootstrapCode(List<String> instructions, String fileName){
        StringBuilder bootstrapCode = new StringBuilder();

        bootstrapCode.append("// bootstrap code").append(LINE_SEPARATOR);
        bootstrapCode.append("@256").append(LINE_SEPARATOR);
        bootstrapCode.append("D=A").append(LINE_SEPARATOR);
        bootstrapCode.append("@SP").append(LINE_SEPARATOR);
        bootstrapCode.append("M=D").append(LINE_SEPARATOR);
        translateCallCommand(bootstrapCode, "call Sys.init 0", fileName);

        instructions.add(bootstrapCode.toString());
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
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("A=A-1").append(LINE_SEPARATOR);
        instructions.append("M=" + (logCommand.equals("neg")? "-" : "!") + "M").append(LINE_SEPARATOR);

        return instructions.toString();
    }

    private String translateLabelCommand(StringBuilder instructions, String labelCommand){
        instructions.append("(" + labelCommand.split(" ")[1] + ")").append(LINE_SEPARATOR);

        return instructions.toString();
    }

    private String translateGotoCommand(StringBuilder instructions, String gotoCommand){
        instructions.append("@").append(gotoCommand.split(" ")[1]).append(LINE_SEPARATOR);
        instructions.append("0;JMP").append(LINE_SEPARATOR);

        return instructions.toString();
    }

    private String translateIfGotoCommand(StringBuilder instructions, String ifGotoCommand){
        decreaseStackPointer(instructions);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@" + ifGotoCommand.split(" ")[1]).append(LINE_SEPARATOR);
        instructions.append("D;JNE").append(LINE_SEPARATOR);

        return instructions.toString();
    }

    private String translateCallCommand(StringBuilder instructions, String callCommand, String fileName){
        String returnLabel = fileName + "$ret." + returnAddressId++;
        // push return address
        instructions.append("@" + returnLabel).append(LINE_SEPARATOR);
        instructions.append("D=A").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("M=M+1").append(LINE_SEPARATOR);
        // push segments of a caller
        pushSegmentToStack(instructions, "LCL");
        pushSegmentToStack(instructions, "ARG");
        pushSegmentToStack(instructions, "THIS");
        pushSegmentToStack(instructions, "THAT");

        repositArgSegment(instructions, 5 + Integer.parseInt(callCommand.split(" ")[2]));
        // change LCL segment for new called func
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@LCL").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);
        // goto called func
        instructions.append("@" + fileName + "." + callCommand.split(" ")[1]).append(LINE_SEPARATOR);
        instructions.append("0;JMP").append(LINE_SEPARATOR);
        // insert return label
        instructions.append("(" + returnLabel + ")").append(LINE_SEPARATOR);

        return instructions.toString();
    }

    private void pushSegmentToStack(StringBuilder prevCommands, String segment){
        prevCommands.append("@" + segment).append(LINE_SEPARATOR);
        prevCommands.append("D=M").append(LINE_SEPARATOR);
        prevCommands.append("@SP").append(LINE_SEPARATOR);
        prevCommands.append("A=M").append(LINE_SEPARATOR);
        prevCommands.append("M=D").append(LINE_SEPARATOR);
        prevCommands.append("@SP").append(LINE_SEPARATOR);
        prevCommands.append("M=M+1").append(LINE_SEPARATOR);
    }

    private void repositArgSegment(StringBuilder prevCommands, int changeOn){
        prevCommands.append("@SP").append(LINE_SEPARATOR);
        prevCommands.append("D=M").append(LINE_SEPARATOR);
        prevCommands.append("@" + changeOn).append(LINE_SEPARATOR);
        prevCommands.append("D=D-A").append(LINE_SEPARATOR);
        prevCommands.append("@ARG").append(LINE_SEPARATOR);
        prevCommands.append("M=D").append(LINE_SEPARATOR);
    }

    private String translateFunctionCommand(StringBuilder instructions, String functionCommand, String fileName){
        // generate label to which PC will jump
        instructions.append("(" + fileName + "." + functionCommand.split(" ")[1] + ")").append(LINE_SEPARATOR);
        // generate local segment
        for(int i = 0; i< Integer.parseInt(functionCommand.split(" ")[2]); i++){
            instructions.append("@0").append(LINE_SEPARATOR);
            instructions.append("D=A").append(LINE_SEPARATOR);
            instructions.append("@SP").append(LINE_SEPARATOR);
            instructions.append("A=M").append(LINE_SEPARATOR);
            instructions.append("M=D").append(LINE_SEPARATOR);
            increaseStackPointer(instructions);
        }


        return instructions.toString();
    }

    private String translateReturnCommand(StringBuilder instructions){
        // save LCL pointer to temp variable
        instructions.append("@LCL").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@endFrame").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);

        // get the return address
        instructions.append("@endFrame").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@5").append(LINE_SEPARATOR);
        instructions.append("D=D-A").append(LINE_SEPARATOR);
        instructions.append("A=D").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@returnAddress").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);

        // place return value to ARG 0
        instructions.append("@0").append(LINE_SEPARATOR);
        instructions.append("D=A").append(LINE_SEPARATOR);
        instructions.append("@ARG").append(LINE_SEPARATOR);
        instructions.append("D=D+M").append(LINE_SEPARATOR);
        instructions.append("@R13").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("M=M-1").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@R13").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);

        // place stack pointer after return value
        instructions.append("@ARG").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@SP").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);
        increaseStackPointer(instructions);

        // recover the callers segments
        instructions.append("@endFrame").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@1").append(LINE_SEPARATOR);
        instructions.append("D=D-A").append(LINE_SEPARATOR);
        instructions.append("A=D").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@THAT").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);

        instructions.append("@endFrame").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@2").append(LINE_SEPARATOR);
        instructions.append("D=D-A").append(LINE_SEPARATOR);
        instructions.append("A=D").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@THIS").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);

        instructions.append("@endFrame").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@3").append(LINE_SEPARATOR);
        instructions.append("D=D-A").append(LINE_SEPARATOR);
        instructions.append("A=D").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@ARG").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);

        instructions.append("@endFrame").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@4").append(LINE_SEPARATOR);
        instructions.append("D=D-A").append(LINE_SEPARATOR);
        instructions.append("A=D").append(LINE_SEPARATOR);
        instructions.append("D=M").append(LINE_SEPARATOR);
        instructions.append("@LCL").append(LINE_SEPARATOR);
        instructions.append("M=D").append(LINE_SEPARATOR);

        // jump to return address
        instructions.append("@returnAddress").append(LINE_SEPARATOR);
        instructions.append("A=M").append(LINE_SEPARATOR);
        instructions.append("0;JMP").append(LINE_SEPARATOR);

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