/**
 * @description：
 * //语句块 Block → '{' { BlockItem } '}'
 * @author ：xxx
 * @date ：2022/9/24 9:30 PM
 */
package Parser.Func;
import AST.ASTNode;
import AST.NodeType;
import Lexer.*;
import Parser.GrammarElement;
import Parser.Parser;
import java.util.ArrayList;

public class Block extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        ASTNode blocknode = new ASTNode(NodeType.Block);
        add(Lexer.getNextSym());//{
        while(!Lexer.symValueIs("}")){
            Parser.lastIsReturn = false;
            BlockItem blockItem = new BlockItem();
            addAll(blockItem.analyze());
            blocknode.addleaf(blockItem.getNode());//todo AST
        }

        add(Lexer.getNextSym()); // }

        this.astnode = blocknode;
        add("<Block>");
        return sublist;
    }
}
