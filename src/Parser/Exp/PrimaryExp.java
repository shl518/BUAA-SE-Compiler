/**
 * @description：
 * PrimaryExp → '(' Exp ')' | LVal | Number
 * @author ：xxx
 * @date ：2022/9/24 7:18 PM
 */
package Parser.Exp;

import Lexer.*;
import Parser.GrammarElement;

import java.util.ArrayList;
import Error.*;

public class PrimaryExp extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        if(Lexer.symValueIs("(")){
            add(Lexer.getNextSym());// (
            Exp exp = new Exp();
            addAll(exp.analyze());//Exp
            astnode = exp.getNode();
            match(")");

        } else if (Lexer.wordKindIs(0,Type.IDENFR)) {
            LVal lVal = new LVal();
            addAll(lVal.analyze());
            astnode = lVal.getNode();

        } else if (Lexer.wordKindIs(0,Type.INTCON)) {
            Number number = new Number();
            addAll(number.analyze());
            astnode = number.getNode();
        }else {
            Word w = Lexer.sym;
            error("PrimaryExp");
        }
        add("<PrimaryExp>");
        return sublist;
    }
}
