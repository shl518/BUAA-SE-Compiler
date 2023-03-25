import AST.ASTNode;
import Lexer.Lexer;
import MipsGenerate.MidCode;
import MipsGenerate.MidCodeGenerate;
import MipsGenerate.MipsGenerate;
import Parser.Parser;
import Error.ErrorChecker;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Compiler {
    public static void main(String[] args) {
        String input_path = "testfile.txt";
        StringBuilder sb = new StringBuilder();
        File input_file = new File(input_path);
        try (Scanner sc = new Scanner(new FileReader(input_file))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                sb.append(line).append("\n");
            }
            sb.append('~');
            Lexer lexer = new Lexer(sb.toString());
            lexer.analyzeWords();
            //lexer.writeWords();
            Parser parser = new Parser();
            parser.parserRun();
            //parser.writeGrammer();
            //Parser.table.checkTable();
            //ErrorChecker.Debugwriteerrors();
            //ErrorChecker.writeerrors();

            //建树，建符号表
            if(true){
                ASTNode AstTree = parser.getAST();
                MidCodeGenerate irGenerator = new MidCodeGenerate(AstTree);
                ArrayList<MidCode> irList = irGenerator.generate();
                irGenerator.writefile();
//
                MipsGenerate mipsGenerate = new MipsGenerate(irList);
                mipsGenerate.mipsgenerate(false);
                mipsGenerate.writefile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
