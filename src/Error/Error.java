/**
 * @description：TODO
 * @author ：szy
 * @date ：2022/10/2 7:49 PM
 */
package Error;

import Lexer.Word;

public class Error implements Comparable<Error>{
    private Word word;
    private String errortype;

    public Error(Word word, String errortype) {
        this.word = word;
        this.errortype = errortype;
    }

    public String toString(){
        return  word.getLine() +" "+errortype;
    }

    public Word getWord() {
        return word;
    }

    public String toDebugString(){
        return  word.getLine() +" "+ word.getValue()+" "+errortype+" "+ErrorChecker.debug.get(errortype);
    }

    @Override
    public int compareTo(Error o) {
        return this.getWord().getLine() - o.getWord().getLine();
    }
}
