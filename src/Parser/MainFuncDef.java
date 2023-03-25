/**
 * @description：
 * MainFuncDef → 'int' 'main' '(' ')' Block
 * @author ：szy
 * @date ：2022/9/24 2:43 PM
 */
package Parser;

import Lexer.*;
import Parser.Func.Block;
import Table.*;
import Error.*;
import java.util.ArrayList;

public class MainFuncDef extends GrammarElement{

    @Override
    public ArrayList<String> analyze(){

        Parser.funckind = FuncKind.INT;

        add(Lexer.getNextSym());// int
        Word ident = Lexer.sym;
        add(Lexer.getNextSym());// main
        add(Lexer.getNextSym());// (
        match(")");

        Parser.createTable(BlockType.MAIN);
        Parser.blockhasReturn = false;
        Parser.lastIsReturn = false;

        Block block = new Block();
        addAll(block.analyze());//Block
        this.astnode = block.getNode();//todo AST

        Symbol newsymbol = new Symbol(ident.getValue(),ident, TableType.FUNC, ConstType.FUNCTTYPE);
        newsymbol.setFuncKind(FuncKind.INT);
        newsymbol.setParams(Parser.table.getSymbols());
        Parser.table.addSymbol(newsymbol);

        Parser.outTable();
        ErrorChecker.checkG(Lexer.sym);

        Parser.funckind = null;
        add("<MainFuncDef>");
        return sublist;
    }
}
