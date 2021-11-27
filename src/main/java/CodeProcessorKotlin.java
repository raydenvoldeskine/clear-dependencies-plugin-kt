import com.intellij.openapi.project.Project;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtImportDirective;
import org.jetbrains.kotlin.psi.KtImportList;

import java.util.*;
import java.util.stream.Collectors;

public class CodeProcessorKotlin extends CodeProcessor {
    private KtFile ktFile;
    CodeProcessorKotlin(KtFile file, ProjectAnalyser analyser) {
        super(analyser);
        ktFile = file;
    }

    @Override
    public Optional<ArrayList<Dependency>> getOutgoingList() {
        ArrayList<Dependency> outgoing = new ArrayList<>();
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
                            .map(ref -> new Dependency(
                                    ref.getText(),
                                    Dependency.Type.OUTGOING,
                                    Dependency.Style.DEFAULT,
                                    ref.getContainingFile().getVirtualFile()))
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
            outgoing.addAll(imports);
        }

        return Optional.of(outgoing);
    }

    @Override
    public Optional<ArrayList<Dependency>> getIncomingList() {
        return Optional.empty();
    }
}
