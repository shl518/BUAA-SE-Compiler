/**
 * @description：TODO
 * @author ：szy
 * @date ：2022/10/22 8:10 PM
 */
package CONST;

public class MyError {
    public static int a = 1;//debug
    public static  void errorat(String s,int a){
        System.err.println("Error at  :"+s+" "+a);
    }
    public static  void errorat(String s,int a,String prmpt){
        System.err.println("Error at :"+s+" "+a+" because "+prmpt);
    }

    public static void check(Object obj, String loc,int num) {
        if (obj == null) {
            System.err.println(loc+ ": Assert Check Error! Null !"+" at "+num);
        }
    }

    public static void sout(String s){
        if(a == 1){
            System.out.println(s);
        }
    }
}
