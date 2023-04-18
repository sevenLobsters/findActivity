package com.example.findactivity.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MouseAction extends AnAction {
    public static final String TASK = "TASK";
    public final String ADD_FRAGMENT = "Added Fragments";
    JBPopup pop;

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (file == null || editor == null) return;
        String cmd = "adb shell dumpsys activity top";
        System.out.println("cmd   ====>  " + cmd);
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String text = dealReader(reader);
            JTextArea jTextArea = new JTextArea();
            jTextArea.setText(text);
            JScrollPane scrollpane = new JBScrollPane();
            scrollpane.setViewportView(jTextArea);
            JPanel container = new JPanel();
            container.setLayout(new BorderLayout());
            container.add(scrollpane, BorderLayout.CENTER);
            System.out.println("click=======================" + text);
            JBPopupFactory instance = JBPopupFactory.getInstance();
            ComponentPopupBuilder builder = instance.createComponentPopupBuilder(container, jTextArea);
            builder.setTitle("find activity");
            builder.setRequestFocus(true);
            builder.setNormalWindowLevel(true);
            builder.setMovable(true);
            pop = builder.createPopup();
            pop.setSize(new Dimension(800, 500));
            pop.showInFocusCenter();
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            process.destroy();
        }
    }

    public String dealReader(BufferedReader reader) throws IOException {
        String line = null;
        String trimLine = null;
        StringBuffer buffer = new StringBuffer();
        StringBuffer result = new StringBuffer();
        ArrayList<StringBuffer> list = new ArrayList<StringBuffer>();
        while ((line = reader.readLine()) != null) {
            if (line == null) return buffer.toString();
            trimLine = line.trim();
            if (trimLine.startsWith(TASK)) {
                if(buffer.length() > 0){
                    list.add(buffer);
                    buffer = new StringBuffer();
                }
                buffer.append("\r\n");
                buffer.append("====================================================================================================================\n");
                buffer.append(line + "\r\n");
                line = reader.readLine();
                buffer.append(line + "\r\n");
                line = reader.readLine();
                buffer.append(line + "\r\n");
                trimLine = line.trim();
            }
            if (trimLine.startsWith(ADD_FRAGMENT)) {
                int c = getSpaceCount(line);
                line = reader.readLine();
                int c2 = getSpaceCount(line);
                while (c < c2) {
                    buffer.append(line + "\r\n");
                    line = reader.readLine();
                    c2 = getSpaceCount(line);
                }
            }
        }
        if(buffer.length() > 0){
            list.add(buffer);
        }
        if(list.size() > 0){
            for(int i = list.size()-1;i >=0;i--){
                StringBuffer b = list.get(i);
                result.append(b);
            }
        }

        return result.toString();
    }

    public static int getSpaceCount(String str) {
        if (str == null || str.length() == 0) return 0;
        return str.length() - str.trim().length();
    }
}
