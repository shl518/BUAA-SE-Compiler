/**
 * @description：Decl → ConstDecl | VarDecl
 * @author ：xxx
 * @date ：2022/9/24 4:02 PM
 */
package Parser.Decl;
import Error.*;
import Lexer.Lexer;
import Parser.GrammarElement;

import java.util.ArrayList;

public class Decl extends GrammarElement {

    @Override
    public ArrayList<String> analyze(){
        if(Lexer.symValueIs("const")){
            ConstDecl constDecl = new ConstDecl();
            addAll(constDecl.analyze());
            astnode = constDecl.getNode();
        }else {
            VarDecl varDecl = new VarDecl();
            addAll(varDecl.analyze());
            astnode = varDecl.getNode();
        }
        return sublist;
    }
}
