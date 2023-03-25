/**
 * @description：
 * LVal → Ident {'[' Exp ']'}
 * @author ：xxx
 * @date ：2022/9/24 7:23 PM
 */
package Parser.Exp;

import AST.ASTNode;
import AST.NodeKind;
import Lexer.*;
import Parser.GrammarElement;

import java.util.ArrayList;
import Error.*;
import Parser.Parser;
import Table.ConstType;
import Table.Symbol;
import Table.TableType;

public class LVal extends GrammarElement {
    private int tmpexpAdressDimention = -404;

    @Override
    public ArrayList<String> analyze(){

        Symbol findSymbolByname = ErrorChecker.checkC(Lexer.sym, TableType.NOTFUNC);//查看是否定义
        Parser.expAddressDimention = 404;

        NodeKind kind = NodeKind.ARRAY;
        //TODO 如果未定义，先把adress设置成404，不知道有什么问题
        if(findSymbolByname != null){
            Parser.expAddressDimention = findSymbolByname.getDimension();
            //System.out.println(Lexer.sym.getValue()+" "+findSymbolByname.getDimension());
            kind = findSymbolByname.getConstType().equals(ConstType.CONST)? NodeKind.CONSTARRAY:NodeKind.ARRAY;
        }

        Word ident = Lexer.sym;
        ASTNode identnode = getIdentNode(ident);
        identnode.setKind(NodeKind.INT);//todo AST

        add(Lexer.getNextSym());//Ident
        tmpexpAdressDimention = Parser.expAddressDimention;
        int dimen = 0;
        while(Lexer.symValueIs("[")){

            identnode.setKind(kind);
            add(Lexer.getNextSym()); // [

            Exp exp = new Exp();
            addAll(exp.analyze());
            if(dimen == 0){
                identnode.setLeft(exp.getNode());
            }else {
                identnode.setRight(exp.getNode());
            }

            match("]");
            dimen += 1;
            identnode.setNum(dimen);
            tmpexpAdressDimention -= 1;
        }
        Parser.expAddressDimention = tmpexpAdressDimention;

        astnode = identnode;
        add("<LVal>");
        return sublist;
    }
}
