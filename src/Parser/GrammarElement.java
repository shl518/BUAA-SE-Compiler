/**
 * @description：
 * @author ：xxx
 * @date ：2022/9/24 3:46 PM
 */
package Parser;

import AST.ASTNode;
import AST.NodeType;
import Error.Error;
import Lexer.Lexer;
import Error.*;
import java.util.ArrayList;
import Lexer.Word;
public class GrammarElement {
    protected ArrayList<String> sublist;
    protected ASTNode astnode = null;

    public GrammarElement(){
        sublist = new ArrayList<>();
    }
    public ArrayList<String> analyze(){
        return null;
    }

    public ASTNode getNode() {
        return astnode;
    }

    public ASTNode getIdentNode(Word ident){
        String name = ident.getValue();
        return new ASTNode(NodeType.Ident,name);
    }

    public void add(String s){
        sublist.add(s);
    }
    public void addAll(ArrayList<String> ss){
        sublist.addAll(ss);
    }

    public void error(String str) {
        System.err.println("Error!"+" in "+str);
    }

    public boolean match(String s){
        if (!Lexer.symValueIs(s)) {
            switch (s) {
                case ";":
                    ErrorChecker.add(new Error(Lexer.getLastToken(),"i"));
                    break;
                case ")":
                    ErrorChecker.add(new Error(Lexer.getLastToken(),"j"));
                    break;
                case "]":
                    ErrorChecker.add(new Error(Lexer.getLastToken(),"k"));
                    break;
                default:
                    break;
            }
            return false;
        }
        add(Lexer.getNextSym());
        return true;
    }
}
