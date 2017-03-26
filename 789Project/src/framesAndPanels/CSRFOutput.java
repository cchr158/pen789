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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
//import javax.swing.JTextPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;

import javafx.embed.swing.JFXPanel;
import pen789.CSRF;
import pen789.pen789;

public class CSRFOutput {
		private JProgressBar progressBar;
		private JFrame csrfFrame;
		private JPanel panel;
		private pen789 myPen789;
		private StringBuilder attackOutput;
//		private JFXPanel jfxPanel;
		private /*JTextPane*/JTextArea trafficDump;
		private JButton okButton;
		private JTextArea title;
		private CSRF csrf;
		private JFileChooser fc;
		private JButton saveOutputButton;
		
        public CSRFOutput(pen789 p, URL loginUrl, URL registerUrl, Map<String, String> users, JFileChooser fc) {
        	this.myPen789 = p;
        	csrfFrame = new JFrame("CSRF output");
        	csrfFrame.addWindowListener(this.myPen789.exitListener);
        	this.csrfFrame.setPreferredSize(new Dimension(2*Constants.SCREEN_SIZE.width/3,4*Constants.SCREEN_SIZE.height/5));
        	this.progressBar = new JProgressBar(0, 100);
            this.progressBar.setValue(0);
            this.progressBar.setStringPainted(true);
            this.progressBar.setPreferredSize(new Dimension(this.csrfFrame.getWidth(), Constants.LABLE_FONT_SIZE));
            this.attackOutput = new StringBuilder();
            this.panel = new JPanel();
//            this.jfxPanel = new JFXPanel();
//            this.jfxPanel.setPreferredSize(new Dimension(this.csrfFrame.getWidth(), 4*this.csrfFrame.getHeight()/5));
            this.trafficDump = new JTextArea(30,80);/* new JTextPane();*/
            this.trafficDump.setPreferredSize(new Dimension(this.csrfFrame.getWidth(), 4*this.csrfFrame.getHeight()/5));
            this.okButton = new JButton("OK");
            this.okButton.addActionListener(this.myPen789.cancleButtonListerner);
            this.okButton.setEnabled(false);
            this.title = new JTextArea();
            this.fc = fc;
            if(Files.exists(Paths.get(fc.getSelectedFile().getPath()))){
            	this.saveOutputButton = new JButton("Save report");
            }else{
            	this.saveOutputButton = new JButton("Append to report");
            }
        	this.saveOutputButton.setEnabled(false);
            this.showResults();            
            this.myPen789.nextFrame(csrfFrame);
            this.csrf = new CSRF(myPen789, this.attackOutput, this.trafficDump,/* this.jfxPanel,*/ loginUrl, registerUrl, users);
        	csrf.addPropertyChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent arg0) {
					if(arg0.getPropertyName().equals("progress")){
						progressBar.setValue(csrf.getProgress());
					}
				}
			});
        	
            this.runTest();
		}
        
		private void runTest() {
			this.csrf.execute();
			try {
				this.csrf.get();
			} catch (InterruptedException | ExecutionException e) {}
			finally{
				this.okButton.setEnabled(true);
				this.saveOutputButton.setEnabled(true);
				this.saveOutputButton.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent arg0) {
						PrintWriter w = null;
						try {
							if(Files.exists(Paths.get(fc.getSelectedFile().getPath()))){
								Files.write(Paths.get(fc.getSelectedFile().getPath()),
										("\n"+attackOutput.toString()).getBytes(), StandardOpenOption.APPEND);
							}else{
								int returnVal = fc.showSaveDialog(new JPanel());
								if(returnVal == JFileChooser.APPROVE_OPTION){
									File file = fc.getSelectedFile();
									w = new PrintWriter(file.getPath());
									w.println(attackOutput.toString());
									w.flush();
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}finally{
							if(w != null)w.close();
						}
					}
				});
			}
		}

		private void showResults() {
        	GridBagConstraints c = new GridBagConstraints();
        	this.panel.setLayout(new GridBagLayout());
    		this.panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
//    		set and add title.
    		this.title.setText("CSRF Test Output");
    		this.title.setFont(new Font(this.title.getFont().getFontName(), Font.BOLD, Constants.TITLE_FONT_SIZE));
    		this.title.setEditable(false);
    		JPanel titlePanel = new JPanel();
    		titlePanel.add(this.title);
    		this.csrfFrame.add(titlePanel, BorderLayout.NORTH);
//    		add progress bar.
    		c.fill = GridBagConstraints.HORIZONTAL;
    		c.gridwidth = 4;
    		c.gridheight = 1;
    		c.weightx = 1.0;
    		c.gridx = 0;
    		c.gridy = 0;
    		c.insets = new Insets(5,5,5,5);
    		this.panel.add(progressBar, c);
//    		set up and add output text area.
    		c.anchor = GridBagConstraints.CENTER;
    		c.gridy = GridBagConstraints.RELATIVE;
//    		this.panel.add(this.jfxPanel,c);
    		this.trafficDump.setMargin(new Insets(5,5,5,5));
    		this.trafficDump.setEditable(false);
    		DefaultCaret caret = (DefaultCaret)this.trafficDump.getCaret();
    		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    		this.panel.add(new JScrollPane(this.trafficDump), c);
//    		add ok button.
    		c.fill = GridBagConstraints.NONE;
    		c.anchor = GridBagConstraints.CENTER;
    		JPanel buttons = new JPanel(new GridLayout(1,0));
    		buttons.add(this.okButton);
    		buttons.add(this.saveOutputButton);
    		c.gridy = GridBagConstraints.RELATIVE;
    		this.panel.add(buttons, c);
    		this.csrfFrame.add(this.panel);
		}
}
