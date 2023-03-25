/**
 * @description：
 * @author ：szy
 * @date ：2022/10/2 7:47 PM
 */
package Error;

import Lexer.*;
import Parser.Parser;
import Table.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


public class ErrorChecker {
    public static final ArrayList<Error> errors = new ArrayList<>();
    public static Table GlobalTable = null;

    public static HashMap<String,String> debug = new HashMap<>();

    public static void initdebug(){
        debug.put("a","格式串非法字符");
        debug.put("b","名字重定义");
        debug.put("c","名字未定义");
        debug.put("d","函数参数个数不匹配");
        debug.put("e","函数参数类型不匹配");
        debug.put("f","无返回值存在不匹配return");
        debug.put("g","有返回值缺少return");
        debug.put("h","不能改变常量");
        debug.put("i","缺少分号");
        debug.put("j","缺少 ）");
        debug.put("k","缺少 ]");
        debug.put("l","printf中格式字符与表达式个数不匹配");
        debug.put("m","在非循环块中使用break和continue语句");
    }

    public static void writeerrors() throws IOException {
        initdebug();
        Collections.sort(errors); //TODO 为啥要排序
        File file = new File("error.txt");
        FileWriter writer = new FileWriter(file);
        for (Error error : errors) {
            //System.out.println(error.toString());
            System.out.println(error.toString());
            writer.write(error.toString()+"\n");
        }
        writer.flush();
        writer.close();
    }
    public static void Debugwriteerrors() throws IOException {
        initdebug();
        Collections.sort(errors); //TODO 为啥要排序
        System.out.println("DEBUG ERROR 开始输出：");
        File file = new File("myerror.txt");
        FileWriter writer = new FileWriter(file);
        for (Error error : errors) {
            //System.out.println(error.toString());
            System.out.println(error.toDebugString());
            writer.write(error.toDebugString()+"\n");
        }
        writer.flush();
        writer.close();
    }
    public static void add(Error error){
        errors.add(error);
    }
    public static void checkA(String formatstring){ //去除引号传进来
        int index = 0;
        while (index < formatstring.length()) {
            int a = formatstring.charAt(index);
            if (a == 32 || a == 33 || (a >= 40 && a <= 126 && a != 92)) {
                index += 1;
            } else if (a == 37) {
                if (index + 1 < formatstring.length() && formatstring.charAt(index + 1) == 'd') {
                    index += 2;
                } else {
                    add(new Error(Lexer.sym,"a"));
                    break;
                }
            } else if (a == 92) {
                if (index + 1 < formatstring.length() && formatstring.charAt(index + 1) == 'n') {
                    index += 2;
                } else {
                    add(new Error(Lexer.sym,"a"));
                    break;
                }
            } else {
                add(new Error(Lexer.sym,"a"));
                break;
            }
        }
    }

    public static boolean checkB(Symbol asymbol){
        for (Symbol symbol : Parser.table.getSymbols()) {
            //flag2 = symbol.getTableType().equals(TableType.FUNC)? 1:0;
            if(symbol.getName().equals(asymbol.getName())){ //&& flag1 == flag2
                //add error
                add(new Error(asymbol.getWord(),"b"));
                return true;
            }
        }
        return false;
    }

    public static Symbol checkC(Word w,TableType tableType){ //未定义
        Symbol checkresult = null;
        //System.out.println("check "+w.getValue()+" "+tableType.toString());
        if(!tableType.equals(TableType.FUNC)){
            checkresult = lookallTableFindsamename(w);
        }else {
            checkresult = lookGlobalTAbleFindSamename(w);
        }

        if( checkresult == null){
            add(new Error(w,"c"));
        }
        return checkresult;
    }

    public static boolean checkD(Word w,Symbol func,int paramnum){
        int shouldhave = func.getParamsLen();
        //System.out.println("should have param num:"+shouldhave);
        if(shouldhave != paramnum){
            add(new Error(w,"d"));
        }
        return shouldhave != paramnum;
    }

    public static void checkE(Word w,Symbol symbol,ArrayList<Integer> dimensions){ //dimensions是调用时的实际dimension
        ArrayList<Integer> rightDimensions = symbol.getRightParamsDimentions();
        for(int i = 0;i<dimensions.size();i++){
            if(!dimensions.get(i).equals(rightDimensions.get(i))){
                add(new Error(w,"e"));
                break;
            }
        }
    }
    public static Symbol lookallTableFindsamename(Word w){  //找不到同名，返回null，优先查找同符号表，顺次向上查父符号表
        Table t = Parser.table;
        Symbol samename = t.sameNameSymbol(w.getValue());
        if(samename == null){
            while(t.getFather() != null){
                t = t.getFather();
                samename = t.sameNameSymbol(w.getValue());
                if(samename != null){
                    return samename;
                }
            }
            return null;
        }
        return samename;
    }

    public static Symbol lookGlobalTAbleFindSamename(Word w){
        Symbol samename = GlobalTable.sameNameSymbol(w.getValue());
        return samename;
    }
    public static void checkM(Word w){
        if(Parser.intoWhile == 0){
            add(new Error(w,"m"));
        }
    }

    public static void checkF(Word w){ //无返回值函数存在return exp; 报错报到return行
        if( Parser.funckind.equals(FuncKind.VOID) && Parser.blockhasReturn){
            add(new Error(w,"f"));
        }
    }

    public static void checkG(Word w){ // 判断有返回值的最后一句是不是return exp；
        if(Parser.funckind.equals(FuncKind.INT) && !Parser.lastIsReturn){
            add(new Error(w,"g"));
        }
    }

    public static void checkH(Word w){
        Symbol symbol = lookallTableFindsamename(w);
        if(symbol == null){
            System.out.println("no define but need to check h????????????");
            return;
        }
        if(symbol.getConstType().equals(ConstType.CONST)){
            add(new Error(w,"h"));
        }
    }

    public static void checkL(Word printf,String formatstr,int num){
        int shouldhavenum = formatstr.split("%d").length-1;
        if(shouldhavenum != num){
            add(new Error(printf,"l"));
        }
    }

    public static void checktest(Word w,Symbol symbol){
        if(symbol.getConstType().equals(ConstType.VAR)){
            add(new Error(w,"test"));
        }
    }
}
