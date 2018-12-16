package com.sci.bfc.ir;

import java.util.ArrayList;
import java.util.List;

public final class Parser {
    public static List<Instruction> parse(final String code) {
        final List<Instruction> result = new ArrayList<>();

        for(final char c : code.toCharArray()) {
            switch(c) {
                case '+':
                    result.add(new Adjust(0, 1));
                    break;
                case '-':
                    result.add(new Adjust(0, -1));
                    break;
                case '>':
                    result.add(new Select(1));
                    break;
                case '<':
                    result.add(new Select(-1));
                    break;
                case ',':
                    result.add(new Read(0));
                    break;
                case '.':
                    result.add(new Write(0));
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