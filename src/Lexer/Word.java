package Lexer;

public class Word {
    private Type kind;
    private int line;
    private String value;

    public Word(Type kind, String s,int line) {
        this.kind = kind;
        this.value = s;
        this.line = line;
    }

    public Type getKind() {
        return kind;
    }

    public String getValue() {
        return value;
    }


    public int getLine() {
        return line;
    }

    public String toStr(){
        return getKind().toString()+' '+getValue();
    }

    public void setLine(int line) {
        this.line = line;
    }
}
