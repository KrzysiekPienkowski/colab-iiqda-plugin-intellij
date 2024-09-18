package com.colab.iiqdaintellij.actions;

import com.colab.iiqdaintellij.utils.IIQPlugin;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;

import java.util.List;

public class ImportArtifactActionGroup extends ActionGroup {

    @NotNull
    @Override
    public AnAction[] getChildren(AnActionEvent event) {
        List<String> targetEnvironments = IIQPlugin.getTargetEnvironments(event.getProject());

        AnAction[] actionsArray = new AnAction[targetEnvironments.size()];
        for (int i = 0; i < targetEnvironments.size(); i++) {
            String targetEnvironment = targetEnvironments.get(i);
            ImportAction obj = new ImportAction(targetEnvironment);
            actionsArray[i] = obj;
        }

        return actionsArray;
    }
}