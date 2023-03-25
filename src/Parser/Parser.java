/**
 * @description：词法分析器，仅仅服务于语法分析和错误处理，Parser错误处理时建立的符号表只建了一半，这个符号表只服务于错误处理
 * ，并不是最终我们要建的符号表，我们在ASTBuild里面分析语法树，建立的是最终的符号表
 * @author ：szy
 * @date ：2022/9/24 1:00 PM
 */
package Parser;

import AST.ASTNode;
import Lexer.*;
import Table.*;
import Error.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
    private final ArrayList<String> grammerList;

    public static  Table table = new Table(null,BlockType.GLOBAL);
    public static int intoWhile = 0;
    public static boolean blockhasReturn = false;
    public static boolean lastIsReturn = false;
    public static FuncKind funckind = null;
    public static int expAddressDimention = 0;
    private ASTNode AST = null;

    public ASTNode getAST() {
        return AST;
    }

    public Parser() {
        Lexer.sym = Lexer.words.get(0);
        Lexer.index = 0;
        grammerList = new ArrayList<>();

    }
    public void parserRun(){
        CompUnit grm = new CompUnit();

        grammerList.addAll(grm.analyze());
        AST = grm.getNode();
    }

    public void writeGrammer()throws IOException {
        File file = new File("output.txt");
        FileWriter writer = new FileWriter(file);
        for (String grammar : grammerList) {
            //System.out.println(grammar);
            writer.write(grammar+"\n");
        }
        writer.flush();
        writer.close();
    }

    public static void createTable(BlockType blockType){
        Table newtable = new Table(Parser.table,blockType);
        Parser.table.addson(newtable);
        Parser.table = newtable;
    }
    public static void outTable(){
        Parser.table = Parser.table.getFather();
    }

    public static void addIntergerOrArray(Word ident,boolean isArray, int dimen, int d1, int d2){
        //varDef FuncFparam 都要加Symbol 将其抽离合并
        TableType tableType = isArray? TableType.ARRAY:TableType.INTEGER;
        Symbol newsymbol = new Symbol(ident.getValue(),ident,tableType, ConstType.VAR);
        if(isArray){
            newsymbol.setDimension(dimen,0,0);
        }

        if(!ErrorChecker.checkB(newsymbol)){
            Parser.table.addSymbol(newsymbol);
        }

    }


}
