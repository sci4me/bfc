package com.sci.bfc;

import com.sci.bfc.ir.Instruction;
import com.sci.bfc.opts.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Optimizer {
    private final int maxPasses;
    private final boolean debug;
    private final List<Optimization> opts;

    public Optimizer(final boolean debug) {
        this(10, debug);
    }

    public Optimizer(final int maxPasses, final boolean debug) {
        this.maxPasses = maxPasses;
        this.debug = debug;
        this.opts = new ArrayList<>();
    }

    public void addStandardPasses() {
        this.addPass(Contraction.INSTANCE);
        this.addPass(ClearLoopRemoval.INSTANCE);
        this.addPass(ScanLoopOptimization.INSTANCE);
        this.addPass(MultiLoopOptimization.INSTANCE);
        this.addPass(AdjustSetOptimization.INSTANCE);
        this.addPass(SetAdjustOptimization.INSTANCE);
        this.addPass(SetDeduplication.INSTANCE);
        this.addPass(NullInstructionRemoval.INSTANCE);
        this.addPass(ReadClobberOptimization.INSTANCE);
        this.addPass(OffsetOptimization.INSTANCE);
    }

    public void addPass(final Optimization opt) {
        this.opts.add(opt);
    }

    public List<Instruction> optimize(final List<Instruction> ir) {
        if(this.opts.isEmpty()) return ir;

        List<Instruction> current = ir;
        int lastSize;

        final StringBuilder sb = new StringBuilder();
        sb.append("Optimizing:\n");

        int pass = 0;
        do {
            pass++;
            lastSize = current.size();

            final StringBuilder passSB = new StringBuilder();
            passSB.append("  ");
            passSB.append(pass);
            passSB.append(".\n");

            for(final Optimization opt : this.opts) {
                int prevSize = current.size();

                current = opt.optimize(Collections.unmodifiableList(current));

                final int diff = prevSize - current.size();
                if(diff > 0)
                    passSB.append(String.format("    - %s removed %d instructions\n", opt.getClass().getSimpleName(), diff));
            }

            if(current.size() != lastSize) sb.append(passSB.toString());
        } while(current.size() != lastSize && pass < this.maxPasses);

        sb.append(String.format("Optimizations removed a total of %d instructions (%d -> %d)\n\n", ir.size() - current.size(), ir.size(), current.size()));

        if(this.debug) System.out.println(sb.toString());

        return current;
    }
}