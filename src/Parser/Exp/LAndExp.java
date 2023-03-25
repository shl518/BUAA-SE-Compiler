/**
 * @description:
 * //逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp
 * 消除左递归
 * //逻辑与表达式 LAndExp → EqExp {'&&' EqExp}
 * @author ：szy
 * @date ：2022/9/24 10:00 PM
 */
package Parser.Exp;

import AST.ASTNode;
import AST.NodeType;
import Lexer.*;
import Parser.GrammarElement;
import Error.*;
import java.util.ArrayList;

public class LAndExp extends GrammarElement {
    @Override
    public ArrayList<String > analyze(){
        EqExp eqExp = new EqExp();
        addAll(eqExp.analyze());
        astnode = eqExp.getNode();

        while(Lexer.wordKindIs(0,Type.AND)){
            add("<LAndExp>");
            add(Lexer.getNextSym());// &&
            EqExp newbranch = new EqExp();
            addAll(newbranch.analyze());
            astnode = new ASTNode(NodeType.AND,astnode,newbranch.getNode());
        }
        add("<LAndExp>");
        return sublist;
    }
}
