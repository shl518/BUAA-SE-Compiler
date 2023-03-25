/**
 * @description：
 * @author ：xxx
 * @date ：2022/9/24 7:12 PM
 */
package Parser.Exp;

import AST.ASTNode;
import AST.NodeType;
import Lexer.Lexer;
import Parser.GrammarElement;
import Error.*;
import java.util.ArrayList;

public class UnaryOp extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        String op = Lexer.sym.getValue();
        add(Lexer.getNextSym());// + - !

        astnode = new ASTNode(NodeType.OP);
        astnode.setOpstring(op);

        add("<UnaryOp>");
        return sublist;
    }
}
