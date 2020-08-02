
package Assignment1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
        public int RId;
        public String RName;

        public Register(int Id, String Name) {
            RId = Id;
            RName = Name;
        }
    }

    public static class Symbol {
        // public String ID;
        public String Name;
        public int Address;
        public int Length;

        public Symbol(String name, int length) {
            Name = name;
            Length = length;
        }
    }

    public static class Literal {
        public String Literal;
        public int Address;

        public Literal(String literal) {
            Literal = literal;
        }
    }

    public static class LinesOfCode {
        public List<String> Lines;
        public List<ArrayList<String>> Words;
    }

    static int LocationCounter;
    static List<Opcode> MachineOperationTable, PseudoOperationTable;
    static List<Symbol> SymbolTable;
    static List<Literal> LiteralTable;
    static List<Register> RegisterTable;
    static List<Integer> PoolTable;

    public static void Initialize() {
        RegisterTable = new ArrayList<Register>();
        MachineOperationTable = new ArrayList<Opcode>();
        PseudoOperationTable = new ArrayList<Opcode>();
        LiteralTable = new ArrayList<Literal>();
        PoolTable = new ArrayList<Integer>();
        SymbolTable= new ArrayList<Symbol>();
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
    }

    public static class Condition{
        
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
        var register = RegisterTable.stream().filter(x -> x.RName.equals(value)).findFirst();
        if (register.isPresent()) {
            return "Register";
        }
        if (value.matches("^['\"](\\d*)['\"]$")) {
            return "Literal";
        }

        if (value.matches("^\\d*$")) {
            return "Address";
        }

        return "Label";
    }

    public static void Pass1(LinesOfCode loc) {
        for (var wordsInLine : loc.Words) {
            for (int i = 0; i < wordsInLine.size(); i++) {
                var word = wordsInLine.get(i);
                switch (GetType(word)) {
                    case "Label":
                        System.out.print(word+"\t"+LocationCounter);
                        var symbol = SymbolTable.stream().filter(x -> x.Name.equals(word)).findFirst();
                        if (!symbol.isPresent()) {
                            var newSymbol = new Symbol(word, 1);
                            SymbolTable.add(newSymbol);
                        }
                        if (i == 0) {
                            SymbolTable.stream().filter(x -> x.Name.equals(word)).findFirst().get().Address = LocationCounter;
                            System.out.print("Assigned address to "+SymbolTable.get(SymbolTable.size() - 1).Name+"\t");
                        }

                        break;
                    case "Literal":
                        System.out.print("Literal");
                        LiteralTable.add(new Literal(word));
                        break;
                    case "LTORG":
                    System.out.print("LTORG\t"+LocationCounter+"\t");    
                    for (var literal : LiteralTable) {
                            literal.Address = LocationCounter;
                            LocationCounter++;
                        }
                        
                        break;
                    case "START":
                    case "ORIGIN":
                        
                        i++;
                        LocationCounter = Integer.parseInt(wordsInLine.get(i));
                        System.out.print("ORIGIN\t"+LocationCounter+"\t");
                        break;
                    case "EQU":
                        System.out.print("EQU\t"+LocationCounter+"\t");
                        LocationCounter++;
                        break;
                    case "DL":
                        System.out.print("DL\t"+LocationCounter+"\t");
                        LocationCounter++;
                        break;

                    case "IS":
                        System.out.print("IS\t"+LocationCounter+"\t");
                        LocationCounter++;
                        break;

                    case "Register":
                    System.out.print("Register\t"+LocationCounter+"\t");
                    break;
                    default:
                    System.out.print(word+"\t");
                }

            }
            System.out.println();
        }
    }

    public static void main(String[] args) throws IOException {
        Initialize();
        InitializeRegisterTable();
        InitializeOT();
        var Reader = new BufferedReader(new FileReader("AssemblyCode2.txt"));
        var loc = ReadAssembly(Reader);
        var lines = loc.Words;

        Pass1(loc);

        for (var words : lines) {
            for (var word : words) {
                System.out.print(word + "\t");
            }
            System.out.println();
        }
        for(var sbl: SymbolTable){
            System.out.println(sbl.Name+"\t"+sbl.Address);
        }
    }
}