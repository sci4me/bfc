package com.sci.bfc.opts;

import com.sci.bfc.ir.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class OffsetOptimization implements Optimization {
    private static boolean counts(final Instruction insn) {
        if(insn instanceof Adjust) return insn.baseOffset() == 0;
        else if(insn instanceof Select) return true;
        else if(insn instanceof Read) return insn.baseOffset() == 0;
        else if(insn instanceof Write) return insn.baseOffset() == 0;
        else if(insn instanceof Set) return insn.baseOffset() == 0;
        else return false;
    }

    private static int match(final List<Instruction> ir, final int index) {
        if(!OffsetOptimization.counts(ir.get(index))) return 0;

        int count = 0;

        int j = index;
        while(j < ir.size() && OffsetOptimization.counts(ir.get(j))) {
            count++;
            j++;
        }

        return count;
    }

    public static final OffsetOptimization INSTANCE = new OffsetOptimization();

    private OffsetOptimization() {
    }

    @Override
    public List<Instruction> optimize(final List<Instruction> ir) {
        final List<Instruction> result = new ArrayList<>();

        int index = 0;
        while(index < ir.size()) {
            final int run = OffsetOptimization.match(ir, index);
            if(run > 1) {
                final int end = index + run;

                final Map<Integer, Integer> deltas = new HashMap<>();
                final BiConsumer<Integer, Integer> adjust = (offset, delta) -> {
                    if(deltas.containsKey(offset)) {
                        deltas.put(offset, deltas.get(offset) + delta);
                    } else {
                        deltas.put(offset, delta);
                    }
                };


                final Runnable dump = () -> {
                    for(final Map.Entry<Integer, Integer> entry : deltas.entrySet()) {
                        result.add(new Adjust(entry.getKey(), entry.getValue()));
                    }
                    deltas.clear();
                };

                int dp = 0;
                while(index < end) {
                    final Instruction insn = ir.get(index);
                    index++;

                    if(insn instanceof Adjust) {
                        adjust.accept(dp, ((Adjust) insn).delta);
                    } else if(insn instanceof Select) {
                        dp += ((Select) insn).delta;
                    } else if(insn instanceof Set) {
                        result.add(new Set(dp, ((Set) insn).value));
                    } else {
                        dump.run();
                        if(dp != 0) result.add(new Select(dp));
                        dp = 0;

                        if(insn instanceof Read) {
                            result.add(new Read(dp));
                        } else if(insn instanceof Write) {
                            result.add(new Write(dp));
                        } else {
                            throw new RuntimeException();
                        }
                    }
                }

                dump.run();
                if(dp != 0) result.add(new Select(dp));
            } else {
                result.add(ir.get(index));
                index++;
            }
        }

        return result;
    }
}