package pen789;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Request;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ApiResponseList;
import org.zaproxy.clientapi.core.ApiResponseSet;
import org.zaproxy.clientapi.core.ClientApiException;

import framesAndPanels.Constants;

public class CSRF extends SwingWorker<Void, Void>{	
	
	private pen789 myPen789;
	private Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(Constants.DEFUALT_LOCAL_PROXY_ADDRESS,
			Constants.DEFUALT_PORT));
	private URL loginUrl;
	private URL registrationUrl;
	private URL testUrl;
	private Map<String, String> users;
	private Tuple<String, String, String> victimUser;
	private Tuple<String, String, String> attackUser;
	private CookieManager victimCookieManager;
	private CookieManager attackerCookieManager;
	private StringBuilder attackOutput;
	private List<Tuple<String, String, String>> loginInputs;
	private List<Tuple<String, String, String>> victimBody;
	private List<Tuple<String, String, String>> attackBody;
	private static final Map<String,String> replaceChars = new HashMap<String, String>();
	private JTextArea trafficDump;
	private String contextId;
	private class Tuple<T, K,V>{
		private K name;
		private V value;
		private T type;
		public Tuple(T t, K k, V v){
			this.type = t;
			this.name = k;
			this.value = v;
		}
		public void setV(V v){this.value=v;}
	}
	
	public CSRF(pen789 myPen789, StringBuilder attackOutput, JTextArea trafficDump, URL loginUrl, URL registrationUrl, Map<String, String> users) {
		this.myPen789 = myPen789;
		this.loginUrl = loginUrl;
		this.registrationUrl = registrationUrl;
		this.users = users;
		this.attackOutput = attackOutput;
		this.trafficDump = trafficDump;
		this.victimCookieManager = new CookieManager();
		this.attackerCookieManager = new CookieManager();
		char[] chars = new char[]{' ','$', '&','`',':','<','>','[',']','{','}','"','+','#','%','@','/',';','=',
				'?','\\','^','|','~','\'',','};
		for(char i : chars)
			replaceChars.put(String.valueOf(i), "%"+Integer.toHexString(i));
//		System.setProperty("http.proxyHost", Constants.DEFUALT_LOCAL_PROXY_ADDRESS);
//		System.setProperty("http.proxyPort", String.valueOf(Constants.DEFUALT_PORT));
	}

	protected Void doInBackground() throws Exception {
		this.setProgress(0);
		this.contextId = ((ApiResponseSet)pen789.api.context.context(pen789.contextName)).getAttribute("id");
		this.trafficDump.append("CSRF Test Started...<br>\n");
		this.trafficDump.append("============HTTP Requests and Responses============<br>\n<br>\n");
		this.loginInputs = this.getInputs(this.loginUrl,true);//5=15%
		this.setProgress(Math.min(this.getProgress() + 30, 100));
		if( this.loginInputs == null){
			this.testFailed("82 Test Failed! No inputs on registration or login page.");
		}
		this.setupAuthentication();//1.33=4%
		this.trafficDump .append("Authentication setup...<br>\n");
		this.setProgress(Math.min(this.getProgress() + 4, 100));
		this.createUsers();//4=12%
		this.trafficDump.append("Users created in ZAP database...<br>\n");
//		perform spider scan as victim user to reveal authentication space.
		this.trafficDump.append("==========Perform Spider scan as victim user==========<br>\n"
				+ "***This uncovers the contents of urls that previously required authentication***<br>\n");
		this.setProgress(this.spiderScan(this.getProgress(), 0.15));//5=15%
//		get message that will be used by victim.
		String testAuthenticationURL = getAuthenticationURL();//5=15%
		this.setProgress(Math.min(this.getProgress() + 15, 100));
		if(testAuthenticationURL != null){
			this.testUrl = new URL(testAuthenticationURL);
	//		victim user makes request in victim session.
			String[] RandR = this.postRequest(this.testUrl, this.victimCookieManager, 
					this.buildBody("victimSession"));//1=3%
			this.setProgress(Math.min(this.getProgress() + 3, 100));
			if(RandR == null){
				this.testFailed("108 Test Failed! Victim user failed to post request.");
			}
			this.trafficDump .append("<br>\n<br>\n============Victim User Post Request============<br>\n<br>\n");
			this.trafficDump.append(RandR[0]);
			this.trafficDump .append("<br>\n<br>\n============Victim User Post Response============<br>\n<br>\n");
			this.trafficDump.append(RandR[2]+"<br>\n");
			this.trafficDump.append(RandR[1]+"<br>\n");
	//		make attack request body
			this.trafficDump.append("***The attacking user will now repeat the request of the victim user"
					+ " using the victim user session ID. If it is successful then a CSRF attack is possible***<br>\n<br>\n");
			this.attackBody = this.getInputs(this.testUrl,false);//1=3%
			this.setProgress(Math.min(this.getProgress() + 3, 100));
			RandR = this.postRequest(this.testUrl, this.victimCookieManager,
					this.buildBody("attackSession"));//1=3%
			this.setProgress(Math.min(this.getProgress() + 3, 100));
			if(RandR == null){
				this.trafficDump.append("CSRF attack might be possible. Check request and response pairs above from both victim and attacking users.<br>\n");
				this.setProgress(100);
				this.cancel(true);
			}
			this.trafficDump.append("<br>\n<br>\n============Attacking User Post Request============<br>\n<br>\n");
			this.trafficDump.append(RandR[0]);
			this.trafficDump.append("<br>\n<br>\n============Attacking User Post Response============<br>\n<br>\n");
			this.trafficDump.append(RandR[2]+"<br>\n");
			this.trafficDump.append(RandR[1]+"<br>\n");
			if(RandR[1].contains(this.victimUser.name)){
				this.trafficDump.append("CSRF attack is likely pressent.<br>\n");
			}else{
				this.trafficDump.append("CSRF attack might be possible. Check request and response pairs above from both victim and attacking users.<br>\n");//0.5=1.5%
			}
			this.setProgress(Math.min(this.getProgress() + 3, 100));
		}else{
			this.testFailed("144 Test Failed! No Post requests found by spider.");
		}
		return null;
	}
	
	private String buildBody(String sessionName) {
		StringBuilder body = new StringBuilder();
		Tuple<String, String, String> user = sessionName.equals("victimSession")?this.victimUser:this.attackUser;
		for(Tuple<String, String, String> p : sessionName.equals("victimSession")?this.victimBody:this.attackBody){
			body.append(p.name);
			body.append('=');
			if(p.type != null && !p.type.equals("Text")){
				if(p.name.equals(user.name) && p.type.equals("Hidden")){
					body.append(user.name);
				}else{
					body.append(p.value==null?"ZAPZAP":p.value);
				}
			}else{
				if(sessionName.equals("victimSession")){
					body.append(this.charReplacment("Im the victim user: "+user.name,"%"));
				}else{
					body.append(this.charReplacment("Im the attacking user: "+user.name
					+" pretending to be user: "+this.victimUser.name,"%"));
				}
			}
			body.append('&');
		}
		body.deleteCharAt(body.length()-1);
		return body.toString();
	}

	private List<Tuple<String, String, String>> getInputs(URL url, boolean show) {
		try{
			List<Tuple<String, String, String>> temp = new ArrayList<Tuple<String, String, String>>();
			String[] response = this.getRequest(url, this.victimCookieManager/*this.victimSession*/);
			Document doc = Jsoup.parse(response[1]);
			if(show){
				this.trafficDump.append("\n\n============Victim User GET Response============<br>\n\n");
				this.trafficDump.append(response[0]+"<br>\n");
				this.trafficDump.append(doc.toString()+"<br>\n");
			}
			for(String inputs : new String[]{"input", "textarea"}){
				ArrayList<Element> attributes = doc.select(inputs);
				String[] types = new String[]{"Date","Datetime-local","Email","File","Hidden","Month","Number", "Password","Search","Tel",
						"Text","Time","Url","Week"};
				for(Element e : attributes){
					String type = e.attr("TYPE");
					if(type.isEmpty())
						type = e.attr("type");
					if(type.isEmpty())
						type = e.attr("Type");
					String tmp = "Name";
					String name = e.attr(tmp);
					if(name.isEmpty())
						name = e.attr(tmp.toLowerCase());
					if(name.isEmpty())
						name = e.attr(tmp.toUpperCase());
					String id = e.attr(tmp="Id");
					if(id.isEmpty())
						id = e.attr(id.toLowerCase());
					if(id.isEmpty())
						id = e.attr(id.toUpperCase());
					String value = e.attr(tmp="Value");
					if(value.isEmpty())
						value = e.attr(tmp.toLowerCase());
					if(value.isEmpty())
						value = e.attr(tmp.toUpperCase());
					if(!type.isEmpty()){
						outerLoop:
						for(String s : types){
							if(type.equalsIgnoreCase(s)){
								if(!(name.isEmpty() && id.isEmpty())){
									switch(s){
									case "Date":
										temp.add(new Tuple<String, String, String>(types[0], name.isEmpty()?id:name,
												value.isEmpty()?"1970-01-01":value));
										break outerLoop;
									case "Datetime-local":
										temp.add(new Tuple<String, String, String>(types[1], name.isEmpty()?id:name,
												value.isEmpty()?"1970-01-01T00:00":value));
										break outerLoop;
									case "Email":
										temp.add(new Tuple<String, String, String>(types[2], name.isEmpty()?id:name, 
												value.isEmpty()?"thisIsAnEmailAddress@trustMe.com":value));
										break outerLoop;
									case "File":
										temp.add(new Tuple<String, String, String>(types[3], name.isEmpty()?id:name, 
												value.isEmpty()?"ZAP.txt":value));
										break outerLoop;
									case "Hidden":
										temp.add(new Tuple<String, String, String>(types[4], name.isEmpty()?id:name,
												value.isEmpty()?"I-am-hidden":value));
										break outerLoop;
									case "Month":
										temp.add(new Tuple<String, String, String>(types[5], name.isEmpty()?id:name, 
												value.isEmpty()?"1970-01":value));
										break outerLoop;
									case "Number":
										temp.add(new Tuple<String, String, String>(types[6], name.isEmpty()?id:name,
												value.isEmpty()?String.valueOf(Integer.MAX_VALUE):value));
										break outerLoop;
									case "Password":
										temp.add(new Tuple<String, String, String>(types[7], name.isEmpty()?id:name, null));
										break outerLoop;
									case "Search":
										temp.add(new Tuple<String, String, String>(types[8], name.isEmpty()?id:name, 
												value.isEmpty()?"looking for CSRF's":value));
										break outerLoop;
									case "Tel":
										temp.add(new Tuple<String, String, String>(types[9], name.isEmpty()?id:name, 
												value.isEmpty()?"+6499999999":value));
										break outerLoop;
									case "Time":
										temp.add(new Tuple<String, String, String>(types[11], name.isEmpty()?id:name, 
												value.isEmpty()?"00:00":value));
										break outerLoop;
									case "Url":
										temp.add(new Tuple<String, String, String>(types[12], name.isEmpty()?id:name, 
												value.isEmpty()?"http://www.example.com":value));
										break outerLoop;
									case "Week":
										temp.add(new Tuple<String, String, String>(types[14], name.isEmpty()?id:name, 
												value.isEmpty()?"1970-W01":value));
										break outerLoop;
									case "Text":
									default:
										temp.add(new Tuple<String, String, String>(types[10], name.isEmpty()?id:name, 
												value.isEmpty()?"ZAP test for CSRF.":value));
										break outerLoop;
									}
								}
							}
						}
					}else{
						temp.add(new Tuple<String, String, String>(types[10], name.isEmpty()?id:name, 
								value.isEmpty()?"ZAP test for CSRF.":value));
					}
				}
			}
			return temp.size()==0?null:temp;
		}catch (Exception e) {
		    e.printStackTrace();
		    this.testFailed("254 Test Failed! Failed to get inputs for: "+url.toString());
		    return null;
		}
	}

	private String[] getRequest(URL url, CookieManager session){
		try{
//			build http request header.
			HttpURLConnection connection = (HttpURLConnection)url.openConnection(proxy);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0");
			connection.setRequestProperty("Accept-Language", "en-GB,en;q=0.5");
			connection.setRequestProperty("Referer", url.toString());
			connection.setRequestProperty("Connection", "keep-alive");
			connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Host", url.getAuthority());
			connection.setRequestProperty("Cache-Control","no-cache");
			List<HttpCookie> cookieList = session.getCookieStore().getCookies();
			String cookieString = "";
			for(int i=0; i<cookieList.size(); i++){
				 cookieString += cookieList.get(i).toString()+(i>0?";":"");
			}
			if(!cookieString.isEmpty())connection.setRequestProperty("Cookie",cookieString);
//			Get Response.
		    String[] response = new String[]{"",""};
		    for(String i : connection.getHeaderFields().keySet()){
		    	response[0] += i+": ";
		    	for(String j: connection.getHeaderFields().get(i)){
		    		response[0] += j;
		    	}
		    	response[0] += "\n<br>";
		    }
		    BufferedReader in = new BufferedReader(
			        new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer sb = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				sb.append(inputLine);
			}
			List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
			if(cookies != null){
				for(String cookie : cookies){
					for(HttpCookie c : HttpCookie.parse(cookie)){
						if(!session.getCookieStore().getCookies().contains(c)){
							session.getCookieStore().add(new URI(url.toString()), c);
						}
					}
					response[0]+= cookie+"\n<br>";
				}
			}
			response[1] = Jsoup.parse(sb.toString()).toString();
			response[0] = "GET: "+url.toString()+"\n<br>"+connection.getResponseCode()+": "+connection.getResponseMessage()+"\n<br>"+response[0];
		    if(connection.getResponseCode() == 200){
		    	return response;
		    }else{return null;}
//		} catch (SocketException | NullPointerException e) {
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e1) {}
//			return getRequest(url, session);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String[] postRequest(URL url, /*Map<String, String>*/CookieManager session, String body){
		try{
//			build http request header.
			HttpURLConnection connection = (HttpURLConnection)url.openConnection(proxy);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0");
			connection.setRequestProperty("Accept-Language", "en-GB,en;q=0.5");
			connection.setRequestProperty("Referer", url.toString());
			connection.setRequestProperty("Connection", "keep-alive");
			connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Host", url.getAuthority());
			connection.setRequestProperty("Cache-Control","no-cache");
			connection.setRequestProperty("Content-Length", String.valueOf(body.getBytes().length));
			List<HttpCookie> cookieList = session.getCookieStore().getCookies();
			String cookieString = "";
			for(int i=0; i<cookieList.size(); i++){
				 cookieString += cookieList.get(i).toString()+(i>0?";":"");
			}
			if(!cookieString.isEmpty())connection.setRequestProperty("Cookie",cookieString);
			String request = "POST: "+url.toString()+"\n<br>";
			for(String i: connection.getRequestProperties().keySet()){
				request += i+": ";
				for(String j : connection.getRequestProperties().get(i)){
					request += j+";";
				}
				request+="\n<br>";
			}
//			for(HttpCookie cookie : session.getCookieStore().getCookies()){
//				 request += "Cookie: "+cookie.toString();
//			}
			request += "Body: "+body+"\n<br>";
//			Send request.
			connection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(body);
			wr.flush();
			wr.close();
//			Get Response.
			BufferedReader in = new BufferedReader(
				        new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer sb = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				sb.append(inputLine);
			}
		    String[] response = new String[]{"",""};
		    response[0] = connection.getResponseCode()+": "+ connection.getResponseMessage()+"\n<br>";
		    for(String i : connection.getHeaderFields().keySet()){
		    	response[0] += i+": ";
		    	for(String j: connection.getHeaderFields().get(i)){
		    		response[0] += j;
		    	}
		    	response[0] += "\n<br>";
		    }
		    List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
			if(cookies != null){
				for(String cookie : cookies){
					for(HttpCookie c : HttpCookie.parse(cookie)){
						if(!session.getCookieStore().getCookies().contains(c)){
							session.getCookieStore().add(new URI(url.toString()), c);
						}
					}
				}
			}
			cookies = connection.getHeaderFields().get("Cookie");
			if(cookies != null){
				response[0] += "Cookie: ";
				for(String cookie : cookies){
					response[0] += cookie+" ";
				}
				response[0]+="\n<br>";
			}
		    response[1] = Jsoup.parse(sb.toString()).toString();
		    if(connection.getResponseCode() == 200){
		    	return new String[]{request, response[1], response[0]};//request Headers+body, response body, response header.
		    }else{return null;}
		} catch (Exception e) {
		    e.printStackTrace();
		    return null;
		  }
	}
	//gets a URL in the authentication space.
	private String getAuthenticationURL() throws MalformedURLException {
		try {
			List<ApiResponse> messages = ((ApiResponseList)pen789.api.core.messages(this.myPen789.target, null, null)).getItems();
			for(ApiResponse resp : messages){
				String[] messageHeader = ((ApiResponseSet)resp).getAttribute("requestHeader").split(" ");
				if(messageHeader[0].equals("POST") && !messageHeader[1].equals(this.loginUrl.toString()) &&
						!messageHeader[1].equals(this.registrationUrl.toString())){
					this.victimBody = this.getInputs(new URL(messageHeader[1]),true);
					return messageHeader[1];
				}
			}
		} catch (ClientApiException e) {
			e.printStackTrace();
			this.testFailed("272 Test Failed! Couldn't aquire messages from ZAP API.");
		}
		return null;
	}
	//Rescan with spider to reveal url's in the authentication space.
	private int spiderScan(int progress, double d) {
		try {
			pen789.api.spider.setOptionMaxDepth(this.myPen789.ZAP_API_KEY, this.myPen789.attackStrength.equals("Low")?1:
				this.myPen789.attackStrength.equals("Medium")?5:
					this.myPen789.attackStrength.equals("High")?10:20);
			pen789.api.spider.setOptionParseComments(this.myPen789.ZAP_API_KEY, true);
			pen789.api.spider.setOptionParseRobotsTxt(this.myPen789.ZAP_API_KEY, true);
			pen789.api.spider.setOptionParseSVNEntries(this.myPen789.ZAP_API_KEY, true);
			pen789.api.spider.setOptionParseSitemapXml(this.myPen789.ZAP_API_KEY, true);
			pen789.api.spider.setOptionPostForm(this.myPen789.ZAP_API_KEY, true);
			pen789.api.spider.setOptionProcessForm(this.myPen789.ZAP_API_KEY, true);
			pen789.api.spider.setOptionSendRefererHeader(this.myPen789.ZAP_API_KEY, true);
			pen789.api.spider.setOptionMaxDuration(this.myPen789.ZAP_API_KEY, this.myPen789.attackStrength.equals("Insane")?60:0);
			String scanid = ((ApiResponseElement) pen789.api.spider.scanAsUser(this.myPen789.ZAP_API_KEY, contextId,
					this.victimUser.type, this.myPen789.target, this.myPen789.attackStrength.equals("Low")?"1":
						this.myPen789.attackStrength.equals("Medium")?"20":
							this.myPen789.attackStrength.equals("High")?"40":"0", null, null)).getValue();
			Thread.sleep(2000);
			int temp = progress;
			while(temp + d*100 > progress){
				this.trafficDump.append("task: "+(Integer.parseInt(((ApiResponseElement) 
						pen789.api.spider.status(scanid)).getValue()))+"%<br>\n");
				progress = (int) (temp + Double.parseDouble(((ApiResponseElement) pen789.api.spider.status(scanid)).getValue())*d);
				this.setProgress(Math.min(progress, 100));
				Thread.sleep(5000);
			}
			Thread.sleep(2000);
		} catch (ClientApiException | InterruptedException e) {
			e.printStackTrace();
			this.testFailed("294 Test Failed! Spider scan failed.");
		}
		return Math.min(progress, 100);
	}

	protected void done(){
		try {
			pen789.api.users.removeUser(this.myPen789.ZAP_API_KEY, this.contextId, this.victimUser.type);
			pen789.api.users.removeUser(this.myPen789.ZAP_API_KEY, this.contextId, this.attackUser.type);
			Toolkit.getDefaultToolkit().beep();
			this.attackOutput.append(this.trafficDump.getText());
			pen789.api.core.runGarbageCollection(this.myPen789.ZAP_API_KEY);
		} catch (ClientApiException e) {
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(null, "Test Complete.");
	}

	private String[] loginOrRegisterUser(String username, String password1, URL loginURL, CookieManager session){
//		Sanitise inputs.
		username = this.charReplacment(username,"%");
		password1 = this.charReplacment(password1,"%");
		try{
			StringBuilder body = new StringBuilder();
			for(Tuple<String, String, String> p : this.loginInputs){
				if(p.value != null)
					p.setV(this.charReplacment(p.value,""));
				body.append(p.name);
				body.append('=');
				if(p.type.equals("Text")){
					switch(p.name){
					case "username":
					case "name":
						body.append(username);
						break;
					default:
						body.append(p.value);
						break;
					}
				}else if(p.type.equals("Password")){
					body.append(password1);
				}else{
					body.append(p.value);
				}
				body.append('&');
			}
			body.deleteCharAt(body.length()-1);
//			Send request.
			String[] temp = this.postRequest(loginURL, session, body.toString());
			if(temp != null){
				return temp;//new String[]{request, response[1], response[0]};//request Headers+body, response body, response header.
			}else{return null;}
		} catch (Exception e) {
		    e.printStackTrace();
		    return null;
		  }
	}
	
	private void setupAuthentication(){
//		FIXME get the session created by zap to be active.
		try {
			StringBuilder params = new StringBuilder();
			params.append("loginUrl=");
			params.append(this.loginUrl.toString());
			params.append('&');
			params.append("loginRequestData=");
			params.append(charReplacment("username={%25username%25}&password={%25password%25}","{}=%"));
			pen789.api.authentication.setAuthenticationMethod(this.myPen789.ZAP_API_KEY, contextId, "formBasedAuthentication",
					params.toString());
			pen789.api.authentication.setLoggedInIndicator(this.myPen789.ZAP_API_KEY, contextId, "^(.*?(\bLogout\b)[^$]*)$");
			pen789.api.authentication.setLoggedOutIndicator(this.myPen789.ZAP_API_KEY, contextId, "^(.*?(\bLogin\b)[^$]*)$");
		} catch (ClientApiException e) {
			e.printStackTrace();
			this.testFailed("508 Test Failed! Could not complete set up of authentication methods.");
		}
	}
	
	private void createUsers(){
		try {
			int i=0;
			for(String username : this.users.keySet()){
				String userId = ((ApiResponseElement)pen789.api.users.newUser(this.myPen789.ZAP_API_KEY, contextId, username)).getValue();
				StringBuilder params = new StringBuilder();
				params.append("username=");
				params.append(charReplacment(username,"%"));
				params.append("&password=");
				params.append(charReplacment(this.users.get(username),"%"));
				pen789.api.users.setAuthenticationCredentials(this.myPen789.ZAP_API_KEY, contextId, userId, params.toString());
				pen789.api.users.setUserEnabled(this.myPen789.ZAP_API_KEY, contextId, userId, "true");
//				register and login users with site.
				if(i++%2==0){
					this.setProgress(Math.min(this.getProgress() + 6, 100));
					this.victimUser = new Tuple<String, String, String>(userId, username, this.users.get(username));
					String[] temp = this.loginOrRegisterUser(username, this.users.get(username), this.loginUrl, this.victimCookieManager);
					if(temp==null)this.testFailed("535 Test Failed! Could not log in as "+username);
					this.trafficDump.append("<br>\n============Victim User Login Request============<br>\n<br>\n");
					this.trafficDump.append(temp[0]+"<br>\n");
					this.trafficDump.append("<br>\n============Victim User Login Response============<br>\n<br>\n");
					this.trafficDump.append(temp[2]+"<br>\n");
					this.trafficDump.append(temp[1]+"<br>\n");
					pen789.api.forcedUser.setForcedUser(this.myPen789.ZAP_API_KEY, contextId, userId);
					this.setProgress(Math.min(this.getProgress() + 3, 100));
				}else{
					this.setProgress(Math.min(this.getProgress() + 6, 100));
					this.attackUser = new Tuple<String, String, String>(userId, username, this.users.get(username));
					String[] temp = this.loginOrRegisterUser(username, this.users.get(username), this.loginUrl, this.attackerCookieManager);
					if(temp==null)this.testFailed("550 Test Failed! Could not log in as "+username);
					this.trafficDump.append("<br>\n============Attacking User Login Request============<br>\n<br>\n");
					this.trafficDump.append(temp[0]+"<br>\n");
					this.trafficDump.append("<br>\n============Attacking User Login Response============<br>\n<br>\n");
					this.trafficDump.append(temp[2]+"<br>\n");
					this.trafficDump.append(temp[1]+"<br>\n");
					this.setProgress(Math.min(this.getProgress() + 3, 100));
				}
			}
			pen789.api.forcedUser.setForcedUserModeEnabled(this.myPen789.ZAP_API_KEY, true);
			this.trafficDump.append("Sessions created...<br>\n");
			this.setProgress(Math.min(this.getProgress() + 6, 100));
		} catch (ClientApiException e) {
			e.printStackTrace();
			this.testFailed("569 Test Failed! failure occured while creating and registering users.");
		}
	}
	private void testFailed(String string) {
		JOptionPane.showMessageDialog(null, string);
		this.setProgress(100);
		if(!this.cancel(true)){
			try {
				this.finalize();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	private String charReplacment(String str, String ignor){
		String temp = str;
		for(String key: replaceChars.keySet()){
			if(temp.contains(key) && !ignor.contains(key))
				temp=temp.replaceAll(key, replaceChars.get(key));
		}
		return temp;
	}
	private String randomString(int length){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<length; i++){
			char j;
			if(((int)(100*Math.random()+1))<29){
				j = (char)((int)(Math.random()*10+48));
			}else{
				j = (char)((int)(Math.random()*26+65));
			}
			sb.append(j);
		}
		return sb.toString();
	}
}
