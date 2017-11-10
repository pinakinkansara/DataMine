package com.datamine.www.compiler;

import com.datamine.www.library.DataKey;
import com.datamine.www.library.DataMine;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({"com.datamine.www.library.DataMine",
"com.datamine.www.library.DataKey"})
public class DataMineProcessor extends AbstractProcessor{

    private static final ClassName contextClass= ClassName.get("android.content","Context");
    private static final ClassName stringClass= ClassName.get("java.lang","String");
    private static final ClassName sharePreferenceClass= ClassName.get("android.content","SharedPreferences");
    private static final String CLASS_NAME_PREFERENCE_REPOSITORY = "PreferenceRepository";
    private static final String PACKAGE_NAME = "com.datamine.www.aprocessor";


    private List<MethodSpec> mInstanceMethodSpecs = new ArrayList<>();
    private List<MethodSpec> mMethodSpecs = new ArrayList<>();
    private List<FieldSpec> mFieldSpecs = new ArrayList<>();

    private boolean processingOver = false;
    private int round = -1;


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        round++;

        if(round == 0){
            ProcessorUtil.init(processingEnv);
        }
        ProcessorUtil.logWarning("Processing round : "+round);

        if(!processAnnotation(roundEnvironment)){
            return processingOver;
        }

        if(roundEnvironment.processingOver()){
            try {
                if(!generatePreferenceRepositoryConstructor()){
                    return false;
                }

                if(!addField()){
                    return false;
                }

                if(!generateMethods()){
                    return false;
                }
                createPreferenceRepository();
                processingOver = true;
                ProcessorUtil.logWarning("Preference Repository generated :-)");
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

       ProcessorUtil.logWarning("Annotated Class Count :- "+elements.size());
       for(Element element : elements){
           ProcessorUtil.logWarning("Class Name : "+element.getSimpleName());
           if(element.getKind() != ElementKind.CLASS){
                ProcessorUtil.logError("Class should be annotated with @DataMine.");
                return false;
           }

           if(!processClassFields((TypeElement) element)){
               return false;
           }
       }
       return true;
    }

    /**
     * Process fields declared within class
     * @param element
     * @return
     */
    public boolean processClassFields(TypeElement element){
        ProcessorUtil.logWarning("Getting element for class : "+element.getSimpleName());
        List<? extends Element> fields = element.getEnclosedElements();
        if(fields == null || fields.isEmpty() || fields.size()<=1){
            ProcessorUtil.logWarning("No Class Field declared.");
            return true;
        }
        ProcessorUtil.logWarning("Class Field Count :- "+fields.size());
        for(Element field : fields){
            final DataKey dataKey = field.getAnnotation(DataKey.class);
            if(dataKey != null){
                ProcessorUtil.logWarning("Field name "+field.getSimpleName());
                ProcessorUtil.logWarning("Field type "+field.asType().getKind().name());
                if(dataKey.key().length() == 0){
                    ProcessorUtil.logError("key must not be null "+field.getSimpleName()
                    +" "+element.getSimpleName());
                    return false;
                }

                //PUT METHOD GENERATION
                MethodSpec.Builder putFieldMethod = MethodSpec.methodBuilder("put"+field.getSimpleName().toString());
                putFieldMethod.addModifiers(Modifier.PUBLIC);
                if(field.asType().getKind().name().equalsIgnoreCase("int")){
                    ProcessorUtil.logWarning("Generating put method for int type");
                    putFieldMethod.addParameter(int.class,field.getSimpleName().toString());
                    putFieldMethod.addStatement("putInt($S,$L)",dataKey.key(),field.getSimpleName().toString());
                }
                mInstanceMethodSpecs.add(putFieldMethod.build());

                //GET METHOD GENERATION
                MethodSpec.Builder getFieldMethod = MethodSpec.methodBuilder("get"+field.getSimpleName().toString());
                getFieldMethod.addModifiers(Modifier.PUBLIC);
                getFieldMethod.returns(ClassName.get(field.asType()));
                if(field.asType().getKind().name().equalsIgnoreCase("int")){
                    ProcessorUtil.logWarning("Generating get method for int type");
                    getFieldMethod.addParameter(int.class,"defaultValue");
                    getFieldMethod.addStatement("return getInt($S,$L)",dataKey.key(),"defaultValue");
                }
                mInstanceMethodSpecs.add(getFieldMethod.build());
            }

        }
        return true;
    }

    /**
     * Declare method for generated class
     * @return
     */
    private boolean generateMethods() {
        //GET SHARE PREFERENCES
        MethodSpec.Builder getSharedPreferences = MethodSpec.methodBuilder("getSharedPreferences");
        getSharedPreferences.addModifiers(Modifier.PRIVATE);
        getSharedPreferences.returns(sharePreferenceClass);
        getSharedPreferences.addStatement("return $N.getSharedPreferences($N,$L)","mContext","mFileName",0);
        mMethodSpecs.add(getSharedPreferences.build());


        //PUT STRING METHOD
        MethodSpec.Builder putStringMethod = MethodSpec.methodBuilder("putString");
        putStringMethod.addModifiers(Modifier.PRIVATE);
        putStringMethod.addParameter(stringClass,"key");
        putStringMethod.addParameter(stringClass,"value");
        putStringMethod.addStatement("$T sharePreference = getSharedPreferences()",sharePreferenceClass);
        putStringMethod.addStatement("$T.Editor editor = sharePreference.edit()",sharePreferenceClass);
        putStringMethod.addStatement("editor.putString($N,$N)","key","value");
        putStringMethod.addStatement("editor.apply()");
        mMethodSpecs.add(putStringMethod.build());

        //GET STRING METHOD
        MethodSpec.Builder getStringMethod = MethodSpec.methodBuilder("getString");
        getStringMethod.addModifiers(Modifier.PRIVATE);
        getStringMethod.returns(stringClass);
        getStringMethod.addParameter(stringClass,"key");
        getStringMethod.addParameter(stringClass,"value");
        getStringMethod.addStatement("$T sharePreference = getSharedPreferences()",sharePreferenceClass);
        getStringMethod.addStatement("return sharePreference.getString($N,$N)","key","value");
        mMethodSpecs.add(getStringMethod.build());


        //PUT INT METHOD
        MethodSpec.Builder putIntMethod = MethodSpec.methodBuilder("putInt");
        putIntMethod.addModifiers(Modifier.PRIVATE);
        putIntMethod.addParameter(stringClass,"key");
        putIntMethod.addParameter(int.class,"value");
        putIntMethod.addStatement("$T sharePreference = getSharedPreferences()",sharePreferenceClass);
        putIntMethod.addStatement("$T.Editor editor = sharePreference.edit()",sharePreferenceClass);
        putIntMethod.addStatement("editor.putInt($N,$N)","key","value");
        putIntMethod.addStatement("editor.apply()");
        mMethodSpecs.add(putIntMethod.build());

        //GET INT METHOD
        MethodSpec.Builder getIntMethod = MethodSpec.methodBuilder("getInt");
        getIntMethod.addModifiers(Modifier.PRIVATE);
        getIntMethod.returns(int.class);
        getIntMethod.addParameter(stringClass,"key");
        getIntMethod.addParameter(int.class,"value");
        getIntMethod.addStatement("$T sharePreference = getSharedPreferences()",sharePreferenceClass);
        getIntMethod.addStatement("return sharePreference.getInt($N,$N)","key","value");
        mMethodSpecs.add(getIntMethod.build());

        //PUT LONG METHOD
        MethodSpec.Builder putLongMethod = MethodSpec.methodBuilder("putLong");
        putLongMethod.addModifiers(Modifier.PRIVATE);
        putLongMethod.addParameter(stringClass,"key");
        putLongMethod.addParameter(long.class,"value");
        putLongMethod.addStatement("$T sharePreference = getSharedPreferences()",sharePreferenceClass);
        putLongMethod.addStatement("$T.Editor editor = sharePreference.edit()",sharePreferenceClass);
        putLongMethod.addStatement("editor.putLong($N,$N)","key","value");
        putLongMethod.addStatement("editor.apply()");
        mMethodSpecs.add(putLongMethod.build());

        //GET LONG METHOD
        MethodSpec.Builder getLongMethod = MethodSpec.methodBuilder("getLong");
        getLongMethod.addModifiers(Modifier.PRIVATE);
        getLongMethod.returns(long.class);
        getLongMethod.addParameter(stringClass,"key");
        getLongMethod.addParameter(long.class,"value");
        getLongMethod.addStatement("$T sharePreference = getSharedPreferences()",sharePreferenceClass);
        getLongMethod.addStatement("return sharePreference.getLong($N,$N)","key","value");
        mMethodSpecs.add(getLongMethod.build());

        return true;
    }

    /**
     * Declare class level fields for PreferenceRepository.
     * @return
     */
    private boolean addField(){
        FieldSpec.Builder fileName = FieldSpec.builder(stringClass,"mFileName",Modifier.PRIVATE);
        mFieldSpecs.add(fileName.build());

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

        //Add fields to the class
        for(FieldSpec spec : mFieldSpecs){
            classBuilder.addField(spec);
        }

        //Adding constructor to the class
        for(MethodSpec methodSpec : mInstanceMethodSpecs){
            classBuilder.addMethod(methodSpec);
        }

        //Add methods to the class
        for(MethodSpec methodSpec : mMethodSpecs){
            classBuilder.addMethod(methodSpec);
        }

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
        constructor.addStatement("$N = $N","mContext","context");
        mInstanceMethodSpecs.add(constructor.build());
        return true;
    }

}
