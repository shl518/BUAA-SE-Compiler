/**
 * @description：
 * 语句块项 BlockItem → Decl | Stmt
 * @author ：xxx
 * @date ：2022/9/24 9:32 PM
 */
package Parser.Func;
import AST.ASTNode;
import AST.NodeType;
import Lexer.*;
import Parser.Decl.Decl;
import Parser.GrammarElement;

import java.util.ArrayList;

public class BlockItem extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        if(Lexer.symValueIs("const") || Lexer.symValueIs("int")){
            this.astnode = new ASTNode(NodeType.BlockItem_Decl);
            Decl decl = new Decl();
            addAll(decl.analyze());
            ASTNode declnode = decl.getNode();
            this.astnode.setLeft(declnode);
        }else{
            this.astnode = new ASTNode(NodeType.BlockItem_Stmt);
            Stmt stmt = new Stmt();
            addAll(stmt.analyze());
            ASTNode stmtnode = stmt.getNode();
            this.astnode.setLeft(stmtnode);
        }
        return sublist;
    }
}
