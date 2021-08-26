package me.bilousov.translator;

import java.util.ArrayList;
import java.util.List;

public class CodeTranslator {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static String STACK_POINTER = "SP";

    private int labelId = 0;

    public List<String> translateCodeToAssembly(List<String> vmCodeLines){
        List<String> translated = new ArrayList<>();

        for (String line : vmCodeLines){
            translated.add(translateLine(line));
        }

        System.out.println("Final list: " + translated);

        return translated;
    }

    private String translateLine(String codeLine){
        if(codeLine.startsWith("push")){
            return translatePushCommand(codeLine);
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

        return translatePopCommand(codeLine);
    }

    private String translatePushCommand(String pushCommand){
        String[] commandParts = pushCommand.split(" ");
        StringBuilder instructions = new StringBuilder();

        if(commandParts[1].equals("constant")){
            instructions.append("// ").append(pushCommand).append(LINE_SEPARATOR);
            instructions.append("@" + commandParts[2]).append(LINE_SEPARATOR);
            instructions.append("D=A").append(LINE_SEPARATOR);
            instructions.append("@SP").append(LINE_SEPARATOR);
            instructions.append("A=M").append(LINE_SEPARATOR);
            instructions.append("M=D").append(LINE_SEPARATOR);
            instructions.append("@SP").append(LINE_SEPARATOR);
            instructions.append("M=M+1").append(LINE_SEPARATOR);

            return instructions.toString();
        }

        return "";
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

    private void getLastStackValue(StringBuilder builder){
        builder.append("@SP").append(LINE_SEPARATOR);
        builder.append("A=M").append(LINE_SEPARATOR);
        builder.append("A=A-1").append(LINE_SEPARATOR);
        builder.append("D=M").append(LINE_SEPARATOR);
    }

    private String translatePopCommand(String popCommand){
        return "";
    }
}
