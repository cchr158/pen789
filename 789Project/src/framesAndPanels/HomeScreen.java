package framesAndPanels;

import pen789.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import java.awt.Color;

import org.zaproxy.clientapi.core.ClientApiException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomeScreen{
	private pen789 myPen789;
	private JPanel titelPanel;
	private JTextPane titelPane;
	private JTextPane instructionPane;
	private JPanel inputsPanel;
	private JLabel targetLabel;
	private JButton zapProxyButton;
	private JTextField targetTextField;
	private JButton okButton;
	private JButton canclButton;
	private JFrame homeFrame;
	private JPanel instructionPanel;

	public HomeScreen(String name, pen789 myPen789) {
		this.myPen789 = myPen789;
		homeFrame = new JFrame(name);
		homeFrame.addWindowListener(this.myPen789.exitListener);
		homeFrame.setPreferredSize(new Dimension(9*Constants.SCREEN_SIZE.width/10,
				2*Constants.SCREEN_SIZE.height/3));
		this.homeFrame.setLayout(new GridLayout(0,1));
		this.titelPanel = new JPanel();
		this.titelPane = new JTextPane();
		this.instructionPanel = new JPanel();
		this.instructionPane = new JTextPane();
		instructionOfUse();

		this.inputsPanel = new JPanel();
		this.zapProxyButton = new JButton();
		this.targetLabel = new JLabel();
		this.targetTextField = new JTextField();
		this.okButton = new JButton("OK");
		this.canclButton = new JButton("Cancle");
		inputFeilds();

		homeFrame.add(this.inputsPanel, BorderLayout.CENTER);
		this.myPen789.nextFrame(homeFrame);
	}

	private void inputFeilds() {
		GridBagConstraints c = new GridBagConstraints();
		this.inputsPanel.setLayout(new GridBagLayout());
		this.zapProxyButton.setText("ZAP Proxy Settings");
		this.zapProxyButton.setFont(new Font(this.zapProxyButton.getFont().getFontName(),
				Font.PLAIN, Constants.BUTTON_FONT_SIZE));
		this.zapProxyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new ZapProxySettings(myPen789);
			}
		});
		this.targetLabel.setFont(new Font(this.targetLabel.getFont().getFontName(), 
				Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.targetLabel.setText("Enter target IP/URL: ");
		this.targetTextField.setFont(new Font(this.targetTextField.getFont().getFontName(),
				Font.PLAIN, Constants.LABLE_FONT_SIZE));
		this.targetTextField.setPreferredSize(new Dimension(this.homeFrame.getWidth()/3,
				Constants.TEXTFEILD_FONT_SIZE));
		this.targetTextField.setText(this.myPen789.target);
		this.inputsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		this.inputsPanel.add(this.zapProxyButton, c);
		c.gridwidth = 2;
		c.weightx = 0.0;
		c.gridx = 0;
		c.gridy = 1;
		c.insets = new Insets(10, 0, 0, 0);
		this.inputsPanel.add(this.targetLabel, c);
		c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		this.inputsPanel.add(this.targetTextField, c);
		this.canclButton.addActionListener(this.myPen789.cancleButtonListerner);
		this.okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okButtonPushed();
			}
		});
		JPanel controlButtons = new JPanel();
		controlButtons.setLayout(new GridLayout(1,0));
		controlButtons.add(this.okButton);
		controlButtons.add(this.canclButton);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.insets = new Insets(10, 0, 10, 0);
		this.inputsPanel.add(controlButtons, c);
		this.inputsPanel.setBackground(Color.WHITE);
	}

	private void okButtonPushed() {
		if(this.targetTextField.getText().isEmpty()){
			JOptionPane.showMessageDialog(null, "A target is needed to run the tests against.");
		}else{
			this.myPen789.setTarget(this.targetTextField.getText());
			try {
				pen789.api.context.includeInContext(this.myPen789.ZAP_API_KEY, pen789.contextName, this.myPen789.target);
				if(this.myPen789.ZAP_ADDRESS_SET && this.myPen789.ZAP_API_KEY_SET && this.myPen789.ZAP_PORT_SET){
					new AttackOptionScreen(this.myPen789);
				}else{
					JOptionPane.showMessageDialog(null, "ZAP proxy settings must be configured in order to perform any tests.");
				}
			} catch (ClientApiException e) {
				JOptionPane.showMessageDialog(null, "ZAP failed to add this target to this context.");
			}
		}
	}

	private void instructionOfUse() {
//		TODO check spelling of instructions.
		String title = "<HTML><head></head><body><FONT size=\"7\"><p align=\"center\"><b>Welcome to pen789!</b>"
				+ "</p></FONT></body></HTML>";
		this.titelPane.setContentType("text/html");
		this.titelPane.setText(title);
		this.titelPane.setEditable(false);
		String instructions = "<HTML><head></head><body> <FONT size='6'>  <p>Welcome to pen789! "
				+ "This is a basic penetration testing tool. This tool makes use of the OWASP ZAP Proxy "
				+ "(https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project) API. You should not "
				+ "use this tool against any web application or sevice you dont have permision to test.</p>  "
				+ "<p>You can set up the ZAP proxy in the ZAP Proxy Settings menu. Do NOT change any of the "
				+ "defualts unless you are fermiliar with the ZAP API.</p>  <p>You must then set a target before "
				+ "running any tests.</p> </FONT></body></HTML>";
		this.instructionPane.setContentType("text/html");
		this.instructionPane.setText(instructions);
		this.instructionPane.setEditable(false);
		this.titelPanel.setLayout(new GridLayout(0,1));
		this.titelPanel.add(this.titelPane, BorderLayout.CENTER);
		this.titelPanel.setBackground(Color.WHITE);
		this.instructionPanel.setLayout(new GridLayout(0,1));
		this.instructionPanel.add(this.instructionPane, BorderLayout.CENTER);
		this.instructionPanel.setBackground(Color.WHITE);
		this.homeFrame.add(this.titelPanel,BorderLayout.NORTH);
		this.homeFrame.add(this.instructionPanel, BorderLayout.NORTH);
	}
}
