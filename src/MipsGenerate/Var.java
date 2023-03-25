/**
 * @description：TODO
 * @author ：szy
 * @date ：2022/10/23 9:57 AM
 */
package MipsGenerate;

import CONST.MyError;
import Table.Symbol;
import Table.Table;

public class Var {
    public static Var zero = new Var("zero",0);
    private String type;    //var,num,str,array, func(name函数名)
    private String name;
    private int num;

    private boolean hasbeenkickout = false;

    public boolean isHasbeenkickout() {
        return hasbeenkickout;
    }

    public void setHasbeenkickout(boolean hasbeenkickout) {
        this.hasbeenkickout = hasbeenkickout;
    }

    private int spoffset = 0;

    public int getSpoffset() {
        return spoffset;
    }

    public void setSpoffset(int spoffset) {
        this.spoffset = spoffset;
    }

    private Var var = null;

    private RegEnum curReg = RegEnum.wrong;     //当前分配的寄存器号

    private boolean kindofsymbol;   //或者局部、全局变量
    private Symbol symbol = null;

    public Table getScope() {
        return scope;
    }

    public Table scope;

    //一般var， 传参时array也用了这个
    public Var(String type, String name) {
        this.type = type;
        this.name = name;
        this.kindofsymbol = false;
    }

    public Var(String type, int num) {
        this.type = type;
        this.num = num;
        this.kindofsymbol = false;
    }


    public Var(String type, String name, Var var) {
        this.type = type;
        this.name = name;
        this.kindofsymbol = false;
        this.var = var;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getNum() {
        return num;
    }

    public RegEnum getCurReg() {
        return curReg;
    }

    public Symbol getSymbol() {
        if(symbol == null){
            MyError.errorat("noproblem in Var",61);
        }
        return symbol;
    }

    public void setVar(Var var) {
        this.var = var;
    }

    public void setCurReg(RegEnum curReg) {
        this.curReg = curReg;
    }

    public boolean isKindofsymbol() {
        return kindofsymbol;
    }

    public void setiskindofsymbolTrue() {
        kindofsymbol = true; ////是临时自定义变量(如ti) 或者局部、全局变量
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        switch (type){
            case "var":
            case "str":
            case "null":
                return name;
            case "array":
                StringBuilder sb = new StringBuilder();
                sb.append(name);
                if(var != null){
                    sb.append("[").append(var.toString()).append("]");
                }
                return sb.toString();
            default:
                return String.valueOf(num);

        }
    }

    public Var getVar() {
        return var;
    }
}

