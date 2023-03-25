/**
 * @description：TODO
 * @author ：szy
 * @date ：2022/10/23 9:33 AM
 */
package MipsGenerate;

import AST.NodeKind;
import CONST.MyError;
import CONST.Note;
import CONST.NoteType;
import Table.*;

import java.util.ArrayList;

public class MidCode {
    private MidType type;    //15种中间代码种类
    private String rawstr;  //输出的ircode字符串格式

    private int id;//debug 用

    private String IRstring;
    private NodeKind kind;    //const 等情况
    private String name;
    private int num = 0;

    public void setNum(int num) {
        this.num = num;
    }

    private boolean global;   //是否全局
    private boolean init = false;    //int,array是否有初始化值
    private final ArrayList<Integer> initList = new ArrayList<>(); //数组的初始化值List
    private boolean voidreturn;


    private Var var;  //含有表达式等情况时，对应的Variable类型

    private int array1 = 0;     //数组形式时第1维的大小
    private int array2 = 0;     //数组形式时第2维的大小

    private String operator;
    private Var dest;      //二元运算或一元运算中的目标变量
    private Var oper1;     //二元运算中的第1个操作数，或一元运算的右操作数
    private Var oper2;     //二元运算第2个操作数

    private Symbol symbol;  //含有表达式等情况时，对应的symbol类型的符号
    private Table scope;    //todo inblockoffset用到

    private String instr;   //branch跳转 的bne等类型
    private String jumploc; //branch的跳转位置

    public String getJumploc() {
        return jumploc;
    }
//优化 todo 先没写
    //public boolean deleted = false;
    //public int startindex = -1;


    //todo 分类包括：note,label
    public MidCode(MidType type, String IRstring) {
        this.type = type;
        this.IRstring = IRstring;
    }

    public MidCode(MidType type, Var var) {

        this.type = type;
        this.var = var;
    }

    //或 "funcDecl" functype + " " + funcname + "()"
    public MidCode(MidType type, NodeKind kind, String name) {
        this.type = type;
        this.kind = kind;
        this.name = name;
    }


    //arrayDef：类别，数组名，第一维大小，第二维（0表示无第2维）
    public MidCode(MidType type, String name, int array1, int array2) {
        this.type = type;
        this.name = name;
        this.array1 = array1;
        this.array2 = array2;
    }

    public MidCode(MidType type, Var dest, Var oper1) { //assign2
        this.type = type;
        this.dest = dest;
        this.oper1 = oper1;
    }

    public MidCode(MidType type, String operator, Var dest, Var oper1, Var oper2) { //assign
        this.type = type;
        this.operator = operator;
        this.dest = dest;
        this.oper1 = oper1;
        this.oper2 = oper2;
    }

    public String getInstr() {
        return instr;
    }

    //Cond
    public MidCode(MidType type, String instr, String jumploc, Var oper1, Var oper2) {
        this.type = type;
        this.instr = instr;
        this.jumploc = jumploc;
        this.oper1 = oper1;
        this.oper2 = oper2;
    }

    public MidType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getNum() {
        return num;
    }

    public String getRawstr() {
        return rawstr;
    }

    public String getIRstring() {
        return IRstring;
    }

    public ArrayList<Integer> getInitList() {
        return initList;
    }

    public Var getVariable() {
        return var;
    }

    public String getOperator() {
        return operator;
    }

    public Var getDest() {
        return dest;
    }

    public Var getOper1() {
        return oper1;
    }

    public Var getOper2() {
        return oper2;
    }

    public int getArray1() {
        return array1;
    }

    public int getArray2() {
        return array2;
    }

    public Table getScope() {
        return scope;
    }

    public Symbol getSymbol() {
        return symbol;
    }


    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean globalBool) {
        this.global = globalBool;
    }

    public void setInitIsTrue() {
        init = true;
    }

    public void addAllInitList(ArrayList<Integer> nums) {
        initList.addAll(nums);
    }

    public void setScope(Table scope) {
        this.scope = scope;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public boolean isVoidreturn() {
        return voidreturn;
    }

    public void setVoidreturn(boolean voidreturn) {
        this.voidreturn = voidreturn;
    }

    public void initRawstr() {
        StringBuilder sb = new StringBuilder();

        if (type.equals(MidType.Note) || type.equals(MidType.funcPara)) {
            sb.append(IRstring);
        } else if (type.equals(MidType.Label)) {
            sb.append(IRstring).append(":");
        } else if (type.equals(MidType.call)) {
            sb.append(type).append(" ").append(IRstring);
        } else if (type.equals(MidType.Return)) {
            sb.append("RETURN");
            if (!voidreturn) {
                sb.append(" ").append(var.toString());
            }
        } else if (type.equals(MidType.Printf) || type.equals(MidType.Push) || type.equals(MidType.Getint)) {
            //System.out.println(type);

            sb.append(type).append(" ").append(var.getType()).append(" ").append(var.toString());

        } else if (type.equals(MidType.assign_ret)) {
            sb.append(var.toString()).append(" = RETURN");
        } else if (type.equals(MidType.assign)) {
            sb.append(dest.toString()).append(" = ").append(oper1.toString()).append(" ")
                    .append(operator).append(" ").append(oper2.toString());
        } else if (type.equals(MidType.assign2)) {
            sb.append(dest.toString()).append(" = ").append(oper1.toString());
        } else if (type.equals(MidType.intDecl)) {
            sb.append(kind).append(" ").append(name);
            if (init) {
                sb.append(" = ").append(name);
            }
        } else if (type.equals(MidType.funcDecl)) {
            sb.append(kind).append(" ").append(name).append("()");
        } else if (type.equals(MidType.arrayDecl)) {
            sb.append("array int ").append(name).append("[").append(array1).append("]");
            if (array2 > 0) {
                sb.append("[").append(array2).append("]");
            }
            if (init) {
                sb.append(" = {");
                for (int initnum : initList) {
                    sb.append(initnum).append(",");
                }

                sb.deleteCharAt(sb.length() - 1);
                sb.append("}");
            }
        } else if (type.equals(MidType.jump)) {
            sb.append("j ").append(IRstring);
        } else if (type.equals(MidType.branch)) {
            sb.append(instr).append(" ").append(oper1.toString()).append(", ")
                    .append(oper2.toString()).append(", ").append(jumploc);
        } else if (type.equals(MidType.setcmp)) {
            sb.append(operator).append(" ").append(dest.toString()).append(", ")
                    .append(oper1.toString()).append(", ").append(oper2.toString());
        } else {
            MyError.errorat("MidCode", 240, "不知道是什么类型");
        }

        rawstr = sb.toString();
    }


    public boolean isNoteAndEqualTo(NoteType t) {
        return type.equals(MidType.Note) && getIRstring().equals(Note.get(t));
    }

    public int getArraySize() {
        int size;
        if (getArray2() == 0) {
            size = getArray1();
        } else {
            size = getArray1() * getArray2();
        }
        return size;
    }

    public boolean isInit() {
        return init;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
