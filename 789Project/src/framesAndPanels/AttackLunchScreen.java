package framesAndPanels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.text.DefaultCaret;

import framesAndPanels.Constants.attacks;
import pen789.Attack;
import pen789.pen789;

public class AttackLunchScreen {
	private pen789 myPen789;
	private Map<String, Constants.attacks> attackTypes;
	private JFrame attackFrame;
	private JPanel attackPanel;
	private JTextArea title;
	private JTextArea attackOutput;
	private JButton startButton;
	private JButton cancleButton;
	private JButton nextButton;
	private JButton saveOutputButton;
	private JProgressBar progressBar;
	private JCheckBox spider;
	private JCheckBox active;
	private JCheckBox aJaxSpider;
	private boolean aJaxSpiderRun; 
	private JFileChooser fc;
	
	public AttackLunchScreen(pen789 p, attacks xss, attacks csrf, attacks sql) {
		this.myPen789 = p;
		this.attackTypes = new HashMap<String, Constants.attacks>();
		this.attackTypes.put("xss", xss);
		this.attackTypes.put("csrf", csrf);
		this.attackTypes.put("sql", sql);
		this.attackFrame = new JFrame("Attacks");
		this.attackFrame.addWindowListener(this.myPen789.exitListener);
		this.attackFrame.setPreferredSize(new Dimension(4*Constants.SCREEN_SIZE.width/5,5*Constants.SCREEN_SIZE.height/6));
		this.attackPanel = new JPanel();
		this.title = new JTextArea();
		this.attackOutput = new JTextArea(40,80);
		this.startButton = new JButton("Start");
		this.cancleButton = new JButton("Cancle");
		this.aJaxSpiderRun = false;
		if(this.attackTypes.containsValue(Constants.attacks.CSRF_ATTACK)){
			this.nextButton = new JButton("next");
			this.nextButton.setEnabled(false);
		}
		this.saveOutputButton = new JButton("Save report");
		this.progressBar = new JProgressBar(0, 100);
		this.progressBar.setPreferredSize(new Dimension(this.attackFrame.getWidth(), Constants.LABLE_FONT_SIZE));
		this.progressBar.setValue(0);
		this.progressBar.setStringPainted(true);
		this.spider = new JCheckBox("Spider scan (Fast)");
		this.active = new JCheckBox("Active scan (Slow)");
		this.aJaxSpider = new JCheckBox("aJax spider scan (Slow)");
		this.fc = new JFileChooser(System.getProperty("user.dir"));
		fc.setDialogTitle("save");
		String[] date = new String[]{new SimpleDateFormat("yyyy").format(Calendar.getInstance().getTime()),
				new SimpleDateFormat("MM").format(Calendar.getInstance().getTime()),
				new SimpleDateFormat("dd").format(Calendar.getInstance().getTime()),
				new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()),
				new SimpleDateFormat("mm").format(Calendar.getInstance().getTime()),
				new SimpleDateFormat("ss").format(Calendar.getInstance().getTime())};
		fc.setSelectedFile(new File("pen789Output-"+date[0]+date[1]+date[2]+date[3]+date[4]+date[5]+".txt"));
		buttonFunctions();
		showAttackLunchScreen();
		this.myPen789.nextFrame(attackFrame);
	}

	private void buttonFunctions() {
		this.cancleButton.addActionListener(this.myPen789.cancleButtonListerner);
		this.startButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				try {
					runScan();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		if(this.nextButton != null){
			this.nextButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					if(!aJaxSpiderRun){
						Toolkit.getDefaultToolkit().beep();
						JOptionPane.showMessageDialog(null, "aJax Spider scanner must have run before manual CSRF test can be run.\n"
								+ "After AJax is finished click next again.");
						aJaxSpider.setSelected(true);
						active.setSelected(false);
						spider.setSelected(false);
//						try {
//							runScan();
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
						aJaxSpiderRun=true;
					}else{
//						new CSRFScreen(myPen789, fc);
					}
					new CSRFScreen(myPen789, fc);
				}
			});
		}
		this.saveOutputButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				PrintWriter w = null;
				try {
					int returnVal = fc.showSaveDialog(new JPanel());
					if(returnVal == JFileChooser.APPROVE_OPTION){
						File file = fc.getSelectedFile();
						w = new PrintWriter(file.getPath());
						w.println(attackOutput.getText());
						w.flush();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}finally{
					if(w != null)w.close();
				}
			}
			
		});
	}

	protected Thread runScan() throws InterruptedException {
		Thread t = new Thread(new Runnable(){
			public void run() {
				if(spider.isSelected() || aJaxSpider.isSelected()){
					startButton.setEnabled(false);
					saveOutputButton.setEnabled(false);
					if(nextButton != null)nextButton.setEnabled(false);
					active.setEnabled(false);
					spider.setEnabled(false);
					aJaxSpider.setEnabled(false);
					SwingWorker<Void,Void> scan = new Attack(myPen789, attackTypes, attackOutput, active.isSelected(),
							spider.isSelected(), aJaxSpider.isSelected());					
					scan.addPropertyChangeListener(new PropertyChangeListener(){
						public void propertyChange(PropertyChangeEvent arg0) {
							if(arg0.getPropertyName().equals("progress")){
								progressBar.setValue(scan.getProgress());
							}
						}
					});
					scan.execute();
					try {
						scan.get();
					} catch (InterruptedException | ExecutionException e) {e.printStackTrace();}
					startButton.setEnabled(true);
					saveOutputButton.setEnabled(true);
					if(nextButton != null)nextButton.setEnabled(true);
					active.setEnabled(true);
					spider.setEnabled(true);
					aJaxSpider.setEnabled(true);
					aJaxSpiderRun = aJaxSpider.isSelected();
				}else if(active.isSelected()){
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(null, "Must select at least one of Spider scan or "
							+ "aJax scan to use Active scan.");
				}else{
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(null, "Must select at least one of the scan options.");
				}
			}
		});
		t.start();
		return t;
	}

	private void showAttackLunchScreen() {
		GridBagConstraints c = new GridBagConstraints();
		this.attackPanel.setLayout(new GridBagLayout());
		this.attackPanel.setBorder(BorderFactory.createEmptyBorder(5*this.attackPanel.getHeight()/100,
				10*this.attackPanel.getWidth()/100, 5*this.attackPanel.getHeight()/100, 10*this.attackPanel.getWidth()/100));
		StringBuilder titleText = new StringBuilder("");
		for(String k : this.attackTypes.keySet()){
			if(this.attackTypes.get(k) != attacks.NONE){
				titleText.append(this.attackTypes.get(k) == attacks.XSS_ATTACK?"XSS-":"");
				titleText.append(this.attackTypes.get(k) == attacks.CSRF_ATTACK?"CSRF-":"");
				titleText.append(this.attackTypes.get(k) == attacks.SQL_ATTACK?"SQL injection-":"");
			}
		}
		titleText.deleteCharAt(titleText.length()-1);
		this.title.setText(titleText.toString());
		this.title.setFont(new Font(this.title.getFont().getFontName(), Font.BOLD, Constants.TITLE_FONT_SIZE));
		this.title.setEditable(false);
		JPanel titlePanel = new JPanel();
		titlePanel.add(this.title);
		this.attackFrame.add(titlePanel, BorderLayout.NORTH);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = this.nextButton==null?4:5;
		c.gridheight = 1;
		c.weightx = this.nextButton==null?4:5;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(10,15,10,15);
		this.attackPanel.add(progressBar, c);
		c.anchor = GridBagConstraints.CENTER;
		c.weighty = this.nextButton==null?4:5;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		this.attackOutput.setMargin(new Insets(5,5,5,5));
		this.attackOutput.setEditable(false);
		DefaultCaret caret = (DefaultCaret)this.attackOutput.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		this.attackPanel.add(new JScrollPane(this.attackOutput), c);
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		JPanel buttons = new JPanel(new GridLayout(1,0));
		buttons.add(this.spider);
		buttons.add(this.active);
		buttons.add(this.aJaxSpider);
		buttons.add(this.startButton);
		buttons.add(this.cancleButton);
		buttons.add(this.saveOutputButton);
		if(this.nextButton != null)
			buttons.add(this.nextButton);
		c.gridy = 2;
		c.gridx = 3;
		c.insets = new Insets(5,10,20,10);
		this.attackPanel.add(buttons, c);
		this.attackFrame.add(this.attackPanel);
	}
}
