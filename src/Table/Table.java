/**
 * @description：
 * @author ：szy
 * @date ：2022/10/7 7:57 PM
 */
package Table;

import Parser.Parser;

import java.util.ArrayList;

public class Table {
    private ArrayList<Symbol> symbols;
    private Table father;
    private ArrayList<Table> sons;

    public int startindex;//todo 优化用,不知道干啥的

    //记录table在块内的偏移
    public int inblockoffset = 0;

    public int getInblockoffset() {
        return inblockoffset;
    }


    private BlockType blockType;
    public Table(Table father,BlockType blockType){
        this.symbols = new ArrayList<>();
        this.sons = new ArrayList<>();
        this.father = father;
        this.blockType = blockType;
    }

    public void addson(Table son){
        sons.add(son);
    }

    public ArrayList<Symbol> getSymbols() {
        return symbols;
    }

    public Table getFather(){
        return father;
    }

    public ArrayList<Table> getSons() {
        return sons;
    }

    public void addSymbol(Symbol symbol){
        symbols.add(symbol);
    }

    public void checkTable(){
        for (Symbol symbol : symbols) {
            System.out.println("名字："+symbol.getName()+" 类型："
                    +symbol.getTableType()+" 常变量 ："+symbol.getConstType()+" 维度："+symbol.getDimension()
            +"函数类型: "+symbol.getFuncKind());
            System.out.println("函数参数start----");
            if(symbol.getParams() == null){
                System.out.println("end--------");
                continue;
            }
            for (Symbol param : symbol.getParams()) {
                System.out.println("名字："+param.getName()+" 类型："
                        +param.getTableType()+" 常变量 ："+param.getConstType()+" 维度："+param.getDimension());
            }
            System.out.println("end--------");
        }
        for (Table son : sons) {
            son.checkTable();
        }
    }

    public Symbol sameNameSymbol(String name){
        for (Symbol symbol : symbols) {
            if(symbol.getName().equals(name)){
                return symbol;
            }
        }
        return null;
    }

    public BlockType getBlockType() {
        return blockType;
    }
}
