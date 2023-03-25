/**
 * @description：
 * //关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
 * 消除左递归
 * //关系表达式 RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}
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

public class RelExp extends GrammarElement {
    @Override
    public ArrayList<String > analyze(){
        AddExp addExp = new AddExp();
        addAll(addExp.analyze());
        astnode = addExp.getNode();
        while(Lexer.symValueIs("<") || Lexer.symValueIs(">") ||
        Lexer.symValueIs("<=") || Lexer.symValueIs(">=")){
            add("<RelExp>");
            String op = Lexer.sym.getValue();
            add(Lexer.getNextSym());// < > <= >=
            AddExp newbranch = new AddExp();
            addAll(newbranch.analyze());
            astnode = new ASTNode(NodeType.OP,astnode,newbranch.getNode());
            astnode.setOpstring(op);
        }
        add("<RelExp>");
        return sublist;
    }
}
