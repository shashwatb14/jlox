package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;

class Scanner {

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

            // report error for unexpected characters
            // still consumed by advance() to avoid infinite loop
            // scanning continues to detect as many errors as possible in one go
            default:
                Lox.error(line, column, "Unexpected character.");
                break;
        }
    }

    // indicate if all characters have been consumed
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // helper methods
    private char advance() {
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