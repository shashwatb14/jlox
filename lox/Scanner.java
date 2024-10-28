package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;

class Scanner {

    // variables used
    private static final int TAB_SPACES = 4;

    // raw source code stored as string
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 0;

    Scanner(String source) {
        this.source = source;
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
                } else {
                    addToken(SLASH);
                }
                break;


            // ignore whitespace
            case ' ': column++; break;
            case '\r': break;
            case '\t': column += TAB_SPACES; break;

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
                } else {
                    Lox.error(line, column, "Unexpected character.");
                }
                break;
        }
    }

    public void number() {
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
            Lox.error(line, column, "Unterminated string.");
            return;
        }

        // closing "
        advance();

        // trim surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value)
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

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

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

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line, column));
    }
}