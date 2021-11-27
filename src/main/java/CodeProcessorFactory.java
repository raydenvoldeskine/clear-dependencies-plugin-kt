import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.kotlin.psi.KtFile;

public class CodeProcessorFactory {

    public static CodeProcessor createProcessor(PsiFile psiFile, ProjectAnalyser analyser){
        if (psiFile instanceof PsiJavaFile){
            return new CodeProcessorJava((PsiJavaFile)psiFile, analyser);
        } else if (psiFile instanceof KtFile){
            return new CodeProcessorKotlin((KtFile)psiFile, analyser);
        }
        return new CodeProcessorUnknown(analyser);
    }
}
