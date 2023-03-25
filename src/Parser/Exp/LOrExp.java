/**
 * @description：
 * //逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp
 * 消除左递归
 * //逻辑或表达式 LOrExp → LAndExp {'||' LAndExp}
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

public class LOrExp extends GrammarElement {
    @Override
    public ArrayList<String > analyze(){
        LAndExp lAndExp = new LAndExp();
        addAll(lAndExp.analyze());
        astnode = lAndExp.getNode();

        while(Lexer.wordKindIs(0,Type.OR)){
            add("<LOrExp>");
            add(Lexer.getNextSym());// ||
            LAndExp newbranch = new LAndExp();
            addAll(newbranch.analyze());
            astnode = new ASTNode(NodeType.OR,astnode,newbranch.getNode());
        }
        add("<LOrExp>");
        return sublist;
    }
}
