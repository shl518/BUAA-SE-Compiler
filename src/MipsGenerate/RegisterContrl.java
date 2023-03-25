/**
 * @description：TODO
 * @author ：szy
 * @date ：2022/10/26 5:10 PM
 */
package MipsGenerate;

import CONST.MyError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class RegisterContrl {

    public static final ArrayList<RegEnum> freeregs = new ArrayList<>();
    //正使用的活跃临时reg，函数调用前入栈用
    public static final ArrayList<RegEnum> activeReg =  new ArrayList<>();

    public static final ArrayList<RegEnum> freeregs_forPush = new ArrayList<>();

    public static final LinkedList<Var> tmpHasReg = new LinkedList<>();

    static { //开始时31个寄存器全为free
        resetFreeRegLists_forpush();
    }

    static { //开始时31个寄存器全为free
        resetFreeRegLists();
    }


    public static  Var kickoutAtmp(){
        if(tmpHasReg.isEmpty()){
            MyError.errorat("Registerctrl",30,"kick不出去");
            //System.out.println(MipsGenerate.mid_id_now);
        }
        Var tmp = tmpHasReg.removeFirst();
        if(tmp.getName().equals("#tmp78")){
            System.out.println(MipsGenerate.mid_id_now);
            System.out.println("here");
        }
        System.out.println("kick out "+tmp.getName());
        RegEnum has =  tmp.getCurReg();
        FreeReg(has);
        return tmp;
    }

    public static RegEnum GetregFrom_pushReg(){
        if(!freeregs_forPush.isEmpty()){
            RegEnum regnum = freeregs_forPush.get(0);
            freeregs_forPush.remove(0);
            return regnum;
        }else {
            return RegEnum.wrong;
        }
    }

    public static void freereg_toPushReg(RegEnum regnum){
        freeregs_forPush.add(regnum);
    }

    //todo 1127 debug 解决tmp一直占用Reg导致Reg不够用的情况
    public static RegEnum Getreg(Var v,boolean istmp){ //寄存器分配函数 todo 目前采用的是循环分配的策略
        //todo 如果没有寄存器了怎么办
        if(MipsGenerate.inpush){
            return GetregFrom_pushReg();
        }
        if(!freeregs.isEmpty()){
            RegEnum regnum = freeregs.get(0);
            //System.out.println("Alloc "+regnum+" to "+v.getName());
            if(istmp){
                tmpHasReg.add(v);
            }

            if(!activeReg.contains(regnum)){
                activeReg.add(regnum);
            }else {
                MyError.errorat("RegfisterCtrl",35,"活跃变量已经有这个寄存器了，有错误，肯定之前没释放？");
            }

            freeregs.remove(0);
            return regnum;
        }else {
            return RegEnum.regisempty;

        }
    }
    public static void FreeReg(RegEnum regnum){ //寄存器释放函数
        if(MipsGenerate.inpush){
            freereg_toPushReg(regnum);
            return;
        }
        int regno = regnum.ordinal();

        if(regno == 28 || regno==29 || regno == 30 || regno == 31 || regno < 8 || regno>31){
            MyError.errorat("Refisterctrl",34,"没有分配这个"+regno+"寄存器，无法把他释放");
            //todo 1127
            System.exit(0);
            freeregs.add(RegEnum.none);
            freeregs.add(RegEnum.wrong);
            freeregs.add(RegEnum.wrong);
            freeregs.add(RegEnum.wrong);
            activeReg.remove(RegEnum.none);
        }else {
            freeregs.add(regnum);
            //新增
            ArrayList<Var> record_var_need_del = new ArrayList<>();
            for (Var var : tmpHasReg) {
                if(var.getCurReg().equals(regnum)){
                    record_var_need_del.add(var);
                }
            }
            for (Var var : record_var_need_del) {
                tmpHasReg.remove(var);
            }
            activeReg.remove(regnum);
        }

    }



    /**
     * 重置空闲寄存器表
     */
    public static void resetFreeRegLists(){
        freeregs.clear();
        for (RegEnum value : RegEnum.values()) {
            int i = value.ordinal();
            if(i >= 8 && i<=31 && !(i == 28 || i==29 || i == 30 || i == 31)){
                freeregs.add(value);
            }
        }
    }

    public static void resetFreeRegLists_forpush(){
        freeregs_forPush.clear();
        freeregs_forPush.add(RegEnum.a1);
        freeregs_forPush.add(RegEnum.a2);
        freeregs_forPush.add(RegEnum.a3);
        freeregs_forPush.add(RegEnum.gp);
        freeregs_forPush.add(RegEnum.fp);
    }
    /**
     * 删除活跃变量表所有寄存器
     */
    public static void  clearActiveRegList(){
        activeReg.clear();
        tmpHasReg.clear();//新增
    }


    /**
     * 查看所有寄存器是否都归位
     */
    public static boolean debugRegList(){
        return false;
    }
}
