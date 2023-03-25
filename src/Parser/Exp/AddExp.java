/**
 * @description：
 * //加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp
 * 消除左递归
 * //加减表达式 AddExp → MulExp {('+' | '−') MulExp}
 * @author ：xxx
 * @date ：2022/9/24 5:38 PM
 */
package Parser.Exp;

import AST.ASTNode;
import AST.NodeType;
import Lexer.*;
import Parser.GrammarElement;
import Error.*;
import java.util.ArrayList;

public class AddExp extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        MulExp mulExp = new MulExp();
        addAll(mulExp.analyze());
        astnode = mulExp.getNode();

        while(Lexer.symValueIs("+") ||Lexer.symValueIs("-")){
            add("<AddExp>");
            String op = Lexer.sym.getValue();
            add(Lexer.getNextSym());// + -
            MulExp newbranch = new MulExp();
            addAll(newbranch.analyze());

            ASTNode newroot = new ASTNode(NodeType.OP,astnode,newbranch.getNode());//todo AST
            newroot.setOpstring(op);
            astnode = newroot;
        }
        add("<AddExp>");
        return sublist;
    }

}
