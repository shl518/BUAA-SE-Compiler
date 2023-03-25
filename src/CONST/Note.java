/**
 * @description：TODO
 * @author ：szy
 * @date ：2022/10/25 10:45 PM
 */
package CONST;

import java.util.HashMap;
import java.util.HashSet;

public class Note {
    public static HashMap<NoteType,String> notes;
    static {
        notes = new HashMap<>();

        notes.put(NoteType.StartPrint,"#Start Print");
        notes.put(NoteType.StartFuncDel,"#Start FuncDecl");
        notes.put(NoteType.StartDecl,"#Start Decl");
        notes.put(NoteType.StartMainFunc,"#Start MainFunc");
        notes.put(NoteType.OutBlock,"#Out Block");
        notes.put(NoteType.OutBlockWHileCut,"#Out Block WhileCut");
        notes.put(NoteType.endafunc,"#end a func");

    }
    public static String get(NoteType t){
        return notes.get(t);
    }
}
