package Assignment3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assignment3 {

    public static Assembly AssemblyCode;
    public static List<Macro> MacroNameTable;

    public static class Assembly {
        public List<String> lines;

        public Assembly() {
            lines = new ArrayList<>();
        }

        public void ReadAssembly(String fileName) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                var line = reader.readLine();
                while (line != null) {
                    if (line != "")
                        lines.add(line);
                    line = reader.readLine();
                }

            }
        }
    }

    public static class Macro {
        String name;
        Definition definition;
        List<Argument> argumentListArray;



        public Macro() {
            definition = new Definition();
            argumentListArray = new ArrayList<>();
        }

        public void addArgument(String formalParameter) {
            Argument arg = new Argument();
            arg.id = argumentListArray.size();
            arg.formalParameter = formalParameter;
            argumentListArray.add(arg);
        }

        public void addActualParameter(Argument arg, String actualParameter) {
            arg.actualParameter = actualParameter;
        }

        public Argument findArgumentById(String query) {
            var argumentStream = argumentListArray.stream().filter(x -> x.id == Integer.parseInt(query));
            var argument = argumentStream.findFirst();
            if (argument.isPresent()) {
                return argument.get();
            }

            return null;
        }

        public Argument findArgumentByFormalParameter(String query) {
            var argumentStream = argumentListArray.stream().filter(x -> x.formalParameter.equals(query));
            var argument = argumentStream.findFirst();
            if (argument.isPresent()) {
                return argument.get();
            }

            return null;
        }

    }

    public static class Argument {
        int id;
        String formalParameter;
        String actualParameter;
    }

    public static class Definition {
        List<String> lines;

        public Definition() {
            lines = new ArrayList<>();
        }
    }
    public static List<String> reverse(List<String> l) {
        var reverse = new ArrayList<String>();
        for (int i = l.size() - 1; i >= 0; i--) {
            reverse.add(l.get(i));
        }

        return reverse;
    }

    public static void Pass1() throws IOException {
        var lines = AssemblyCode.lines;
        for (int i = 0; i < lines.size();) {
            if (lines.get(i).equals("MACRO")) {
                lines.remove(i);
                var macro = new Macro();
                var words = lines.get(i).split(" |,|\t");

                for (String word : words) {
                    if (word.matches("&[A-Z0-9_]*")) {
                        macro.addArgument(word);
                    } else {
                        macro.name = word;
                    }
                }
                lines.remove(i);
                var line = lines.get(i);
                while (!line.equals("MEND")) {
                    words = line.split(" |,");
                    for (String word : words) {
                        if (word.equals("")) {
                            continue;
                        }

                        if (word.matches("&[A-Z0-9_]*")) {
                            String rep = "%" + Integer.toString(macro.findArgumentByFormalParameter(word).id);
                            line = line.replace(word, rep);

                        }
                    }
                    macro.definition.lines.add(line);
                    lines.remove(i);
                    line = lines.get(i);
                }
                MacroNameTable.add(macro);
                lines.remove(i);
            } else {
                i++;
            }
        }
        var bufferedWriter = new BufferedWriter(new FileWriter("Pass1Output.txt"));
        for (String string : AssemblyCode.lines) {
            bufferedWriter.write(string + "\n");
        }
        bufferedWriter.close();
    }

    public static void Pass2() throws IOException {
        for (int lineCounter = 0; lineCounter < AssemblyCode.lines.size(); lineCounter++) {
            var line = AssemblyCode.lines.get(lineCounter);
            var words = line.split(" |,|\t");
            var macroIndex = 0;
            Macro macro;
            Boolean noMacro = true;
            for (int i = 0; i < words.length; i++) {
                var word = words[i];
                if (MacroNameTable.stream().filter(x -> x.name.equals(word)).findAny().isPresent()) {
                    macroIndex = i;
                    noMacro = false;
                    break;
                }
            }
            if (!noMacro) {
                var macroWord = words[macroIndex];
                macro = MacroNameTable.stream().filter(x -> x.name.equals(macroWord)).findAny().get();
                for (int i = 0; i < words.length; i++) {
                    if (i != macroIndex) {
                        var word = words[i];
                        if (word.matches("&[A-Z0-9_]*=[A-Z0-9_]*")) {
                            var params = word.split("=");
                            macro.findArgumentByFormalParameter(params[0]).actualParameter = params[1];
                        } else {
                            if (i < macroIndex) {
                                macro.argumentListArray.get(i).actualParameter = word;
                            } else {
                                macro.argumentListArray.get(i - 1).actualParameter = word;
                            }

                        }

                    }
                }

                AssemblyCode.lines.remove(lineCounter);
                for (String macroLines : reverse(macro.definition.lines)) {
                    Pattern pattern = Pattern.compile("%[\\d]+");
                    Matcher matcher = pattern.matcher(macroLines);
                    while (matcher.find()) {
                        String id = matcher.group();

                        var ac = macro.findArgumentById(id.replace("%", "")).actualParameter;
                        macroLines = macroLines.replace(id, ac);
                    }

                    AssemblyCode.lines.add(lineCounter, macroLines);
                }
            }
        }
        var bufferedWriter = new BufferedWriter(new FileWriter("Pass2Output.txt"));
        for (String string : AssemblyCode.lines) {
            bufferedWriter.write(string + "\n");
        }
        bufferedWriter.close();

    }

    public static void main(String[] args) throws IOException {
        AssemblyCode = new Assembly();
        MacroNameTable = new ArrayList<>();
        AssemblyCode.ReadAssembly(args[0]);
        Pass1();
        Pass2();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("MacroDefintition.txt"));
        for (Macro _macro : MacroNameTable) {
            bufferedWriter.write("Macro Name\n" + _macro.name + "\n\n");
            bufferedWriter.write("Argument List Array\n");
            for (var arg : _macro.argumentListArray) {
                bufferedWriter.write(arg.id + " " + arg.formalParameter + " " + arg.actualParameter + "\n");
            }
            bufferedWriter.write("\nDefinition\n");
            for (String line : _macro.definition.lines) {
                bufferedWriter.write(line + "\n");
            }
            bufferedWriter.write("\n");
        }
        bufferedWriter.close();
    }

}