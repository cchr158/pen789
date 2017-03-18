package framesAndPanels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import pen789.pen789;

public class CSRFScreen {
	private JFrame CSRFFrame;
	private JPanel CSRFPanel;
	private JPanel titlePanel;
	private JButton testButton;
	private JButton backButton;
	private JTextField loginUrlText;
	private JTextField username1Text;
	private JTextField username2Text;
	private JTextField password1Text;
	private JTextField password2Text;
	private JTextField registerUrlText;
	private JLabel registerUrlLable;
	private JLabel username1Label;
	private JLabel username2Label;
	private JLabel password1Label;
	private JLabel password2Label;
	private JLabel loginUrlLabel;
	private JLabel user1Label;
	private JLabel user2Label;
	private JTextPane title;
	private pen789 myPen789;
	private Map<String, String> users;
	private JFileChooser fc;
	
	public CSRFScreen(pen789 p, JFileChooser fc) {
		this.myPen789=p;
		this.CSRFFrame = new JFrame("CSRF Test");
		this.CSRFPanel = new JPanel();
		this.titlePanel = new JPanel();
		this.testButton = new JButton("Test");
		this.backButton = new JButton("Back");
		this.loginUrlLabel = new JLabel("User login URL: ");
		this.registerUrlLable = new JLabel("User registration URL: ");
		this.title = new JTextPane();
		this.loginUrlText = new JTextField(this.myPen789.target+"login.jsp");
		this.registerUrlText = new JTextField(this.myPen789.target+"register.jsp");
		this.username1Text = new JTextField("user1@zap.com");
		this.username2Text = new JTextField("user2@zap.com");
		this.password1Text = new JTextField("user1Password");
		this.password2Text = new JTextField("user2Password");
		this.username1Label = new JLabel("username: ");
		this.username2Label = new JLabel("username: ");
		this.password1Label = new JLabel("password: ");
		this.password2Label = new JLabel("password: ");
		this.user1Label = new JLabel("User 1");
		this.user2Label = new JLabel("User 2");
		this.users = new HashMap<String, String>();
		this.fc = fc;
		setButton();
		setLayout();
		setFrame();
	}

	private void setButton() {
		this.backButton.addActionListener(this.myPen789.cancleButtonListerner);
		this.testButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable(){
					public void run() {
						if(!loginUrlText.getText().isEmpty() && !registerUrlText.getText().isEmpty() && 
								!username1Text.getText().isEmpty() && !username2Text.getText().isEmpty() &&
								!password1Text.getText().isEmpty() && !password2Text.getText().isEmpty()){
							users.put(username1Text.getText(), password1Text.getText());
							users.put(username2Text.getText(), password2Text.getText());
							try {
								new CSRFOutput(myPen789, new URL(loginUrlText.getText()), new URL(registerUrlText.getText()), users, fc);
							}catch (MalformedURLException e) {
								e.printStackTrace();
							}
						}else{
							JOptionPane.showMessageDialog(null, "This test requires BOTH the User registration URL and"
									+ " a valid URL in the authentication space");
						}
					}
				}).start();
			}
		});
	}

	private void setFrame() {
		this.CSRFFrame.addWindowListener(this.myPen789.exitListener);
		this.CSRFFrame.add(this.CSRFPanel, BorderLayout.CENTER);
		this.CSRFFrame.setPreferredSize(new Dimension(Constants.SCREEN_SIZE.width/2, 4*Constants.SCREEN_SIZE.height/5));
		this.myPen789.nextFrame(this.CSRFFrame);
	}

	private void setLayout() {
//		set title font and text.
		this.title.setFont(new Font(this.title.getFont().getFontName(), Font.BOLD, Constants.TITLE_FONT_SIZE));
		this.title.setText("Manual CSRF Test");
		this.title.setEditable(false);
//		set label fonts.
		this.loginUrlLabel.setFont(new Font(this.loginUrlLabel.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.registerUrlLable.setFont(new Font(this.registerUrlLable.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.username1Label.setFont(new Font(this.username1Label.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.username2Label.setFont(new Font(this.username2Label.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.password1Label.setFont(new Font(this.password1Label.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.password2Label.setFont(new Font(this.password2Label.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.user1Label.setFont(new Font(this.user1Label.getFont().getFontName(), Font.BOLD, Constants.LABLE_FONT_SIZE));
		this.user2Label.setFont(new Font(this.user2Label.getFont().getFontName(), Font.BOLD, Constants.LABLE_FONT_SIZE));
//		set text area font.
		this.loginUrlText.setFont(new Font(this.loginUrlLabel.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE/2));
		this.registerUrlText.setFont(new Font(this.registerUrlText.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE/2));
		this.username1Text.setFont(new Font(this.username1Text.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.username2Text.setFont(new Font(this.username2Text.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.password1Text.setFont(new Font(this.password1Text.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.password2Text.setFont(new Font(this.password2Text.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE));
//		set text area preferred dimensions.
		this.loginUrlText.setPreferredSize(new Dimension(this.CSRFFrame.getWidth()/2,Constants.TEXTFEILD_FONT_SIZE));
		this.registerUrlText.setPreferredSize(new Dimension(this.CSRFFrame.getWidth()/2,Constants.TEXTFEILD_FONT_SIZE));
		this.username1Text.setPreferredSize(new Dimension(this.CSRFFrame.getWidth()/2,Constants.TEXTFEILD_FONT_SIZE));
		this.username2Text.setPreferredSize(new Dimension(this.CSRFFrame.getWidth()/2,Constants.TEXTFEILD_FONT_SIZE));
		this.password1Text.setPreferredSize(new Dimension(this.CSRFFrame.getWidth()/2,Constants.TEXTFEILD_FONT_SIZE));
		this.password2Text.setPreferredSize(new Dimension(this.CSRFFrame.getWidth()/2,Constants.TEXTFEILD_FONT_SIZE));
		
		this.titlePanel.add(this.title);
		this.CSRFFrame.add(this.titlePanel, BorderLayout.NORTH);
		GridBagConstraints c = new GridBagConstraints();
		this.CSRFPanel.setLayout(new GridBagLayout());
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 5, 5, 5);
//		starting point (0,0) in top left.
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.BOTH;
//		add labels to panel.
//		loginUrl added at start point.
		this.CSRFPanel.add(this.loginUrlLabel, c);
//		move down one grid space.
		c.gridy = GridBagConstraints.RELATIVE;
		this.CSRFPanel.add(this.registerUrlLable, c);
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.REMAINDER;
		c.gridwidth = 2;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.CENTER;
		this.CSRFPanel.add(this.user1Label, c);
		c.weightx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		c.gridy = GridBagConstraints.RELATIVE;
		this.CSRFPanel.add(this.username1Label, c);
		c.gridy = GridBagConstraints.RELATIVE;
		this.CSRFPanel.add(this.password1Label, c);
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.REMAINDER;
		c.gridwidth = 2;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.CENTER;
		this.CSRFPanel.add(this.user2Label, c);
		c.weightx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		this.CSRFPanel.add(this.username2Label, c);
		c.gridy = GridBagConstraints.RELATIVE;
		this.CSRFPanel.add(this.password2Label, c);
//		add text areas.
//		move back to top and move right one grid space.
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		this.CSRFPanel.add(this.loginUrlText, c);
		c.gridy++;
		this.CSRFPanel.add(this.registerUrlText, c);
		c.gridy += 2;
		this.CSRFPanel.add(this.username1Text, c);
		c.gridy++;
		this.CSRFPanel.add(this.password1Text, c);
		c.gridy += 2; 
		this.CSRFPanel.add(this.username2Text, c);
		c.gridy++;
		this.CSRFPanel.add(this.password2Text, c);
//		move down one grid space and add buttons.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,0));
		buttonPanel.add(this.testButton, BorderLayout.EAST);
		buttonPanel.add(this.backButton, BorderLayout.EAST);
		c.gridx = 0;
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.insets = new Insets(10, 5, 10, 5);
		this.CSRFPanel.add(buttonPanel, c);
	}
}
