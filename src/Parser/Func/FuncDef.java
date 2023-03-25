/**
 * @description
 * //函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
 * @author ：xxx
 * @date ：2022/9/24 4:12 PM
 */
package Parser.Func;

import AST.ASTNode;
import AST.NodeKind;
import AST.NodeType;
import Error.Error;
import Lexer.*;
import Parser.GrammarElement;
import Error.*;
import Parser.Parser;
import Table.*;

import java.util.ArrayList;

public class FuncDef extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        //
        String fkind = Lexer.sym.getValue();
        FuncType funcType = new FuncType();
        ArrayList<String> funcTypereturn = funcType.analyze();

        FuncKind funckind = fkind.equals("void") ? FuncKind.VOID : FuncKind.INT;
        NodeKind nodeKind = fkind.equals("void") ? NodeKind.VOID : NodeKind.RETURNINT;
        Parser.funckind = funckind;

        addAll(funcTypereturn);

        Word ident = Lexer.sym;
        add(Lexer.getNextSym());//IDENT

        // todo AST
        ASTNode identnode = new ASTNode(NodeType.Ident,ident.getValue());//IDENT name
        identnode.setKind(nodeKind);
        //todo AST

        add(Lexer.getNextSym());// (

        Symbol newsymbol = new Symbol(ident.getValue(),ident, TableType.FUNC, ConstType.FUNCTTYPE);
        newsymbol.setFuncKind(funckind);

        if(!ErrorChecker.checkB(newsymbol)){
            Parser.table.addSymbol(newsymbol);
        }
        Parser.createTable(BlockType.FUNC);
        Parser.blockhasReturn = false;
        Parser.lastIsReturn = false;

        ArrayList<Symbol> params = new ArrayList<>();
        if(Lexer.symValueIs(")")){
            add(Lexer.getNextSym());// )
        }else if(Lexer.symValueIs("{")){
            ErrorChecker.add(new Error(Lexer.getLastToken(),"j"));
        } else {
            FuncFParams funcFParams = new FuncFParams();
            addAll(funcFParams.analyze());
            identnode.setLeft(funcFParams.getNode());//todo AST

            params.addAll(Parser.table.getSymbols());
            match(")");
        }
        newsymbol.setParams(params);

        Block block = new Block();
        addAll(block.analyze());
        ASTNode funcbody = block.getNode();//todo AST

        Parser.outTable();
        ErrorChecker.checkG(Lexer.getLastToken());//因为前一个才是 }
        Parser.funckind = null;

        this.astnode = new ASTNode(NodeType.FuncDef,identnode,funcbody);//todo AST

        add("<FuncDef>");
        return sublist;
    }
}
