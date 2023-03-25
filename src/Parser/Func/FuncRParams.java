/**
 * @description：
 * FuncRParams → Exp { ',' Exp }
 * @author ：xxx
 * @date ：2022/9/24 7:47 PM
 */
package Parser.Func;

import AST.ASTNode;
import AST.NodeType;
import Lexer.Lexer;
import Parser.Exp.Exp;
import Parser.Exp.LVal;
import Parser.GrammarElement;

import java.util.ArrayList;
import Error.*;
public class FuncRParams extends GrammarElement {
    private int paranum = 0;
    private ArrayList<Integer> addressDimensions; //记录每个Exp的地址维数

    public int getParanum() {
        return paranum;
    }

    public FuncRParams() {
        this.addressDimensions = new ArrayList<>();
    }

    public ArrayList<Integer> getAddressDimensions() {
        return addressDimensions;
    }

    @Override
    public ArrayList<String> analyze(){
        astnode = new ASTNode(NodeType.FuncRParams);

        paranum = 1;
        Exp e = new Exp();
        addAll(e.analyze());
        astnode.addleaf(e.getNode());//todo AST

        addressDimensions.add(e.getAdressDimention());

        while (Lexer.symValueIs(",")){
            add(Lexer.getNextSym());
            e = new Exp();
            addAll(e.analyze());
            astnode.addleaf(e.getNode());

            addressDimensions.add(e.getAdressDimention());
            paranum += 1;
        }
        add("<FuncRParams>");
        return sublist;
    }


}
