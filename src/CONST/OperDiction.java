package CONST;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author ：szy
 * @description：TODO
 * @date ：2022/10/22 7:59 PM
 */

public class OperDiction {
    public static HashSet<String> operator;
    public static HashSet<String> opcmp;

    static {
        operator = new HashSet<>();
        operator.add("+");
        operator.add("-");
        operator.add("*");
        operator.add("/");
        operator.add("%");
        operator.add("bitand");
    }

    static {
        opcmp = new HashSet<>();
        opcmp.add(">");
        opcmp.add("<");
        opcmp.add(">=");
        opcmp.add("<=");
        opcmp.add("==");
        opcmp.add("!=");
        opcmp.add("&&");
        opcmp.add("||");
    }

    public static boolean hasOperator(String s){
        return operator.contains(s);
    }
    public static boolean hascmp(String s){
        return opcmp.contains(s);
    }

    //判断是> < >= <=
    public static boolean isnumcmp(String s){
        return s.equals(">") || s.equals("<") || s.equals(">=") || s.equals("<=");
    }
    public static boolean iseqcmp(String s){
        return s.equals("==") || s.equals("!=");
    }

}
