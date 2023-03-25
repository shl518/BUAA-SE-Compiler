/**
 * @description： 变量声明 VarDecl → BType VarDef { ',' VarDef } ';'
 * @author ：szy
 * @date ：2022/9/24 4:17 PM
 */
package Parser.Decl;

import AST.ASTNode;
import AST.NodeKind;
import AST.NodeType;
import Lexer.Lexer;
import Parser.GrammarElement;
import Error.*;

import java.util.ArrayList;

public class VarDecl extends GrammarElement {
    @Override
    public ArrayList<String> analyze() {
        add(Lexer.getNextSym());//Btype int;
        astnode = new ASTNode(NodeType.VarDecl);
        astnode.setKind(NodeKind.INT);

        VarDef varDef = new VarDef();
        addAll(varDef.analyze());
        astnode.addleaf(varDef.getNode());

        while (Lexer.symValueIs(",")) {
            add(Lexer.getNextSym());// ,
            VarDef varDef1 = new VarDef();
            addAll(varDef1.analyze());
            astnode.addleaf(varDef1.getNode());
        }
        match(";");
        add("<VarDecl>");
        return sublist;
    }
}
