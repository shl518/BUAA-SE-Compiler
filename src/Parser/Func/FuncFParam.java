/**
 * @description：
 * //函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
 * @author ：szy
 * @date ：2022/9/24 10:51 PM
 * todo d1,d2
 */
package Parser.Func;

import AST.ASTNode;
import AST.NodeKind;
import Lexer.*;
import Parser.Exp.ConstExp;
import Parser.GrammarElement;

import java.util.ArrayList;
import Error.*;
import Parser.Parser;
import Table.ConstType;
import Table.Symbol;
import Table.TableType;

public class FuncFParam extends GrammarElement {

    @Override
    public ArrayList<String > analyze(){
        add(Lexer.getNextSym());//Btype int

        Word ident = Lexer.sym;
        ASTNode identnode = getIdentNode(ident);//todo AST
        identnode.setKind(NodeKind.INT);

        add(Lexer.getNextSym());//IDENT


        boolean isArray = false;
        int dimen = 0;

        ASTNode exp = null;//[]里面可能有constexp求值
        if(Lexer.symValueIs("[")){
            isArray =true;
            add(Lexer.getNextSym());//[
            match("]");
            dimen += 1;

            identnode.setKind(NodeKind.ARRAY);
            identnode.setNum(dimen);

            while (Lexer.symValueIs("[")){ //应该就一层，这块其实可以用if
                add(Lexer.getNextSym());//[

                ConstExp constExp = new ConstExp();
                addAll(constExp.analyze());
                identnode.setRight(constExp.getNode());//todo AST 放右边，因为只有第二个【】里面才有exp
                match("]");
                dimen+=1;
                identnode.setNum(dimen);
            }
        }

        Parser.addIntergerOrArray(ident,isArray,dimen,0,0);//todo d1 d2
        this.astnode = identnode;
        add("<FuncFParam>");
        return sublist;
    }
}
