/**
 * @description：TODO
 * @author ：szy
 * @date ：2022/10/26 2:54 PM
 */
package MipsCode;

import CONST.MyError;
import MipsGenerate.MidCode;
import MipsGenerate.MidType;

import java.util.ArrayList;
import java.util.HashMap;

public class Data extends MipsString{

    private HashMap<MidCode,String> printfStringMap = new HashMap<>();//midcode print1_str1
    private ArrayList<MidCode> globalInitArr = new ArrayList<>();



    @Override
    public StringBuilder toMipsString() {
        StringBuilder ans = new StringBuilder(".data\n");
        for (MidCode midCode : printfStringMap.keySet()) {
            ans.append(tab).append(printfStringMap.get(midCode)).append(":")
                    .append(" .asciiz").append(tab).append("\"")
                    .append(midCode.getVariable().getName()).append("\"").append("\n");
        }
        for (MidCode midCode : globalInitArr) {
            ans.append(tab).append("Global_")
                    .append(midCode.getName()).append(": ").append(".word ");
            //初始化数组
            if(midCode.getType().equals(MidType.intDecl)){
                if(midCode.isInit()){
                    ans.append(midCode.getNum());
                }else {
                    ans.append("0");
                }
            }else{
                if (midCode.isInit()){
                    for (Integer num : midCode.getInitList()) {
                        ans.append(num).append(",");
                    }
                    ans.deleteCharAt(ans.length()-1);//去掉最后一个逗号
                }else {
                    int size = midCode.getArraySize();
                    ans.append("0:").append(size);
                }
            }
            ans.append("\n");
        }
        return  ans;
    }
    public void addPrintfLabel(MidCode m ,String printflabel){
        printfStringMap.put(m,printflabel);
    }

    public void addGLobalString(MidCode m){
        globalInitArr.add(m);
    }

    public String getPrintfLabelFromDataMapByMidcode(MidCode midCode){
        String s = printfStringMap.getOrDefault(midCode,null);
        if(s == null){
            MyError.errorat("Data",64,"没找到怎么可能，已经存了");
        }
        return s;
    }
}
