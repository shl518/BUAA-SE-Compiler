/**
 * @description：
 *  //函数形参表 FuncFParams → FuncFParam { ',' FuncFParam }
 * @author ：szy
 * @date ：2022/9/24 10:51 PM
 */
package Parser.Func;
import AST.ASTNode;
import AST.NodeType;
import Error.*;
import Lexer.*;
import Parser.GrammarElement;

import java.util.ArrayList;

public class FuncFParams extends GrammarElement {
    @Override
    public ArrayList<String > analyze(){
        this.astnode = new ASTNode(NodeType.FuncFParams);

        FuncFParam funcFParam = new FuncFParam();
        addAll(funcFParam.analyze());
        astnode.addleaf(funcFParam.getNode());//todo AST

        while(Lexer.symValueIs(",")){
            add(Lexer.getNextSym());//,
            FuncFParam funcFParam1 = new FuncFParam();
            addAll(funcFParam1.analyze());
            astnode.addleaf(funcFParam1.getNode());//todo AST
        }
        add("<FuncFParams>");
        return sublist;
    }
}
