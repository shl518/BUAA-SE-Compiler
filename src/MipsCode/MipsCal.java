/**
 * @description：TODO
 * @author ：szy
 * @date ：2022/10/27 7:04 PM
 */
package MipsCode;

import MipsGenerate.RegEnum;

public class MipsCal extends MipsString{
    private String type =  "";//addu subu mul div
    private RegEnum dest = RegEnum.wrong;
    private RegEnum op1 = RegEnum.wrong;
    private RegEnum op2 = RegEnum.wrong;

    public MipsCal(String type, RegEnum dest, RegEnum op1, RegEnum op2) {
        this.type = type;
        this.dest = dest;
        this.op1 = op1;
        this.op2 = op2;
    }

    public MipsCal(String type, RegEnum dest) {
        this.type = type;
        this.dest = dest;
    }

    public MipsCal(String type, RegEnum op1, RegEnum op2) {
        this.type = type;
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public StringBuilder toMipsString(){
        StringBuilder sb = new StringBuilder();

        if(op1.equals(RegEnum.wrong) && op2.equals(RegEnum.wrong) ){
            //只有dest mflo $s1
            sb.append(tab).append(type).append(" $").append(dest);

        }else if (dest.equals(RegEnum.wrong)){
            //add("mult $" + op1reg + ", $" + op2reg);
            sb.append(tab).append(type).append(" $").append(op1).append(" ,$").append(op2);
        }else {
            //addu $s1, $s2, $t0
            sb.append(tab).append(type).append(" $").append(dest)
                    .append(" ,$").append(op1).append(" ,$").append(op2);
        }
        sb.append("\n");
        return sb;
    }
}
