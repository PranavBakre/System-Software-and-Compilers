
package Assignment1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Assignment1 {
    static public class Opcode {
        public String Opcode;
        public String StatementClass;
        public String MneumonicInformation;

        public Opcode(String opcode, String Class, String info) {
            Opcode = opcode;
            StatementClass = Class;
            MneumonicInformation = info;
        }
    }

    static public class Register {
        public int RId;
        public String RName;

        public Register(int Id, String Name) {
            RId = Id;
            RName = Name;
        }
    }

    static public class Symbol {
        public String ID;
        public String Name;
        public String Address;
        public int Length;
    }

    static public class Literal {
        public String Literal;
        public String Address;
    }

    static public class LinesOfCode {
        public List<String> Lines;
        public List<StringTokenizer> Tokenizers;
    }

    static int LocationCounter;
    static List<Opcode> MachineOperationTable, PseudoOperationTable;
    static List<Symbol> SymbotTable;
    static List<Literal> LiteralTable;
    static List<Register> RegisterTable;
    // static int LiteralTablePtr;
    // static int SymbolTablePtr;
    static List<Integer> PoolTable;

    public static void Initialize() {
        RegisterTable = new ArrayList<Register>();
        MachineOperationTable = new ArrayList<Opcode>();
        PseudoOperationTable = new ArrayList<Opcode>();
        LiteralTable = new ArrayList<Literal>();
        PoolTable = new ArrayList<Integer>();
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

    public static LinesOfCode ReadAssembly(BufferedReader reader) throws IOException {
        var list = new ArrayList<String>();
        var tokenizers = new ArrayList<StringTokenizer>();
        var line = reader.readLine();
        while (line != null) {
            var tokenizer = new StringTokenizer(line, " ,");
            tokenizers.add(tokenizer);
            list.add(line);
            line = reader.readLine();
        }
        var result = new LinesOfCode();
        result.Lines = list;
        result.Tokenizers = tokenizers;
        reader.close();
        return result;

    }

    public static Boolean isNumber(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String GetType(String value) {

        var mOpcode = MachineOperationTable.stream().filter(x -> x.Opcode == value).findFirst();
        if (mOpcode.isPresent()) {
            return mOpcode.get().StatementClass;
        }
        var pOpcode = PseudoOperationTable.stream().filter(x -> x.Opcode == value).findFirst();
        if (pOpcode.isPresent()) {
            return pOpcode.get().Opcode;
        }
        var register = RegisterTable.stream().filter(x -> x.RName == value).findFirst();
        if (register.isPresent()) {
            return "Register";
        }
        if ((value.contains("'") || value.contains("\"")) && isNumber(value.replaceAll("^['\"](.*)['\"]$", ""))) {
            return "Literal";
        }
        return "Label";
    }

    public static void Pass1(LinesOfCode loc) {
        for (var tokenizer: loc.Tokenizers) {
            while (tokenizer.hasMoreTokens()){
                
            switch (GetType(tokenizer.nextToken())) {
                case "Label":
                break;
                case "LTORG":
                break;
                case "START":
                case "ORIGIN":

                break;
                case "EQU":
                break;
                case "DL":
                break;
                case "IS":
                break;
            }
            
        }
        }
    }

    public static void main(String[] args) throws IOException {
        Initialize();
        InitializeRegisterTable();
        InitializeOT();
        var Reader = new BufferedReader(new FileReader("AssemblyCode.txt"));
        var loc = ReadAssembly(Reader);
        var tokenizers = loc.Tokenizers;

        // Pass1(loc);

        for (var tokenizer : tokenizers) {
            while (tokenizer.hasMoreTokens()) {
                System.out.print(tokenizer.nextToken());
            }
            System.out.println();
        }

    }
}