/**
 * @description：
 * 消除左递归之后 MulExp → UnaryExp {('*' | '/' | '%') UnaryExp}
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

public class MulExp extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        UnaryExp unaryExp = new UnaryExp();
        addAll(unaryExp.analyze());
        astnode = unaryExp.getNode();

        while (Lexer.symValueIs("*") || Lexer.symValueIs("%")
        || Lexer.symValueIs("/") || Lexer.symValueIs("bitand")){
            add("<MulExp>"); //是上一个的语法父节点
            String op = Lexer.sym.getValue();
            add(Lexer.getNextSym()); //* / %

            UnaryExp newbranch = new UnaryExp();
            addAll(newbranch.analyze());
            ASTNode newroot = new ASTNode(NodeType.OP,astnode,newbranch.getNode());
            newroot.setOpstring(op);
            astnode = newroot;
        }
        add("<MulExp>");
        return sublist;
    }
}
