package com.sci.bfc.util.jit;

import com.sci.bfc.ir.*;
import com.sci.bfc.util.Stack;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

import java.util.List;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

public final class JIT {
    private static final String CLASS_NAME_PREFIX = "JittedBFCode$";
    private static int counter;

    private final List<Instruction> ir;
    private final String className;
    private final ClassWriter cw;

    public JIT(final List<Instruction> ir) {
        this.ir = ir;
        this.className = JIT.CLASS_NAME_PREFIX + JIT.counter++;
        this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    }

    public Program compile(final List<Integer> stdin, final int tapeSize) {
        this.cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, this.className, null, Type.getInternalName(Program.class), new String[]{});
        this.cw.visitSource(null, null);

        this.generateConstructor();
        this.generateRun();

        try {
            final byte[] clazzBytes = this.cw.toByteArray();
            final Class<?> clazz = JITClassLoader.INSTANCE.loadClass(this.className, clazzBytes);
            return (Program) clazz.getConstructor(List.class, int.class).newInstance(stdin, tapeSize);
        } catch(final Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private void generateConstructor() {
        final String signature = String.format("(L%s;I)V", Type.getInternalName(List.class));

        final MethodVisitor mv = this.cw.visitMethod(ACC_PUBLIC, "<init>", signature, null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Program.class), "<init>", signature, false);
        mv.visitInsn(RETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateRun() {
        MethodVisitor mv = this.cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
        mv.visitCode();

        final LoopHolder loopHolder = new LoopHolder();
        final Stack<MethodVisitor> methods = new Stack<>();

        int methodIndex = 0;
        for(final Instruction insn : this.ir) {
            if(insn instanceof Open) {
                methods.push(mv);

                this.generateInstruction(mv, insn, loopHolder);

                methodIndex++;

                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, this.className, "m" + methodIndex, "()V", false);

                mv = cw.visitMethod(ACC_PRIVATE, "m" + methodIndex, "()V", null, null);
                mv.visitCode();
            } else if(insn instanceof Close) {
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();

                mv = methods.pop();

                this.generateInstruction(mv, insn, loopHolder);
            } else {
                this.generateInstruction(mv, insn, loopHolder);
            }
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateAdjust(final MethodVisitor mv, final int delta) {
        if(delta > 0) {
            mv.visitLdcInsn(delta);
            mv.visitInsn(IADD);
        } else {
            mv.visitLdcInsn(-delta);
            mv.visitInsn(ISUB);
        }
    }

    private void generateWrap(final MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Program.class), "wrap", "(I)I", false);
    }

    private void generateInstruction(final MethodVisitor mv, final Instruction insn, final LoopHolder loopHolder) {
        if(insn instanceof Adjust) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "tape", "[I");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "dp", "I");
            mv.visitInsn(DUP2);
            mv.visitInsn(IALOAD);
            this.generateAdjust(mv, ((Adjust) insn).delta);
            this.generateWrap(mv);
            mv.visitInsn(IASTORE);
        } else if(insn instanceof Select) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETFIELD, this.className, "dp", "I");
            this.generateAdjust(mv, ((Select) insn).delta);
            mv.visitFieldInsn(PUTFIELD, this.className, "dp", "I");
        } else if(insn instanceof Read) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, this.className, "read", "()V", false);
        } else if(insn instanceof Write) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, this.className, "write", "()V", false);
        } else if(insn instanceof Open) {
            if(loopHolder.loop != null) {
                loopHolder.loops.push(loopHolder.loop);
            }

            final Loop loop = new Loop();
            loop.start = new Label();
            loop.end = new Label();
            loopHolder.loop = loop;

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "tape", "[I");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "dp", "I");
            mv.visitInsn(IALOAD);
            mv.visitJumpInsn(IFEQ, loop.end);
            mv.visitLabel(loop.start);
        } else if(insn instanceof Close) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "tape", "[I");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "dp", "I");
            mv.visitInsn(IALOAD);
            mv.visitJumpInsn(IFNE, loopHolder.loop.start);
            mv.visitLabel(loopHolder.loop.end);

            if(!loopHolder.loops.isEmpty()) {
                loopHolder.loop = loopHolder.loops.pop();
            }
        } else if(insn instanceof Set) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "tape", "[I");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "dp", "I");
            mv.visitLdcInsn(((Set) insn).value);
            this.generateWrap(mv);
            mv.visitInsn(IASTORE);
        } else if(insn instanceof Mul) {
            final Label skip = new Label();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "tape", "[I");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "dp", "I");
            mv.visitInsn(IALOAD);
            mv.visitJumpInsn(IFEQ, skip);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "tape", "[I");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "dp", "I");
            mv.visitLdcInsn(((Mul) insn).offset);
            mv.visitInsn(IADD);

            mv.visitInsn(DUP2);

            mv.visitInsn(IALOAD);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "tape", "[I");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.className, "dp", "I");
            mv.visitInsn(IALOAD);

            mv.visitLdcInsn(((Mul) insn).factor);
            mv.visitInsn(IMUL);
            this.generateWrap(mv);
            mv.visitInsn(IADD);
            this.generateWrap(mv);

            mv.visitInsn(IASTORE);
            mv.visitLabel(skip);
        } else if(insn instanceof ScanLeft) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, this.className, "scanLeft", "()V", false);
        } else if(insn instanceof ScanRight) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, this.className, "scanRight", "()V", false);
        } else {
            throw new RuntimeException(insn.getClass().toString());
        }
    }

    private static class LoopHolder {
        public Stack<Loop> loops;
        public Loop loop;

        public LoopHolder() {
            this.loops = new Stack<>();
        }
    }

    private static class Loop {
        public Label start;
        public Label end;
    }
}