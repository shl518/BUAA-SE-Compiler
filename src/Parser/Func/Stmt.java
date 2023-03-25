/**
 * @description：
 * /*Stmt →
 *         | 'if' '( Cond ')' Stmt [ 'else' Stmt ]
 *         | 'while' '(' Cond ')' Stmt
 *         | 'break' ';' | 'continue' ';'
 *         | 'return' [Exp] ';'
 *         | 'printf''('FormatString{,Exp}')'';'
 *         | Block
 *         | LVal = 'getint''('')'';'
 *         | LVal '=' Exp ';'
 *         | [Exp] ';'
 *
 * @author ：xxx
 * @date ：2022/9/24 9:41 PM
 */
package Parser.Func;

import AST.ASTNode;
import AST.NodeType;
import Error.Error;
import Lexer.*;
import Parser.Exp.Cond;
import Parser.Exp.Exp;
import Parser.Exp.FormatString;
import Parser.Exp.LVal;
import Parser.GrammarElement;
import Parser.Parser;
import Table.BlockType;
import Error.*;
import java.util.ArrayList;

public class Stmt extends GrammarElement {
    @Override
    public ArrayList<String> analyze(){
        Word w =Lexer.sym;
        if(Lexer.symValueIs("if")){
            add(Lexer.getNextSym()); // if
            add(Lexer.getNextSym()); // (

            Cond cond = new Cond();
            addAll(cond.analyze());//cond
            ASTNode condnode = cond.getNode();
            match(")");

            Parser.createTable(BlockType.IF);
            Stmt stmt = new Stmt();
            addAll(stmt.analyze());// stmt
            ASTNode ifstmtnode =  stmt.getNode();

            Parser.outTable();
            ASTNode elsestmtnode = null;
            if(Lexer.wordKindIs(0,Type.ELSETK)){
                add(Lexer.getNextSym());// else

                Parser.createTable(BlockType.ELSE);
                Stmt stmt1 = new Stmt();
                addAll(stmt1.analyze());//stmt
                elsestmtnode = stmt1.getNode();
                Parser.outTable();

            }
            astnode = new ASTNode(NodeType.IfStatement,condnode,ifstmtnode,elsestmtnode);
        }else if(Lexer.symValueIs("while")){
            add(Lexer.getNextSym());//while

            add(Lexer.getNextSym());// (

            Cond cond = new Cond();
            addAll(cond.analyze()); //cond
            ASTNode whilecondnode = cond.getNode();

            match(")");

            Parser.createTable(BlockType.WHILE);
            Parser.intoWhile += 1;
            Stmt stmt = new Stmt();
            addAll(stmt.analyze());
            ASTNode whilestmtnode = stmt.getNode();
            Parser.intoWhile -= 1;
            Parser.outTable();
            astnode = new ASTNode(NodeType.While,whilecondnode,whilestmtnode);

        }else if(Lexer.symValueIs("break") || Lexer.symValueIs("continue")){
            if(Lexer.symValueIs("break")){
                astnode = new ASTNode(NodeType.Break);
            }else {
                astnode = new ASTNode(NodeType.Continue);
            }
            ErrorChecker.checkM(Lexer.sym);
            add(Lexer.getNextSym());//break continue
            match(";");
        }else if(Lexer.symValueIs("return")){
            Word returnSym = Lexer.sym;
            add(Lexer.getNextSym());//return
            Parser.blockhasReturn = false;
            Parser.lastIsReturn = false;

            ASTNode returnnode = null;
            if(Lexer.symValueIs(";")){

                add(Lexer.getNextSym());
            }else if(Lexer.symValueIs("}")){
                ErrorChecker.add(new Error(Lexer.getLastToken(),"i"));
            } else {
                Exp exp = new Exp();
                addAll(exp.analyze());//exp
                returnnode = exp.getNode();

                Parser.blockhasReturn = true;
                Parser.lastIsReturn = true;
                match(";");
            }

            ErrorChecker.checkF(returnSym);
            astnode = new ASTNode(NodeType.Return,returnnode);

        }else if(Lexer.symValueIs("printf")){
            Word printf = Lexer.sym;
            add(Lexer.getNextSym());//printf
            add(Lexer.getNextSym());// (
            String formatstringvalue = Lexer.sym.getValue();
            FormatString formatString = new FormatString();
            addAll(formatString.analyze());//formatstring
            ASTNode formatnode = formatString.getNode();
            ASTNode printexp = new ASTNode(NodeType.ExpList);

            int num = 0;//表达式语句的表达式个数
            while(Lexer.symValueIs(",")){
                add(Lexer.getNextSym());//,
                Exp exp = new Exp();
                addAll(exp.analyze());//Exp
                printexp.addleaf(exp.getNode());
                num += 1;
            }
            match(")");
            match(";");

            ErrorChecker.checkL(printf,formatstringvalue,num);
            astnode = new ASTNode(NodeType.Printf,formatnode,printexp);

        }else if(Lexer.symValueIs("{")){ //block

            Parser.createTable(BlockType.BLOCK);
            Block  block = new Block();
            addAll(block.analyze());
            astnode = new ASTNode(NodeType.Block,block.getNode());//todo question 为啥要这样？block套block

            Parser.lastIsReturn = false;
            Parser.outTable();

        }else if(Lexer.wordKindIs(0,Type.IDENFR)
                && Lexer.hasassign()){ //LVal 开头
            Word lvalIdent = Lexer.sym;

            LVal lVal = new LVal();
            addAll(lVal.analyze());//Lval
            ASTNode lvalnode = lVal.getNode();

            //todo test
//            boolean flag = false;
//            if(Lexer.sym.getValue().equals("+")){
//                flag = true;
//                add(Lexer.getNextSym());// +
//            }
            //todo test
            add(Lexer.getNextSym());// =
            if(Lexer.symValueIs("getint")){
                add(Lexer.getNextSym());// getint
                add(Lexer.getNextSym());// (
                match(")");
                match(";");
                ASTNode getintnode = new ASTNode(NodeType.Getint);
                astnode = new ASTNode(NodeType.Assign_getint,lvalnode,getintnode);
            }else{
                Exp exp = new Exp();
                addAll(exp.analyze());//Exp
                astnode = new ASTNode(NodeType.Assign_value,lvalnode,exp.getNode());
                match(";");
//                if (flag){
//                    ASTNode newexpnode = new ASTNode(NodeType.OP,lvalnode,exp.getNode());
//                    newexpnode.setOpstring("+");
//                    astnode = new ASTNode(NodeType.Assign_value,lvalnode,newexpnode);
//                    match(";");
//                }else {
//                    astnode = new ASTNode(NodeType.Assign_value,lvalnode,exp.getNode());
//                    match(";");
//                }
            }

            ErrorChecker.checkH(lvalIdent);
        }else {//[Exp]
//            if(Lexer.wordKindIs(0,Type.IDENFR) && Lexer.hasaddadd()){
//                LVal lVal = new LVal();
//                addAll(lVal.analyze());
//                ASTNode lvalnode = lVal.getNode();
//                add(Lexer.getNextSym());//+
//                add(Lexer.getNextSym());//+
//                ASTNode numnode = new ASTNode(NodeType.Number,1);
//                ASTNode expnode = new ASTNode(NodeType.OP,lvalnode,numnode);
//                expnode.setOpstring("+");
//                astnode = new ASTNode(NodeType.Assign_value,lvalnode,expnode);
//                match(";");
//            }else {
                ASTNode expnode = null;
                if(Lexer.symValueIs(";")){
                    add(Lexer.getNextSym());// ;
                }else {
                    Exp exp = new Exp();
                    addAll(exp.analyze());//Exp
                    expnode = exp.getNode();
                    match(";");
                }
                astnode = new ASTNode(NodeType.Exp,expnode);
            //}

        }
        add("<Stmt>");
        return sublist;
    }
}
