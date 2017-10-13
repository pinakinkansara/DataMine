package com.mastercard.www.compiler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Created by e064173 on 10/11/2017.
 */

public class ProcessorUtil {
    private static ProcessingEnvironment processingEnvironment;

    private ProcessorUtil() {
        // Empty private constructor
    }

    public static void init(ProcessingEnvironment environment) {
        processingEnvironment = environment;
    }

    public static boolean isInitialized(){
        return processingEnvironment != null;
    }

    public static void logError(String message) {
        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

    public static void logWarning(String message) {
        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
    }

    public static void generateFile(final TypeSpec typeSpec, String packageName) throws IOException {
        JavaFile.builder(packageName, typeSpec)
                .build()
                .writeTo(processingEnvironment.getFiler());
    }

    public static boolean isSerializable(TypeMirror typeMirror) {
        final TypeMirror serializable = processingEnvironment.getElementUtils()
                .getTypeElement("java.io.Serializable").asType();
        return processingEnvironment.getTypeUtils().isAssignable(typeMirror, serializable);
    }

    public static boolean isParcelable(TypeMirror typeMirror) {
        final TypeMirror parcelable = processingEnvironment.getElementUtils()
                .getTypeElement("android.os.Parcelable").asType();
        return processingEnvironment.getTypeUtils().isAssignable(typeMirror, parcelable);
    }

    public static boolean isArray(TypeMirror typeMirror) {
        final TypeMirror array = processingEnvironment.getElementUtils()
                .getTypeElement("").asType();
        return false;
    }
}
