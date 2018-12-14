package com.sci.bfc;

import com.sci.bfc.ir.Instruction;
import com.sci.bfc.ir.Parser;
import com.sci.bfc.opts.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;

public final class CLI {
    private static void usage() {
        System.err.println("Usage: bfc <file>");
        System.exit(1);
    }

    public static void main(final String[] args) {
        if(args.length != 1) {
            CLI.usage();
            return;
        }

        final File file = new File(args[0]);
        if(!file.exists()) {
            System.err.println("File not found: '" + args[0] + "'");
            System.exit(1);
            return;
        }

        try {
            final String code = new String(Files.readAllBytes(file.toPath()));
            final List<Instruction> ir = Parser.parse(code);

            final Optimizer optimizer = new Optimizer(true);
            optimizer.addPass(Contraction.INSTANCE);
            optimizer.addPass(ClearLoopRemoval.INSTANCE);
            optimizer.addPass(ClearAdjustOptimization.INSTANCE);
            optimizer.addPass(AdjustSetOptimization.INSTANCE);
            optimizer.addPass(SetDeduplication.INSTANCE);
            optimizer.addPass(NullInstructionRemoval.INSTANCE);

            final List<Instruction> optimizedIR = optimizer.optimize(ir);

            final CCodeGenerator compiler = new CCodeGenerator(optimizedIR, 30000);

            try (final PrintWriter out = new PrintWriter(new File(file.getParentFile(), file.getName() + ".c"))) {
                out.print(compiler.compile());
            }
        } catch(IOException e) {
            System.err.println("Error reading file:");
            e.printStackTrace();
        }
    }

    private CLI() {
    }
}