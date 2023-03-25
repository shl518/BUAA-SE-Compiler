/**
 * @description：
 * Cond → LOrExp
 * @author ：xxx
 * @date ：2022/9/24 9:58 PM
 */
package Parser.Exp;

import Parser.GrammarElement;

import java.util.ArrayList;
import Error.*;
public class Cond extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        LOrExp lOrExp = new LOrExp();
        addAll(lOrExp.analyze());
        astnode = lOrExp.getNode();
        add("<Cond>");
        return sublist;
    }
}
