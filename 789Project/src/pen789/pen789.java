package pen789;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.text.DefaultCaret;

import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ApiResponseList;
import org.zaproxy.clientapi.core.ApiResponseSet;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;
import org.zaproxy.zap.ZAP;

import framesAndPanels.*;

public class pen789 {
	private Deque<JFrame> frames = new ArrayDeque<JFrame>();
	public WindowListener exitListener = new MyCancleButtonListener();
	public MyCancleButtonListener cancleButtonListerner = new MyCancleButtonListener();
	public static ClientApi api;
	public static String contextName;
	public String ZAP_ADDRESS;
	public boolean ZAP_ADDRESS_SET;
    public int ZAP_PORT;
    public boolean ZAP_PORT_SET;
	public String ZAP_API_KEY;
	public boolean ZAP_API_KEY_SET;
	public String target;
	public boolean target_SET;
	private String attackStrength;
	
	public pen789(){}
	
	public pen789(String address, int port, String key, String target){
		this.setZAPAddress(address);
		this.setZAPPort(port);
		this.setZAPAPIKEY(key);
		this.setTarget(target);
		this.setAttackStrength("High");
		new HomeScreen("pen789", this);
	}
	
	public void setZAPAPIKEY(String s){
		this.ZAP_API_KEY = s;
		this.ZAP_API_KEY_SET = s.length()>=26;
	}
	
	public void setTarget(String s){
		this.target = s;
		
		this.target_SET = true;
	}
	
	public void setZAPAddress(String s){
		this.ZAP_ADDRESS = s;
		this.ZAP_ADDRESS_SET = true;
	}
	
	public void setZAPPort(int i){
		this.ZAP_PORT = i;
		this.ZAP_PORT_SET = true;
	}
	
	public void setAttackStrength(String attackStrength) {
		this.attackStrength = attackStrength.toString();
	}
	
	public void nextFrame(JFrame frame){
		if(!this.frames.isEmpty()){
			this.frames.peek().setEnabled(false);
		}
		this.frames.push(frame);
		showRequest();
	}
	
	public void prevFrame(){
		if(this.frames.size() > 1){
			this.frames.pop().dispose();
			showRequest();
		}else{
			if(api != null){
				try {
					Thread zap = null;
					for(Thread t : Thread.getAllStackTraces().keySet()){
						if(t.getName().equals("ZAP-daemon")){
							zap = t;
						}
					}
					int i = JOptionPane.showOptionDialog(null, "Are you sure you want to quit?", "Exit", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, null, null);
					if (i == JOptionPane.YES_OPTION) {
						this.frames.pop().dispose();
						api.core.runGarbageCollection(ZAP_API_KEY);
						api.core.deleteAllAlerts(ZAP_API_KEY);
						api.core.shutdown(this.ZAP_API_KEY);
						if(zap != null){
							zap.join();
						}
					}else{
						return;
					}
				} catch (ClientApiException | InterruptedException e) {System.exit(1);}
			}
			System.exit(0);
		}
	}
	
	public void showRequest(){
		this.frames.peek().pack();
		this.frames.peek().setEnabled(true);
		this.frames.peek().setLocationRelativeTo(null);
		this.frames.peek().setVisible(true);		
	}
	
	public void resetFrames(){
		while(!this.frames.isEmpty())
			this.frames.pop().dispose();
		this.frames = new ArrayDeque<JFrame>();
	}

//	public void attack(boolean xss, boolean csrf, boolean sql) {
//		if(xss||csrf||sql){
//			new AttackLunchScreen(this, xss?attacks.XSS_ATTACK:attacks.NONE,
//					csrf?attacks.CSRF_ATTACK:attacks.NONE, sql?attacks.SQL_ATTACK:attacks.NONE);
//		}
//	}
	
	void activeScan(Attack scan) throws ClientApiException, InterruptedException {
		double start = System.currentTimeMillis()/1000.0;
		scan.output.append("Starting Active scan...\n");
		api.ascan.setOptionHandleAntiCSRFTokens(ZAP_API_KEY, true);
		scan.resp = api.ascan.scan(this.ZAP_API_KEY, this.target, "True", null, null, null,	null);
		scan.scanid = ((ApiResponseElement) scan.resp).getValue();
		api.ascan.pause(ZAP_API_KEY, scan.scanid);
		for(int i=0; i < ((ApiResponseList)api.ascan.policies(null, null)).getItems().size(); i++){
			switch(this.attackStrength){
			case "Insane":
				api.ascan.setPolicyAlertThreshold(ZAP_API_KEY, String.valueOf(i), "Low", null);
				break;
			case "High":
			case "Medium":
				api.ascan.setPolicyAlertThreshold(ZAP_API_KEY, String.valueOf(i), "Medium", null);
				break;
			case "Low":
				api.ascan.setPolicyAlertThreshold(ZAP_API_KEY, String.valueOf(i), "High", null);
				break;
			default:
				break;
			}
			api.ascan.setPolicyAttackStrength(ZAP_API_KEY, String.valueOf(i), this.attackStrength, null);
		}
		api.ascan.resume(ZAP_API_KEY, scan.scanid);
		int temp1 = scan.progress;
		while (scan.progress < 100) {
			Thread.sleep(10000);
			scan.progress = (int) (temp1 + Integer.parseInt(((ApiResponseElement)
					api.ascan.status(scan.scanid)).getValue()) * (1.0/scan.numberOfScanes));
			scan.output.append(String.format("Active scan %d%% of task.\n", (scan.progress-temp1)*scan.numberOfScanes));
			scan.setProgress(Math.min(scan.progress, 100));
			if(Integer.parseInt(((ApiResponseElement)api.ascan.status(scan.scanid)).getValue()) == 100)
				scan.setProgress(scan.progress=100);
		}
		Thread.sleep(2000);
		double end = System.currentTimeMillis()/1000.0;
		scan.output.append("Finished Active scan in "+String.valueOf(end-start).substring(0,3)+" seconds...\n");
	}
	
	void ajaxScan(Attack scan) throws ClientApiException, InterruptedException {
		double start = System.currentTimeMillis()/1000.0;
		scan.output.append("Starting ajaxSpider scan...\n");
		api.ajaxSpider.setOptionBrowserId(scan.myPen789.ZAP_API_KEY, "HtmlUnit");
		scan.resp = api.ajaxSpider.scan(scan.myPen789.ZAP_API_KEY, scan.myPen789.target, null);
		scan.scanid = ((ApiResponseElement) scan.resp).getValue();
		while(((ApiResponseElement)api.ajaxSpider.status()).getValue().equals("running")){
			Thread.sleep(5000);
			String numberOfResults = ((ApiResponseElement)(api.ajaxSpider.numberOfResults())).getValue();
			int temp = Integer.valueOf(numberOfResults);
			List<ApiResponse> mostRecentResults = ((ApiResponseList)api.ajaxSpider.results(null, null)).getItems();
			if(mostRecentResults != null){
				if(!mostRecentResults.isEmpty()){
					String mostRecentResult = ((ApiResponseSet)mostRecentResults.get(temp==0?0:temp-1)).getAttribute("requestHeader");
					mostRecentResult = mostRecentResult.split(" ")[1];
					scan.output.append("aJaxSpider scan: "+mostRecentResult+"      Number of results: "+numberOfResults+".\n");
				}
			}
		}
		scan.progress = scan.getProgress()+100/scan.numberOfScanes;
		scan.setProgress(Math.min(scan.progress, 100));
		Thread.sleep(2000);
		double end = System.currentTimeMillis()/1000.0;
		scan.output.append("Finished aJaxSpider scan in "+String.valueOf(end-start).substring(0,3)+" seconds...\n");
	}
	
	void spiderScan(Attack scan) throws ClientApiException, InterruptedException{
		double start = System.currentTimeMillis()/1000.0;
		scan.output.append("Starting Spider scan...\n");
		pen789.api.spider.setOptionMaxDepth(scan.myPen789.ZAP_API_KEY, 10);
		scan.resp = api.spider.scan(scan.myPen789.ZAP_API_KEY, scan.myPen789.target, null, "true", null, null);
		Thread.sleep(2000);
		scan.scanid = ((ApiResponseElement) scan.resp).getValue();
		int temp1 = scan.progress;
		while ((scan.progress-temp1)*scan.numberOfScanes < 99) {
			Thread.sleep(1000);
			scan.progress = (int) (temp1 + Integer.parseInt(((ApiResponseElement) 
					api.spider.status(scan.scanid)).getValue()) * (1.0/ scan.numberOfScanes));
			scan.output.append(String.format("Spider completed %d%% of task.\n", (scan.progress-temp1)*scan.numberOfScanes));
			scan.setProgress(Math.min(scan.progress, 100));
		}
		Thread.sleep(2000);
		double end = System.currentTimeMillis()/1000.0;
		scan.output.append("Finished Spider scan in "+String.valueOf(end-start).substring(0,3)+" seconds...\n");
	}

	
	class MyCancleButtonListener implements ActionListener, WindowListener{
		public void actionPerformed(ActionEvent e) {
			try {
				pen789.api.ascan.stopAllScans(ZAP_API_KEY);
				pen789.api.ajaxSpider.stop(ZAP_API_KEY);
				pen789.api.spider.stopAllScans(ZAP_API_KEY);
			} catch (ClientApiException e1) {
				e1.printStackTrace();
			}
			prevFrame();
		}
		public void windowClosed(WindowEvent arg0) {}
		public void windowClosing(WindowEvent arg0) {
			prevFrame();
		}
		public void windowDeactivated(WindowEvent arg0) {}
		public void windowDeiconified(WindowEvent arg0) {}
		public void windowIconified(WindowEvent arg0) {}
		public void windowOpened(WindowEvent arg0) {}
		public void windowActivated(WindowEvent e) {}
	}
	
	public static void main(String[] args) {
		/*
		 * TODO: DONT add runtime or install files to build path since it courses zap logger to fail.
		 */
		/*
		 * FIXME: Display contents need to be set relative to screen size.  
		 */
		String key="";
		do{
		 key = new BigInteger(130, new SecureRandom()).toString(32);
		}while(key.length() != 26);
		try {
			StartupTask st = new pen789().new StartupTask(key);
			st.addPropertyChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent arg0) {
					if(arg0.getPropertyName().equals("progress")){
						st.progressBar.setValue(st.getProgress());
					}
				}
			});
			ZAP.main(new String[]{"-daemon", "-config", "scanner.strength=HIGH", "-host",Constants.DEFUALT_LOCAL_PROXY_ADDRESS, "-port",
					Integer.toString(Constants.DEFUALT_PORT), "-config", "api.key="+key,"-dir", Constants.runDir, "-installdir",Constants.installDir});
			st.execute();
			st.get();
			pen789.contextName = String.valueOf((int)(Math.random()*Integer.MAX_VALUE+1));
			Thread.sleep(1000);
			pen789.api = new ClientApi(Constants.DEFUALT_LOCAL_PROXY_ADDRESS, Constants.DEFUALT_PORT);
			pen789.api.core.generateRootCA(key);
			pen789.api.context.newContext(key, contextName);
			new pen789(Constants.DEFUALT_LOCAL_PROXY_ADDRESS,Constants.DEFUALT_PORT, key, Constants.DEFUALT_TARGET);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "pen789 Failed to start.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private	class StartupTask extends SwingWorker<Void, Void> {
		private JProgressBar progressBar;
		private JFrame jf;
		private String key;
		private JTextArea output;
		
        public Void doInBackground() {
            boolean[] threadChecks = new boolean[5];
//            if(!(new File(Constants.runDir).exists() || new File(Constants.installDir).exists())){
//				this.install();
//			}
            try {
            	this.output.append("Starting ZAP...\n");
	            while (this.getProgress() < 100) {
	            	for(Thread t : Thread.getAllStackTraces().keySet()){
	            		if(t.getName().equals("ZAP-daemon") && !threadChecks[0]){
	            			this.output.append("Started: "+t.getName()+"\n");
	            			setProgress(Math.min(this.getProgress()+5, 100));
	            			threadChecks[0] = true;
	            		}else if(t.getName().equals("ZAP-DownloadManager") && !threadChecks[1]){
	            			this.output.append("Started: "+t.getName()+"\n");
	            			setProgress(Math.min(this.getProgress()+5, 100));
	            			threadChecks[1] = true;
	            		}else if(t.getName().equals("ZAP-ExtensionFilter") && ! threadChecks[2]){
	            			this.output.append("Started: "+t.getName()+"\n");
	            			setProgress(Math.min(this.getProgress()+5, 100));
	            			threadChecks[2] = true;
	            		}else if(t.getName().equals("ZAP-PassiveScanner") && !threadChecks[3]){
	            			this.output.append("Started: "+t.getName()+"\n");
	            			setProgress(Math.min(this.getProgress()+5, 100));
	            			threadChecks[3] = true;
	            		}else if(t.getName().equals("ZAP-ProxyServer") && t.getState().equals(Thread.State.RUNNABLE)){
	            			this.output.append("Started: "+t.getName()+"\n");
	            			threadChecks[4] = true;
//	            			pen789.contextName = String.valueOf((int)(Math.random()*Integer.MAX_VALUE+1));
//	            			this.output.append("Get random context name: "+pen789.contextName+"\n");
//	            			Thread.sleep(1000);
//	            			pen789.api = new ClientApi(Constants.DEFUALT_LOCAL_PROXY_ADDRESS, Constants.DEFUALT_PORT);
//	            			this.output.append("New API created...\n");
//	            			pen789.api.context.newContext(key, contextName);
//	            			this.output.append("Created new contex...\n");
	            			setProgress(Math.min(this.getProgress()+10, 100));
	            		}
	            	}
	            	if(threadChecks[0] && threadChecks[1] && threadChecks[2] && threadChecks[3] && threadChecks[4]){
//	            		if(!((ApiResponseElement)pen789.api.autoupdate.isLatestVersion()).getValue().equals("true")){
//	            			this.output.append("Updateing ZAP...\n");
//		            		pen789.api.autoupdate.downloadLatestRelease(this.key);
//		            		this.output.append("Changing ZAP settings...\n");
//		            		pen789.api.autoupdate.setOptionCheckAddonUpdates(this.key, true);
//		            		pen789.api.autoupdate.setOptionCheckOnStart(this.key, true);
//		            		pen789.api.autoupdate.setOptionDownloadNewRelease(this.key, true);
//		            		pen789.api.autoupdate.setOptionInstallAddonUpdates(this.key, true);
//		            		pen789.api.autoupdate.setOptionInstallScannerRules(this.key, true);
//		            		pen789.api.autoupdate.setOptionReportAlphaAddons(this.key, true);
//		            		pen789.api.autoupdate.setOptionReportReleaseAddons(this.key, true);
//	            		}
	            		this.output.append("Done!");
	            		setProgress(100);
	            	}
					Thread.sleep(1000);
	            }
            } catch (InterruptedException e) {
            	JOptionPane.showMessageDialog(null, "ZAP Failed to start.");
            	System.exit(1);
            } /*catch (ClientApiException e) {
            	JOptionPane.showMessageDialog(null, "ZAP Failed to update.");
				e.printStackTrace();
			} */catch (Exception e) {
				JOptionPane.showMessageDialog(null, "ZAP Failed to start.");
				e.printStackTrace();
            	System.exit(1);
			}
            return null;
        }
        protected void done(){
        	jf.dispose();
        }
        private void install() {
    		try {
    			this.output.append("Beggining installation...\n");
    			String dir = "ZAP_Instal_Files";
    			if(!Files.exists(Paths.get(Constants.installDir))){
    				this.extractFiles(dir,Constants.installFileNames,0.4);
    			}
    			dir = "ZAP_Runtime_Files";
    			if(!Files.exists(Paths.get(Constants.runDir))){
    				this.extractFiles(dir,Constants.runtimeFileNames,0.2);
    			}
    			this.output.append("Finished installation...\n");
    		} catch (IOException e) {
    			JOptionPane.showMessageDialog(null, "ZAP Failed to install.");
    			e.printStackTrace();
    		}
    	}
        private void extractFiles(String dir, String[] fileNames, double percentage) throws IOException {
        	int temp = this.getProgress();
        	if(Paths.get(System.getProperty("user.dir"),dir).toFile().mkdir()){
				for(int i=0; i<fileNames.length; i++){
					if(fileNames[i].endsWith("\\")){
						Paths.get(System.getProperty("user.dir"),dir,System.getProperty("os.name").contains("Windows")?
								fileNames[i]:fileNames[i].replace("\\", "/")).toFile().mkdirs();
						continue;
					}
					File file = Paths.get(System.getProperty("user.dir"),dir,System.getProperty("os.name").contains("Windows")?
							fileNames[i]:fileNames[i].replace("\\", "/")).toFile();
					if(!file.exists()){
						file.getParentFile().mkdirs();
						InputStream link = (pen789.class.getResourceAsStream(fileNames[i].replace("\\", "/")));
						if(link == null){
							int start = fileNames[i].lastIndexOf('\\');
							link = (pen789.class.getResourceAsStream(fileNames[i].substring(start).replace("\\", "/")));
						}
						if(link!=null){
							System.out.print("Extracting: "+(link==null)+" "+file.getAbsolutePath()+"\n");
							this.output.append("Extracting: "+file.getAbsolutePath()+"\n");
							Files.copy(link, file.getAbsoluteFile().toPath());
							link.close();
						}
					}
					this.setProgress(temp+(int)(100*percentage*i/fileNames.length));
				}
			}else{throw new IOException("Could not create "+dir+" directory.");}			
		}
		public StartupTask(String key){
        	jf = new JFrame("Install and Starting...");
        	jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        	this.key = key;
        	this.output = new JTextArea(20,60);
        	this.output.setEditable(false);
        	DefaultCaret caret = (DefaultCaret)this.output.getCaret();
    		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        	progressBar = new JProgressBar(0, 100);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            progressBar.setPreferredSize(new Dimension((int)(this.jf.getWidth()*0.99),
            		Constants.TEXTFEILD_FONT_SIZE));
            JPanel panel = new JPanel();
            GridBagConstraints c = new GridBagConstraints();
            panel.setLayout(new GridBagLayout());
            c.fill = GridBagConstraints.HORIZONTAL;
    		c.gridwidth = 4;
    		c.gridheight = 1;
    		c.weightx = 1.0;
    		c.gridx = 0;
    		c.gridy = 0;
    		c.insets = new Insets(10,15,10,15);
            panel.add(progressBar,c);
            c.gridy=GridBagConstraints.RELATIVE;
            panel.add(new JScrollPane(this.output),c);
            jf.add(panel);
            jf.pack();
            jf.setLocationRelativeTo(null);
            jf.setVisible(true);
//            setProgress(0);
        }
    }
}
