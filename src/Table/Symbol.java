/**
 * @description：这是符号表
 * @author ：szy
 * @date ：2022/10/7 7:47 PM
 */
package Table;

import CONST.ConstValue;
import CONST.MyError;
import Lexer.Word;

import java.util.ArrayList;

public class Symbol {
    //错误处理时构想的符号表
    private String name;
    private Word word;
    private TableType tableType = null;//integer func array
    private ConstType constType = null;//var const
        //数组相关
    private int dimension = 0;
    private int dimen1 = 0;
    //todo private int irindex;

    public int getSpoffset() {
        return spoffset;
    }

    public void setSpoffset(int spoffset) {
        this.spoffset = spoffset;
    }

    private int spoffset = 0;

    public boolean isArray(){
        return tableType.equals(TableType.ARRAY);
    }

//    public void setIrindex(int irindex) {
//        this.irindex = irindex;
//    }

    private Table table;//所属table

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public int getDimen1() {
        return dimen1;
    }

    public void setConstType(ConstType constType) {
        this.constType = constType;
    }

    public int getDimen2() {
        return dimen2;
    }

    public ArrayList<Integer> getArrayValue() { //数组的初始值
        return arrayValue;
    }

    private int dimen2 = 0;
    private boolean global;

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public boolean isGlobal() {
        return global;
    }

    //函数相关
    private ArrayList<Symbol> params = new ArrayList<>();
    private FuncKind funcKind = null;
    private int num;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    //值相关
    private ArrayList<Integer> arrayValue = new ArrayList<>();


    public Symbol(String name, TableType tableType) {
        this.name = name;
        this.tableType = tableType;
    }

    public Symbol(String name, Word word, TableType tableType, ConstType constType) {
        this.name = name;
        this.word = word;
        this.tableType = tableType;
        this.constType = constType;
    }

    public void setDimension(int dimension,int d1,int d2) {
        this.dimension = dimension;
        this.dimen1 = d1;
        this.dimen2 = d2;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }


    public void setParams(ArrayList<Symbol> params) {
        this.params = params;
    }

    public void setFuncKind(FuncKind funcKind) {
        this.funcKind = funcKind;
    }

    public void addArraynum(int a){
        arrayValue.add(a);
    }

    public String getName() {
        return name;
    }

    public TableType getTableType() {
        return tableType;
    }

    public ConstType getConstType() {
        return constType;
    }

    public int getDimension() {
        return dimension;
    }


    public ArrayList<Symbol> getParams() {
        return params;
    }

    public ArrayList<Integer> getRightParamsDimentions(){
        ArrayList<Integer> ans = new ArrayList<>();
        for (Symbol param : params) {
            ans.add(param.getDimension());
        }
        return ans;
    }

    public ArrayList<Integer> hasthisPara(String name){
        ArrayList<Integer> ans = new ArrayList<>();
        if(!tableType.equals(TableType.FUNC)){
            MyError.errorat("Symbol",159,"这个Symbol不是func，调用错误hasPara");
        }else {
            for(int i=0;i<params.size();i++){
                if(params.get(i).getName().equals(name)){
                    ans.add(1);
                    ans.add(i+1);
                    return ans;
                }
            }
        }
        ans.add(0);
        return ans;
    }
    public FuncKind getFuncKind() {
        return funcKind;
    }

    public Word getWord() {
        return word;
    }

    public int getParamsLen(){
        return params.size();
    }

    public void addParam(Symbol param){
        params.add(param);
    }

    public void setDimen1(int dimen1) {
        this.dimen1 = dimen1;
    }

    public void setDimen2(int dimen2) {
        this.dimen2 = dimen2;
    }

    public int getPamaNum(){
        return params.size();
    }
    public boolean isConst(){
        return constType.equals(ConstType.CONST);
    }
}
