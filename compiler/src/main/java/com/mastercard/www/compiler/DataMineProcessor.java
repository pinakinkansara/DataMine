package com.mastercard.www.compiler;

import com.mastercard.www.library.DataMine;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

@SupportedAnnotationTypes({"com.mastercard.www.library.DataMine"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class DataMineProcessor extends AbstractProcessor{

    private static final ClassName contextClass= ClassName.get("android.content","Context");
    private static final String CLASS_NAME_PREFERENCE_REPOSITORY = "PreferenceRepository";
    private static final String PACKAGE_NAME = "com.datamine.www";


    private List<MethodSpec> mInstanceMethodSpecs = new ArrayList<>();
    private List<FieldSpec> mFieldSpecs = new ArrayList<>();

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
            try {
                createPreferenceRepository();
                processingOver = true;
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
            }
        }
        return processingOver;
    }

    private boolean processAnnotation(RoundEnvironment roundEnvironment) {
       Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(DataMine.class);

       if(elements == null || elements.isEmpty()){
           ProcessorUtil.logWarning("No Annotation found with @DataMine");
           return true;
       }

       for(Element element : elements){
           if(element.getKind() != ElementKind.CLASS){
                ProcessorUtil.logError("Class should be annotated with @DataMine.");
                return false;
           }
       }

       if(!addField()){
           return false;
       }

       if(!generatePreferenceRepositoryConstructor()){
           return false;
       }
       return true;
    }

    private boolean addField(){
        FieldSpec.Builder context = FieldSpec.builder(contextClass,"mContext",Modifier.PRIVATE);
        mFieldSpecs.add(context.build());
        return true;
    }
    /**
     * Creates class of PreferenceRepository
     * @throws IOException
     */
    private void createPreferenceRepository() throws IOException {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(CLASS_NAME_PREFERENCE_REPOSITORY);
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addField(contextClass,"mContext",Modifier.PRIVATE);

        //Add fields to the class
        for(FieldSpec spec : mFieldSpecs){
            classBuilder.addField(spec);
        }

        //Adding constructor to the class
        for(MethodSpec methodSpec : mInstanceMethodSpecs){
            classBuilder.addMethod(methodSpec);
        }

        //Add methods to the class

        //writing file
        ProcessorUtil.generateFile(classBuilder.build(), PACKAGE_NAME);
    }

    /**
     * Creates constructor of PreferenceRepository
     * @return
     */
    private boolean generatePreferenceRepositoryConstructor() {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
        constructor.addModifiers(Modifier.PUBLIC);
        constructor.addParameter(contextClass,"context");
        constructor.addCode("mContext = context;" +
                "\n");
        mInstanceMethodSpecs.add(constructor.build());
        return true;
    }
}
