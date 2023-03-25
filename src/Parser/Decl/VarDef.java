/**
 * @description： //变量定义 VarDef → Ident { '[' ConstExp ']' }  |  Ident { '[' ConstExp ']' } '=' InitVal
 * 消除左递归
 * //变量定义 VarDef → Ident { '[' ConstExp ']' }  ['=' InitVal]
 * @author ：xxx
 * @date ：2022/9/24 9:07 PM
 */
package Parser.Decl;

import AST.ASTNode;
import AST.NodeKind;
import AST.NodeType;
import Lexer.*;
import Parser.Exp.ConstExp;
import Parser.GrammarElement;

import java.util.ArrayList;

import Error.*;
import Parser.Parser;
import Table.ConstType;
import Table.Symbol;
import Table.TableType;

public class VarDef extends GrammarElement {
    @Override
    public ArrayList<String> analyze() {

        Word ident = Lexer.sym;
        ASTNode identnode = getIdentNode(ident);
        identnode.setKind(NodeKind.INT);

        add(Lexer.getNextSym());//IDENT

        boolean isArray = false;
        int dimen = 0;
        while (Lexer.symValueIs("[")) {
            isArray = true;
            identnode.setKind(NodeKind.ARRAY);
            add(Lexer.getNextSym());// [

            ConstExp constExp = new ConstExp();
            addAll(constExp.analyze());
            if (dimen == 0) {
                identnode.setLeft(constExp.getNode());//dimen1
            } else {
                identnode.setRight(constExp.getNode());//dimen2
            }
            match("]");
            dimen += 1;
            identnode.setNum(dimen);
        }

        ASTNode initvalnode = null;
        if (Lexer.symValueIs("=")) {
            add(Lexer.getNextSym());// =
            if(Lexer.symValueIs("getint")){
                int line = Lexer.sym.getLine();
                int index = Lexer.index;
                add(Lexer.getNextSym());//getint
                add(Lexer.getNextSym());//(
                add(Lexer.getNextSym());//)
                Lexer.preDeal2(line,index,ident.getValue());
            }else {
                InitVal initVal = new InitVal();
                addAll(initVal.analyze());
                initvalnode = initVal.getNode();
            }
        }


        Parser.addIntergerOrArray(ident, isArray, dimen, 0, 0);//todo d1,d2

        this.astnode = new ASTNode(NodeType.VarDef, identnode, initvalnode);
        add("<VarDef>");
        return sublist;
    }
}
