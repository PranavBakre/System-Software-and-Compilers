package Assignment1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Assignment1 {

    // Parent class for Constants, Literals and Symbols
    public static class Operand {
        public int Id;
        public int Value;
        public Address Address;

        public char GetType() {
            return this.getClass().getSimpleName().charAt(0);
        }

    }

    public static class Symbol extends Operand {
        public String Name;
        public int Length;

        public Symbol(String name, int length) {
            Id = SymbolTable.size();
            Name = name;
            Length = length;
            Address = new Address();
        }

        public String toString() {
            return this.Name + "\t" + this.Address.Value;
        }
    }

    public static class Constant extends Operand {
        public Constant(int value) {
            Id = Constants.size();
            Value = value;

        }
    }

    public static class Literal extends Operand {
        public Literal(String literal) {
            Value = Integer.parseInt(literal.replaceAll("'", ""));
            Id = LiteralTable.size();
            Address = new Address();
        }

        public String toString() {
            return this.Value + "\t" + this.Address.Value;
        }
    }

    // Opcode class for Operation Table
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

    // Wrapper class for integer
    public static class Address {
        public int Value;

        public Address() {
            Value = 0;
        }
    }

    // Register class for Register table
    public static class Register {
        public int Id;
        public String Name;

        public Register(int id, String name) {
            Id = id;
            Name = name;
        }
    }

    // Class for storing lines and words in assembly code
    public static class LinesOfCode {
        public List<String> Lines;
        public List<ArrayList<String>> Words;
    }

    // Condition class used specially for BC instruction. However other instructions
    // can be added as well
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

    // Class for intermediate code. Each line is saved in a Intermediae Code
    // statement
    public static class IntermediateCodeStatement {
        public Address Address;
        public Opcode Opcode;
        public int Operand1;
        public Operand Operand2;

        public IntermediateCodeStatement() {
            Address = new Address();
        }

        public String toString() {

            return String.format("%d)\t(%s,%s)\t(%d)\t(%s)", this.Address.Value,
                    this.Opcode != null ? this.Opcode.StatementClass : "-",
                    this.Opcode != null ? this.Opcode.MneumonicInformation : "-", this.Operand1,
                    this.Operand2 != null
                            ? "" + this.Operand2.GetType() + ","
                                    + (this.Operand2.GetType() == 'C' ? this.Operand2.Value : this.Operand2.Id)
                            : '-');
        }
    }

    static int LocationCounter;
    // Machine Operation Table stores IS and DL statements while Pseudo Operation
    // Table stores Assembler Directives
    static List<Opcode> MachineOperationTable, PseudoOperationTable;
    static List<Symbol> SymbolTable;
    static List<Literal> LiteralTable;
    static List<Register> RegisterTable;
    static List<Integer> PoolTable;
    static int NextPoolIndex;
    static List<Condition> Conditions;
    static List<Constant> Constants;
    static List<IntermediateCodeStatement> IntermediateCode;
    static List<String> MachineCode;

    // Function for initializing all the required data structures
    public static void Initialize() {
        RegisterTable = new ArrayList<>();
        MachineOperationTable = new ArrayList<>();
        PseudoOperationTable = new ArrayList<>();
        LiteralTable = new ArrayList<>();
        PoolTable = new ArrayList<>();
        SymbolTable = new ArrayList<>();
        Conditions = new ArrayList<>();
        Constants = new ArrayList<>();
        NextPoolIndex = 0;
        IntermediateCode = new ArrayList<>();
        MachineCode = new ArrayList<>();
    }

    // Insert Registers into Register table
    public static void InitializeRegisterTable() {
        RegisterTable.add(new Register(1, "AREG"));
        RegisterTable.add(new Register(2, "BREG"));
        RegisterTable.add(new Register(3, "CREG"));
        RegisterTable.add(new Register(4, "DREG"));
    }

    // Insert instructions into Operation Table
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

    // Read Assembly Code into LinesOfCode object
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

    // Returns the type of the token.
    // Returns class if instructions are present in MOT,
    // opcode if present in POT, Label,Register,Literal or Constant
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

        return "Label";
    }

    // Pass 1 of the assembler
    public static void Pass1(LinesOfCode loc) {
        for (var wordsInLine : loc.Words) {
            var line = new IntermediateCodeStatement();
            for (int i = 0; i < wordsInLine.size(); i++) {
                var word = wordsInLine.get(i);
                Opcode opcode;
                Register register;
                Constant c;
                switch (GetType(word)) {
                case "Label":
                    var symbol = SymbolTable.stream().filter(x -> x.Name.equals(word)).findFirst();
                    if (!symbol.isPresent()) {
                        var newSymbol = new Symbol(word, 1);
                        SymbolTable.add(newSymbol);
                    }
                    symbol = SymbolTable.stream().filter(x -> x.Name.equals(word)).findFirst();
                    if (i == 0) {
                        symbol.get().Address.Value = LocationCounter;
                    } else {
                        line.Operand2 = symbol.get();
                    }

                    break;
                case "Constant":
                    c = new Constant(Integer.parseInt(word));
                    line.Operand2 = c;
                    Constants.add(c);
                    break;
                case "Literal":
                    var l = new Literal(word);
                    line.Operand2 = l;
                    LiteralTable.add(l);
                    break;
                case "LTORG":
                    opcode = PseudoOperationTable.stream().filter(x -> x.Opcode.equals(word)).findFirst().get();
                    PoolTable.add(NextPoolIndex);
                    for (var literal : LiteralTable.stream().filter(x -> x.Address.Value == 0)
                            .collect(Collectors.toList())) {
                        line = new IntermediateCodeStatement();
                        literal.Address.Value = LocationCounter;
                        line.Address.Value = LocationCounter;
                        line.Opcode = opcode;
                        line.Operand2 = literal;
                        IntermediateCode.add(line);
                        LocationCounter++;
                        line = null;
                    }
                    NextPoolIndex = LiteralTable.size();

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
                                    .get().Address.Value;
                            break;
                        case "Constant":
                            address += Integer.parseInt(nextWord);
                            break;
                        }
                        i++;
                    }
                    LocationCounter = address;
                    opcode = PseudoOperationTable.stream().filter(x -> x.Opcode.equals(word)).findFirst().get();
                    line.Opcode = opcode;
                    c = new Constant(address);
                    Constants.add(c);
                    line.Operand2 = c;
                    break;
                case "EQU":
                    opcode = PseudoOperationTable.stream().filter(x -> x.Opcode.equals(word)).findFirst().get();
                    line.Opcode = opcode;
                    int y = i + 1;
                    int z = i - 1;
                    var label = SymbolTable.stream().filter(x -> x.Name.equals(wordsInLine.get(y))).findFirst().get();
                    SymbolTable.stream().filter(x -> x.Name.equals(wordsInLine.get(z))).findFirst()
                            .get().Address = label.Address;
                    break;
                case "DL":
                    opcode = MachineOperationTable.stream().filter(x -> x.Opcode.equals(word)).findFirst().get();
                    line.Opcode = opcode;
                    line.Address.Value = LocationCounter;
                    LocationCounter++;
                    break;
                case "IS":
                    opcode = MachineOperationTable.stream().filter(x -> x.Opcode.equals(word)).findFirst().get();
                    var condition = Conditions.stream().filter(x -> x.OPCODE.equals(opcode)).findFirst();
                    line.Opcode = opcode;
                    if (condition.isPresent()) {
                        i++;
                        var cond = wordsInLine.get(i);
                        line.Operand1 = Conditions.stream().filter(x -> x.Name.equals(cond)).findFirst().get().Id;
                    }
                    line.Address.Value = LocationCounter;
                    LocationCounter++;
                    break;
                case "Register":
                    register = RegisterTable.stream().filter(x -> x.Name.equals(word)).findFirst().get();
                    line.Operand1 = register.Id;
                    break;
                case "END":
                    opcode = PseudoOperationTable.stream().filter(x -> x.Opcode.equals(word)).findFirst().get();
                    var unassignedLiterals = LiteralTable.stream().filter(x -> x.Address.Value == 0)
                            .collect(Collectors.toList());
                    if (unassignedLiterals.size() != 0) {
                        PoolTable.add(NextPoolIndex);
                        for (var literal : unassignedLiterals) {
                            line = new IntermediateCodeStatement();
                            literal.Address.Value = LocationCounter;
                            line.Address.Value = LocationCounter;
                            line.Opcode = opcode;
                            line.Operand2 = literal;
                            IntermediateCode.add(line);
                            LocationCounter++;
                            line = null;
                        }
                    }
                    break;
                default:
                    System.out.print(word + "\t");
                }

            }
            if (line != null)
                if (line.Opcode != null)
                    IntermediateCode.add(line);
        }
    }

    // Pass 2 of the assembler
    public static void Pass2() {
        LocationCounter = 0;
        String MCLine;
        for (var line : IntermediateCode) {
            MCLine = "";
            switch (GetType(line.Opcode.Opcode)) {
            case "START":
            case "ORIGIN":
                LocationCounter = line.Operand2.Value;
                break;
            case "DL":
                MCLine += line.Address.Value + ")";
                break;
            case "IS":
                MCLine += line.Address.Value + ")\t";
                if (line.Opcode != null) {
                    MCLine += line.Opcode.MneumonicInformation + "\t";
                    if (line.Opcode.Opcode.equals("STOP")) {
                        line.Operand2 = new Literal("'0'");
                    }
                }
                MCLine += line.Operand1 + "\t";
                if (line.Operand2 != null)
                    MCLine += line.Operand2.Address.Value;

                break;
            case "LTORG":
            case "END":
                MCLine += line.Address.Value + ")\t";
                MCLine += 00 + "\t";
                MCLine += line.Operand1 + "\t";
                MCLine += line.Operand2.Value;
                break;
            }
            if (MCLine != "")
                MachineCode.add(MCLine);
        }
    }

    // Main calling function
    public static void main(String[] args) throws IOException {
        Initialize();
        InitializeRegisterTable();
        InitializeOT();
        var Reader = new BufferedReader(new FileReader(args[0]));
        var loc = ReadAssembly(Reader);
        var lines = loc.Words;
        System.out.println("Code");
        for (var words : lines) {
            for (var word : words) {
                System.out.print(word + "\t");
            }
            System.out.println();
        }
        System.out.println("Intermediate Code");
        Pass1(loc);
        var fileWriter = new FileWriter("IntermediateCode.txt");
        var bufferedWriter = new BufferedWriter(fileWriter);
        for (var line : IntermediateCode) {
            System.out.println(line.toString());
            bufferedWriter.write(line.toString() + "\n");
        }
        bufferedWriter.close();
        fileWriter.close();
        System.out.println("Symbol Table");
        for (var sbl : SymbolTable) {
            System.out.println(sbl.toString());
        }
        System.out.println("Literal Table");
        for (var sbl : LiteralTable) {
            System.out.println(sbl.toString());
        }
        System.out.println("Pool Table");
        for (var poolPtr : PoolTable) {
            System.out.println(poolPtr);
        }
        Pass2();
        fileWriter = new FileWriter("MachineCode.txt");
        bufferedWriter = new BufferedWriter(fileWriter);

        System.out.println("Machine Code");
        for (var line : MachineCode) {
            System.out.println(line);
            bufferedWriter.write(line + "\n");
        }
        bufferedWriter.close();
        fileWriter.close();
    }
}