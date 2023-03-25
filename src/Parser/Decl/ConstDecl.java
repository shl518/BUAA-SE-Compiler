/**
 * @description：ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
 * @author ：xxx
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

public class ConstDecl extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        add(Lexer.getNextSym()); //const
        add(Lexer.getNextSym()); //BType int
        astnode = new ASTNode(NodeType.ConstDecl);
        astnode.setKind(NodeKind.CONSTINT);

        ConstDef constDef = new ConstDef();
        addAll(constDef.analyze());
        astnode.addleaf(constDef.getNode());

        while (Lexer.symValueIs(",")){
            add(Lexer.getNextSym());//,
            ConstDef constDef1 = new ConstDef();
            addAll(constDef1.analyze());
            astnode.addleaf(constDef1.getNode());
        }
        match(";");
        add("<ConstDecl>");
        return sublist;
    }
}
