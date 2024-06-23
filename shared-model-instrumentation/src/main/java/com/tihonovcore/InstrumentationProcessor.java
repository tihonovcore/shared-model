package com.tihonovcore;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.objectweb.asm.*;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;

public class InstrumentationProcessor extends ClassVisitor {

    private static final String FIELD_NAME = "_unusedFields";
    private static final int FLAGS = ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;

    private InstrumentationProcessor(ClassWriter writer) {
        super(ASM9, writer);
    }

    public static byte[] instrument(byte[] bytes) {
        var reader = new ClassReader(bytes);
        var writer = new ClassWriter(reader, FLAGS);

        var visitor = new InstrumentationProcessor(writer);
        reader.accept(visitor, 0);

        return writer.toByteArray();
    }

    private String className;
    private boolean wasAnnotated;

    @Override
    public void visit(
        int version, int access, String name, String signature,
        String superName, String[] interfaces
    ) {
        className = name;
        wasAnnotated = false;

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (Type.getDescriptor(SharedModel.class).equals(descriptor)) {
            wasAnnotated = true;

            return null;
        }

        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public void visitEnd() {
        if (!wasAnnotated) {
            return;
        }

        var fv = cv.visitField(ACC_PRIVATE, FIELD_NAME, Type.getDescriptor(Map.class), null, null);
        fv.visitAnnotation(Type.getDescriptor(JsonIgnore.class), true);

        var getDescriptor = Type.getMethodDescriptor(Type.getType(Map.class));
        var getVisitor = cv.visitMethod(ACC_PUBLIC, "_anyGet", getDescriptor, null, null);
        addLazyInitialization(getVisitor);

        getVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        getVisitor.visitFieldInsn(GETFIELD, className, FIELD_NAME, Type.getDescriptor(Map.class));
        getVisitor.visitInsn(ARETURN);
        getVisitor.visitMaxs(3, 1);
        getVisitor.visitAnnotation(Type.getDescriptor(JsonAnyGetter.class), true);
        getVisitor.visitEnd();

        var setDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class), Type.getType(Object.class));
        var setVisitor = cv.visitMethod(ACC_PUBLIC, "_anySet", setDescriptor, null, null);
        addLazyInitialization(setVisitor);

        setVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        setVisitor.visitFieldInsn(GETFIELD, className, FIELD_NAME, Type.getDescriptor(Map.class));
        setVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        setVisitor.visitVarInsn(Opcodes.ALOAD, 2);
        var putDescriptor = Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class), Type.getType(Object.class));
        setVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", putDescriptor, true);
        setVisitor.visitInsn(POP);
        setVisitor.visitInsn(RETURN);
        setVisitor.visitMaxs(3, 3);
        setVisitor.visitAnnotation(Type.getDescriptor(JsonAnySetter.class), true);
        setVisitor.visitEnd();

        super.visitEnd();
    }

    public void addLazyInitialization(MethodVisitor methodVisitor) {
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, className, FIELD_NAME, Type.getDescriptor(Map.class));
        var label = new Label();
        methodVisitor.visitJumpInsn(IFNONNULL, label);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitTypeInsn(NEW, "java/util/HashMap");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        methodVisitor.visitFieldInsn(PUTFIELD, className, FIELD_NAME, Type.getDescriptor(Map.class));
        methodVisitor.visitLabel(label);
    }
}
