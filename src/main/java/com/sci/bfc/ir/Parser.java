package com.sci.bfc.ir;

import java.util.ArrayList;
import java.util.List;

public final class Parser {
    public static List<Instruction> parse(final String code) {
        final List<Instruction> result = new ArrayList<>();

        for(final char c : code.toCharArray()) {
            switch(c) {
                case '+':
                    result.add(new Adjust(1));
                    break;
                case '-':
                    result.add(new Adjust(-1));
                    break;
                case '>':
                    result.add(new Select(1));
                    break;
                case '<':
                    result.add(new Select(-1));
                    break;
                case ',':
                    result.add(new Read());
                    break;
                case '.':
                    result.add(new Write());
                    break;
                case '[':
                    result.add(new Open());
                    break;
                case ']':
                    result.add(new Close());
                    break;
            }
        }

        return result;
    }

    private Parser() {
    }
}