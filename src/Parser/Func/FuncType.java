/**
 * @description：
 * FuncType → 'void' | 'int'
 * @author ：xxx
 * @date ：2022/9/24 9:26 PM
 */
package Parser.Func;
import Error.*;
import Lexer.*;
import Parser.GrammarElement;

import java.util.ArrayList;

public class FuncType extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        add(Lexer.getNextSym());// void int
        add("<FuncType>");
        return sublist;
    }
}
