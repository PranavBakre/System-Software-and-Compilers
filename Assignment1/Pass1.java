import java.io.*;
import java.util.ArrayList;


public class Pass1 {

/*
Label Handling()
LTORG_Handling()
START_ORG_handling()
EQU_Handling
DL_Handling()
IS_Handling()
*/
    public static void main (String args[]) throws IOException {
        //Initialization
        int LC=0;
        int PoolTablePtr=0;
        int LitTabPtr=0;
        int SymTabPtr=0;
        //ArrayList<Pool> PoolTable;


        var reader=new BufferedReader(new FileReader("AssemblyCode.txt"));
        var lines=new ArrayList<String>();
        var line=reader.readLine();
        while(line!=null) {
            lines.add(line);
            line=reader.readLine();
        }
        reader.close();
        for(var x:lines) {
            System.out.println(x);
        }

        //While loop {
/*
            while(NextStmt!=end) {      
                switch(NextStmt){

                }
            }
*/
        //}
        //Processing of END statement
        /*LTORG handling
        Generate intermediate code
        pass 2*/
    }
}