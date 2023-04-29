package client;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

public class User extends JPanel {
    private String name;
    private JEditorPane nameField;

    public User(String name /*, String wrapper*/) {
    	this.name = name;
    	nameField = new JEditorPane();
     	nameField.setContentType("text/html");
     	nameField.setText("<b>" + name + "</b>");
     	//nameField.setText(String.format(wrapper, name));
     	nameField.setEditable(false);
	this.add(nameField);
    }

    public String getName() {
	return name;
    }
}