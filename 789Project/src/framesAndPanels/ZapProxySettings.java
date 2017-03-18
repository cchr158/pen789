package framesAndPanels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import pen789.pen789;

class ZapProxySettings {
	private pen789 myPen789;
	private JFrame ProxyFrame;
	private JPanel titlePanel;
	private JPanel inputPanel;
	private JPanel buttonPanel;
	private JTextPane title;
	private JLabel addressLabel;
	private JTextField addressField;
	private JLabel portLabel;
	private JTextField portField;
	private JLabel APIKeyLabel;
	private JTextField APIKeyField;
	private JLabel attackStrengthLabel;
	private JComboBox<String> attackStrengthComboBox;
	private JButton okButton;
	private JButton helpButton;
	private JPanel helpPanel;
	private JTextPane helpPane;
	private JFrame helpFrame;
	private JButton helpOkButton;

	ZapProxySettings(pen789 p) {
		this.myPen789 = p;
		this.ProxyFrame = new JFrame("OWSAP ZAP Proxy settings");
		this.ProxyFrame.addWindowListener(this.myPen789.exitListener);
		this.ProxyFrame.setPreferredSize(new Dimension(3*Constants.SCREEN_SIZE.width/5,2*Constants.SCREEN_SIZE.height/3));
		this.titlePanel = new JPanel();
		this.inputPanel = new JPanel();
		this.buttonPanel = new JPanel();
		this.title = new JTextPane();
		this.addressLabel = new JLabel("ZAP Proxy Address: ");
		this.portLabel = new JLabel("Proxy Port: ");
		this.APIKeyLabel = new JLabel("ZAP API Key: ");
		this.addressField = new JTextField();
		this.portField = new JTextField();
		this.APIKeyField = new JTextField();
		this.attackStrengthLabel = new JLabel("Attack Strength: ");
		this.attackStrengthComboBox = new JComboBox<String>(new String[]{"Low","Medium","High", "Insane"});
		this.okButton = new JButton("OK");
		this.helpButton = new JButton("Help");
		this.helpPanel = new JPanel();
		this.helpPane = new JTextPane();
		this.helpFrame = new JFrame("Proxy settings help");
		this.helpFrame.setPreferredSize(new Dimension(7*Constants.SCREEN_SIZE.width/10,2*Constants.SCREEN_SIZE.height/3));
		this.helpFrame.addWindowListener(this.myPen789.exitListener);
		this.helpOkButton = new JButton("OK");
		this.helpOkButton.addActionListener(this.myPen789.cancleButtonListerner);
		displayTitlePanel();
		proxyInputs();
		this.myPen789.nextFrame(ProxyFrame);
	}

	private void proxyInputs() {
		this.inputPanel.setLayout(new GridBagLayout());
		this.addressLabel.setFont(new Font(this.addressLabel.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.portLabel.setFont(new Font(this.portLabel.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.APIKeyLabel.setFont(new Font(this.APIKeyLabel.getFont().getFontName(), Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.attackStrengthLabel.setFont(new Font(this.attackStrengthLabel.getFont().getFontName(),
				Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.addressField.setFont(new Font(this.addressField.getFont().getFontName(), Font.PLAIN, Constants.TEXTFEILD_FONT_SIZE));
		this.addressField.setText(this.myPen789.ZAP_ADDRESS);
		this.portField.setFont(new Font(this.portField.getFont().getFontName(), Font.PLAIN, Constants.TEXTFEILD_FONT_SIZE));
		this.portField.setText(Integer.toString(this.myPen789.ZAP_PORT));
		this.APIKeyField.setFont(new Font(this.APIKeyField.getFont().getFontName(), Font.PLAIN, Constants.TEXTFEILD_FONT_SIZE));
		this.addressField.setPreferredSize(new Dimension(this.ProxyFrame.getWidth()/2,
				Constants.TEXTFEILD_FONT_SIZE));
		this.portField.setPreferredSize(new Dimension(this.ProxyFrame.getWidth()/2,
				Constants.TEXTFEILD_FONT_SIZE));
		this.APIKeyField.setPreferredSize(new Dimension(this.ProxyFrame.getWidth()/2,
				Constants.TEXTFEILD_FONT_SIZE));
		this.APIKeyField.setText(this.myPen789.ZAP_API_KEY);
		this.attackStrengthComboBox.setSelectedIndex(2);
		this.attackStrengthComboBox.setFont(new Font(this.attackStrengthComboBox.getFont().getFontName(), Font.PLAIN, Constants.TEXTFEILD_FONT_SIZE));
		this.attackStrengthComboBox.setPreferredSize(new Dimension(this.ProxyFrame.getWidth()/2,
				Constants.TEXTFEILD_FONT_SIZE));
		this.inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 15, 0, 0);
		c.gridheight = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		this.inputPanel.add(this.addressLabel, c);
		c.gridy = 1;
		c.insets = new Insets(15, 15, 0, 0);
		this.inputPanel.add(this.portLabel, c);
		c.gridy = 2;
		this.inputPanel.add(this.APIKeyLabel, c);
		c.gridy = 3;
		this.inputPanel.add(this.attackStrengthLabel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		c.insets = new Insets(15, 0, 0, 15);
		c.gridx = 2;
		c.gridy = 0;
		this.inputPanel.add(this.addressField, c);
		c.gridy = 1;
		this.inputPanel.add(this.portField, c);
		c.gridy = 2;
		this.inputPanel.add(this.APIKeyField, c);
		c.gridx = 1;
		c.gridy = 3;
		this.inputPanel.add(this.attackStrengthComboBox, c);

		this.okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				okFunction();
			}
		});
		this.helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				helpFunction();
			}
		});

		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridx = 4;
		c.gridy = 4;
		c.insets = new Insets(10, 0, 10, 0);
		c.anchor = GridBagConstraints.EAST;
		this.buttonPanel.add(this.okButton);
		this.buttonPanel.add(this.helpButton);
		this.inputPanel.add(this.buttonPanel, c);

		ProxyFrame.add(this.inputPanel, BorderLayout.CENTER);
	}

	private void helpFunction() {
//		TODO Check help text.
		String helpText = "<HTML><head> <style type='text/css'>  li {   font-size: 24;  } "
				+ "</style></head><body> <p align='center'><FONT size='7'><b>Proxy Settings Help</b>"
				+ "</FONT></p> <P><FONT size='4'>This help section will help you complete the ZAP "
				+ "proxy settings for pen789. You should not modifie the proxy settings unless you are "
				+ "fermilar with the ZAP API.</FONT></P> <ol>  <li><FONT size='6'>Proxy Address:</FONT>"
				+ "<p><FONT size='4'>The Local ZAP proxy address is the address where the ZAP proxy can "
				+ "be found. If you have manually configured a ZAP proxy then enter its address here."
				+ "</FONT></p></li>  <li><FONT size='6'>Proxy Port:</FONT><p><FONT size='4'>The Local "
				+ "ZAP proxy port is the port that the ZAP proxy is listening on.</FONT></p></li>"
				+ "  <li><FONT size='6'>ZAP API Key:</FONT><p><FONT size='4'>The ZAP API Key is used "
				+ "to prevent other applications from accessing the ZAP API without permission. "
				+ "If you have more that one ZAP session on the go you can switch between them by "
				+ "changing the text in this feild to their respective API keys.</FONT></p></li> </ol> "
				+ "<P><FONT size='4'>More information is avalable at "
				+ "https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project.</a></FONT></P></body></HTML>";
		this.helpPane.setContentType("text/html");
		this.helpPane.setText(helpText);
		this.helpPane.setEditable(false);
		GridBagConstraints c = new GridBagConstraints();
		this.helpPanel.setLayout(new GridBagLayout());
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.BOTH;
		c.gridx=c.gridy=0;
		this.helpPanel.add(this.helpPane, c);
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		this.helpPanel.add(this.helpOkButton,c);
		this.helpPanel.setPreferredSize(new Dimension(helpFrame.getWidth(),9*helpFrame.getHeight()/10));
		this.helpFrame.getContentPane().add(this.helpPanel, BorderLayout.CENTER);
		this.myPen789.nextFrame(helpFrame);
	}

	private void okFunction() {
		if(this.addressField.getText().isEmpty()){
			JOptionPane.showMessageDialog(null, "ZAP Proxy address is required to connect to the local proxy.");
		}else if(this.portField.getText().isEmpty()){
			JOptionPane.showMessageDialog(null, "ZAP Proxy port number is required to connect to the local proxy.");
		}else if (this.APIKeyField.getText().isEmpty() || this.APIKeyField.getText().length() < 26){
			JOptionPane.showMessageDialog(null, "ZAP API key must be at least 26 characters long and is required to run any tests.");
		}else{
			myPen789.setZAPAddress(this.addressField.getText());
			int portNumber = -1;
			try{
				portNumber = Integer.parseInt(this.portField.getText());
			}catch(NumberFormatException e){
				JOptionPane.showMessageDialog(null, "ZAP Proxy port number must be a valid integer.");
			}
			if(portNumber >= 0 && portNumber <= 65535){
				myPen789.setZAPPort(portNumber);
			}else{
				JOptionPane.showMessageDialog(null, "ZAP Proxy port number must be between 0 and 65535.");
			}
			myPen789.setZAPAPIKEY(this.APIKeyField.getText());
			myPen789.setAttackStrength(this.attackStrengthComboBox.getItemAt(this.attackStrengthComboBox.getSelectedIndex()));
			myPen789.prevFrame();
		}
	}

	private void displayTitlePanel() {
		this.title.setFont(new Font(this.title.getFont().getFontName(), Font.BOLD, Constants.TITLE_FONT_SIZE));
		this.title.setText("ZAP Proxy Settings");
		this.title.setEditable(false);
		this.titlePanel.add(this.title);
		this.ProxyFrame.add(this.titlePanel, BorderLayout.NORTH);
	}
}
