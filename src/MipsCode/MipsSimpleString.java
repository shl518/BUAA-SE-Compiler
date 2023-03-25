/**
 * @description：TODO
 * @author ：szy
 * @date ：2022/10/26 3:46 PM
 */
package MipsCode;

import java.util.HashMap;
import java.util.HashSet;

public class MipsSimpleString extends MipsString{
    private String note;
    public MipsSimpleString(String note,boolean hasTab) {
        if(hasTab){
            this.note = tab+note;
        }else {
            this.note = note;
        }

    }

    @Override
    public StringBuilder toMipsString(){
        StringBuilder sb = new StringBuilder().append(note).append("\n");
        return sb;
    };
}
