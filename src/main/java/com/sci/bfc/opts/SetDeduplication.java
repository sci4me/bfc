package com.sci.bfc.opts;

import com.sci.bfc.ir.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SetDeduplication implements Optimization {
    public static final SetDeduplication INSTANCE = new SetDeduplication();

    private SetDeduplication() {
    }

    @Override
    public List<Instruction> optimize(final List<Instruction> ir) {
        final List<Instruction> result = new ArrayList<>();

        final Map<Integer, Integer> tape = new HashMap<>();
        int dp = 0;

        final Runnable dump = () -> {
            for(final Map.Entry<Integer, Integer> entries : tape.entrySet()) {
                result.add(new Set(entries.getKey(), entries.getValue()));
            }
            tape.clear();
        };

        int index = 0;
        while(index < ir.size()) {
            final Instruction insn = ir.get(index);

            if(insn instanceof Set) {
                tape.put(dp + insn.baseOffset(), ((Set) insn).value);
            } else if(insn instanceof Select) {
                dp += ((Select) insn).delta;
            } else {
                if(insn instanceof Adjust || insn instanceof Write || insn instanceof Open ||
                        insn instanceof Close || insn instanceof ScanLeft || insn instanceof ScanRight ||
                        insn instanceof MAdd) {
                    dump.run();
                }

                if(dp != 0) {
                    result.add(new Select(dp));
                    dp = 0;
                }

                result.add(insn);
            }

            index++;
        }

        dump.run();
        if(dp != 0) result.add(new Select(dp));

        return result;
    }
}