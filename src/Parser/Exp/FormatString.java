/**
 * @description：TODO
 * @author ：szy
 * @date ：2022/9/24 10:22 PM
 */
package Parser.Exp;

import AST.ASTNode;
import AST.NodeType;
import Lexer.*;
import Parser.GrammarElement;

import java.util.ArrayList;
import Error.*;

public class FormatString extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){

        String formatstring = Lexer.sym.getValue();
        ErrorChecker.checkA(formatstring.substring(1,formatstring.length()-1)); //delete "
        add(Lexer.getNextSym());//string
        astnode = new ASTNode(NodeType.FormatString,formatstring);
        return sublist;
    }
}
