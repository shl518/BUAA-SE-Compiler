/**
 * @description：
 * //相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp
 * 消除左递归
 * //相等性表达式 EqExp → RelExp {('==' | '!=') RelExp}
 * @author ：xxx
 * @date ：2022/9/24 10:01 PM
 */
package Parser.Exp;

import AST.ASTNode;
import AST.NodeType;
import Lexer.*;
import Parser.GrammarElement;
import Error.*;
import java.util.ArrayList;

public class EqExp extends GrammarElement {
    @Override
    public ArrayList<String > analyze(){
        RelExp relExp = new RelExp();
        addAll(relExp.analyze());
        astnode = relExp.getNode();

        while(Lexer.symValueIs("==") ||Lexer.symValueIs("!=")){
            add("<EqExp>");
            String op = Lexer.sym.getValue();
            add(Lexer.getNextSym());// ==  !=
            RelExp newbranch = new RelExp();
            addAll(newbranch.analyze());
            astnode = new ASTNode(NodeType.OP,astnode,newbranch.getNode());
            astnode.setOpstring(op);
        }
        add("<EqExp>");
        return sublist;
    }
}
