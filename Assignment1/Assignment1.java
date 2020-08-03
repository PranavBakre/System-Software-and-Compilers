
package Assignment1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Assignment1 {
    public static class Opcode {
        public String Opcode;
        public String StatementClass;
        public String MneumonicInformation;

        public Opcode(String opcode, String Class, String info) {
            Opcode = opcode;
            StatementClass = Class;
            MneumonicInformation = info;
        }
    }

    public static class Register {
        public int Id;
        public String Name;

        public Register(int id, String name) {
            Id = id;
            Name = name;
        }
    }

    public static class Symbol {
        public int Id;
        public String Name;
        public int Address;
        public int Length;

        public Symbol(String name, int length) {
            Id = SymbolTable.size();
            Name = name;
            Length = length;
        }
    }

    public static class Literal {
        public int Id;
        public String Literal;
        public int Address;

        public Literal(String literal) {
            Literal = literal;
            Id = LiteralTable.size();
        }
    }

    public static class LinesOfCode {
        public List<String> Lines;
        public List<ArrayList<String>> Words;
    }

    public static class Condition {
        public Opcode OPCODE;
        public String Name;
        public int Id;

        public Condition(Opcode opcode, String name, int id) {
            OPCODE = opcode;
            Name = name;
            Id = id;
        }
    }

    public static class IntermediateCode {

    }

    static int LocationCounter;
    static List<Opcode> MachineOperationTable, PseudoOperationTable;
    static List<Symbol> SymbolTable;
    static List<Literal> LiteralTable;
    static List<Register> RegisterTable;
    static List<Integer> PoolTable;
    static List<Condition> Conditions;
    static List<Integer> Constants;

    public static void Initialize() {
        RegisterTable = new ArrayList<>();
        MachineOperationTable = new ArrayList<>();
        PseudoOperationTable = new ArrayList<>();
        LiteralTable = new ArrayList<>();
        PoolTable = new ArrayList<>();
        SymbolTable = new ArrayList<>();
        Conditions = new ArrayList<>();
        Constants = new ArrayList<>();
        PoolTable.add(0);
    }

    public static void InitializeRegisterTable() {
        RegisterTable.add(new Register(1, "AREG"));
        RegisterTable.add(new Register(2, "BREG"));
        RegisterTable.add(new Register(3, "CREG"));
        RegisterTable.add(new Register(4, "DREG"));
    }

    public static void InitializeOT() {
        MachineOperationTable.add(new Opcode("STOP", "IS", "00"));
        MachineOperationTable.add(new Opcode("ADD", "IS", "01"));
        MachineOperationTable.add(new Opcode("SUB", "IS", "02"));
        MachineOperationTable.add(new Opcode("MULT", "IS", "03"));
        MachineOperationTable.add(new Opcode("MOVER", "IS", "04"));
        MachineOperationTable.add(new Opcode("MOVEM", "IS", "05"));
        MachineOperationTable.add(new Opcode("COMP", "IS", "06"));
        MachineOperationTable.add(new Opcode("BC", "IS", "07"));
        MachineOperationTable.add(new Opcode("DIV", "IS", "08"));
        MachineOperationTable.add(new Opcode("READ", "IS", "09"));
        MachineOperationTable.add(new Opcode("PRINT", "IS", "10"));
        MachineOperationTable.add(new Opcode("DC", "DL", "01"));
        MachineOperationTable.add(new Opcode("DS", "DL", "02"));
        PseudoOperationTable.add(new Opcode("START", "AD", "01"));
        PseudoOperationTable.add(new Opcode("END", "AD", "02"));
        PseudoOperationTable.add(new Opcode("ORIGIN", "AD", "03"));
        PseudoOperationTable.add(new Opcode("EQU", "AD", "04"));
        PseudoOperationTable.add(new Opcode("LTORG", "AD", "05"));
        var opcode = MachineOperationTable.stream().filter(x -> x.Opcode.equals("BC")).findFirst().get();

        Conditions.add(new Condition(opcode, "LT", 1));
        Conditions.add(new Condition(opcode, "LE", 2));
        Conditions.add(new Condition(opcode, "EQ", 3));
        Conditions.add(new Condition(opcode, "GT", 4));
        Conditions.add(new Condition(opcode, "GE", 5));
        Conditions.add(new Condition(opcode, "ANY", 6));
    }

    public static LinesOfCode ReadAssembly(BufferedReader reader) throws IOException {
        var list = new ArrayList<String>();
        var allWords = new ArrayList<ArrayList<String>>();
        var line = reader.readLine();
        while (line != null) {
            var words = line.split(" |,");
            var wordslist = new ArrayList<>(Arrays.asList(words));
            wordslist.removeAll(Arrays.asList("", null));
            allWords.add(wordslist);
            list.add(line);
            line = reader.readLine();
        }
        var result = new LinesOfCode();
        result.Lines = list;
        result.Words = allWords;
        reader.close();
        return result;

    }

    public static String GetType(String value) {
        var mOpcode = MachineOperationTable.stream().filter(x -> x.Opcode.equals(value)).findFirst();
        if (mOpcode.isPresent()) {
            return mOpcode.get().StatementClass;
        }
        var pOpcode = PseudoOperationTable.stream().filter(x -> x.Opcode.equals(value)).findFirst();
        if (pOpcode.isPresent()) {
            return pOpcode.get().Opcode;
        }
        var register = RegisterTable.stream().filter(x -> x.Name.equals(value)).findFirst();
        if (register.isPresent()) {
            return "Register";
        }
        if (value.matches("^['\"](\\d*)['\"]$")) {
            return "Literal";
        }

        if (value.matches("^[+-]*\\d*$")) {
            return "Constant";
        }
        if (value.matches("^.+[\\*+-\\/].+$")) {
            return "Complex";
        }
        return "Label";
    }

    public static void LabelHandling(int index, String word) {
        var symbol = SymbolTable.stream().filter(x -> x.Name.equals(word)).findFirst();
        if (!symbol.isPresent()) {
            var newSymbol = new Symbol(word, 1);
            SymbolTable.add(newSymbol);
        }
        symbol = SymbolTable.stream().filter(x -> x.Name.equals(word)).findFirst();
        if (index == 0) {
            symbol.get().Address = LocationCounter;
        }
        System.out.print("S\t" + symbol.get().Id + "\t");
    }

    public static void Pass1(LinesOfCode loc) {
        for (var wordsInLine : loc.Words) {
            for (int i = 0; i < wordsInLine.size(); i++) {
                var word = wordsInLine.get(i);
                Opcode opcode;
                Register register;
                switch (GetType(word)) {
                    case "Label":
                        LabelHandling(i, word);
                        break;
                    case "Constant":
                        System.out.print("C\t" + word + "\t");
                        Constants.add(Integer.parseInt(word));
                        break;
                    case "Literal":
                        System.out.print("L\t" + LiteralTable.size());
                        LiteralTable.add(new Literal(word));
                        break;
                    case "LTORG":
                        opcode = PseudoOperationTable.stream().filter(x -> x.Opcode.equals(word)).findFirst().get();
                        System.out.print(opcode.StatementClass + "\t" + opcode.MneumonicInformation + "\t");
                        for (var literal : LiteralTable.stream().filter(x -> x.Address == 0)
                                .collect(Collectors.toList())) {
                            literal.Address = LocationCounter;
                            System.out.print(literal.Literal + "\n\t\t");
                            LocationCounter++;
                        }
                        PoolTable.add(LiteralTable.size());
                        break;
                    case "START":
                    case "ORIGIN":
                        var address = 0;
                        i++;
                        while (i < wordsInLine.size()) {
                            var nextWord = wordsInLine.get(i);
                            switch (GetType(nextWord)) {

                                case "Label":

                                    address += SymbolTable.stream().filter(x -> x.Name.equals(nextWord)).findFirst()
                                            .get().Address;
                                    break;
                                case "Constant":
                                    address += Integer.parseInt(nextWord);
                                    break;
                            }
                            i++;
                        }
                        LocationCounter = address;
                        opcode = PseudoOperationTable.stream().filter(x -> x.Opcode.equals(word)).findFirst().get();
                        System.out.print(opcode.StatementClass + "\t" + opcode.MneumonicInformation + "\t" + "C\t"
                                + LocationCounter);

                        break;
                    case "EQU":
                        System.out.print("EQU\t" + LocationCounter + "\t");
                        int y = i + 1;
                        int z = i - 1;
                        var label = SymbolTable.stream().filter(x -> x.Name.equals(wordsInLine.get(y))).findFirst()
                                .get();
                        SymbolTable.stream().filter(x -> x.Name.equals(wordsInLine.get(z))).findFirst()
                                .get().Address = label.Address;
                        LocationCounter++;
                        break;
                    case "DL":
                        opcode = MachineOperationTable.stream().filter(x -> x.Opcode.equals(word)).findFirst().get();
                        System.out.print(opcode.StatementClass + "\t" + opcode.MneumonicInformation + "\t");
                        LocationCounter++;
                        break;

                    case "IS":

                        opcode = MachineOperationTable.stream().filter(x -> x.Opcode.equals(word)).findFirst().get();
                        var condition = Conditions.stream().filter(x -> x.OPCODE.equals(opcode)).findFirst();
                        System.out.print(opcode.StatementClass + "\t" + opcode.MneumonicInformation + "\t");

                        if (condition.isPresent()) {
                            i++;
                            System.out.print(wordsInLine.get(i) + "\t");
                        }
                        LocationCounter++;

                        break;

                    case "Register":
                        register = RegisterTable.stream().filter(x -> x.Name.equals(word)).findFirst().get();
                        System.out.print("R\t" + register.Id + "\t");
                        break;
                    case "END":
                        opcode = PseudoOperationTable.stream().filter(x -> x.Opcode.equals(word)).findFirst().get();
                        System.out.print(opcode.StatementClass + "\t" + opcode.MneumonicInformation + "\t");
                        for (var literal : LiteralTable.stream().filter(x -> x.Address == 0)
                                .collect(Collectors.toList())) {
                            literal.Address = LocationCounter;
                            System.out.print(literal.Literal + "\n\t\t");
                            LocationCounter++;
                        }
                        break;
                    default:
                        System.out.print(word + "\t");
                }

            }
            System.out.println();
        }
    }

    public static void main(String[] args) throws IOException {
        Initialize();
        InitializeRegisterTable();
        InitializeOT();
        var Reader = new BufferedReader(new FileReader("AssemblyCode.txt"));
        var loc = ReadAssembly(Reader);
        var lines = loc.Words;

        Pass1(loc);

        for (var words : lines) {
            for (var word : words) {
                System.out.print(word + "\t");
            }
            System.out.println();
        }
        for (var sbl : SymbolTable) {
            System.out.println(sbl.Name + "\t" + sbl.Address);
        }
    }
}