/**
 * @description：TODO 数组的相关操作，感觉他写的太冗余了，数组的读取访问应该也是统一的过程
 * @author ：szy
 * @date ：2022/10/25 9:34 PM
 */
package MipsGenerate;

import CONST.ConstValue;
import CONST.MyError;
import CONST.Note;
import CONST.NoteType;
import MipsCode.Data;
import MipsCode.MipsCal;
import MipsCode.MipsSimpleString;
import MipsCode.MipsString;
import Table.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class MipsGenerate {
    private ArrayList<MidCode> midcodeGen = null;
    private ArrayList<MipsString> mipsgen = new ArrayList<>();
    public static int mid_id_now = 0;

    private boolean inmain = false;
    private boolean infuncdel = false;
    private int infuncoffset = 0;
    private Symbol curfunc = null;
    private int activevaroffset = 0;
    //private boolean nowisinfunc = false;
    private boolean optimize = false;
    public static boolean inpush = false;

    private int pushoffset = 0;//push的时候-4,-8这样，call的时候回复0，下次push再-4，-8
    private ArrayList<MidCode> pushStore = new ArrayList<>();

    private int spoffset = 0;
    private int print_index1 = 0;

    private Data data = new Data(); //生成Data（全局变量和字符串标签）

    public MipsGenerate(ArrayList<MidCode> midcodeGen) {
        this.midcodeGen = midcodeGen;
    }


    public void writefile() throws IOException {
        File file = new File("mips.txt");
        FileWriter writer = new FileWriter(file);
        System.out.println("输出mips.txt...");
        for (MipsString s : mipsgen) {
            System.out.print(s.toMipsString().toString());
            writer.write(s.toMipsString().toString());
        }
        writer.flush();
        writer.close();
    }

    public void mipsgenerate(boolean optimize) {
        this.optimize = optimize;
        genadd(data);
        genPrintfString(); //data字段生成字符串标签
        for (MidCode midCode : midcodeGen) {
            if (inmain) { //主函数声明
                genmipsString(midCode);
            } else if (infuncdel) { //函数声明

                if (midCode.isNoteAndEqualTo(NoteType.StartMainFunc)) {
                    inmain = true;
                    infuncdel = false;
                    genadd("main:", false);
                    continue;
                } else if (midCode.getType().equals(MidType.funcDecl)) {
                    //函数定义
                    infuncoffset = 0;
                    String funcname = midCode.getName();
                    curfunc = MidCodeGenerate.lookGlobalTableFindsamename(funcname);
                    if (curfunc == null) {
                        MyError.errorat("Mipsgen", 82, "不可能找不到func定义");
                    }
                    genadd("Function_" + funcname + ":", false); // 加函数标签
                    MyError.sout("Function_" + funcname + ":");
                    continue;
                } else if (midCode.isNoteAndEqualTo(NoteType.endafunc)) { // 结束函数
                    RegisterContrl.clearActiveRegList();
                    ;
                    RegisterContrl.resetFreeRegLists();
                    if (infuncoffset != 0) {
                        genaddIm("addi", RegEnum.sp, RegEnum.sp, -infuncoffset);
                    }
                    genadd("jr $ra", true);
                    continue;
                }
                genmipsString(midCode);
            } else { //常变量全局声明
                genmipsString(midCode); // 生成全局变量定义

                if (midCode.isNoteAndEqualTo(NoteType.StartFuncDel)) {
                    infuncdel = true;
                    genadd(new MipsSimpleString(".text", false));
                    genadd("j main", true);
                }
            }
        }
        genadd("#End Program", true);
        genadd("li $v0, 10", true);
        genadd("syscall", true);

        //debug

    }

    public void genPrintfString() { //生成标签字符串
        int print_index2 = 0;
        for (MidCode midCode : midcodeGen) {

            if (midCode.isNoteAndEqualTo(NoteType.StartPrint)) {
                print_index1 += 1;
                print_index2 += 1;
            }
            if (midCode.getType().equals(MidType.Printf)
                    && midCode.getVariable().getType().equals("str")) {
                String labelname = "print" + print_index1 + "_str" + print_index2;
                data.addPrintfLabel(midCode, labelname);
                print_index2 += 1;
            }
        }
    }

    //todo over
    public void genintDecl(MidCode midCode) {
        if (midCode.isGlobal()) {
            data.addGLobalString(midCode);
        } else {
            //局部变量
            Symbol symbol = midCode.getSymbol();
            genadd("addi $sp,$sp,-4", true);
            //这里main和func唯一区别就是偏移设置不一样
            //const int 有初值
            //int 初值拆成多条，一条定义加多条assign
            //但都要在栈里给他分配空间，以防寄存器不够用，这样处理好像比较方便，但是可能性能不太好
            if (symbol == null) {
                MyError.errorat("MipsGen", 126, "这里为啥找不到Symbol，不科学");
            } else {
                //todo bug B10,22,indecl没有减去blockoffset导致栈指针没有恢复
                Table scope = midCode.getScope();  //修改后可获取到。！！！此处一定Main内也要这样处理！！！
                scope.inblockoffset -= 4;   //记录目前block内偏移

                if (infuncdel) { //函数内
                    infuncoffset -= 4;
                    symbol.setSpoffset(infuncoffset);
                } else { //Main内
                    spoffset -= 4;
                    symbol.setSpoffset(spoffset);
                }
                if (midCode.isInit()) {//const int一定初始化,int 拆多条，一定没初始化
                    genadd("li $v1, " + midCode.getNum(), true);
                    genadd("sw $v1, 0($sp)", true);//直接存，别宫我啦，我牙刷儿
                }
            }
        }
    }


    public void genmipsString(MidCode midCode) {
        MidType type = midCode.getType();
        mid_id_now = midCode.getId();
        switch (type) {
            case Note:
                genNote(midCode);
                break;
            case Label:
                genadd(new MipsSimpleString(midCode.getRawstr(), false));
                break;
            case Printf:
                genPrintf(midCode);
                break;
            case Getint:
                genGetint(midCode);
                break;
            case Push:
                storePush(midCode);
                break;
            case call:
                genCall(midCode);
                break;
            case assign:
                genAssign(midCode);
                break;
            case assign2:
                genAssign2(midCode);
                break;
            case assign_ret:
                genAssignRet(midCode);
                break;
            case intDecl:
                genintDecl(midCode);
                break;
            case Return:
                genReturn(midCode);
                break;
            case jump:
                genadd(midCode.getRawstr(), true);
                break;
            case arrayDecl:
                genarrayDecl(midCode);
                break;
            case branch:
                genBranch(midCode);
                break;
            case setcmp:
                genSetCmp(midCode);
                break;
            //下面这两个不用处理直接跳过，函数参数都已经在符号表中，函数声明就是一个标签，已经在最开始的初始循环完成了
            case funcPara:
                break;
            case funcDecl:
                break;
            default:
                MyError.errorat("Mipsgen", 212, "没有这种类型," + midCode.getType());
        }
    }

    /**
     * 要根据Note做一些特殊处理，类似Out Block / Out Block While Cut
     */
    public void genNote(MidCode midCode) {


        String note = midCode.getRawstr();
        if (note.equals(Note.get(NoteType.OutBlock))) {
            int inblockoffset = midCode.getScope().getInblockoffset();
            if (inblockoffset != 0) {
                genaddIm("addi", RegEnum.sp, RegEnum.sp, -inblockoffset);
                if (infuncdel) {
                    infuncoffset -= inblockoffset;
                } else {
                    spoffset -= inblockoffset;
                }
            }
        } else if (note.equals(Note.get(NoteType.OutBlockWHileCut))) {
            //
            Table blocktable = midCode.getScope();
            int sumoffset = 0;
            while (!blocktable.getBlockType().equals(BlockType.WHILE)) {
                sumoffset += blocktable.inblockoffset;
                blocktable = blocktable.getFather();
            }

            sumoffset += blocktable.inblockoffset;
            if (sumoffset != 0) {
                genaddIm("addi", RegEnum.sp, RegEnum.sp, -sumoffset);
            }
        } else {
            genadd(midCode.getRawstr(), true);
        }
    }


    public void storePush(MidCode midCode) {
        pushStore.add(midCode);
    }

    /**
     * @param midCode 注意：因为要推入活跃变量，理论上来讲是先推活跃变量，再push参数，但是推入活跃变量的时机只能放在gencall，
     *                所以我们只能先把这些midcode存起来，call的时候再解析
     */
    //todo 变量推入函数栈 没考虑函数，先考虑main内call
    public RegEnum genPush(MidCode midCode) {
        pushoffset -= 4; // -4,-8,-12这样
        Var pushVar = midCode.getVariable();

        if (midCode.getId() == 44) {
            System.out.println("here");
        }

        if (pushVar.getType().equals("array")) {
            //array push的是数组地址
            RegEnum pusharray_baseaddr_reg = getReg_kickouttmp_necessary(pushVar,false);
            writeArrayAddrToReg(pushVar, pusharray_baseaddr_reg);
            genaddSwLwLa("sw", pusharray_baseaddr_reg, pushoffset + "($sp)");
            RegisterContrl.FreeReg(pusharray_baseaddr_reg);

        } else {
            RegEnum pushReg = getVarReg(pushVar, true, false);
            //push的sp统一下降放到call
            genaddSwLwLa("sw", pushReg, pushoffset + "($sp)");
            //todo 这里的tmpvar，不放，先活跃变量入栈，再push，push这里放掉，活跃变量出寄存器的时候顺序不对应了
            //todo 但是tmpvar也不能不放
            //todo 目前方案,不是临时变量的放掉，是临时变量的不放，返回
            if (pushVar.getType().equals("num") ||
                    (pushVar.getType().equals("var") && pushVar.isKindofsymbol())) {
                RegisterContrl.FreeReg(pushReg);
            } else {
                //tmp变量
                if(pushVar.isHasbeenkickout()){
                    RegisterContrl.FreeReg(pushReg);
                }else {
                    return pushReg;
                }

            }
        }
        return RegEnum.none;
    }

    //push 专用，把数组的地址写到寄存器里面，之后push的时候sw到函数参数栈里
    //注意这里跟数组存取不一样，数组存取用lw，sw存取内存里面的数，而该函数访问的是地址，会用到la等instr
    public void writeArrayAddrToReg(Var array, RegEnum toreg) {
        //todo Bug B11 A 25 array的下标获取应该是getVarReg，写成checkif了，忽略了下标是symbol的情况，太傻了
        String arrname = array.getName();
        Symbol arrsymbol = array.getSymbol();
        if (arrsymbol.isGlobal()) {
            //全局数组
            if (array.getVar() == null) {
                //正常传2维，1维数组,直接la到对应数组首地址
                genaddSwLwLa("la", toreg, "Global_" + arrname);
            } else {
                //二维传一维
                Var indexvar = array.getVar();

                if (indexvar.getType().equals("num")) {
                    int arroffset = indexvar.getNum() * arrsymbol.getDimen2() * 4;
                    genaddSwLwLa("la", toreg, "Global_" + arrname);
                    genaddIm("addi", toreg, toreg, arroffset);
                } else if (indexvar.getType().equals("var")) {
                    //RegEnum varindex_reg = checkIfVariableHadReg_IFNotApply(indexvar, false);
                    RegEnum varindex_reg = getVarReg(indexvar, false, false);
                    genaddSll(varindex_reg, varindex_reg, 2);
                    genadd("li $" + toreg + ", " + arrsymbol.getDimen2(), true);
                    genadd("mult", varindex_reg, toreg);
                    genadd("mflo", toreg);
                    genaddSwLwLa("la", toreg, "Global_" + arrname + "($" + toreg + ")");
                    RegisterContrl.FreeReg(varindex_reg);
                } else {
                    MyError.errorat("Mipsgen", 297, "没有这种类型");
                }
            }
        } else if (infuncdel) {
            //函数内
            ArrayList<Integer> res = curfunc.hasthisPara(arrname);
            int isAParam = res.get(0);
            String arrparaoffset = getSymbolAdressAtStack(arrsymbol, 0);
            if (isAParam == 1) {
                //函数参数
                if (array.getVar() == null) {
                    //正常传数组的情况

                    genaddSwLwLa("lw", toreg, arrparaoffset);
                } else {
                    //二维传一维
                    Var indexvar = array.getVar();
                    if (indexvar.getType().equals("num")) {
                        int indexnum = indexvar.getNum();

                        //先取基地址再加
                        genaddSwLwLa("lw", toreg, arrparaoffset);
                        //todo 这Liu写的不一样，他应该写错了
                        genaddIm("addi", toreg, toreg, indexnum);

                    } else if (indexvar.getType().equals("var")) {
                        //RegEnum varindex_reg = checkIfVariableHadReg_IFNotApply(indexvar, false);
                        RegEnum varindex_reg = getVarReg(indexvar, false, false);
                        genaddSll(varindex_reg, varindex_reg, 2);
                        genadd("li $" + toreg + ", " + arrsymbol.getDimen2(), true);
                        genadd("mult", varindex_reg, toreg);
                        genadd("mflo", toreg);
                        //先取基地址再加
                        genaddSwLwLa("lw", varindex_reg, arrparaoffset);
                        genadd("add", toreg, toreg, varindex_reg);
                        RegisterContrl.FreeReg(varindex_reg);
                    } else {
                        MyError.errorat("Mipsgen", 342, "没有这种类型");
                    }
                }
            } else {
                //函数体内部
                String localarrayAddr = getSymbolAdressAtStack(arrsymbol, 0);
                if (array.getVar() == null) {
                    //正常传数组
                    genaddSwLwLa("la", toreg, localarrayAddr);
                } else {
                    //二维传一维
                    Var index = array.getVar();
                    if (index.getType().equals("num")) {
                        int indexnum = index.getNum();
                        String localarrayaddr2 = getSymbolAdressAtStack(arrsymbol, indexnum * arrsymbol.getDimen2() * 4);
                        genaddSwLwLa("la", toreg, localarrayaddr2);
                    } else if (index.getType().equals("var")) {
                        //RegEnum varindex_reg = checkIfVariableHadReg_IFNotApply(index, false);
                        RegEnum varindex_reg = getVarReg(index, false, false);
                        genaddSll(varindex_reg, varindex_reg, 2);
                        genadd("li $" + toreg + ", " + arrsymbol.getDimen2(), true);
                        genadd("mult", varindex_reg, toreg);
                        genadd("mflo", toreg);
                        genadd("addu", toreg, toreg, RegEnum.sp);
                        genaddSwLwLa("la", toreg, localarrayAddr + "($" + toreg + ")");

                        RegisterContrl.FreeReg(varindex_reg);
                    } else {
                        MyError.errorat("Mipsgen", 359, "没有这种类型");
                    }

                }

            }
        } else {
            //main
            String addr = getSymbolAdressAtStack(arrsymbol, 0);
            if (array.getVar() == null) {
                //正常传数组
                genadd("li $" + toreg + ", " + addr, true);
            } else {
                //二维传一维
                Var index = array.getVar();
                if (index.getType().equals("num")) {
                    int indexnum = index.getNum();
                    String addr2 = getSymbolAdressAtStack(arrsymbol, indexnum * arrsymbol.getDimen2() * 4);
                    genadd("li $" + toreg + ", " + addr2, true);
                } else if (index.getType().equals("var")) {
                    RegEnum varindex_reg = getVarReg(index, false, false);
                    //RegEnum varindex_reg = checkIfVariableHadReg_IFNotApply(index, false);
                    genaddSll(varindex_reg, varindex_reg, 2);
                    genadd("li $" + toreg + ", " + arrsymbol.getDimen2(), true);
                    genadd("mult", varindex_reg, toreg);
                    genadd("mflo", toreg);

                    genaddImHex("addi", toreg, toreg, addr);

                    RegisterContrl.FreeReg(varindex_reg);

                } else {
                    MyError.errorat("Mipsgen", 344, "没有这种类型");
                }
            }

        }

    }

    //todo 活跃变量入本地栈
    public void genCall(MidCode midCode) {
        if (midCode.getId() == 131 || midCode.getId() == 134) {
            System.out.println("here");
        }
        String funcname = midCode.getIRstring();
        Symbol funcsymbol = MidCodeGenerate.lookGlobalTableFindsamename(funcname);
        if (funcsymbol == null) {
            MyError.errorat("Mipsgen", 421, "没有这个函数");
        }
        assert funcsymbol != null;
        int paranum = funcsymbol.getPamaNum();

        pushoffset = 0;
        //先推活跃变量到本地栈(倒推正取)
        activevaroffset = 0;
        ArrayList<RegEnum> activeRegList = RegisterContrl.activeReg; // 这里用深克隆
        int preactiveoffset = 0;

        //活跃变量全部推入，不管
        for (int i = activeRegList.size() - 1; i >= 0; i--) {
            RegEnum activereg = activeRegList.get(i);
            genaddIm("addi", RegEnum.sp, RegEnum.sp, -4);
            genaddSwLwLa("sw", activereg, "($sp)");
            preactiveoffset += 4;
        }
        activevaroffset = preactiveoffset;


        //再推函数参数到函数栈,并将push从pushstore删除
        ArrayList<Integer> removepush = new ArrayList<>();
        ArrayList<RegEnum> should_free_aft_push = new ArrayList<>();
        inpush = true;
        for (int i = pushStore.size() - paranum; i < pushStore.size(); i++) {
            RegEnum should_free = genPush(pushStore.get(i));
            if (!should_free.equals(RegEnum.none)) {
                should_free_aft_push.add(should_free);
            }
            removepush.add(i);
        }
        inpush = false;
        for (int j = removepush.size() - 1; j >= 0; j--) {
            int i = removepush.get(j);
            pushStore.remove(i);
        }
        activevaroffset = 0;

        //推ra到函数栈
        genaddIm("addi", RegEnum.sp, RegEnum.sp, pushoffset - 4);//再减4存ra
        genaddSwLwLa("sw", RegEnum.ra, "($sp)");

        //跳转
        genadd("jal Function_" + midCode.getIRstring(), true);

        //栈维护，提取ra
        genaddSwLwLa("lw", RegEnum.ra, "($sp)");
        genaddIm("addi", RegEnum.sp, RegEnum.sp, -(pushoffset - 4));


        //活跃寄存器全部提取
        for (RegEnum activeReg : activeRegList) {
            genaddSwLwLa("lw", activeReg, "($sp)");
            genaddIm("addi", RegEnum.sp, RegEnum.sp, 4);
        }

        //todo 这个不要了，push改成均不释放，到call完然后活跃寄存器按顺序出栈以后，放掉该放掉的
        for (RegEnum shoud_free : should_free_aft_push) {
            RegisterContrl.FreeReg(shoud_free);
        }
//        if(aftactiveoffset != preactiveoffset){
//            genaddIm("addi", RegEnum.sp, RegEnum.sp, preactiveoffset-aftactiveoffset);
//        }
    }


    /**
     * 函数返回值提取
     */
    //todo over
    public void genAssignRet(MidCode midCode) {
        saveRegToVarable(midCode.getVariable(), RegEnum.v0);
    }


    public void genReturn(MidCode midCode) {
        if (inmain) {
            //todo 有没有可能main函数没有Return？？？存疑
            genadd("#End Program", true);
            genadd("li $v0, 10", true);
            genadd("syscall", true);
        } else if (infuncdel) {
            if (midCode.isVoidreturn()) {

            } else {
                Var returnVar = midCode.getVariable();
                RegEnum reg = RegEnum.wrong;
                if (returnVar.getType().equals("array")) {
                    reg = getReg_kickouttmp_necessary(returnVar,false);
                    LwOrSW_between_Reg_Array("lw", reg, returnVar);
                } else {
                    reg = getVarReg(returnVar, true, false);
                }

                genadd("move", RegEnum.v0, reg);
                RegisterContrl.FreeReg(reg);
                //todo 理论上来讲getVarReg 是不是一定配一个Free？？？存疑 就是！
            }
            if (infuncoffset == 0) {
                genadd("#addi $sp, $sp, 0", true);
            } else {
                genadd("addi $sp, $sp, " + -infuncoffset, true);
            }
            genadd("jr $ra", true);
        }
    }

    //todo 没考虑数组
    public void genAssign2(MidCode midCode) {
        Var dest = midCode.getDest();
        Var right = midCode.getOper1();
        RegEnum rightReg = RegEnum.wrong;
        //先取出右边
        if (right.getType().equals("array")) {
            rightReg = getReg_kickouttmp_necessary(right,false);
            LwOrSW_between_Reg_Array("lw", rightReg, right);
        } else {
            rightReg = getVarReg(right, true, false);
        }


        if (dest.getType().equals("array")) {
            LwOrSW_between_Reg_Array("sw", rightReg, dest);
        } else {
            //MyError.errorat("assign2 "+ curfunc.getName(),495);
            //MyError.errorat(dest.getName()+" "+dest.getType()+" "+dest.getVar(),497);

            RegEnum destReg = saveRegToVarable(dest, rightReg);
        }
        //释放 rightreg一定释放，dest这路一定是symbol类的，一定释放，所以均释放掉
        assert rightReg != null;
        RegisterContrl.FreeReg(rightReg);

    }

    public RegEnum saveRegToVarable(Var dest, RegEnum rightReg) {  //var = right
        Symbol destsymbol = dest.getSymbol();
        //存到左边
        if (dest.getType().equals("var")) {
            //理论来讲左边一定是symbol类的VAriable
            if (!dest.isKindofsymbol()) {
                //todo 这个理论上来讲只能在assign return或者数组赋值出现，因为左值是一个临时变量
                RegEnum retreg = getVarReg(dest, false, false);
                genadd("move", retreg, rightReg);
                return retreg;
            } else {
                if (destsymbol.isGlobal()) {
                    //全局变量改Global
                    //add("sw $" + regForOper1 + ", Global_" + globalvarname);
                    genaddSwLwLa("sw", rightReg, "Global_" + destsymbol.getName());
                } else {
                    //局部变量sw存回stack
                    genaddSwLwLa("sw", rightReg, getSymbolAdressAtStack(destsymbol, 0));
                }
            }
        } else {
            MyError.errorat("Mipsgen", 362, "左值不可能不是tmp var");
        }
        return RegEnum.wrong;
    }

    //todo 没考虑函数
    public void genAssign(MidCode midCode) { // a = b + c
        Var dest = midCode.getDest();
        Var left = midCode.getOper1();
        Var right = midCode.getOper2();
        String op = midCode.getOperator();
        RegEnum leftreg = null, righntreg = null, destreg = null;
        if (left.getType().equals("array")) {
            leftreg = getReg_kickouttmp_necessary(left,false);
            LwOrSW_between_Reg_Array("lw", leftreg, left);
        } else {
            leftreg = getVarReg(left, true, false);
        }

        if (right.getType().equals("array")) {
            righntreg = getReg_kickouttmp_necessary(right,false);
            LwOrSW_between_Reg_Array("lw", righntreg, right);
        } else {
            righntreg = getVarReg(right, true, false);
        }


        //dest 在这一定是一个临时变量，因为这里是assign
        destreg = checkIfVariableHadReg_IFNotApply(dest,true);

        //取到然后计算
        switch (op) {
            case "+":
                genadd("addu", destreg, leftreg, righntreg);
                break;
            case "-":
                genadd("subu", destreg, leftreg, righntreg);
                break;
            case "*":
                genadd("mult", leftreg, righntreg);
                genadd("mflo", destreg);
                break;
            case "/":
                genadd("div", leftreg, righntreg);
                genadd("mflo", destreg);
                break;
            case "%":
                genadd("div", leftreg, righntreg);
                genadd("mfhi", destreg);
                break;
            case "bitand":
                genadd("and", destreg, leftreg, righntreg);
                break;
            default:
                MyError.errorat("Mipsgen", 398, "异常运算符");
                break;
        }
        //计算之后除了dest是临时变量都释放
        RegisterContrl.FreeReg(leftreg);
        RegisterContrl.FreeReg(righntreg);
        if (dest.isKindofsymbol()) {
            RegisterContrl.FreeReg(destreg);
        }
    }

    public void genarrayDecl(MidCode midCode) {
        if (midCode.isGlobal()) {
            data.addGLobalString(midCode);
        } else {
            int arroffset = midCode.getArraySize() * 4;
            Symbol symbol = midCode.getSymbol();
            //todo table 的 inblockoffset有些没有特别理解
            midCode.getScope().inblockoffset -= arroffset;

            if (infuncdel) {
                infuncoffset -= arroffset;
                symbol.setSpoffset(infuncoffset);
            } else {
                spoffset -= arroffset;      //记录sp指针偏移
                symbol.setSpoffset(spoffset);    ///记录相对sp的地址
            }
            genaddIm("addi", RegEnum.sp, RegEnum.sp, -arroffset);
            if (midCode.isInit()) {
                int offset = 0;
                for (int i = 0; i < midCode.getInitList().size(); i++) {
                    offset = i * 4;
                    //todo 这里没申请直接用t0存，可能有问题？但是没有想到，先试试，应该可以
                    genadd("li $t0, " + midCode.getInitList().get(i), true);
                    genaddSwLwLa("sw", RegEnum.t0, offset + "($sp)");
                }
            }
        }
    }

    public void genBranch(MidCode midCode) {
        Var left = midCode.getOper1();
        Var right = midCode.getOper2();
        String ltype = left.getType();
        String rtype = right.getType();
        String cmp = midCode.getInstr();
        String jlabel = midCode.getJumploc();

        RegEnum leftreg = getVarReg(left, true, true);
        RegEnum rightreg = getVarReg(right, true, true);
        if (ltype.equals("var") && rtype.equals("var")) {
            genaddBeq(cmp, leftreg, rightreg, jlabel);
        } else if (ltype.equals("var") && rtype.equals("num")) {
            genaddBeqi(cmp, leftreg, right.getNum(), jlabel);
        } else if (ltype.equals("num") && rtype.equals("var")) {
            //需要把条件反转一下，因为beq这种只能寄存器在前，数字在后
            String reversecmp = ConstValue.reverseCmpAndSet(cmp);
            genaddBeqi(reversecmp, rightreg, left.getNum(), jlabel);
        } else {
            //num - num 直接比较数字
            boolean jump = ConstValue.compareBeq(cmp, left.getNum(), right.getNum());
            if (jump) {
                genadd("j " + jlabel, true);
            }
        }
        if (!leftreg.equals(RegEnum.wrong)) {
            RegisterContrl.FreeReg(leftreg);
        }
        if (!rightreg.equals(RegEnum.wrong)) {
            RegisterContrl.FreeReg(rightreg);
        }
    }

    public void genSetCmp(MidCode midCode) {

        Var left = midCode.getOper1();
        Var right = midCode.getOper2();
        Var dest = midCode.getDest();
        String ltype = left.getType();
        String rtype = right.getType();
        String set = midCode.getOperator();


        RegEnum leftreg = getVarReg(left, true, false);
        RegEnum rightreg = getVarReg(right, true, false);
        RegEnum destreg = getVarReg(dest, false, false);

        if (ltype.equals("num") && rtype.equals("num")) {
            //num - num 直接比较数字
            boolean reljudge = ConstValue.compareBeq(set, left.getNum(), right.getNum());
            if (reljudge) {
                genadd("li $" + destreg + ", 1", true);
            } else {
                genadd("li $" + destreg + ", 0", true);
            }
        } else {
            genaddSlt(set, destreg, leftreg, rightreg);
        }


        RegisterContrl.FreeReg(leftreg);

        RegisterContrl.FreeReg(rightreg);

    }

    /**
     * 获取一个类型为Var的Variable的Reg，此处考虑了Var为符号表中（const，global，局部），临时，num的多种情况
     * musthave的意思是如果这里是一个临时变量，他是不是必须要分配过寄存器,musthave = false理论上只能在assign2左边是一个临时变量出现
     * <p>
     * neednum 是为了branch指令因为beq这些指令都可以直接用立即数，对于num类型var我们不妨直接返回数，就不存tmp了(这里其实是一个小优化，存tmp会冗余很多临时寄存器分配)
     * 但是其他的比如mult,这种必须要存tmp的我们还是存tmp
     */
    public RegEnum getVarReg(Var v, boolean musthave, boolean needPureNum) {
        RegEnum reg = RegEnum.wrong;
        if (v.getType().equals("var")) {
            if (v.isKindofsymbol()) { //在符号表里有，证明栈里肯定有
                Symbol symbol = v.getSymbol();
                if (symbol.getConstType().equals(ConstType.CONST)) {
                    reg = getReg_kickouttmp_necessary(v,false);
                    int num = symbol.getNum();
                    genadd("li $" + reg + ", " + num, true);
                } else if (v.getSymbol().isGlobal()) { //  全局 从Global里取
                    reg = lwSymbolFromGlobalLabel(v, null);
                } else { // 局部lw从stack里面取
                    reg = lwSymbolFromStackToReg(v, null, false,false);
                }
            } else { //符号表里面没有定义，是个临时tmp  todo 要是一个函数返回值怎么办，yasi
                reg = checkIfVariableHadReg_IFNotApply(v, musthave);
            }
        } else if (v.getType().equals("num")) {
            if (needPureNum) {
                return RegEnum.wrong; //返回一个wrong，数让父程序自己找
            }
            reg = getReg_kickouttmp_necessary(v,false);//todo 这应该是false吧
            genadd("li $" + reg + ", " + v.getNum(), true);
        } else {

            MyError.errorat("MipsGen", 774, v.getType() + " " + v.getName() + " " + "这次应该没有除了var,num之外的情况");
        }
        return reg;
    }

    /**
     * 该函数查看Variable有没有之前分配过Reg(如果分配过则返回已分配的Reg,否则新分配一个Reg)，对临时变量使用，加上musthave可以额外assert必须分配过，否则报错
     */
    public RegEnum checkIfVariableHadReg_IFNotApply(Var v, boolean musthave) {
        RegEnum reg = RegEnum.none;
        if (v.getCurReg() == RegEnum.wrong) { //没分配
            if(v.isHasbeenkickout()){
                //  被踢出去了，要重新取回来到reg里面
                reg = getReg_kickouttmp_necessary(v,true);
                lwSymbolFromStackToReg(v,reg,false,true);
                v.setCurReg(reg);
            }else{
                reg = getReg_kickouttmp_necessary(v,true);
                v.setCurReg(reg);
            }
            return reg;
        } else { // 已经分配
            return v.getCurReg();
        }
    }

    /**
     * mustneed 转用于对arraynum，和被kickout的tmp取值，因为他们不是iskindofsymbol，
     * 但是我们依然要利用这个函数给他们从栈取值，所以加了mustneed
     */
    public RegEnum lwSymbolFromStackToReg(Var var, RegEnum toReg, boolean arraymustneed,boolean tmpmustneed) {
        //计算地址，将stack中的变量取出
        Symbol s = var.getSymbol();
        if ((var.isKindofsymbol() || arraymustneed) && s != null) {
            String address = getSymbolAdressAtStack(s, 0);
            if (toReg == null) {
                RegEnum reg = getReg_kickouttmp_necessary(var,false);
                genadd("lw $" + reg + " ," + address, true);
                return reg;
            } else {
                genadd("lw $" + toReg + " ," + address, true);
                return toReg;
            }

        } else if(tmpmustneed){
            String tmpvarAddr = getTmpVarAdressAtStack(var);
            genadd("lw $" + toReg + " ," + tmpvarAddr, true);
            return toReg;
        }else {
            MyError.errorat("Mipsgen", 739, "symbol有问题，找不到");
        }
        return null;
    }

    public RegEnum lwSymbolFromGlobalLabel(Var var, RegEnum toReg) {
        Symbol s = var.getSymbol();

        if (s.isGlobal()) {
            if (toReg == null) {
                RegEnum reg = getReg_kickouttmp_necessary(var,false);
                genadd("lw $" + reg + ",  Global_" + s.getName(), true);
                return reg;
            } else {
                genadd("lw $" + toReg + ",  Global_" + s.getName(), true);
                return toReg;
            }

        } else {
            MyError.errorat("Mipsgen", 488, "不是全局变量");
        }
        return null;
    }


    public void genPrintf(MidCode m) {
        String type = m.getVariable().getType();
        Var printVar = m.getVariable();
        if (type.equals("var")) { //task1 只能是var或者str或者num 别宫
            //add("li $v0, 1");
            genadd("li $v0, 1", true);
            if (printVar.isKindofsymbol()) {
                Symbol printSymbol = printVar.getSymbol();
                if (printSymbol.getConstType().equals(ConstType.CONST)) {
                    //const 直接取
                    int num = printSymbol.getNum();
                    genadd("li $a0, " + num, true);
                } else if (printSymbol.isGlobal()) {
                    lwSymbolFromGlobalLabel(printVar, RegEnum.a0);
                } else {
                    lwSymbolFromStackToReg(printVar, RegEnum.a0, false,false);
                }
            } else {//临时变量，之前一定算过
                RegEnum hadreg = checkIfVariableHadReg_IFNotApply(printVar, true);
                genadd("move $a0, $" + hadreg, true);
                RegisterContrl.FreeReg(hadreg);//todo 新增
            }
            genadd("syscall", true);

        } else if (type.equals("str")) {//输出字符串
            genadd("li $v0, 4", true);
            String printfLabel = data.getPrintfLabelFromDataMapByMidcode(m);
            genadd("la $a0, " + printfLabel, true);
            genadd("syscall", true); //todo 之后把这些东西合并吧，这样实在容易写错；
        } else if (type.equals("num")) {//输出数字
            int num = m.getVariable().getNum();
            genadd("li $v0, 1", true);
            genadd("li $a0, " + num, true);
            genadd("syscall", true);
        } else {
            genadd("li $v0, 1", true);
            LwOrSW_between_Reg_Array("lw", RegEnum.a0, printVar);
            genadd("syscall", true);
        }
    }


    /**
     * 左边可能是变量var，也有可能是数组a[2][2] = getint()
     */
    public void genGetint(MidCode midCode) {
        String type = midCode.getVariable().getType();
        Var leftvariable = midCode.getVariable();
        //读取
        genadd("li $v0, 5", true);
        genadd("syscall", true);

        if (type.equals("var")) {
            saveRegToVarable(leftvariable, RegEnum.v0);
        } else if (type.equals("array")) {
            LwOrSW_between_Reg_Array("sw", RegEnum.v0, leftvariable);
        } else {
            MyError.errorat("MiipsGen", 674);
        }
    }


    /**
     * 存储访问数组一起封装，和saveRegToSymbol是一对，ToArray用作对数组访问，ToSYmbol主要用作对其他int常变量的访问，主要是二者的存取包括地址计算
     * 有差别，所以分开封装
     */
    public void LwOrSW_between_Reg_Array(String instrction, RegEnum reg, Var arraynum) {
        if (!arraynum.getType().equals("array")) {
            MyError.errorat("Mipsgen", 842, "这里必须传array var");
        }
        Symbol arraysymbol = arraynum.getSymbol();
        Var index = arraynum.getVar();
        String arrayname = arraynum.getName();

        //global 数组
        if (arraysymbol.isGlobal()) {
            if (index.getType().equals("num")) {
                //todo bug A3 全局数组la访问错误
                int offsetnum = index.getNum();
                if (instrction.equals("lw")) {
                    genaddSwLwLa("la", reg, "Global_" + arrayname);
                    genaddSwLwLa(instrction, reg, offsetnum * 4 + "($" + reg + ")");
                } else if (instrction.equals("sw")) {
                    RegEnum tmp = getReg_kickouttmp_necessary(arraynum,false); // todo 这块应该是true吧哈哈
                    genaddSwLwLa("la", tmp, "Global_" + arrayname);
                    genaddSwLwLa(instrction, reg, offsetnum * 4 + "($" + tmp + ")");
                    RegisterContrl.FreeReg(tmp);
                } else {
                    MyError.errorat("Mipsgen", 918, "没有sw，lw以外的");
                }


            } else if (index.getType().equals("var")) {
                RegEnum indexreg = getVarReg(index, true, false);
                genaddSll(indexreg, indexreg, 2);
                genaddSwLwLa(instrction, reg, "Global_" + arrayname + "($" + indexreg + ")");
                //不做优化时，申请必须释放
                RegisterContrl.FreeReg(indexreg);
            }
        } else {
            //下面计算两种情况，分别在函数内和main内，主要是地址计算有差别

            if (infuncdel) {
                //函数内
                ArrayList<Integer> res = curfunc.hasthisPara(arrayname);
                int isAParam = res.get(0);
                if (isAParam == 1) {
                    //是函数参数,函数参数中的函数需要取基地址，因为数组传参数传的是地址
                    //todo 这里如果不给arraynum设置iskindofsymbol 获得不了正确的基地址
                    //todo 到底要不要设置，还得看Visit那边有没有什么关系，待定，如果不设置，给lwSymbolFromStackToReg加个参数也行
                    RegEnum arraybasetmp = lwSymbolFromStackToReg(arraynum, null, true,false);
                    if (index.getType().equals("num")) {
                        int offset = index.getNum() * 4;
                        genaddSwLwLa(instrction, reg, offset + "($" + arraybasetmp + ")");

                    } else if (index.getType().equals("var")) {

                        RegEnum varindex_reg = getVarReg(index, true, false);
                        genaddSll(varindex_reg, varindex_reg, 2);
                        genadd("add", arraybasetmp, arraybasetmp, varindex_reg);
                        genaddSwLwLa(instrction, reg, "($" + arraybasetmp + ")");
                        RegisterContrl.FreeReg(varindex_reg);
                    } else {
                        MyError.errorat("Mipsgen", 732, "没有这种类型");
                    }
                    RegisterContrl.FreeReg(arraybasetmp);
                } else {
                    //是函数内部数组，内部数组不需要取基地址，直接算偏移然后用sp取
                    //todo 有问题
                    //int localbaseadr = Integer.parseInt(baseaddress);
                    if (index.getType().equals("num")) {
                        String arraddress = getSymbolAdressAtStack(arraysymbol, index.getNum() * 4);
                        genaddSwLwLa(instrction, reg, arraddress);
                    } else if (index.getType().equals("var")) {
                        RegEnum varindex_reg = getVarReg(index, true, false);
                        genaddSll(varindex_reg, varindex_reg, 2);
                        genadd("add $" + varindex_reg + ", $" + varindex_reg + ", $sp", true);
                        int arraybaseoffset = getSymbolOffsetInfunc(arraysymbol, 0);
                        genaddSwLwLa(instrction, reg, arraybaseoffset + "($" + varindex_reg + ")");
                        RegisterContrl.FreeReg(varindex_reg);

                    } else {
                        MyError.errorat("Mipsgen", 745, "没有这种类型");
                    }
                }
            } else {
                //main内
                if (index.getType().equals("num")) {
                    String arradress = getSymbolAdressAtStack(arraysymbol, index.getNum() * 4);
                    genaddSwLwLa(instrction, reg, arradress);
                } else if (index.getType().equals("var")) {
                    String arradress = getSymbolAdressAtStack(arraysymbol, 0);
                    RegEnum varindex_reg = getVarReg(index, true, false);
                    genaddSll(varindex_reg, varindex_reg, 2);
                    genaddSwLwLa(instrction, reg, arradress + "($" + varindex_reg + ")");
                    RegisterContrl.FreeReg(varindex_reg);

                } else {
                    MyError.errorat("Mipsgen", 762, "没有这种类型");
                }
            }
        }
    }


    /**
     * 如果临时变量寄存器满了，就踢出一个临时变量，把它存到栈里
     */
    public RegEnum getReg_kickouttmp_necessary(Var v,boolean istmp){
        RegEnum reg = RegisterContrl.Getreg(v,istmp);
        if(!reg.equals(RegEnum.regisempty)){
            return reg;
        }else {
            Var tmpvar = RegisterContrl.kickoutAtmp();
            RegEnum prereg = tmpvar.getCurReg();
            tmpvar.setCurReg(RegEnum.wrong);
            if(!tmpvar.isHasbeenkickout()){
                tmpvar.setHasbeenkickout(true);
                //store tmpvar to stack 相当于将tmp看成一个int局部变量执行intdecl
                Table scope = tmpvar.getScope();
                scope.inblockoffset -= 4;
                genadd("addi $sp,$sp,-4", true);
                if (infuncdel) { //函数内
                    infuncoffset -= 4;
                    tmpvar.setSpoffset(infuncoffset);
                } else { //Main内
                    spoffset -= 4;
                    tmpvar.setSpoffset(spoffset);
                }
                genadd("move",RegEnum.v1,prereg);
                genadd("sw $v1, 0($sp)", true);//直接存，别宫我啦，我牙刷儿
            }else {
                String tmpvaraddress = getTmpVarAdressAtStack(v);
                //genadd("sw $v1, 0($sp)", true);//直接存，别宫我啦，我牙刷儿
                genaddSwLwLa("sw",prereg,tmpvaraddress);
            }

            //
            //再次获取
            return RegisterContrl.Getreg(v,istmp);
        }
    }
    public void genadd(MipsString s) {
        mipsgen.add(s);
    }

    public void genadd(String s, boolean hastab) {
        mipsgen.add(new MipsSimpleString(s, hastab));
    }

    public void genadd(String type, RegEnum dest, RegEnum op1, RegEnum op2) {
        mipsgen.add(new MipsCal(type, dest, op1, op2));
    }

    public void genadd(String type, RegEnum op1, RegEnum op2) {
        mipsgen.add(new MipsCal(type, op1, op2));
    }

    public void genadd(String type, RegEnum dest) {
        mipsgen.add(new MipsCal(type, dest));
    }

    public void genaddSwLwLa(String type, RegEnum reg, String address) {
        genadd(new MipsSimpleString(type + " $" + reg + ", " + address, true));

    }

    public void genaddBeq(String type, RegEnum left, RegEnum right, String label) {
        genadd(new MipsSimpleString(type + " $" + left + ", " + " $" + right + ", " + label, true));
    }

    public void genaddBeqi(String type, RegEnum left, int right, String label) {
        genadd(new MipsSimpleString(type + " $" + left + ", " + right + ", " + label, true));
    }

    public void genaddSlt(String type, RegEnum dest, RegEnum left, RegEnum right) {
        genadd(new MipsSimpleString(type + " $" + dest + ", $" + left + ", $" + right, true));
    }

    public void genaddSll(RegEnum r1, RegEnum r2, int num) {
        genadd("sll $" + r1 + ", $" + r2 + ", " + num, true);
    }

    /**
     * 该函数既返回符号表中符号在栈中地址，可以返回0x77dd，或者offset($sp)两种形式
     */
    public String getSymbolAdressAtStack(Symbol symbol, int array_numoffset) {
        int offset = 0;
        if (infuncdel) {
            offset = getSymbolOffsetInfunc(symbol, array_numoffset);
            return offset + activevaroffset + "($sp)";
        } else {
            offset = ConstValue.topstack + symbol.getSpoffset() + array_numoffset;
            return "0x" + Integer.toHexString(offset);
        }
    }

    public String getTmpVarAdressAtStack(Var v) {
        int offset = 0;
        if (infuncdel) {
            offset = -infuncoffset + v.getSpoffset();
            return offset + activevaroffset + "($sp)";
        } else {
            offset = ConstValue.topstack + v.getSpoffset();
            return "0x" + Integer.toHexString(offset);
        }
    }
    public int getSymbolOffsetInfunc(Symbol symbol, int array_numoffset) {
        int offset = 0;
        ArrayList<Integer> res = curfunc.hasthisPara(symbol.getName());
        int isAParam = res.get(0);
        if (isAParam == 1) {
            //是个参数
            int paraoder = res.get(1);
            offset = -infuncoffset + 4 + (curfunc.getPamaNum() - paraoder) * 4;
        } else {
            //是函数体内定义
            offset = -infuncoffset + symbol.getSpoffset();
        }
        offset += array_numoffset;
        return offset;
    }


    /**
     * 添加立即数指令
     */
    public void genaddIm(String type, RegEnum reg1, RegEnum reg2, int num) {
        genadd(type + " $" + reg1 + ", $" + reg2 + ", " + num, true);
    }

    public void genaddImHex(String type, RegEnum reg1, RegEnum reg2, String hexnum) {
        genadd(type + " $" + reg1 + ", $" + reg2 + ", " + hexnum, true);
    }


}
