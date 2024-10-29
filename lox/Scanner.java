package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;

class Scanner {

    // map for reserved keywords
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    // variables used
    private static final int TAB_SPACES = 4;

    // raw source code stored as string
    private final String source;
    private final String[] lines;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 0;

    Scanner(String source) {
        this.source = source;
        this.lines = source.split("\n");
    }

    // traverses source code, adding tokens until no more characters left
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // at beginning of next lexeme
            start = current;
            scanToken();
        }

        // adds "end of file" token at the end after completing scanning
        tokens.add(new Token(EOF, "", null, line, column));
        return tokens;
    }

    // recognize lexemes
    private void scanToken() {
        char c = advance();

        // for single characters
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '%': addToken(PERCENT); break;

            // need to look at second character as well
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            
            // '/' used for division and comments
            case '/':
                if (match('/')) {
                    // comments go until end of line
                    // no addToken() called so parser doesn't deal with them
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    // block comments
                    comment();
                } else {
                    addToken(SLASH);
                }
                break;


            // ignore whitespace
            case ' ': break;
            case '\r': break;
            case '\t': column += TAB_SPACES - 1; break;

            case '\n':
                line++;
                column = 0;
                break;
            
            // literals
            case '"': string(); break;
            
            // report error for unexpected characters
            // still consumed by advance() to avoid infinite loop
            // scanning continues to detect as many errors as possible in one go
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, column, "Unexpected character.", 
                        lines[line - 1]);
                }
                break;
        }
    }

    // check for identifiers/reserved keywords
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    // for number literals
    private void number() {
        while (isDigit(peek())) advance();

        // for fraction
        if (peek() == '.' && isDigit(peekNext())) {
            // consume '.'
            advance();

            while(isDigit(peek())) advance();
        }

        // all number literals are double
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));

    }

    // to parse strings - TODO ESCAPE SEQUENCES
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 0;
            }
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, column, "Unterminated string.", lines[line - 1]);
            return;
        }

        // closing "
        advance();

        // trim surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    // to parse nested block comments
    private void comment() {
        int depth = 1;
        while (!isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 0;
            } else if ((peek() == '/' && peekNext() == '*')) {
                depth++;
                advance();
            } else if ((peek() == '*' && peekNext() == '/')) {
                depth--;
                if (depth == 0) break;
                advance(); // advance after breaking to avoid consuming extra
            }

            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, column, "Unterminated comment.", lines[line - 1]);
            return;
        }

        // closing "*/"
        advance(); advance();
    }

    // checks next expected character
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        column++;
        return true;
    }

    // helper method that doesn't consume character - lookahead
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // looks two characters ahead
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    // helper methods for identifier
    private boolean isAlpha(char c) {
        return (c >= 'A' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    // if either character or digit
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // helper method for number literals detection
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // indicate if all characters have been consumed
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // helper methods
    private char advance() {
        column++;
        return source.charAt(current++);
    }

    // for non-literals
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // add token to list
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line, column));
    }
}