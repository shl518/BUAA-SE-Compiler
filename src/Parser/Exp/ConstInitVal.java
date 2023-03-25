/**
 * @description：
 * //常量初值 ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
 * @author ：szy
 * @date ：2022/9/24 5:36 PM
 */
package Parser.Exp;

import AST.ASTNode;
import AST.NodeType;
import Lexer.*;
import Parser.GrammarElement;

import java.util.ArrayList;
import Error.*;
public class ConstInitVal extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        if(Lexer.symValueIs("{")){
            astnode = new ASTNode(NodeType.ConstInitVal);
            add(Lexer.getNextSym());//{
            if(Lexer.symValueIs("}")){
                add(Lexer.getNextSym());//}
            }else{
                ConstInitVal constInitVal = new ConstInitVal();
                addAll(constInitVal.analyze());
                astnode.addleaf(constInitVal.getNode());

                while(Lexer.symValueIs(",")){
                    add(Lexer.getNextSym());//,
                    ConstInitVal constInitVal1 = new ConstInitVal();
                    addAll(constInitVal1.analyze());
                    astnode.addleaf(constInitVal1.getNode());
                }
                add(Lexer.getNextSym());//}
            }
        }else {
            ConstExp constExp = new ConstExp();
            addAll(constExp.analyze());
            astnode = constExp.getNode();
        }
        add("<ConstInitVal>");
        return sublist;
    }
}
