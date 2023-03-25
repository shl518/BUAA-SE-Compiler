/**
 * @description：TODO
 * @author ：szy
 * @date ：2022/10/22 1:38 PM
 */
package AST;

import CONST.MyError;
import CONST.OperDiction;
import MipsGenerate.MidCodeGenerate;
import Table.Symbol;

import java.util.ArrayList;

public class ASTNode {

    private NodeType type;

    public NodeType getType() {
        return type;
    }

    public NodeKind getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    public int getNum() {
        return num;
    }

    public ASTNode getLeft() {
        return left;
    }

    public ASTNode getRight() {
        return right;
    }

    public ASTNode getMiddle() {
        return middle;
    }

    public String getOpstring() {
        return opstring;
    }

    public ArrayList<ASTNode> getLeafs() {
        return leafs;
    }

    private NodeKind kind;    //int, const int, array, const array, func 五种， 和一种funcDef时记录函数返回值类型
    private String name;
    private int num;
    private ASTNode left = null;
    private ASTNode right = null;
    private ASTNode middle = null;
    private String opstring = "??";

    public void setOpstring(String opstring) {
        this.opstring = opstring;
    }

    private ArrayList<ASTNode> leafs = new ArrayList<>();

    public ASTNode(NodeType type) {
        this.type = type;
    }

    public void addleaf(ASTNode n) {
        leafs.add(n);
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public void setKind(NodeKind kind) {
        this.kind = kind;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setLeft(ASTNode left) {
        this.left = left;
    }

    public void setRight(ASTNode right) {
        this.right = right;
    }

    public ASTNode(NodeType type, String name) {
        this.type = type;
        this.name = name;
    }

    public ASTNode(NodeType type, int num) {
        this.type = type;
        this.num = num;
    }

    public ASTNode(NodeType type, ASTNode left, ASTNode middle, ASTNode right) {
        this.type = type;
        this.left = left;
        this.right = right;
        this.middle = middle;
    }

    public ASTNode(NodeType type, ASTNode left) {
        this.type = type;
        this.left = left;
    }

    public ASTNode(NodeType type, ASTNode left, ASTNode right) {
        this.type = type;
        this.left = left;
        this.right = right;
    }

    public int calcuValue() {
        if (OperDiction.hasOperator(opstring)) {
            switch (opstring) {
                case "+":
                    if (right != null) {
                        return left.calcuValue() + right.calcuValue();
                    }
                    return left.calcuValue();
                case "-":
                    if (right != null) {
                        return left.calcuValue() - right.calcuValue();
                    }
                    return -left.calcuValue();
                case "*":
                    return left.calcuValue() * right.calcuValue();
                case "/":
                    return left.calcuValue() / right.calcuValue();
                case "%":
                    return left.calcuValue() % right.calcuValue();
                default:
                    MyError.errorat("ASTnode",151);
                    return 0;
            }
        }else if(type.equals(NodeType.Number)){
            return num;
        }else if(type.equals(NodeType.Ident)){
            if(kind.equals(NodeKind.FUNC)){
                //func 不再这里算，这里只能算一些简单的加减乘除
                MyError.errorat("ASTNODE",158,"这里不应该有这种情况，不能在编译时计算函数");
            } else if (kind.equals(NodeKind.INT)||kind.equals(NodeKind.CONSTINT)) {
                Symbol symbol = MidCodeGenerate.lookallTableFindsamename(name);
                return symbol.getNum();
            }else if(kind.equals(NodeKind.ARRAY)||kind.equals(NodeKind.CONSTARRAY)){
                Symbol symbol = MidCodeGenerate.lookallTableFindsamename(name);
                if(symbol != null){
                    if(right != null){ //二维
                        int dimen2 = symbol.getDimen2();
                        int index1 = left.calcuValue();
                        int index2 = right.calcuValue();
                        int index = index1*dimen2 + index2;
                        return symbol.getArrayValue().get(index);
                    }else {//一维数组
                        int index = left.calcuValue();
                        return symbol.getArrayValue().get(index);
                    }
                }else {
                    MyError.errorat("ASTNODE",179,"符号表没找到，但理应之前定义");
                }
            }else {
                MyError.errorat("ASTNODE",178);
            }

        }else {
            MyError.errorat("ASTNODE",182);
        }
        return -92929299;
    }

    public boolean haselse(){
        if(type.equals(NodeType.IfStatement)){
            if(right != null){
                return true;
            }
        }else {
            MyError.errorat("ASTNode",194);
        }
        return false;
    }
}
