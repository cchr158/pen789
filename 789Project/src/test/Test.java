package test;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Test {

	public static void main(String[] args){
		if(args == null){
			System.out.println("yes");
		}else{
			System.out.println("no");
			System.out.println(args.length);
		}
//		System.out.println(System.currentTimeMillis());
//		String output = "";
//		try {
//			List<String> f = Files.readAllLines(Paths.get("G:\\the test build\\789Project\\src", "AttackHelp"+".html"));
//			for(String s : f){
//				output+=s.replace('\t', '\0').replace('"', '\'');
//			}
//			PrintWriter out = new PrintWriter(Paths.get("G:\\the test build\\789Project\\src","dump"+".txt")
//					.toString());
//			out.println(output);
//			out.flush();
//			out.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println(output);
	}
}
