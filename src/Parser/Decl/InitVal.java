/**
 * @description
 * //变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
 * @author ：xxx
 * @date ：2022/9/24 9:14 PM
 */
package Parser.Decl;

import AST.ASTNode;
import AST.NodeType;
import Lexer.*;
import Parser.Exp.Exp;
import Parser.GrammarElement;

import java.util.ArrayList;
import Error.*;
public class InitVal extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        if(Lexer.symValueIs("{")){
            astnode = new ASTNode(NodeType.InitVal);
            add(Lexer.getNextSym()); // {
            if(Lexer.symValueIs("}")){
                add(Lexer.getNextSym());// }
            } else {
                InitVal initVal = new InitVal();
                addAll(initVal.analyze());
                astnode.addleaf(initVal.getNode());

                while (Lexer.symValueIs(",")){
                    add(Lexer.getNextSym());//,
                    InitVal initVal1 = new InitVal();
                    addAll(initVal1.analyze());
                    astnode.addleaf(initVal1.getNode());
                }
                add(Lexer.getNextSym());// }
            }
        } else {
            Exp exp = new Exp();
            addAll(exp.analyze());
            astnode = exp.getNode();
        }
        add("<InitVal>");
        return sublist;
    }
}
