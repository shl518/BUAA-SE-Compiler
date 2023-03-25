/**
 * @description：// 常量表达式 ConstExp → AddExp  注：使用的Ident 必须是常量
 * @author ：xxx
 * @date ：2022/9/24 5:32 PM
 */
package Parser.Exp;
import Error.*;
import Parser.GrammarElement;

import java.util.ArrayList;

public class ConstExp extends GrammarElement {
    public static boolean inConstexp = false;
    @Override
    public ArrayList<String> analyze(){
        //ConstExp.inConstexp = true;
        AddExp addExp = new AddExp();
        addAll(addExp.analyze());
        astnode = addExp.getNode();
        //ConstExp.inConstexp = false;
        add("<ConstExp>");
        return sublist;
    }
}
