package Lexer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private String source;
    private int line;
    private int pointer = 0;//词法解析的时候指向字符级别

    private final HashMap<String, Type> word2code;
    public static ArrayList<Word> words = new ArrayList<>();
    public static Word sym = null;
    public static int index = 0;//指向解析好的word，语法解析的时候用于向前看向后看
    private final String[] category_code_token = new String[]{
            "IDENFR", "INTCON", "STRCON", "MAINTK",
            "CONSTTK", "INTTK", "BREAKTK", "CONTINUETK", "IFTK",
            "ELSETK", "NOT", "AND", "OR", "WHILETK", "GETINTTK",
            "PRINTFTK", "RETURNTK", "PLUS", "MINU", "VOIDTK",
            "MULT", "DIV", "MOD", "LSS", "LEQ", "GRE", "GEQ", "EQL",
            "NEQ", "ASSIGN", "SEMICN", "COMMA", "LPARENT",
            "RPARENT", "LBRACK", "RBRACK", "LBRACE", "RBRACE", "BITAND"
    };

    public Lexer(String source) {
        this.source = source;
        this.line = 1;
        word2code = new HashMap<String, Type>();
        init_word2code_map();
    }

    public void init_word2code_map() {
        word2code.put("main", Type.MAINTK);
        word2code.put("const", Type.CONSTTK);
        word2code.put("int", Type.INTTK);
        word2code.put("break", Type.BREAKTK);
        word2code.put("continue", Type.CONTINUETK);
        word2code.put("if", Type.IFTK);
        word2code.put("else", Type.ELSETK);
        word2code.put("while", Type.WHILETK);
        word2code.put("getint", Type.GETINTTK);
        word2code.put("printf", Type.PRINTFTK);
        word2code.put("return", Type.RETURNTK);
        word2code.put("bitand", Type.BITAND);
        word2code.put("void", Type.VOIDTK);
        word2code.put("!", Type.NOT);
        word2code.put("!=", Type.NEQ);
        word2code.put("<", Type.LSS);
        word2code.put(">", Type.GRE);
        word2code.put(">=", Type.GEQ);
        word2code.put("<=", Type.LEQ);
        word2code.put("==", Type.EQL);
        word2code.put("=", Type.ASSIGN);
        word2code.put("&&", Type.AND);
        word2code.put("||", Type.OR);
        word2code.put("+", Type.PLUS);
        word2code.put("-", Type.MINU);
        word2code.put("*", Type.MULT);
        word2code.put("/", Type.DIV);
        word2code.put("%", Type.MOD);
        word2code.put(";", Type.SEMICN);
        word2code.put(",", Type.COMMA);
        word2code.put("(", Type.LPARENT);
        word2code.put(")", Type.RPARENT);
        word2code.put("[", Type.LBRACK);
        word2code.put("]", Type.RBRACK);
        word2code.put("{", Type.LBRACE);
        word2code.put("}", Type.RBRACE);
    }

    public boolean isSymbol(char c) {
        String s = "!&|+-*/%<>=;,()[]{}";
        return s.indexOf(c) != -1;
    }

    public char fgetc() {
        char c = source.charAt(pointer);
        pointer++;
        return c;
    }

    public void ungetc() {
        pointer--;
    }

    public void setc(int atindex) { //只有一个作用换/为e
        String str = source;
        StringBuilder strBuilder = new StringBuilder(str);
        strBuilder.setCharAt(atindex, 'e');
        str = strBuilder.toString();
        source = str;
    }

    public boolean peekWord() {
        char c;
        StringBuilder sb = new StringBuilder();
        boolean hadGet = false;
        while (!hadGet) {
            c = fgetc();
            while (Character.isWhitespace(c)) {
                if (c == '\n') {
                    line += 1;
                }
                c = fgetc();
            }
            if (c == '~') { //END
                return false;
            } else if (Character.isLetter(c) || c == '_') {
                do {
                    sb.append(c);
                    c = fgetc();
                } while (Character.isLetterOrDigit(c) || c == '_');
                ungetc();
                if (!word2code.containsKey(sb.toString())) {
                    Word w = new Word(Type.IDENFR, sb.toString(), line);
                    words.add(w);
                } else {
                    Word w = new Word(word2code.get(sb.toString()), sb.toString(), line);
                    words.add(w);
                }
                hadGet = true;
            } else if (c == '"') {
                do {
                    sb.append(c);
                    c = fgetc();
                } while (c != '"');
                sb.append(c);
                Word w = new Word(Type.STRCON, sb.toString(), line);
                words.add(w);
                hadGet = true;
            } else if (Character.isDigit(c)) {
                do {
                    sb.append(c);
                    c = fgetc();
                } while (Character.isDigit(c));
                ungetc();
                words.add(new Word(Type.INTCON, sb.toString(), line));
                hadGet = true;
            } else if (isSymbol(c)) {
                if (c == '/') {
                    c = fgetc();
                    if (c == '/') {
                        while (fgetc() != '\n') ;
                        ungetc();
                        continue;
                    } else if (c == '*') {
                        c = fgetc();
                        if (c == '/') {
                            setc(pointer - 1);// /*/三连，把/换掉e，排除这种情况
                        }
                        char pre, aft = c;
                        do {
                            pre = aft;
                            aft = fgetc();
                            if (aft == '\n') {
                                line += 1;
                            }
                        } while (!(pre == '*' && aft == '/'));

                        continue;
                    } else {
                        ungetc();
                    }
                    words.add(new Word(Type.DIV, "/", line));
                    hadGet = true;
                } else if (c == '&') {
                    c = fgetc();
                    words.add(new Word(Type.AND, "&&", line));
                    hadGet = true;
                } else if (c == '|') {
                    c = fgetc();
                    words.add(new Word(Type.OR, "||", line));
                    hadGet = true;
                } else if (c == '<' || c == '>' || c == '=' || c == '!') {
                    sb.append(c);
                    c = fgetc();
                    if (c == '=') {
                        sb.append(c);
                        words.add(new Word(word2code.get(sb.toString()), sb.toString(), line));
                        hadGet = true;
                    } else {
                        ungetc();
                        words.add(new Word(word2code.get(sb.toString()), sb.toString(), line));
                        hadGet = true;
                    }
                } else {
                    sb.append(c);
                    words.add(new Word(word2code.get(sb.toString()), sb.toString(), line));
                    hadGet = true;
                }
            }
        }
        return true;
    }

    public void analyzeWords() {
        while (peekWord()) ;
        //preDeal1();
    }

    public void writeWords() throws IOException {
        File file = new File("output.txt");
        FileWriter writer = new FileWriter(file);
        for (Word w : words) {
            Type c = w.getKind();
            String value = w.getValue();
//            System.out.println(value);
//            System.out.println("and");
//            System.out.println(c.ordinal());
            String sb = category_code_token[c.ordinal()] +
                    ' ' +
                    value + ' ' + w.getLine() +
                    '\n';
            writer.write(sb);
        }
        writer.flush();
        writer.close();
    }

    public static boolean wordKindIs(int offset, Type type) {
        //offset = 0,默认为当前位置sym的判断
        if (index + offset > words.size()) {
            return false;
        }
        Word t = words.get(index + offset);
        return t.getKind().equals(type);
    }

    public static boolean symValueIs(String value) {
        return sym.getValue().equals(value);
    }


    public static String getNextSym() {
        //方法将sym向下调整，并返回调整之前的sym用于调用者放到list里面
        String presym = sym.toStr();
        if (index < words.size() - 1) {
            index += 1;
            sym = words.get(index);
        }
        return presym;
    }

    public static boolean hasassign() {   //查找分号前有无等号
        int offset = 1;
        int curline = words.get(index).getLine();
        while (index + offset < words.size()) {
            Word newsym = words.get(index + offset);
            if (newsym.getValue().equals(";") || newsym.getLine() > curline) {
                break;
            } else if (newsym.getValue().equals("=")) {
                return true;
            }
            offset += 1;
        }
        return false;
    }

    public static boolean hasaddadd() {
        int offset = 1;
        int curline = words.get(index).getLine();
        while (index + offset + 1 < words.size()) {
            Word nsym = words.get(index + offset);
            Word nnsym = words.get(index + offset + 1);
            if (nsym.getValue().equals("+") && nnsym.getValue().equals("+")
                    && nsym.getLine() == curline && nnsym.getLine() == curline) {
                return true;
            } else if (nsym.getLine() > curline || nnsym.getLine() > curline) {
                break;
            }
        }
        return false;
    }

    public static Word getLastToken() {    //获取上一个 符
        return words.get(index - 1);
    }

    public static void preDeal1() {
        int rec = 0;
        int recline = -1;
        String recname = "";
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).getValue().equals("int")) {
                if (i + 2 < words.size()) {
                    if (words.get(i + 2).getValue().equals("=")) {
                        if (words.get(i + 3).getValue().equals("getint")) {
                            recline = words.get(i).getLine();
                            rec = i;
                            recname = words.get(i + 1).getValue();
                        }
                    }
                }
            }
        }
        if (recline != -1) {
            ArrayList<Word> newWords = new ArrayList<>();
            for (int i = 0; i <= rec + 1; i++) {
                newWords.add(words.get(i));
            }
            newWords.add(new Word(Type.SEMICN, ";", recline));
            newWords.add(new Word(Type.IDENFR, recname, recline + 1));
            newWords.add(new Word(Type.ASSIGN, "=", recline + 1));
            newWords.add(new Word(Type.GETINTTK, "getint", recline + 1));
            newWords.add(new Word(Type.LPARENT, "(", recline + 1));
            newWords.add(new Word(Type.RPARENT, ")", recline + 1));
            newWords.add(new Word(Type.SEMICN, ";", recline + 1));
            for (int i = rec + 7; i < words.size(); i++) {
                Word w = words.get(i);
                w.setLine(w.getLine() + 1);
                newWords.add(w);
            }
            words = newWords;
        }
    }

    public static void preDeal2(int recline, int index,String recname) {
        int rec = -1;
        for (int i = index; i < words.size(); i++) {
            if (words.get(i).getValue().equals(";")) {
                rec = i;
                break;
            }
        }
        ArrayList<Word> newWords = new ArrayList<>();
        for (int i = 0; i <= rec; i++) {
            newWords.add(words.get(i));
        }
        newWords.add(new Word(Type.IDENFR, recname, recline + 1));
        newWords.add(new Word(Type.ASSIGN, "=", recline + 1));
        newWords.add(new Word(Type.GETINTTK, "getint", recline + 1));
        newWords.add(new Word(Type.LPARENT, "(", recline + 1));
        newWords.add(new Word(Type.RPARENT, ")", recline + 1));
        newWords.add(new Word(Type.SEMICN, ";", recline + 1));
        for (int i = rec + 1; i < words.size(); i++) {
            Word w = words.get(i);
            w.setLine(w.getLine() + 1);
            newWords.add(w);
        }
        words = newWords;
        for (Word w:words){
            System.out.println(w.getValue());
        }
        System.out.println("here");
    }

    public Word getnestindex() {
        return words.get(index + 1);
    }
}
