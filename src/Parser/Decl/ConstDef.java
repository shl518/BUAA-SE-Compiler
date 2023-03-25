/**
 * @description：Ident { '[' <ConstExp> ']' } '=' <ConstInitVal>
 * @author ：xxx
 * @date ：2022/9/24 4:43 PM
 * todo d1,d2，数组初值没得到，之后可能要获取
 */
package Parser.Decl;

import AST.ASTNode;
import AST.NodeKind;
import AST.NodeType;
import Lexer.*;
import Parser.Exp.ConstExp;
import Parser.Exp.ConstInitVal;
import Parser.GrammarElement;

import java.util.ArrayList;
import Error.*;
import Parser.Parser;
import Table.ConstType;
import Table.Symbol;
import Table.TableType;

public class ConstDef extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){

        Word ident = Lexer.sym;
        ASTNode identnode = getIdentNode(ident);
        identnode.setKind(NodeKind.CONSTINT);

        add(Lexer.getNextSym()); //IDENT
        boolean isArray = false;
        int dimen = 0;
        while (Lexer.symValueIs("[")){
            isArray = true;
            add(Lexer.getNextSym());// [
            identnode.setKind(NodeKind.CONSTARRAY);

            ConstExp constExp = new ConstExp();
            addAll(constExp.analyze());//ConstExp
            if(dimen == 0){
                identnode.setLeft(constExp.getNode());
            }else {
                identnode.setRight(constExp.getNode());
            }

            match("]");
            dimen += 1;
            identnode.setNum(dimen);
        }
        add(Lexer.getNextSym());// =

        ConstInitVal constInitVal = new ConstInitVal();
        addAll(constInitVal.analyze());
        astnode = new ASTNode(NodeType.ConstDef,identnode,constInitVal.getNode());

        //以下仅仅服务于错误处理
        TableType tableType = isArray? TableType.ARRAY:TableType.INTEGER;
        Symbol newsymbol = new Symbol(ident.getValue(),ident,tableType, ConstType.CONST);
        if(isArray){
            newsymbol.setDimension(dimen,0,0);//todo d1,d2没写,数组初值也没写
        }

        if(!ErrorChecker.checkB(newsymbol)){
            Parser.table.addSymbol(newsymbol);
        }

        add("<ConstDef>");
        return sublist;
    }
}
