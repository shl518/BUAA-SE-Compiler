/**
 * @description：TODO
 * @author ：szy
 * @date ：2022/10/27 3:34 PM
 */
package CONST;

public class ConstValue {
    public static final int topstack = 0x7fffeffc;

    public static String reverseCmpAndSet(String cmp) {
        switch (cmp) {
            case "bge":
                return "blt";
            case "ble":
                return "bgt";
            case "bgt":
                return "ble";
            case "blt":
                return "bge";
            case "sge":
                return "slt";
            case "sgt":
                return "sle";
            case "sle":
                return "sgt";
            case "slt":
                return "sge";
            default:
                return cmp;
        }
    }

    public static boolean compareBeq(String cmp, int num1, int num2) {
        boolean jump = false;
        boolean isbeq = false;
        switch (cmp) {
            case "beq":
                isbeq = true;
                if (num1 == num2) {
                    jump = true;
                }
                break;
            case "bne":
                isbeq = true;
                if (num1 != num2) {
                    jump = true;
                }
                break;
            case "bge":
                isbeq = true;
                if (num1 >= num2) {
                    jump = true;
                }
                break;
            case "ble":
                isbeq = true;
                if (num1 <= num2) {
                    jump = true;
                }
                break;
            case "bgt":
                isbeq = true;
                if (num1 > num2) {
                    jump = true;
                }
                break;
            case "blt":
                isbeq = true;
                if (num1 < num2) {
                    jump = true;
                }
                break;
            default:
                break;

        }
        if(isbeq){
            return jump;
        }

        boolean reljudge = false;
        switch (cmp) {
            case "sge":
                if (num1 >= num2) {
                    reljudge = true;
                }
                break;
            case "sgt":
                if (num1 > num2) {
                    reljudge = true;
                }
                break;
            case "sle":
                if (num1 <= num2) {
                    reljudge = true;
                }
                break;
            case "slt":
                if (num1 < num2) {
                    reljudge = true;
                }
                break;
            case "seq":
                if (num1 == num2) {
                    reljudge = true;
                }
                break;
            case "sne": //暂无用
                if (num1 != num2) {
                    reljudge = true;
                }
                break;
            default:
                break;
        }
        return reljudge;
    }

}
