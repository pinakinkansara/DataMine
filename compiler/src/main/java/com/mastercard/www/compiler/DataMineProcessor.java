package com.mastercard.www.compiler;

import com.mastercard.www.library.DataMine;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Created by e064173 on 10/10/2017.
 */
@SupportedAnnotationTypes({"com.mastercard.www.library.DataMine"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class DataMineProcessor extends AbstractProcessor{

    private boolean processingOver = false;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if(!ProcessorUtil.isInitialized()){
            ProcessorUtil.init(processingEnv);
        }

        if(!processAnnotation(roundEnvironment)){
            return processingOver;
        }

        if(roundEnvironment.processingOver()){


        }
        return processingOver;
    }

    private boolean processAnnotation(RoundEnvironment roundEnvironment) {
       Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(DataMine.class);

       if(elements == null || elements.isEmpty()){
           return true;
       }

       Element element1 = null;
       for(Element element : elements){
           if(element.getKind() != ElementKind.CLASS){
                ProcessorUtil.logError("Class should be annotated with @Validator.");
                return false;
           }
           element1 = element;
       }
        try {
            generateValidatorClass(element1);
            processingOver = true;
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,e.toString());
        }
       return true;
    }

    private void generateValidatorClass(Element element) throws IOException {
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .build();

        TypeSpec testClass = TypeSpec.classBuilder("TestClass")
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(element.asType()))
                .addMethod(constructor).build();

        ProcessorUtil.generateFile(testClass,"com.test.masti");
    }
}
