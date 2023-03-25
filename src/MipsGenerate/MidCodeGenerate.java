/**
 * @description：TODO
 * @author ：szy
 * @date ：2022/10/23 9:32 AM
 */
package MipsGenerate;

import AST.ASTNode;
import AST.NodeKind;
import AST.NodeType;
import CONST.MyError;
import CONST.OperDiction;
import Table.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MidCodeGenerate {
    private ASTNode tree;
    private ArrayList<MidCode> midCodes = new ArrayList<>();
    private int idrecord = 0;
    private int varcount = 0;//生成tmp变量

    private boolean isglobal = true;
    public static Table headerTable = new Table(null, BlockType.GLOBAL);
    public static Table foreverGlobalTAble = headerTable;
    private Symbol curFunc;//当前调用的函数
    private boolean noneedblock = false;

    private int ifcount = 0;
    private int whilecount = 1;
    private int orcnt = 0;


    public MidCodeGenerate(ASTNode tree) {
        this.tree = tree;
    }

    public ArrayList<MidCode> generate() {
        parseTree();
        return midCodes;
    }

    public static void insertTable(Symbol symbol) {
        headerTable.addSymbol(symbol);
        symbol.setTable(headerTable);
    }

    public static void openTable(BlockType blocktype) {
        Table newTable = new Table(headerTable, blocktype);
        headerTable.addson(newTable);
        headerTable = newTable;
    }

    public static void closeTable() {
        setScopeForEachSymbol();
        headerTable = headerTable.getFather();
    }

    private static void setScopeForEachSymbol() {
        for (Symbol symbol : headerTable.getSymbols()) {
            symbol.setTable(headerTable);
        }
    }

    public void writefile() throws IOException {
        File file = new File("midcode.txt");
        FileWriter writer = new FileWriter(file);
        System.out.println("输出ircode.txt...");
        for (MidCode irc : midCodes) {
            String t = irc.getRawstr();
            System.out.println(irc.getId() + " " + t);
            //writer.write(irc.getId() + " " + t + " ::Kind is: " + irc.getType() + "\n");
            writer.write(t + "\n");
        }
        writer.flush();
        writer.close();
    }

    public void parseTree() {
        createMidCode(MidType.Note, "#Start Decl");
        if (tree.getLeft() != null) {
            for (ASTNode leaf : tree.getLeft().getLeafs()) {
                parseDecl(leaf);
            }
        }
        isglobal = false;
        createMidCode(MidType.Note, "#Start FuncDecl");
        if (tree.getMiddle() != null) {
            parseFuncDeclList(tree.getMiddle());
        }
        createMidCode(MidType.Note, "#Start MainFunc");

        //main
        MidCodeGenerate.openTable(BlockType.MAIN);
        parseBlock(tree.getRight(), -404);
        MidCodeGenerate.closeTable();
    }


    public void parseDecl(ASTNode n) { // constDef,VarDef
        for (ASTNode leaf : n.getLeafs()) {
            parseDef(leaf);
        }
    }

    /**
     * 数组声明，const int ,int 声明
     */
    public void parseDef(ASTNode n) {
        ASTNode ident = n.getLeft();
        String name = ident.getName();
        NodeKind kind = ident.getKind();

        //符号表
        ASTNode init = n.getRight();
        Symbol symbol = insertTosymbolTable(ident, init, false, false);


        //数组
        if (kind.equals(NodeKind.CONSTARRAY) || kind.equals(NodeKind.ARRAY)) {
            parseArrayDef(n);
        } else {
            //symbol.setIrindex(midCodes.size());//todo 干啥的
            //const int,int
            if (init != null) {
                if (kind.equals(NodeKind.CONSTINT) || isglobal) { //isglobal 也直接计算初值，因为这块不可能有函数
                    int constinitnum = symbol.getNum();
                    MidCode ir = new MidCode(MidType.intDecl, kind, name);
                    ir.setNum(constinitnum); //初值
                    ir.setInitIsTrue();
                    ir.setSymbol(symbol);
                    ir4init(ir);
                } else { //如果是var int ，需要计算表达式，采取一个生成两条的策略
                    //第一条声明
                    MidCode numInitir = new MidCode(MidType.intDecl, kind, name);
                    numInitir.setSymbol(symbol);
                    ir4init(numInitir);

                    //第二条赋值
                    //startindex = midCodes.size(); //初始化复制语句开始位置 todo 干啥的

                    Var intinitVar = parseExp(init);
                    Var intinitLval = new Var("var", name);

                    intinitLval.setSymbol(symbol);
                    intinitLval.setiskindofsymbolTrue();

                    //checkIfAssignGlobalValue(intinitVar);

                    MidCode ir = new MidCode(MidType.assign2, intinitLval, intinitVar);
                    ir4init(ir);

                }
            } else { //没有初值，可以直接构建
                MidCode ir = new MidCode(MidType.intDecl, kind, name);
                ir.setSymbol(symbol);
                ir4init(ir);
            }
        }
    }

    public void parseArrayDef(ASTNode n) {

        ASTNode ident = n.getLeft();
        String name = ident.getName();
        Symbol symbol = lookallTableFindsamename(name);
        assert symbol != null;
        NodeKind kind = ident.getKind();

        //数组identnode 左右节点分别为dimen1，dimen2,
        //在写语法分析的symbol里面当时并没有设置这两个值，需要在这里算一下两个维度
        ASTNode dimen1Node = ident.getLeft();
        ASTNode dimen2Node = ident.getRight();
        int dimen1num = dimen1Node.calcuValue();
        int dimen2num = 0;
        if (dimen2Node != null) {
            dimen2num = dimen2Node.calcuValue();
        }

        MidCode midCode = new MidCode(MidType.arrayDecl, name, dimen1num, dimen2num);
        midCode.setSymbol(symbol);

        //开始设置初值，如果是const array 或者global我们直接存储到中间代码arraylist里面
        //如果是int类型数组，我们需要生成多条assign2语句，因为这里可能不能直接得到初值，需要运行时计算
        ASTNode init = n.getRight();

        //没初值，直接打包返回
        if (init == null) {
            ir4init(midCode);
            return;
        }

        midCode.setInitIsTrue();
        if (symbol.isConst() || symbol.isGlobal()) {
            //这里直接把初值存到midCode，方便生成

            parseArrayInitNums(midCode, symbol, name, init, dimen1num, dimen2num, true);
            ir4init(midCode);
        } else {
            ir4init(midCode);
            parseArrayInitNums(midCode, symbol, name, init, dimen1num, dimen2num, false);
        }
    }

    /**
     * 用于生成数组的初始值，对于const，global array将初值存到midcode里面，而
     * 对于int 局部 array 将生成多条assign2语句来赋值
     */
    public void parseArrayInitNums(MidCode midCode, Symbol arraysymbol,
                                   String arrayidentname, ASTNode init, int dimen1num, int dimen2num, boolean isConstOrGlobal) {
        ArrayList<Integer> extract = new ArrayList<>();
        //const 和 global 直接存储初值到midcode
        if (isConstOrGlobal) {
            if (dimen2num == 0) {   //一维数组

                for (int i = 0; i < dimen1num; i++) {
                    int initnum = init.getLeafs().get(i).calcuValue();
                    extract.add(initnum);
                }

                midCode.addAllInitList(extract);
                return;
            } else {
                for (int i = 0; i < dimen1num; i++) {
                    for (int j = 0; j < dimen2num; j++) {
                        System.out.println(arraysymbol.getName());
                        int initnum = init.getLeafs().get(i).getLeafs().get(j).calcuValue();
                        extract.add(initnum);
                    }
                }
                midCode.addAllInitList(extract);
                return;
            }
        }

        //var array 生成多条语句
        int size = 4444;
        if (dimen2num == 0) {
            size = dimen1num;
        } else {
            size = dimen1num * dimen2num;
        }
        for (int i = 0; i < size; i++) {
            //下标
            Var index = new Var("num", i);
            //右值
            Var arrInitvar = null;
            if (dimen2num == 0) {
                arrInitvar = parseExp(init.getLeafs().get(i));
            } else {
                arrInitvar = parseExp(init.getLeafs().get(i / dimen2num).getLeafs().get(i - (i / dimen2num) * dimen2num));
            }

            //左值array类型
            Var arrInitLval = new Var("array", arrayidentname, index);

            arrInitLval.setiskindofsymbolTrue();
            arrInitLval.setSymbol(arraysymbol);
            createMidCode(MidType.assign2, arrInitLval, arrInitvar);
        }
    }

    public void parseFuncDeclList(ASTNode n) {
        for (ASTNode leaf : n.getLeafs()) {
            parseFuncDecl(leaf);
        }
    }

    public void parseFuncDecl(ASTNode n) {
        ASTNode identnode = n.getLeft();
        String funcname = identnode.getName();
        NodeKind functype = identnode.getKind();


        //填符号表
        curFunc = insertTosymbolTable(identnode, null, true, false);


        MidCodeGenerate.openTable(BlockType.FUNC);
        createMidCode(MidType.funcDecl, functype, funcname);

        ASTNode params = identnode.getLeft();
        if (params != null) {
            for (ASTNode leaf : params.getLeafs()) {
                parseFuncFParam(leaf);
            }
        }

        parseBlock(n.getRight(), -404);
        createMidCode(MidType.Note, "#end a func");
        MidCodeGenerate.closeTable();
        curFunc = null;
    }

    private void parseFuncFParam(ASTNode n) {//这里都是identnode
        String name = n.getName();
        Symbol symbol = insertTosymbolTable(n, null, false, true);
        curFunc.addParam(symbol);

        //todo 这里好像可以集到concatraw那里，不管了先理解吧,这里确实可以
        StringBuilder sb = new StringBuilder();
        sb.append("para int ");
        if (n.getKind().equals(NodeKind.INT) || n.getKind().equals(NodeKind.CONSTINT)) {
            sb.append(name);

        } else if (n.getKind().equals(NodeKind.ARRAY) || n.getKind().equals(NodeKind.CONSTARRAY)) {
            int dimen = n.getNum();
            if (dimen == 1) {
                sb.append(name).append("[]");
            } else if (dimen == 2) {
                sb.append(name).append("[][]");
            }
        } else {
            MyError.errorat("Mipsgenrater", 300);
        }
        //
        createMidCode(MidType.funcPara, sb.toString());
    }


    /**
     * @param localwhilecount 为了生成循环或者子块的跳转标签用
     */
    public void parseBlock(ASTNode n, int localwhilecount) {
        for (ASTNode leaf : n.getLeafs()) {
            parseBlockItem(leaf, localwhilecount);
        }
    }

    public void parseBlockItem(ASTNode n, int localwhilecount) {
        if (n.getType().equals(NodeType.BlockItem_Decl)) {
            parseDecl(n.getLeft());
        } else if (n.getType().equals(NodeType.BlockItem_Stmt)) {
            parseStmt(n.getLeft(), localwhilecount);
        } else {
            MyError.errorat("Midcodegen", 349);
        }
    }

    public void parseStmt(ASTNode n, int localwhilecount) {
        NodeType type = n.getType();
        if (type.equals(NodeType.IfStatement)) {
            parseIfStatement(n, localwhilecount);
        } else if (type.equals(NodeType.While)) {
            parseWhileLoop(n);
        } else if (type.equals(NodeType.Return)) {
            if (n.getLeft() != null) {
                Var t = parseExp(n.getLeft());

                MidCode ir = new MidCode(MidType.Return, t);
                ir.setVoidreturn(false);
                ir4init(ir);

            } else {
                MidCode ir = new MidCode(MidType.Return, "void return");
                ir.setVoidreturn(true);
                ir4init(ir);
            }
        } else if (type.equals(NodeType.Continue)) {

            createMidCode(MidType.Note, "#Out Block WhileCut");
            createMidCode(MidType.jump, "begin_loop" + localwhilecount);
        } else if (type.equals(NodeType.Break)) {

            createMidCode(MidType.Note, "#Out Block WhileCut");
            createMidCode(MidType.jump, "end_loop" + localwhilecount);

        } else if (type.equals(NodeType.Printf)) {
            parsePrintf(n);
        } else if (type.equals(NodeType.Block)) {
            boolean localneedblk = true;
            //noneedblock能抵消一次block，抵消以后不用开符号表
            if (noneedblock) {
                localneedblk = false;
                noneedblock = false;
            }
            if (localneedblk) {
                MidCodeGenerate.openTable(BlockType.BLOCK);
            }

            parseBlock(n.getLeft(), localwhilecount);

            if (localneedblk) {
                createMidCode(MidType.Note, "#Out Block");
                MidCodeGenerate.closeTable();
            }
        } else if (type.equals(NodeType.Exp)) {
            if (n.getLeft() != null) {
                parseExp(n.getLeft());
            }
        } else if (type.equals(NodeType.Assign_getint)) {
            Var getintexp = parseLVal(n.getLeft());    //需要LVal而不是Exp处理，因为可能得sw
            assert getintexp != null;
            //checkIfAssignGlobalValue(getintexp);//todo  这个函数好像只跟无用函数有关，是不是可以考虑去掉，无用函数也不能优化性能感觉
            createMidCode(MidType.Getint, getintexp);

        } else if (type.equals(NodeType.Assign_value)) {
            //todo startindex = midCodes.size();     //初始化ir赋值语句起始位置
            Var lval = parseLVal(n.getLeft());
            Var exp = parseExp(n.getRight());

            assert lval != null;
            //checkIfAssignGlobalValue(lval);      //检查是否赋值global

            createMidCode(MidType.assign2, lval, exp);
        } else {
            MyError.errorat("MIpsgenerator", 431);
        }
    }

    /**
     * begin_loop1:
     * cond
     * cond
     * intostmt_loop1:
     * stmt
     * jump begin_loop1
     * end_loop1
     */
    public void parseWhileLoop(ASTNode n) {
        //将whilecount本地化，防止while嵌套，生成break，continue的时候没法找到原来的标签
        int localwhilecnt = whilecount;


        String beginloop_label = "begin_loop" + whilecount;
        String endloop_label = "end_loop" + whilecount;
        String intoloop_label = "intostmt_loop" + whilecount;
        createMidCode(MidType.Label, beginloop_label);

        whilecount += 1;

        parseCond(n.getLeft(), intoloop_label, endloop_label);

        //进入while基本块
        MidCodeGenerate.openTable(BlockType.WHILE);
        noneedblock = true;
        createMidCode(MidType.Label, intoloop_label);

        parseStmt(n.getRight(), localwhilecnt);

        createMidCode(MidType.Note, "#Out Block");
        createMidCode(MidType.jump, beginloop_label);
        MidCodeGenerate.closeTable();
        noneedblock = false;
        createMidCode(MidType.Label, endloop_label);

    }

    public void parseIfStatement(ASTNode n, int localwhilecount) {
        ifcount += 1;

        String end_ifLabel = "end_if" + ifcount;
        String end_elseLabel = "end_else" + ifcount;
        String intoblocklabel = "into_if" + ifcount;      //主要用于短路求值直接跳入

        parseCond(n.getLeft(), intoblocklabel, end_ifLabel);

        //进入If基本块范围，打标签，建立表
        createMidCode(MidType.Label, intoblocklabel);
        MidCodeGenerate.openTable(BlockType.IF);
        noneedblock = true;

        //if stmt
        parseStmt(n.getMiddle(), localwhilecount);
        createMidCode(MidType.Note, "#Out Block");

        //else stmt
        if (!n.haselse()) { //如果没有else
            MidCodeGenerate.closeTable();
            noneedblock = false;
            createMidCode(MidType.Label, end_ifLabel);
        } else {
            createMidCode(MidType.jump, end_elseLabel);
            MidCodeGenerate.closeTable();
            noneedblock = false;
            createMidCode(MidType.Label, end_ifLabel);

            //开始解析else
            noneedblock = true;
            MidCodeGenerate.openTable(BlockType.ELSE);
            parseStmt(n.getRight(), localwhilecount);
            createMidCode(MidType.Note, "#Out Block");
            MidCodeGenerate.closeTable();
            noneedblock = false;

            createMidCode(MidType.Label, end_elseLabel);
        }

    }

    public void parseCond(ASTNode n, String jinlabel, String joutlabel) {

        NodeType type = n.getType();
        String opstring = n.getOpstring();

        // > < >= <=
        if (OperDiction.isnumcmp(opstring)) {

            Var leftexp = parseRelExp(n.getLeft());
            Var rightexp = parseRelExp(n.getRight());

            if (opstring.equals(">=")) {
                createMidCode(MidType.branch, "blt", joutlabel, leftexp, rightexp);
            } else if (opstring.equals("<=")) {
                createMidCode(MidType.branch, "bgt", joutlabel, leftexp, rightexp);
            } else if (opstring.equals(">")) {
                createMidCode(MidType.branch, "ble", joutlabel, leftexp, rightexp);
            } else {
                createMidCode(MidType.branch, "bge", joutlabel, leftexp, rightexp);
            }
        } else if (OperDiction.iseqcmp(opstring)) {
            Var lefteq = parseEqExp(n.getLeft());
            Var righteq = parseEqExp(n.getRight());
            createMidCode(MidType.branch, opstring.equals("!=") ? "beq" : "bne", joutlabel, lefteq, righteq);
        } else if (type.equals(NodeType.OR) || type.equals(NodeType.AND)) {
            //重点在于短路求值怎么处理
            if (type.equals(NodeType.AND)) {
                parseCond(n.getLeft(), jinlabel, joutlabel);
            } else {
                String orLabel = "orLabel_" + orcnt;
                orcnt += 1;

                parseCond(n.getLeft(), jinlabel, orLabel);
                createMidCode(MidType.jump, jinlabel);

                createMidCode(MidType.Label, orLabel);
            }
            parseCond(n.getRight(), jinlabel, joutlabel);
        } else {
            if (opstring.equals("!")) {
                Var notexp = parseExp(n.getLeft());
                createMidCode(MidType.branch, "bne", joutlabel, notexp, new Var("num", 0));
            } else {
                Var exp = parseExp(n);
                createMidCode(MidType.branch, "beq", joutlabel, exp, new Var("num", 0));
            }
        }

    }

    public Var parseEqExp(ASTNode n) {
        String op = n.getOpstring();
        if (OperDiction.iseqcmp(op)) {
            Var lefteq = parseEqExp(n.getLeft());
            Var righteq = parseEqExp(n.getRight());
            Var tmp = getTmpVar();

            createMidCode(MidType.setcmp, op.equals("==") ? "seq" : "sne", tmp, lefteq, righteq);
            return tmp;
        }
        return parseRelExp(n);
    }

    public Var parseRelExp(ASTNode n) {
        String op = n.getOpstring();
        if (OperDiction.isnumcmp(op)) {
            Var leftrel = parseRelExp(n.getLeft());
            Var rightrel = parseRelExp(n.getRight());
            Var tmp = getTmpVar();
            String setcmp = "";
            if (op.equals(">=")) {
                setcmp = "sge";
            } else if (op.equals("<=")) {
                setcmp = "sle";
            } else if (op.equals(">")) {
                setcmp = "sgt";
            } else if (op.equals("<")) {
                setcmp = "slt";
            }
            createMidCode(MidType.setcmp, setcmp, tmp, leftrel, rightrel);
            return tmp;
        } else {
            return parseExp(n);
        }
    }

    private void parsePrintf(ASTNode n) {
        String formatString = n.getLeft().getName();
        formatString = formatString.substring(1, formatString.length() - 1);


        createMidCode(MidType.Note, "#Start Print");

        if (n.getRight() != null) {
            String[] splits = formatString.split("%d", -1);
            ASTNode explist = n.getRight();
            int i = 0;
            while (i < splits.length) {
                String splitstr = splits[i];
                if (!splitstr.equals("")) {
                    Var var_splitstr = new Var("str", splitstr);
                    createMidCode(MidType.Printf, var_splitstr);
                }
                //防止越界
                if (explist.getLeafs() == null || i > explist.getLeafs().size() - 1) {
                    break;
                }

                ASTNode oneexp = explist.getLeafs().get(i);
                Var printexp = parseExp(oneexp);
                createMidCode(MidType.Printf, printexp);
                i += 1;
            }
        } else {
            Var var_formatString = new Var("str", formatString);
            createMidCode(MidType.Printf, var_formatString);
        }

        //todo 11.26 处理了printf函数先执行返回后再一起执行printf，解决printf的顺序问题
        //todo 主要采取移动printf顺序的方法，函数执行放前面，所有printf按照顺序放在最后面
        int record_start = -1;
        for (int i = midCodes.size() - 1; i >= 0; i--) {
            if(midCodes.get(i).getIRstring() == null){
                continue;
            }
            if (midCodes.get(i).getIRstring().equals("#Start Print")) {
                record_start = i;
                break;
            }
        }
        if (record_start != -1) {
            //先去掉，然后重新排序,把函数调用相关的放在前面，最后全部放printf
            int record_end = midCodes.size() - 1;
            ArrayList<MidCode> tmplist = new ArrayList<>();
            //正序倒出来
            for (int i = record_start; i <= record_end; i++) {
                tmplist.add(midCodes.get(i));
            }
            //原来的删掉
            if (record_end >= record_start) {
                midCodes.subList(record_start, record_end + 1).clear();
            }
            //开始往里面加
            ArrayList<Integer> printfid = new ArrayList<>();
            for(int i =0 ;i < tmplist.size();i++){
                MidCode mid = tmplist.get(i);
                if(!mid.getType().equals(MidType.Printf)){
                    midCodes.add(mid);
                }else {
                    printfid.add(i);
                }
            }
            for (Integer id : printfid) {
                midCodes.add(tmplist.get(id));
            }
            createMidCode(MidType.Note, "#End Print");
        } else {
            MyError.errorat("Midcodegen", 621, "record_start = -1不可能");
        }
    }


    private Var parseExp(ASTNode n) {
        NodeType type = n.getType();
        String opstring = n.getOpstring();
        if (type.equals(NodeType.Ident)) {
            //todo 包括函数调用func；整数int；数组array

            Var ident = parseIdent(n);

            if (!n.getKind().equals(NodeKind.FUNC)) {    //todo 访问非func类的 int和array 这里干啥，不知道
                Symbol symbol = MidCodeGenerate.lookallTableFindsamename(n.getName());
                assert ident != null;
                ident.setSymbol(symbol);
            }

            return ident;

        } else if (type.equals(NodeType.Number)) { //如果是数字，不生成，直接返回
            return new Var("num", n.getNum());
        } else if (OperDiction.hasOperator(opstring)) {
            if (opstring.equals("+")) {
                if (n.getRight() != null) {
                    return generateOperatorIR(n, "+");
                }
                return parseExp(n.getLeft());
            } else if (opstring.equals("-")) {
                if (n.getRight() != null) {
                    return generateOperatorIR(n, "-");
                }

                Var leftexp = new Var("num", 0);
                Var rightexp = parseExp(n.getLeft());

                if (rightexp.getType().equals("num")) {
                    return new Var("num", -rightexp.getNum());

                } else {
                    Var tmpvar = getTmpVar();

                    createMidCode(MidType.assign, "-", tmpvar, leftexp, rightexp);
                    return tmpvar;
                }
            } else if (opstring.equals("*")) {
                return generateOperatorIR(n, "*");
            } else if (opstring.equals("/")) {
                return generateOperatorIR(n, "/");
            } else if (opstring.equals("%")) {
                return generateOperatorIR(n, "%");
            } else if(opstring.equals("bitand")){
                return generateOperatorIR(n, "bitand");
            }
            else {
                MyError.errorat("MidcodeGen", 395);
                return null;
            }

        } else if (opstring.equals("!")) {
            Var nexp = parseExp(n.getLeft());
            Var tmpvar = getTmpVar();
            createMidCode(MidType.setcmp, "seq", tmpvar, nexp, new Var("num", 0));    //seq tmpvar等于0置1
            return tmpvar;

        } else {
            MyError.errorat("MidCodeGen", 406);
            return null;
        }
    }


    private Var parseLVal(ASTNode n) {
        NodeKind kind = n.getKind();

        if (kind.equals(NodeKind.INT) || kind.equals(NodeKind.CONSTINT)) {
            Var lval = new Var("var", n.getName());

            Symbol symbol = MidCodeGenerate.lookallTableFindsamename(n.getName());

            lval.setSymbol(symbol);
            lval.setiskindofsymbolTrue();

            return lval;

        } else if (kind.equals(NodeKind.ARRAY) || kind.equals(NodeKind.CONSTARRAY)) {
            return parseArrayVisit(n);

        } else {
            MyError.errorat("MidCodeGen", 430, "Lval不知是什么类型");
            return null;
        }
    }

    private Var parseIdent(ASTNode n) {
        //MyError.sout("In parseIdent " + n.getName() + " " + n.getKind());
        NodeKind kind = n.getKind();

        if (kind.equals(NodeKind.FUNC)) {   //函数调用！！！！！！！
            String funcname = n.getName();
            ASTNode rparams = n.getLeft();


            Symbol func = MidCodeGenerate.lookGlobalTableFindsamename(funcname);


            if (rparams != null) {      //函数有参数则push
                for (int i = 0; i < rparams.getLeafs().size(); i++) {
                    ASTNode para = rparams.getLeafs().get(i);

                    Symbol fparami = func.getParams().get(i); //函数的第i个参数类型
                    int arraydimen = fparami.getDimension();

                    if (fparami.isArray()) {       //如果是array类型的函数参数,传参数的时候用
                        Var paraexp = parseParaArray(para, arraydimen);       //需返回array类型
                        createMidCode(MidType.Push, paraexp);

                    } else {
                        Var paraexp = parseExp(para);      //正常的var类型exp
                        createMidCode(MidType.Push, paraexp);
                    }
                }
            }

            //call
            MidCode ir = new MidCode(MidType.call, funcname);
            ir.setSymbol(func);
            ir4init(ir);

            //ret
            if (func.getFuncKind() != null && !func.getFuncKind().equals(FuncKind.VOID)) {
                Var tmpvar = getTmpVar();
                createMidCode(MidType.assign_ret, tmpvar);
                return tmpvar;
            }
            return null;


        } else if (kind.equals(NodeKind.INT) || kind.equals(NodeKind.CONSTINT)) {
            if (kind.equals(NodeKind.CONSTINT)) {     //优化
                String name = n.getName();
                Symbol symbol = MidCodeGenerate.lookallTableFindsamename(name);
                assert symbol != null;
                int num = symbol.getNum();
                return new Var("num", num);

            }

            Var intvar = new Var("var", n.getName());
            intvar.setiskindofsymbolTrue();//设置成是符号表里面的东西
            return intvar;


        } else if (kind.equals(NodeKind.CONSTARRAY)) {
            //return parseArrayVisit(n);
            Var arrnnum = parseArrayVisit(n);
            Var tmp = getTmpVar();
            createMidCode(MidType.assign2, tmp, arrnnum);
            return tmp;
        } else if (kind.equals(NodeKind.ARRAY)) {
            //这里必须分两类
            //我们要把临时数组的访问最终聚合为临时变量

            Var arrnnum = parseArrayVisit(n);
            Var tmp = getTmpVar();
            createMidCode(MidType.assign2, tmp, arrnnum);
            return tmp;
        }
        MyError.errorat("Midgen", 801, "不知道ident是什么类型");
        return null;
    }

    /**
     * 这里数组访问对每个array var设置了symbol为其所属的数组symbol，但是没有设置issymbol
     */
    public Var parseArrayVisit(ASTNode n) {
        String arrname = n.getName();
        Symbol array = lookallTableFindsamename(arrname);
        assert array != null;
        if (array.getDimension() == 1) {
            //todo 如果下标是num，也直接取，11.21添加 这里缺了一个优化

            Var index1_var = parseExp(n.getLeft()); //dimen1 var
            //todo Bug B19
            if (array.getConstType().equals(ConstType.CONST)
                    && index1_var.getType().equals("num")) {
                //const 而且下标为num 直接返回值
                int offsetnum = index1_var.getNum();
                int arrayvalue = array.getArrayValue().get(offsetnum);
                return new Var("num", arrayvalue);
            } else {
                Var arrVar = new Var("array", arrname, index1_var);
                arrVar.setSymbol(array); //只设置symbol，没有设置kindofSymbol=True
                return arrVar;
            }

        } else {
            int dimen2 = array.getDimen2();

            Var index1_var = parseExp(n.getLeft());

            Var index2_var = parseExp(n.getRight());
            assert index1_var != null;
            if (index1_var.getType().equals("num")) {
                assert index2_var != null;
                if (index2_var.getType().equals("num")) {
                    int offsetnum = index1_var.getNum() * dimen2 + index2_var.getNum();
                    if (array.getConstType().equals(ConstType.VAR)) {
                        //Var 返回a[offset]
                        Var offset = new Var("num", offsetnum);
                        Var retVar = new Var("array", arrname, offset);
                        retVar.setSymbol(array);    //只设置symbol，没有设置kindofSymbol=True
                        return retVar;
                    } else {
                        //const 直接返回值
                        int arrayvalue = array.getArrayValue().get(offsetnum);
                        return new Var("num", arrayvalue);
                    }
                } else {
                    int tnum = index1_var.getNum() * dimen2;
                    Var t = new Var("num", tnum);
                    Var offset = getTmpVar();
                    createMidCode(MidType.assign, "+", offset, index2_var, t);
                    Var retVar = new Var("array", arrname, offset);
                    retVar.setSymbol(array);    //只设置symbol，不能kindofSymbol=True
                    return retVar;
                }
            } else {
                Var tmpvar1 = getTmpVar();
                Var dimen2_var = new Var("num", dimen2);
                createMidCode(MidType.assign, "*", tmpvar1, index1_var, dimen2_var);

                Var offset = getTmpVar();

                createMidCode(MidType.assign, "+", offset, index2_var, tmpvar1);
                Var retVar = new Var("array", arrname, offset);
                retVar.setSymbol(array);
                return retVar;
            }
        }
    }


    /**
     * arrdimen是应该传的维数，n是实参
     */
    private Var parseParaArray(ASTNode n, int arrdimen) {     //为函数传参服务，只返回array类型，arrdimen是参数应当的维度

        String name = n.getName();

        Symbol arraysymbol = MidCodeGenerate.lookallTableFindsamename(name);

        Var arrayident = new Var("array", name);

        assert arraysymbol != null;
        if (arraysymbol.getDimension() == 2 && arrdimen == 1) {   //二维数组传一维，需要记录一维数组下标

            Var numvar = parseExp(n.getLeft());        //数组第一维下标
            arrayident.setVar(numvar);
        }
        //其他直接传入
        arrayident.setSymbol(arraysymbol);
        arrayident.setiskindofsymbolTrue();
        return arrayident;
    }


    public static Symbol lookallTableFindsamename(String name) {  //找不到同名，返回null，优先查找同符号表，顺次向上查父符号表
        Table t = headerTable;
        Symbol samename = t.sameNameSymbol(name);
        if (samename == null) {
            while (t.getFather() != null) {
                t = t.getFather();
                samename = t.sameNameSymbol(name);
                if (samename != null) {
                    return samename;
                }
            }
            return null;
        }
        return samename;
    }

    public static Symbol lookLocalTableFindSamename(String name, Table localtable) {
        Symbol ans = localtable.sameNameSymbol(name);
        return ans;
    }

    public static Symbol lookGlobalTableFindsamename(String name) {
        Table t = foreverGlobalTAble;
        return t.sameNameSymbol(name);
    }

    private Var getTmpVar() {
        varcount += 1;
        String name = "#tmp" + varcount;
        Var var = new Var("var", name);
        var.scope = MidCodeGenerate.headerTable;
        return var;
    }

    private void ir4init(MidCode ir) {
        ir.setGlobal(isglobal);
        ir.setScope(headerTable);
        idrecord += 1;
        ir.setId(idrecord);
        ir.initRawstr();

        midCodes.add(ir);
    }


    private void createMidCode(MidType type, String IRstring) {
        MidCode ir = new MidCode(type, IRstring);
        ir4init(ir);
    }

    private void createMidCode(MidType type, Var var) {
        MidCode ir = new MidCode(type, var);
        ir4init(ir);
    }


    private void createMidCode(MidType type, NodeKind kind, String name) {
        if (type.equals(MidType.funcDecl)) {
            MidCode ir = new MidCode(type, kind, name);
            ir4init(ir);
            return;
        }
        MyError.errorat("Midcodegen", 575, "这里暂时只能create funcdecl");

    }

    //assign2
    private void createMidCode(MidType type, Var dest, Var oper1) {
        if (type.equals(MidType.assign2)) {
            MidCode ir = new MidCode(type, dest, oper1);
            ir4init(ir);
            return;
        }
        MyError.errorat("Midcodegen", 575, "这里暂时只能create assign2");
    }

    //assign 和 setcmp
    private void createMidCode(MidType type, String operator, Var dest, Var oper1, Var oper2) {

        if (type.equals(MidType.assign) || type.equals(MidType.setcmp)) {
            MidCode ir = new MidCode(type, operator, dest, oper1, oper2);
            ir4init(ir);
            return;
        }
        MyError.errorat("Midcodegen", 587, "这里暂时只能create assign setcmp");
    }

    //branch blt slt....
    private void createMidCode(MidType type, String instr, String jumpto, Var oper1, Var oper2) {
        MidCode ir = new MidCode(type, instr, jumpto, oper1, oper2);
        ir4init(ir);
    }

    /**
     * 为函数名(isfunc true)，数组，int的定义填写符号表
     **/

    public Symbol insertTosymbolTable(ASTNode identnode, ASTNode init, Boolean isfunc, Boolean isfuncpara) {
        String name = identnode.getName();
        NodeKind kind = identnode.getKind();
        //MyError.sout("INsert into table "+name+" "+kind);

        Symbol symbol = null;
        if (isfunc) {
            String funcname = identnode.getName();
            NodeKind functype = identnode.getKind();
            symbol = new Symbol(funcname, TableType.FUNC);
            FuncKind funcKind = functype.equals(NodeKind.VOID) ? FuncKind.VOID : FuncKind.INT;
            symbol.setFuncKind(funcKind);
        } else if (kind.equals(NodeKind.INT) || kind.equals(NodeKind.CONSTINT)) { //int 和 const int
            symbol = new Symbol(name, TableType.INTEGER);
            if (identnode.getKind().equals(NodeKind.CONSTINT)) {
                symbol.setConstType(ConstType.CONST);
            } else if (identnode.getKind().equals(NodeKind.INT)) {
                symbol.setConstType(ConstType.VAR);
            }

            //const编译时一定能算，Var不一定，var需要运行时算

            if (init != null && ((kind.equals(NodeKind.CONSTINT)) || isglobal)) {
                int constinitnum = init.calcuValue();
                symbol.setNum(constinitnum);
            }
        } else if (kind.equals(NodeKind.ARRAY) || kind.equals(NodeKind.CONSTARRAY)) {
            symbol = new Symbol(name, TableType.ARRAY);

            if (kind.equals(NodeKind.CONSTARRAY)) {
                symbol.setConstType(ConstType.CONST);
            } else {
                symbol.setConstType(ConstType.VAR);
            }

            symbol.setDimension(1);
            ASTNode dimen1 = identnode.getLeft();
            int dimen1num = 0;
            if (!isfuncpara) { //如果是函数数组参数，这里的一维一定不会有数字，我们直接跳过
                dimen1num = dimen1.calcuValue();
                symbol.setDimen1(dimen1num);
            }

            int dimen2num = 0;
            if (identnode.getRight() != null) {
                symbol.setDimension(2);
                ASTNode dimen2 = identnode.getRight();
                dimen2num = dimen2.calcuValue();
                symbol.setDimen2(dimen2num);
            }

            //这里只把常量数组或者global数组的init加进去了，因为这里如果是局部的vararray，其值有可能是运行时才得知
            if (init != null && (kind.equals(NodeKind.CONSTARRAY) || isglobal)) {
                if (dimen2num == 0) {//一维数组数组初值，加到symbol的array里面
                    int i = 0;
                    while (i < dimen1num) {
                        int initnum = init.getLeafs().get(i).calcuValue();
                        symbol.addArraynum(initnum);
                        i += 1;
                    }
                } else {//二维  int a[4][4] = {{1, 2, 3, 4},{0,0,0,0}, {0,0,0,0},{1,2,3,4}};
                    int i = 0;
                    while (i < dimen1num) {
                        int j = 0;
                        while (j < dimen2num) {
                            System.out.println(name);
                            assert init != null;
                            ASTNode numnode = init.getLeafs().get(i).getLeafs().get(j);
                            int initnum = numnode.calcuValue();
                            symbol.addArraynum(initnum);
                            j += 1;
                        }
                        i += 1;
                    }
                }
            }
        }
        assert symbol != null;
        symbol.setGlobal(isglobal);
        MidCodeGenerate.insertTable(symbol);
        return symbol;
    }

    private Var generateOperatorIR(ASTNode n, String op) {
        Var leftexp = parseExp(n.getLeft());
        Var rightexp = parseExp(n.getRight());

        assert leftexp != null;
        if (leftexp.getType().equals("num") && !op.equals("bitand")) {
            assert rightexp != null;
            if (rightexp.getType().equals("num")) { //左面是数，右边是数，直接计算完返回就行
                int leftnum = leftexp.getNum();
                int rightnum = rightexp.getNum();

                Var newvar = null;
                if (op.equals("+")) {
                    newvar = new Var("num", leftnum + rightnum);
                } else if (op.equals("-")) {
                    newvar = new Var("num", leftnum - rightnum);
                } else if (op.equals("*")) {
                    newvar = new Var("num", leftnum * rightnum);
                } else if (op.equals("/")) {
                    newvar = new Var("num", leftnum / rightnum);
                } else if (op.equals("%")) {
                    newvar = new Var("num", leftnum % rightnum);
                } else {
                    MyError.errorat("Midgenrater", 1096, "这里是啥符号？？");
                }
                return newvar;
            }
        }
        //有不是数的东西，申请一个tmp，运行时计算
        Var tmpvar = getTmpVar();
        createMidCode(MidType.assign, op, tmpvar, leftexp, rightexp);
        return tmpvar;

    }
}
