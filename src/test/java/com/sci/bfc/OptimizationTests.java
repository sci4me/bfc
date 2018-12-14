package com.sci.bfc;

import com.sci.bfc.ir.Instruction;
import com.sci.bfc.ir.Parser;
import com.sci.bfc.opts.*;
import com.sci.bfc.util.IRRunner;
import com.sci.bfc.util.TestUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class OptimizationTests {
    private RegressionTest read(final String file) throws IOException {
        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(OptimizationTests.class.getResourceAsStream(file)))) {
            final String code = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            return new RegressionTest(file.substring(1, file.length() - 3), Parser.parse(code));
        }
    }

    private IRRunner run(final List<Instruction> ir, final int tapeSize, final List<Integer> stdin) {
        final IRRunner runner = new IRRunner(ir, tapeSize, stdin);
        runner.run();
        return runner;
    }

    private static class RegressionTest {
        public final String name;
        public final List<Instruction> ir;

        public RegressionTest(final String name, final List<Instruction> ir) {
            this.name = name;
            this.ir = Collections.unmodifiableList(ir);
        }
    }

    @Test
    public void optimizationsPreserveFunctionality() throws IOException {
        final int tapeSize = 30000;

        final List<RegressionTest> tests = Arrays.asList(
                this.read("/hw.bf"),
                this.read("/squares.bf"),
                this.read("/mandelbrot.bf")
        );
        for(final RegressionTest test : tests) {
            System.out.printf("Testing '%s':\n", test.name);

            final IRRunner control = this.run(test.ir, tapeSize, new ArrayList<>());

            final List<Optimization> opts = Arrays.asList(
                    Contraction.INSTANCE,
                    ClearLoopRemoval.INSTANCE,
                    ClearAdjustOptimization.INSTANCE,
                    SetDeduplication.INSTANCE,
                    NullInstructionRemoval.INSTANCE
            );

            final List<List<Optimization>> permutations = TestUtils.generatePermutations(new ArrayList<>(opts));
            for(final List<Optimization> perm : permutations) {
                System.out.println("  Running with:");
                for(final Optimization opt : perm) {
                    System.out.printf("    - %s\n", opt.getClass().getSimpleName());
                }

                final Optimizer optimizer = new Optimizer(false);
                perm.forEach(optimizer::addPass);
                final List<Instruction> optimized = optimizer.optimize(test.ir);
                final IRRunner run = this.run(optimized, tapeSize, new ArrayList<>());

                if(!Arrays.equals(run.getTape(), control.getTape()))
                    throw new RuntimeException("Tape mismatch");

                if(!run.getOutput().equals(control.getOutput()))
                    throw new RuntimeException("Output mismatch");

                System.out.println("  SUCCESS\n");
            }
        }
    }
}