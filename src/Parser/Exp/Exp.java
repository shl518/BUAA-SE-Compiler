/**
 * @description：
 * Exp → AddExp
 * @author ：xxx
 * @date ：2022/9/24 7:20 PM
 */
package Parser.Exp;

import Parser.GrammarElement;
import Parser.Parser;

import java.util.ArrayList;

public class Exp extends GrammarElement {
    private int adressDimention = 404;

    public int getAdressDimention() {
        return adressDimention;
    }

    @Override
    public ArrayList<String> analyze(){
        Parser.expAddressDimention = 0;
        AddExp addExp = new AddExp();
        addAll(addExp.analyze());
        astnode = addExp.getNode();
        add("<Exp>");
        this.adressDimention = Parser.expAddressDimention;
        return sublist;
    }
}
