package com.tihonovcore;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.objectweb.asm.*;

import java.io.IOException;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class InstrumentationProcessor extends ClassVisitor {

    private static final int flags = ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;

    private InstrumentationProcessor(ClassWriter writer) {
        super(ASM9, writer);
    }

    public static byte[] instrument(byte[] bytes) throws IOException {
        var reader = new ClassReader(bytes);
        var writer = new ClassWriter(reader, flags);

        var visitor = new InstrumentationProcessor(writer);
        reader.accept(visitor, 0);

        return writer.toByteArray();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (Type.getDescriptor(SharedModel.class).equals(descriptor)) {
            return null;
        }

        return super.visitAnnotation(descriptor, visible);
    }

    private String className;

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        className = name;
    }

    //TODO: поверки что таких методов и полей нет

    @Override
    public void visitEnd() {
        FieldVisitor fv = cv.visitField(
            ACC_PRIVATE, "unusedFields", Type.getDescriptor(Map.class), null, null);
        fv.visitAnnotation(Type.getDescriptor(JsonIgnore.class), true);

        var getD = Type.getMethodDescriptor(Type.getType(Map.class));
        var getVisitor = cv.visitMethod(ACC_PUBLIC, "anyGet", getD, null, null);

        getVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        getVisitor.visitFieldInsn(GETFIELD, className, "unusedFields", Type.getDescriptor(Map.class));
        var l1 = new Label();
        getVisitor.visitJumpInsn(IFNONNULL, l1);
        getVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        getVisitor.visitTypeInsn(NEW, "java/util/HashMap");
        getVisitor.visitInsn(DUP);
        getVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        getVisitor.visitFieldInsn(PUTFIELD, className, "unusedFields", Type.getDescriptor(Map.class));
        getVisitor.visitLabel(l1);

        getVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        getVisitor.visitFieldInsn(GETFIELD, className, "unusedFields", Type.getDescriptor(Map.class));
        getVisitor.visitInsn(ARETURN);
        getVisitor.visitMaxs(3, 1);
        getVisitor.visitAnnotation(Type.getDescriptor(JsonAnyGetter.class), true);
        getVisitor.visitEnd();


        var setD = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class), Type.getType(Object.class));
        var putD = Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class), Type.getType(Object.class));
        var setVisitor = cv.visitMethod(ACC_PUBLIC, "anySet", setD, null, null);

        setVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        setVisitor.visitFieldInsn(GETFIELD, className, "unusedFields", Type.getDescriptor(Map.class));
        var l2 = new Label();
        setVisitor.visitJumpInsn(IFNONNULL, l2);
        setVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        setVisitor.visitTypeInsn(NEW, "java/util/HashMap");
        setVisitor.visitInsn(DUP);
        setVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        setVisitor.visitFieldInsn(PUTFIELD, className, "unusedFields", Type.getDescriptor(Map.class));
        setVisitor.visitLabel(l2);

        setVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        setVisitor.visitFieldInsn(GETFIELD, className, "unusedFields", Type.getDescriptor(Map.class));
        setVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        setVisitor.visitVarInsn(Opcodes.ALOAD, 2);
        setVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", putD, true);
        setVisitor.visitInsn(POP);
        setVisitor.visitInsn(RETURN);
        setVisitor.visitMaxs(3, 3);
        setVisitor.visitAnnotation(Type.getDescriptor(JsonAnySetter.class), true);
        setVisitor.visitEnd();

        super.visitEnd();
    }
}
