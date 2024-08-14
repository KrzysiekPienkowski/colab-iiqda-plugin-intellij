package com.colab.iiqdaintellij.actions;

import com.colab.iiqdaintellij.Exceptions.ConnectionException;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.colab.iiqdaintellij.utils.CoreUtils;
import com.colab.iiqdaintellij.utils.IIQRESTClient;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ImportAction extends AnAction {

    private String selectedObjectType;
    private String selectedObjectName;
    private AnActionEvent event;


    public ImportAction() {
    }

    public ImportAction(String target) {
        super(target);
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        this.event = e;
        IIQRESTClient iiqrestClient;
        try {
            iiqrestClient = new IIQRESTClient(this.event.getProject(), this.getTemplateText());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        List<String> items = null;
        try {
            items = iiqrestClient.getObjectTypes();
        } catch (ConnectionException ex) {
            throw new RuntimeException(ex);
        }

        ComboBox<String> comboBoxObjectTypes = new ComboBox<>(new CollectionComboBoxModel<>(items));
        JList<String> dataListJList = new JBList<>();
        JScrollPane scrollPane = new JBScrollPane(dataListJList);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        JButton button = new JButton("Finish");
        button.setEnabled(false);


        JPanel panel = new JPanel(new BorderLayout());
        panel.add(comboBoxObjectTypes, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);


        JFrame frame = new JFrame("Importing from: " + this.getTemplateText());
        frame.getContentPane().add(panel);
        frame.setSize(550, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        comboBoxObjectTypes.addActionListener(actionEvent -> {
            selectedObjectType = (String) comboBoxObjectTypes.getSelectedItem();
            if (selectedObjectType != null) {
                System.out.println("Selected item: " + selectedObjectType);
                List<String> objectNames;
                try {
                    objectNames = iiqrestClient.getObjects(selectedObjectType);
                    dataListJList.setModel(getModelFromList(objectNames));

                } catch (ConnectionException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        dataListJList.addListSelectionListener(actionEvent -> {
            selectedObjectName = dataListJList.getSelectedValue();
            if (selectedObjectName != null) {
                button.setEnabled(true);
            }
        });

        button.addActionListener(actionEvent -> {
            selectedObjectName = dataListJList.getSelectedValue();
            if (selectedObjectName != null) {
                String selectedObject;
                try {
                    selectedObject = iiqrestClient.getObject(selectedObjectType, selectedObjectName);
                } catch (ConnectionException ex) {
                    throw new RuntimeException(ex);
                }
                String cleanedObject = CoreUtils.clean(selectedObject);
               // cleanedObject = CoreUtils.addCDATA(cleanedObject);
                String savedFilePath = saveStringToFile(cleanedObject);
                VirtualFileManager.getInstance().syncRefresh();
                FileEditorManager.getInstance(event.getProject()).openFile(CoreUtils.getVirtualFileFromPath(savedFilePath), true);
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(panel, "No value selected.");
            }
        });

        selectObjectTypeByFolderName(comboBoxObjectTypes, e);
    }

    private void selectObjectTypeByFolderName(ComboBox<String> comboBoxObjectTypes, AnActionEvent e) {
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        String folderName = getLastFolderName(selectedFile.getPath());
        selectInComponent(comboBoxObjectTypes, folderName);
    }

    private static void selectInComponent(ComboBox<String> comboBoxObjectTypes ,String objectName) {
        comboBoxObjectTypes.setSelectedItem(objectName);
        ActionEvent selectedEvent = new ActionEvent(comboBoxObjectTypes, ActionEvent.ACTION_PERFORMED, objectName);
        comboBoxObjectTypes.getActionListeners()[0].actionPerformed(selectedEvent);

    }

    private String saveStringToFile(String fileContent) {
        String currentDirectory = getCurrentDirectory();
        String filePath = saveFile(fileContent, currentDirectory);
        return filePath;
    }

    private @NotNull String getCurrentDirectory() {
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        String currentDirectory = virtualFile.getPath();
        System.out.println("Current directory: " + currentDirectory);
        return currentDirectory;
    }

    private String saveFile(String fileContent, String currentDirectory) {
        String fileName = "/" + selectedObjectType + "-" + selectedObjectName + ".xml";
        String filePath = currentDirectory + fileName;
        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(fileContent);
            writer.close();
            System.out.println("Content has been written to the file.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;
    }

    private DefaultListModel getModelFromList(List list) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addAll(list);
        return listModel;
    }

    private static String getLastFolderName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        path = path.replace("\\", "/");

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        int lastSlashIndex = path.lastIndexOf("/");

        if (lastSlashIndex != -1) {
            return path.substring(lastSlashIndex + 1);
        } else {
            return path;
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        e.getPresentation().setEnabledAndVisible(selectedFile != null && selectedFile.isDirectory());
    }
}

