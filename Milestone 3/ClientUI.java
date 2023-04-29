package client;

import java.awt.BorderLayout;


import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;

import server.Room;
import server.ServerThread;

public class ClientUI extends JFrame implements Event /*, ActionListener*/ {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    CardLayout card;
    ClientUI self;
    JPanel textArea;
    JPanel userPanel;
   // GamePanel game;
    List<User> users = new ArrayList<User>();
    private final static Logger log = Logger.getLogger(ClientUI.class.getName());
    Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
    
    String username;
    RoomsPanel roomsPanel;
    JMenuBar menu;
    
    List<String> muted = new ArrayList<String>();

    public ClientUI(String title) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		menu = new JMenuBar();
		JMenu roomsMenu = new JMenu("Actions");
		JMenuItem roomsSearch = new JMenuItem("Rooms");
		roomsSearch.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
			goToPanel("rooms");
		    }
		});
		roomsMenu.add(roomsSearch);
		menu.add(roomsMenu);
		windowSize.width *= .8;
		windowSize.height *= .8;
		setPreferredSize(windowSize);
		setSize(windowSize);
		setLocationRelativeTo(null);
		self = this;
		setTitle(title);
		card = new CardLayout();
		setLayout(card);
		createConnectionScreen();
		createUserInputScreen();
		createPanelRoom();
		createPanelUserList();
		this.setJMenuBar(menu);
		//createDrawingPanel();
		createRoomsPanel();
		showUI();
    }

    void createConnectionScreen() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel hostLabel = new JLabel("Host:");
		JTextField host = new JTextField("127.0.0.1");
		panel.add(hostLabel);
		panel.add(host);
		JLabel portLabel = new JLabel("Port:");
		JTextField port = new JTextField("3000");
		panel.add(portLabel);
		panel.add(port);
		JButton button = new JButton("Next");
		button.addActionListener(new ActionListener() {
	
		    @Override
		    public void actionPerformed(ActionEvent e) {
			String _host = host.getText();
			String _port = port.getText();
			if (_host.length() > 0 && _port.length() > 0) {
			    try {
				connect(_host, _port);
				self.next();
			    }
			    catch (IOException e1) {
				e1.printStackTrace();
				// TODO handle error properly
				log.log(Level.SEVERE, "Error connecting");
			    }
			}
		    }
	
		});
		host.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "sendAction");
		host.getActionMap().put("sendAction", new AbstractAction() {
		    public void actionPerformed(ActionEvent actionEvent) {
			button.doClick();
		    }
		});
		panel.add(button);
		this.add(panel, "login");
    }

    void createUserInputScreen() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel userLabel = new JLabel("Username:");
		JTextField username = new JTextField();
		panel.add(userLabel);
		panel.add(username);
		JButton button = new JButton("Join");
		ClientUI self = this;
		button.addActionListener(new ActionListener() {
	
		    @Override
		    public void actionPerformed(ActionEvent e) {
			String name = username.getText();
			if (name != null && name.length() > 0) {
			    // need external ref since "this" context is the action event, not ClientUI
			    self.username = name;
			    // this order matters
			    //createDrawingPanel();
			    pack();
			    self.setTitle(self.getTitle() + " - " + self.username);
			    //game.setPlayerName(self.username);
			    SocketClient.INSTANCE.setUsername(self.username);
	
			    self.next();
			}
		    }
	
		});
		// TODO set focus later
		username.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "sendAction");
		username.getActionMap().put("sendAction", new AbstractAction() {
			public void actionPerformed(ActionEvent actionEvent) {
			button.doClick();
			}
		});
		panel.add(button);
		this.add(panel, "details");
    }

    void createPanelRoom() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
	
		textArea = new JPanel();
		textArea.setLayout(new BoxLayout(textArea, BoxLayout.Y_AXIS));
		textArea.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		Dimension d = new Dimension((int) (windowSize.width * .3), windowSize.height);
		scroll.setPreferredSize(d);
		scroll.setMinimumSize(d);
		scroll.setMaximumSize(d);
		panel.add(scroll, BorderLayout.CENTER);
	
		JPanel input = new JPanel();
		input.setLayout(new BoxLayout(input, BoxLayout.X_AXIS));
		JTextArea text = new JTextArea();
		input.add(text);
		JButton button = new JButton("Send");
		text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "sendAction");
		text.getActionMap().put("sendAction", new AbstractAction() {
		    public void actionPerformed(ActionEvent actionEvent) {
			button.doClick();
		    }
		});
	
		button.addActionListener(new ActionListener() {
	
		    @Override
		    public void actionPerformed(ActionEvent e) {
			if (text.getText().length() > 0) {
			    SocketClient.INSTANCE.sendMessage(text.getText());
			    text.setText("");
			}
		    }
	
		});
		input.add(button);
		panel.add(input, BorderLayout.SOUTH);
		this.add(panel, "lobby");
    }

    void createPanelUserList() {
		userPanel = new JPanel();
		userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
		userPanel.setAlignmentY(Component.TOP_ALIGNMENT);
	
		JScrollPane scroll = new JScrollPane(userPanel);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	
		Dimension d = new Dimension((int) (windowSize.width * .1), windowSize.height);
		scroll.setPreferredSize(d);
		scroll.setMinimumSize(d);
		scroll.setMaximumSize(d);

	
		textArea.getParent().getParent().getParent().add(scroll, BorderLayout.EAST);
    }
    
    void createRoomsPanel() {
    	roomsPanel = new RoomsPanel(this);
    	this.add(roomsPanel, "rooms");
    }

    void addClient(String name) {
		User u = new User(name, "%s");
		Dimension p = new Dimension(userPanel.getSize().width, 30);
		u.setPreferredSize(p);
		u.setMinimumSize(p);
		u.setMaximumSize(p);
		userPanel.add(u);
		users.add(u);
		pack();
	}

    void removeClient(User client) {
		userPanel.remove(client);
		client.removeAll();
		userPanel.revalidate();
		userPanel.repaint();
    }

    /***
     * Attempts to calculate the necessary dimensions for a potentially wrapped
     * string of text. This isn't perfect and some extra whitespace above or below
     * the text may occur
     * 
     * @param str
     * @return
     */
    int calcHeightForText(String str) {
		FontMetrics metrics = self.getGraphics().getFontMetrics(self.getFont());
		int hgt = metrics.getHeight();
		int adv = metrics.stringWidth(str);
		final int PIXEL_PADDING = 6;
		Dimension size = new Dimension(adv, hgt + PIXEL_PADDING);
		final float PADDING_PERCENT = 1.1f;
		// calculate modifier to line wrapping so we can display the wrapped message
		int mult = (int) Math.floor(size.width / (textArea.getSize().width * PADDING_PERCENT));
		// System.out.println(mult);
		mult++;
		return size.height * mult;
    }

    void addMessage(String str) {
		JEditorPane entry = new JEditorPane();
		entry.setContentType("text/html");
		entry.setEditable(false);
		// entry.setLayout(null);
		entry.setText(str);
		Dimension d = new Dimension(textArea.getSize().width, calcHeightForText(str));
		// attempt to lock all dimensions
		entry.setMinimumSize(d);
		entry.setPreferredSize(d);
		entry.setMaximumSize(d);
		textArea.add(entry);
	
		pack();
		resizeTexts();
		// System.out.println(entry.getSize());
		JScrollBar sb = ((JScrollPane) textArea.getParent().getParent()).getVerticalScrollBar();
		sb.setValue(sb.getMaximum());
    }
    
    void resizeTexts() {
    	// attempts to fix sizing of messages when text area gets resized
    	// sort of works so good enough for my example
    	int areaWidth = textArea.getSize().width;
    	int cc = textArea.getComponents().length;
    	if (cc > 1) {
    	    // if we have more than 1 text item
    	    Component test = textArea.getComponent(cc - 2);
    	    // check if the test width is different than our container
    	    if (areaWidth != test.getWidth()) {
    		// if so let's try to resize all the components to be the same width
    		for (Component c : textArea.getComponents()) {
    		    Dimension current = c.getSize();
    		    Dimension updated = new Dimension(areaWidth, current.height);
    		    c.setPreferredSize(updated);
    		    c.setMinimumSize(updated);
    		    c.setMaximumSize(updated);
    		}
    	    }
    	}
    }
    
    void next() {
    	card.next(this.getContentPane());
    }

    void previous() {
    	card.previous(this.getContentPane());
    }
    
    void goToPanel(String panel) {
    	switch (panel) {
    	case "rooms":
    	    // TODO get rooms
    	    roomsPanel.removeAllRooms();
    	    SocketClient.INSTANCE.sendGetRooms(null);
    	    break;
    	default:
    	    break;
    	}
    	card.show(this.getContentPane(), panel);

        }

    void connect(String host, String port) throws IOException {
	SocketClient.INSTANCE.registerCallbackListener(this);
	SocketClient.INSTANCE.connectAndStart(host, port);
    }

    void showUI() {
		pack();
		Dimension lock = textArea.getSize();
		textArea.setMaximumSize(lock);
		lock = userPanel.getSize();
		userPanel.setMaximumSize(lock);
		setVisible(true);
    }

    @Override
    public void onClientConnect(String clientName, String message) {
		log.log(Level.INFO, String.format("%s: %s", clientName, message));
		addClient(clientName);
		if (message != null && !message.isBlank()) {
		    self.addMessage(String.format("%s: %s", clientName, message));
		}
    }

    @Override
    public void onClientDisconnect(String clientName, String message) {
		log.log(Level.INFO, String.format("%s: %s", clientName, message));
		Iterator<User> iter = users.iterator();
		while (iter.hasNext()) {
		    User u = iter.next();
		    if (u.getName() == clientName) {
			removeClient(u);
			iter.remove();
			self.addMessage(String.format("%s: %s", clientName, message));
			break;
		    }
	
		}
    }

  
	
    @Override
    public void onMessageReceive(String clientName, String message) {   	
    	//muted = ServerThread.getMutedClients();
    	log.log(Level.INFO, String.format("%s: %s", clientName, message));
		self.addMessage(String.format("%s: %s", clientName, message));
    	
    }

    @Override
    public void onChangeRoom() {
		Iterator<User> iter = users.iterator();
		while (iter.hasNext()) {
		    User u = iter.next();
		    removeClient(u);
		    iter.remove();
		}
		goToPanel("lobby");
    }

    public static void main(String[] args) {
	ClientUI ui = new ClientUI("My UI");
	if (ui != null) {
	    log.log(Level.FINE, "Started");
	}
    }
    
    @Override
    public void onGetRoom(String roomName) {
		// TODO Auto-generated method stub
		if (roomsPanel != null) {
		    roomsPanel.addRoom(roomName);
		    pack();
		}
    }

	/*
	 * @Override
	public void onIsMuted(String clientName, boolean isMuted) {
		// TODO Auto-generated method stub
		Iterator<User> iter = users.iterator();
		while (iter.hasNext()) {
		    User u = iter.next();
		    if(u.getName().equalsIgnoreCase(clientName)) {
		    	if (isMuted) {
		    		u.setName(clientName, "<del>%s</del>");
		 		}
		 		else {
		 		    u.setName(clientName, "%s");
		 		}
		    	break;
		    }
		}    
	}
	*/
}