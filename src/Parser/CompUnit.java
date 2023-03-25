/**
 * @description：<CompUnit> := { <Decl> } { <FuncDef> } <MainFuncDef>
 * @author ：szy
 * @date ：2022/9/24 1:02 PM
 */
package Parser;
import java.util.ArrayList;

import AST.ASTNode;
import AST.NodeType;
import Lexer.*;
import Parser.Decl.*;
import Parser.Func.*;
import Error.*;
public class CompUnit extends GrammarElement{

    private boolean global = true;
    @Override
    public ArrayList<String> analyze(){
        ErrorChecker.GlobalTable = Parser.table; // 记录全局变量，全局函数所在Table
        ASTNode decl = new ASTNode(NodeType.Decl);
        ASTNode func = new ASTNode(NodeType.Func);

        while (isDecl()) {
            Decl grm = new Decl();
            addAll(grm.analyze());
            decl.addleaf(grm.astnode);
        }
        while (isFunc()) {
            FuncDef grm = new FuncDef();
            addAll(grm.analyze());
            func.addleaf(grm.astnode);
        }


        global = false;


        MainFuncDef grm = new MainFuncDef();
        addAll(grm.analyze());
        ASTNode main = grm.astnode;
        this.astnode = new ASTNode(NodeType.CompUnit,decl,func,main);

        add("<CompUnit>");
        return sublist;
    }
    private boolean isDecl(){
        return (Lexer.wordKindIs(0, Type.CONSTTK)
                        && Lexer.wordKindIs(1, Type.INTTK) && Lexer.wordKindIs(2,Type.IDENFR)) ||
                (Lexer.wordKindIs(0, Type.INTTK)
                        && Lexer.wordKindIs(1, Type.IDENFR) && Lexer.wordKindIs(2,Type.LBRACK)) ||
                (Lexer.wordKindIs(0, Type.INTTK)
                        && Lexer.wordKindIs(1, Type.IDENFR) && Lexer.wordKindIs(2,Type.ASSIGN)) ||
                (Lexer.wordKindIs(0, Type.INTTK)
                        && Lexer.wordKindIs(1, Type.IDENFR) && Lexer.wordKindIs(2,Type.COMMA)) ||
                (Lexer.wordKindIs(0, Type.INTTK)
                        && Lexer.wordKindIs(1, Type.IDENFR) && Lexer.wordKindIs(2,Type.SEMICN))||
                (Lexer.wordKindIs(0, Type.INTTK)
                        && Lexer.wordKindIs(1, Type.IDENFR) && !Lexer.wordKindIs(2,Type.LPARENT)) ;

    }

    private boolean isFunc(){
        return (Lexer.wordKindIs(0,Type.VOIDTK)
                        && Lexer.wordKindIs(1,Type.IDENFR) && Lexer.wordKindIs(2,Type.LPARENT)) ||
                (Lexer.wordKindIs(0,Type.INTTK)
                        && Lexer.wordKindIs(1,Type.IDENFR) && Lexer.wordKindIs(2,Type.LPARENT));
    }
}
