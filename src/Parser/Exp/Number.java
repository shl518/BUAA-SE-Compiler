/**
 * @description：TODO
 * @author ：xxx
 * @date ：2022/9/24 7:29 PM
 */
package Parser.Exp;

import AST.ASTNode;
import AST.NodeType;
import Lexer.Lexer;
import Parser.GrammarElement;

import java.util.ArrayList;
import Error.*;

public class Number extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        int num = Integer.parseInt(Lexer.sym.getValue());
        add(Lexer.getNextSym()); // INTCON
        astnode = new ASTNode(NodeType.Number,num);
        add("<Number>");
        return sublist;
    }
}
