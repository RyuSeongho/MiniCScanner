import java.io.*;

public class Scanner {

    private boolean isEof = false;
    private char ch = ' ';
    private BufferedReader input;
    private String line = "";
    private int lineno = 0;
    private int col = 1;
    private final String letters = "abcdefghijklmnopqrstuvwxyz"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String digits = "0123456789";
    private final char eolnCh = '\n';
    private final char eofCh = '\004';


    public Scanner (String fileName) { // source filename
        System.out.println("Begin scanning... programs/" + fileName + "\n");
        try {
            input = new BufferedReader (new FileReader(fileName));
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
            System.exit(1);
        }
    }

    private char nextChar() { // Return next char
        if (ch == eofCh)
            error("Attempt to read past end of file");
        col++;
        if (col >= line.length()) {
            try {
                line = input.readLine( );
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            } // try
            if (line == null) // at end of file
                line = "" + eofCh;
            else {
                // System.out.println(lineno + ":\t" + line);
                lineno++;
                line += eolnCh;
            } // if line
            col = 0;
        } // if col
        return line.charAt(col);
    }

    private String getExponentialPart() {
        String exponentialPart = "" + ch;
        ch = nextChar();
        if (ch == '+' || ch == '-' || isDigit(ch)){
            exponentialPart += ch;
            ch = nextChar();
            exponentialPart += concat(digits);
            return exponentialPart;
        }
        error("Illegal character, expecting +, -, digits");
        return ""; //error occurred
    }

    private String getFractionalPart() {
        String fractionalPart = "." + concat(digits);
        if (ch == 'e' || ch == 'E') { // has exponential expression
            String exponentialPart = getExponentialPart();
            if(exponentialPart.isEmpty())  // error occurred
                return "";
            return fractionalPart + exponentialPart;
        } else {
            return fractionalPart;
        }
    }


    public Token next( ) { // Return next token
        do {
            if (isLetter(ch) || ch == '_') { // identifier or keyword
                String spelling = concat(letters + digits + '_');
                return Token.keyword(spelling);
            } else if (isDigit(ch)) { // int literal or double literal
                String integerPart = concat(digits);
                if (ch == '.') { // double literal
                    ch = nextChar();
                    String fractionalPart = getFractionalPart();
                    if(!fractionalPart.isEmpty())  // error not occurred
                        return Token.mkDoubleLiteral(integerPart + fractionalPart);
                } else {
                    return Token.mkIntLiteral(integerPart);
                }
            } else if (ch == '.') { // double literal short form
                ch = nextChar();
                String fractionalPart = getFractionalPart();
                if(!fractionalPart.isEmpty())  // error not occurred
                    return Token.mkDoubleLiteral(fractionalPart);
            } else if (ch == '\'') {
                ch = nextChar();
                String charLiteral = "" + ch;
                if(ch == '\\') {
                    ch = nextChar();
                    charLiteral += ch;
                }
                check('\'');
                return Token.mkCharLiteral("'" + charLiteral + "'");
            } else if (ch == '\"') {
                ch = nextChar();
                String stringLiteral = "\"";
                while (ch != '\"') {
                    if(ch == '\\') {
                        stringLiteral += ch;
                        ch = nextChar();
                        stringLiteral += ch;
                        ch = nextChar();
                        continue;
                    }
                    stringLiteral += ch;
                    ch = nextChar();
                }
                stringLiteral += "\"";
                ch = nextChar();
                return Token.mkStringLiteral(stringLiteral);
            } else switch (ch) {
                case ' ': case '\t': case '\r': case eolnCh:
                    ch = nextChar();
                    break;

                case '/':  // divide or divAssign or comment
                    ch = nextChar();
                    if (ch == '=')  { // divAssign
                        ch = nextChar();
                        return Token.divAssignTok;
                    }

                    // divide
                    if (ch != '*' && ch != '/') return Token.divideTok;

                    // multi line comment
                    if (ch == '*') {
                        do {
                            while (ch != '*') ch = nextChar();
                            ch = nextChar();
                        } while (ch != '/');
                        ch = nextChar();
                    }
                    // single line comment
                    else if (ch == '/')  {
                        do {
                            ch = nextChar();
                        } while (ch != eolnCh);
                        ch = nextChar();
                    }

                    break;
            /*
            case '\'':  // char literal
                char ch1 = nextChar();
                nextChar(); // get '
                ch = nextChar();
                return Token.mkCharLiteral("" + ch1);
            */
                case eofCh: return Token.eofTok;

                case '+':
                    ch = nextChar();
                    if (ch == '=')  { // addAssign
                        ch = nextChar();
                        return Token.addAssignTok;
                    }
                    else if (ch == '+')  { // increment
                        ch = nextChar();
                        return Token.incrementTok;
                    }
                    return Token.plusTok;

                case '-':
                    ch = nextChar();
                    if (ch == '=')  { // subAssign
                        ch = nextChar();
                        return Token.subAssignTok;
                    }
                    else if (ch == '-')  { // decrement
                        ch = nextChar();
                        return Token.decrementTok;
                    }
                    return Token.minusTok;

                case '*':
                    ch = nextChar();
                    if (ch == '=')  { // multAssign
                        ch = nextChar();
                        return Token.multAssignTok;
                    }
                    return Token.multiplyTok;

                case '%':
                    ch = nextChar();
                    if (ch == '=')  { // remAssign
                        ch = nextChar();
                        return Token.remAssignTok;
                    }
                    return Token.reminderTok;

                case '(': ch = nextChar();
                    return Token.leftParenTok;

                case ')': ch = nextChar();
                    return Token.rightParenTok;

                case '[': ch = nextChar();
                    return Token.leftBracketTok;

                case ']': ch = nextChar();
                    return Token.rightBracketTok;

                case '{': ch = nextChar();
                    return Token.leftBraceTok;

                case '}': ch = nextChar();
                    return Token.rightBraceTok;

                case ':': ch = nextChar();
                    return Token.colonTok;

                case ';': ch = nextChar();
                    return Token.semicolonTok;

                case ',': ch = nextChar();
                    return Token.commaTok;

                case '&': check('&'); return Token.andTok;
                case '|': check('|'); return Token.orTok;

                case '=':
                    return chkOpt('=', Token.assignTok,
                            Token.eqeqTok);

                case '<':
                    return chkOpt('=', Token.ltTok,
                            Token.lteqTok);
                case '>':
                    return chkOpt('=', Token.gtTok,
                            Token.gteqTok);
                case '!':
                    return chkOpt('=', Token.notTok,
                            Token.noteqTok);

                default:  error("Illegal character " + ch);
            } // switch
        } while (true);
    } // next


    private boolean isLetter(char c) {
        return (c>='a' && c<='z' || c>='A' && c<='Z');
    }

    private boolean isDigit(char c) {
        return (c>='0' && c<='9');
    }

    private void check(char c) {
        ch = nextChar();
        if (ch != c)
            error("Illegal character, expecting " + c);
        ch = nextChar();
    }

    private Token chkOpt(char c, Token one, Token two) {
        ch = nextChar();
        if (ch != c)
            return one;
        ch = nextChar();
        return two;
    }

    private String concat(String set) {
        String r = "";
        while (set.indexOf(ch) >= 0){
            r += ch;
            ch = nextChar();
        }
        return r;
    }

    public void error (String msg) {
        System.out.print("Error: " + line);
        System.out.println("Error: column " + col + " " + msg);
        ch = nextChar();
        //System.exit(1);
    }

    static public void main ( String[] argv ) {
        Scanner lexer = new Scanner(argv[0]);
        Token tok = lexer.next( );
        while (tok != Token.eofTok) {
            System.out.println(tok.toString());
            tok = lexer.next( );
        }
    } // main
}