/**
 * @description：
 * PrimaryExp | Ident '(' [FuncRParams] ')'  | UnaryOp UnaryExp // 存在即可
 * @author ：xxx
 * @date ：2022/9/24 5:39 PM
 */
package Parser.Exp;

import AST.ASTNode;
import AST.NodeKind;
import AST.NodeType;
import Lexer.*;
import Parser.Func.FuncRParams;
import Parser.GrammarElement;
import Error.*;
import Parser.Parser;
import Table.FuncKind;
import Table.Symbol;
import Table.TableType;

import java.util.ArrayList;

public class UnaryExp extends GrammarElement {
    private int tmpExpAdressDimention;
    @Override
    public ArrayList<String> analyze(){
        if(Lexer.symValueIs("+")
                || Lexer.symValueIs("-") || Lexer.symValueIs("!")){
            String op  = Lexer.sym.getValue();
            UnaryOp unaryOp = new UnaryOp();
            addAll(unaryOp.analyze());
            UnaryExp unaryExp = new UnaryExp();
            addAll(unaryExp.analyze());
            astnode = new ASTNode(NodeType.OP,unaryExp.getNode());
            astnode.setOpstring(op);

        } else if (Lexer.wordKindIs(0, Type.IDENFR) && Lexer.wordKindIs(1,Type.LPARENT)) {

            //Ident '(' [FuncRParams] ')'
            Word funcident = Lexer.sym;
            ASTNode identnode = getIdentNode(funcident);//todo AST
            identnode.setKind(NodeKind.FUNC);

            Symbol findFuncByName = ErrorChecker.checkC(funcident, TableType.FUNC); // check未定义函数名
            if(findFuncByName != null){
                Parser.expAddressDimention = (findFuncByName.getFuncKind().equals(FuncKind.INT))? 0:-403;
            }

            add(Lexer.getNextSym());//IDENT
            add(Lexer.getNextSym());// (

            ASTNode params = null;
            int paranum = 0;
            ArrayList<Integer> dimensions = null;
            boolean startDEcheck = true;
            if(Lexer.symValueIs(")")){
                add(Lexer.getNextSym());// )
            }else{
                tmpExpAdressDimention = Parser.expAddressDimention;
                FuncRParams f = new FuncRParams();
                addAll(f.analyze());
                params = f.getNode();//todo AST
                paranum = f.getParanum();//实参个数

                dimensions = f.getAddressDimensions();//实参维度

                if(!match(")")){
                    startDEcheck = false;
                }
                Parser.expAddressDimention = tmpExpAdressDimention;
            }
            identnode.setLeft(params);
            astnode = identnode;
            //TODO 写e,想找到两个维度列表进行对比
            //System.out.println(startDEcheck);
            if(startDEcheck){

                if(findFuncByName != null){ //check d e
                    if(!ErrorChecker.checkD(funcident,findFuncByName,paranum) && paranum != 0){
                        ErrorChecker.checkE(funcident,findFuncByName,dimensions);
                    }
                }
            }
        } else {
            PrimaryExp primaryExp = new PrimaryExp();
            addAll(primaryExp.analyze());
            astnode = primaryExp.getNode();
        }
        add("<UnaryExp>");
        return sublist;
    }
}
