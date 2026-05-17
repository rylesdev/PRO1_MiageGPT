package com.miage.miagegpt.view;

import javafx.scene.control.Hyperlink;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextFlow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownRenderer {

    private static final Pattern INLINE_MD = Pattern.compile(
            "\\*\\*(.+?)\\*\\*|\\*([^*\\n]+?)\\*|`([^`\\n]+)`|(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)");

    public static TextFlow renderFlow(String text, boolean isDarkMode) {
        TextFlow flow = new TextFlow();
        flow.setLineSpacing(4);
        flow.setMaxWidth(670);
        javafx.scene.paint.Color textFill = javafx.scene.paint.Color.web(isDarkMode ? "#ECECF1" : "#1F2937");
        String[] lines = text.split("\n", -1);
        for (int li = 0; li < lines.length; li++) {
            String line = lines[li];
            if (line.startsWith("### ")) {
                addInlineNodes(flow, line.substring(4), textFill, true, 15, isDarkMode);
            } else if (line.startsWith("## ")) {
                addInlineNodes(flow, line.substring(3), textFill, true, 16, isDarkMode);
            } else if (line.startsWith("# ")) {
                addInlineNodes(flow, line.substring(2), textFill, true, 18, isDarkMode);
            } else if (line.length() > 2 && (line.startsWith("- ") || line.startsWith("* "))) {
                javafx.scene.text.Text bullet = new javafx.scene.text.Text("  • ");
                bullet.setFill(textFill);
                bullet.setFont(Font.font("System", 14));
                flow.getChildren().add(bullet);
                addInlineNodes(flow, line.substring(2), textFill, false, 14, isDarkMode);
            } else {
                addInlineNodes(flow, line, textFill, false, 14, isDarkMode);
            }
            if (li < lines.length - 1) {
                flow.getChildren().add(new javafx.scene.text.Text("\n"));
            }
        }
        return flow;
    }

    private static void addInlineNodes(TextFlow flow, String text,
            javafx.scene.paint.Color textFill, boolean bold, double fontSize, boolean isDarkMode) {
        Matcher matcher = INLINE_MD.matcher(text);
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                javafx.scene.text.Text plain = new javafx.scene.text.Text(text.substring(lastEnd, matcher.start()));
                plain.setFill(textFill);
                plain.setFont(Font.font("System", bold ? FontWeight.BOLD : FontWeight.NORMAL, fontSize));
                flow.getChildren().add(plain);
            }
            if (matcher.group(1) != null) {
                javafx.scene.text.Text t = new javafx.scene.text.Text(matcher.group(1));
                t.setFill(textFill);
                t.setFont(Font.font("System", FontWeight.BOLD, fontSize));
                flow.getChildren().add(t);
            } else if (matcher.group(2) != null) {
                javafx.scene.text.Text t = new javafx.scene.text.Text(matcher.group(2));
                t.setFill(textFill);
                t.setFont(Font.font("System", FontWeight.NORMAL, FontPosture.ITALIC, fontSize));
                flow.getChildren().add(t);
            } else if (matcher.group(3) != null) {
                javafx.scene.text.Text t = new javafx.scene.text.Text(" " + matcher.group(3) + " ");
                t.setFill(isDarkMode
                        ? javafx.scene.paint.Color.web("#93C5FD")
                        : javafx.scene.paint.Color.web("#1D4ED8"));
                t.setFont(Font.font("Monospace", fontSize));
                t.getProperties().put("isCode", Boolean.TRUE);
                flow.getChildren().add(t);
            } else if (matcher.group(4) != null) {
                String url = matcher.group(4);
                Hyperlink link = new Hyperlink(url);
                link.setStyle("-fx-text-fill: #58A6FF; -fx-font-size: " + (int) fontSize
                        + "px; -fx-border-width: 0; -fx-padding: 0;");
                link.setOnAction(ev -> {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    } catch (Exception ignored) {
                    }
                });
                flow.getChildren().add(link);
            }
            lastEnd = matcher.end();
        }
        if (lastEnd < text.length()) {
            javafx.scene.text.Text remaining = new javafx.scene.text.Text(text.substring(lastEnd));
            remaining.setFill(textFill);
            remaining.setFont(Font.font("System", bold ? FontWeight.BOLD : FontWeight.NORMAL, fontSize));
            flow.getChildren().add(remaining);
        }
    }
}
