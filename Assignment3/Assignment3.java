package Assignment3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Assignment3 {

    public static Assembly  AssemblyCode;
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
                    if (line!="")
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

    public static void Pass1() throws IOException{
        var lines=AssemblyCode.lines;
        for (int i=0 ;i< lines.size();) {
            if (lines.get(i).equals("MACRO")){
                lines.remove(i);
                var macro=new Macro();
                var words=lines.get(i).split(" |,|\t");
                
                for (String word : words) {
                    if (word.matches("&[A-Z0-9_]*")){
                        System.out.println(word);
                        macro.addArgument(word);
                    }
                    else {
                        macro.name=word;
                    }
                }
                lines.remove(i);
                var line=lines.get(i);
                while(!line.equals("MEND")){
                    
                    words=line.split(" |,");
                    for (String word : words) {
                        if (word.equals("")){
                            continue;
                        }
                        
                        if (word.matches("&[A-Z0-9_]*")){
                            //System.out.println(word);
                            String rep="%"+Integer.toString(macro.findArgumentByFormalParameter(word).id);
                            line=line.replace(word,rep);
                            
                        }
                    }
                    macro.definition.lines.add(line);
                    lines.remove(i);
                    line=lines.get(i);
                }
                MacroNameTable.add(macro);
                lines.remove(i);
            }
            else {
                i++;
            }
        }
        var bufferedWriter=new BufferedWriter(new FileWriter("AssemblMinusMacro.txt"));
        for (String string : AssemblyCode.lines) {
            bufferedWriter.write(string+"\n");
        }
        bufferedWriter.close();
    }
    
    public static void Pass2() {

        for (var line : AssemblyCode.lines) {
            var words=line.split(" |,");
            var macroIndex=0;
            for(int i=0;i<words.length;i++){
                var word=words[i];
                if(MacroNameTable.stream().filter(x->x.name.equals(word)).findAny().isPresent()){
                    macroIndex=i;
                }
                else {
                    if (word.matches("&[A-Z0-9_]*=[A-Z0-9_]*")){
                        var params=word.split("&|=");
                        
                    }
                }
            }
        }
    }
    public static void main(String[] args) throws IOException {
        AssemblyCode=new Assembly();
        MacroNameTable=new ArrayList<>();
        AssemblyCode.ReadAssembly(args[0]);
        Pass1();
        Pass2();
    }

}