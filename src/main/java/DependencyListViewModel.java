

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.*;

import com.intellij.openapi.project.Project;


import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class DependencyListViewModel extends Observable {

    private @Nullable  Project project;
    private @Nullable FileEditor editor;
    private ProjectAnalyser analyser;


    public DependencyListViewModel(){

        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        Project activeProject = null;
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            if (window != null && window.isActive()) {
                activeProject = project;
            }
        }
        this.project = activeProject;

        if (project != null){
            this.analyser = ProjectAnalyserFactory.createAnalyser(project);
            editor = FileEditorManager.getInstance(project).getSelectedEditor();
            analyser.setCurrentEditor(editor);

            project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
                @Override
                public void selectionChanged(@Nonnull FileEditorManagerEvent event) {
                    editor = FileEditorManager.getInstance(project).getSelectedEditor();
                    analyser.setCurrentEditor(editor);
                    setChanged();
                    notifyObservers();
                }
            });
        }
    }



    public DefaultListModel<Dependency> getModel(){
        DefaultListModel<Dependency> listModel = new DefaultListModel<>();
        Optional<ArrayList<Dependency>> outgoing = analyser.getOutgoingList();
        Optional<ArrayList<Dependency>> incoming = analyser.getIncomingList();
        listModel.addElement(new Dependency("DEPENDS ON", Dependency.Type.MESSAGE, Dependency.Style.SEPARATOR));
        if (outgoing.isPresent()){
            outgoing.get().forEach(listModel::addElement);
        } else {
            listModel.addElement(new Dependency("N/A", Dependency.Type.MESSAGE, Dependency.Style.GRAYEDOUT));
        }

        listModel.addElement(new Dependency("DEPENDANTS", Dependency.Type.MESSAGE, Dependency.Style.SEPARATOR));
        if (incoming.isPresent()){
            incoming.get().forEach(listModel::addElement);
        } else {
            listModel.addElement(new Dependency("N/A", Dependency.Type.MESSAGE, Dependency.Style.GRAYEDOUT));
        }

        return listModel;
    }

    public void open(@Nullable VirtualFile file){
        if (file != null && project != null){
            FileEditorManager.getInstance(project).openFile(file, true);
        }
    }


}
