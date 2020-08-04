import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectAnalyserAndroid extends ProjectAnalyser {

    private ArrayList<PackageId> packageIDs = new ArrayList<>();

    public ProjectAnalyserAndroid(@Nullable Project project){
        super(project);
        packageIDs = getAllPackageIds(project);
    }
    public Optional<PackageId> getCorrespondingPackageId(String codePackageID){
        return packageIDs
                .stream()
                .filter(packageId -> codePackageID.startsWith(packageId.toString()))
                .findFirst();
    }

    public List<PackageId> getAllPackages(){
        return packageIDs;
    }

    private ArrayList<PackageId> getAllPackageIds(Project project){
        ArrayList<PackageId> all = new ArrayList<>();
        for (Module module: ModuleManager.getInstance(project).getModules()){
            for (Facet facet: FacetManager.getInstance(module).getAllFacets()){
                if (facet instanceof AndroidFacet){
                    AndroidFacet androidFacet = (AndroidFacet)facet;
                    VirtualFile manifestFile = androidFacet.getMainIdeaSourceProvider().getManifestFile();
                    if (manifestFile != null){
                        Manifest manifest = AndroidUtils.loadDomElement(module, manifestFile, Manifest.class);
                        if (manifest != null){
                            GenericAttributeValue<String> pack = manifest.getPackage();
                            if (pack != null){
                                all.add(new PackageId(pack.toString()));
                            }
                        }
                    }
                }
            }
        }
        return all;
    }


}
