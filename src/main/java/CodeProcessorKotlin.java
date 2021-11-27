import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.Query;
import org.jetbrains.kotlin.psi.KtExpression;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtImportDirective;
import org.jetbrains.kotlin.psi.KtImportList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CodeProcessorKotlin extends CodeProcessor {

    private String[] exclusions = {
            "java.",
            "android.",
            "org.",
            "kotlin."
    };

    private KtFile ktFile;
    CodeProcessorKotlin(KtFile file, ProjectAnalyser analyser) {
        super(analyser);
        ktFile = file;
    }

    @Override
    public Optional<ArrayList<Dependency>> getOutgoingList() {
        ArrayList<Dependency> outgoing = new ArrayList<>();

        try {
            Project project = analyser.getProject();
            KtImportList importList = ktFile.getImportList();
            Optional<PackageId> ownPackageID = analyser.getCorrespondingPackageId(ktFile.getPackageName());
            List<PackageId> allProjectPackages = analyser.getAllPackages();
            HashMap<String, Integer> outgoingPackageReferences = new HashMap<>();
            if (importList != null && project != null && ownPackageID.isPresent()) {

                List<KtImportDirective> kImports = importList.getImports();
                List<Dependency> imports = importList.getImports().stream()
                        .map(KtImportDirective::getImportedReference)
                        .filter(Objects::nonNull)
                        .filter(ref -> ref.getText() != null )
                        .filter(ref -> !isExclusionReference(ref.getText()))
                        .map(ref -> new AbstractMap.SimpleImmutableEntry<KtExpression, PackageId>(ref, new PackageId(ref.getText())))
                        .filter(entry -> entry.getValue().doesBeginWith(ownPackageID.get()))
                        .filter(entry -> !entry.getValue().isEmpty())
                        .map(entry -> new Dependency(
                                entry.getValue().getLast(),
                                Dependency.Type.OUTGOING,
                                Dependency.Style.DEFAULT,
                                entry.getKey().getContainingFile().getVirtualFile()))
                        .collect(Collectors.toList());


            /*
            List<Dependency> imports = Arrays.stream(importList.getImportStatements())
                    .map(PsiImportStatementBase::getImportReference)
                    .filter(Objects::nonNull)
                    .filter(ref -> !isExclusionReference(ref.getQualifiedName()))
                    .map(ref -> new AbstractMap.SimpleImmutableEntry<PsiJavaCodeReferenceElement, PackageId>(ref, new PackageId(ref.getQualifiedName())))
                    .filter(entry -> entry.getValue().doesBeginWith(ownPackageID.get()))
                    .filter(entry -> !entry.getValue().isEmpty())
                    .map(entry -> new AbstractMap.SimpleImmutableEntry<PsiElement, PackageId>(entry.getKey().resolve(), entry.getValue()))
                    .map(entry -> new Dependency(
                            entry.getValue().getLast(),
                            Dependency.Type.OUTGOING,
                            Dependency.Style.DEFAULT,
                            entry.getKey() != null? entry.getKey().getContainingFile().getVirtualFile() : null))
                    .collect(Collectors.toList());*/
                outgoing.addAll(imports.stream().filter(distinctByKey(Dependency::getName)).collect(Collectors.toList()));
            }

            outgoing.sort(new Comparator<Dependency>() {
                public int compare(Dependency entry1, Dependency entry2) {
                    return entry1.getName().compareTo(entry2.getName());
                }
            });
        }
        catch (IndexNotReadyException e) {

        }

        return Optional.of(outgoing);
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private boolean isExclusionReference(String fullName){
        for (String ex: exclusions){
            if (fullName.startsWith(ex)){
                return true;
            }
        }
        return false;
    }


    @Override
    public Optional<ArrayList<Dependency>> getIncomingList() {
        ArrayList<Dependency> incoming = new ArrayList<>();
        try {
            PsiClass[] classes = ktFile.getClasses();
            PsiClass mainClass = classes.length > 0? classes[0] : null;
            if (mainClass != null){
                Query<PsiReference> refs = ReferencesSearch.search(mainClass);

                for (PsiReference ref: refs){
                    PsiElement element = ref.getElement();
                    PsiClass refClass = PsiUtil.getTopLevelClass(element);
                    PsiFile refFile = element.getContainingFile();
                    if (refClass != null && refFile != null) {
                        if (!refFile.getVirtualFile().getPath().contains("generated")){
                            String className = refClass.getName();
                            if (className != null){
                                if (incoming.stream().noneMatch(entry -> entry.getName().equals(className))) {
                                    incoming.add(new Dependency(className, Dependency.Type.INCOMING, refFile.getVirtualFile()));
                                }
                            }
                        }
                    }

                }
            }

        }
        catch (IndexNotReadyException e) {

        }
        incoming.sort(new Comparator<Dependency>() {
            public int compare(Dependency entry1, Dependency entry2) {
                return entry1.getName().compareTo(entry2.getName());
            }
        });

        return Optional.of(incoming);
    }
}
