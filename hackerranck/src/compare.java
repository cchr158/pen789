import java.io.File;
import java.util.Scanner;

public class compare {

	public static void main(String[] args) throws Exception{
		Scanner scan1 = new Scanner(new File("D:\\the test build\\hackerranck\\bin\\output.txt"));
		Scanner scan2 = new Scanner(new File("D:\\the test build\\hackerranck\\bin\\answers.txt"));
		int count = 2;
		int index = 0;
		while(scan1.hasNextLine() && scan2.hasNextLine()){
			if(!scan1.nextLine().equals(scan2.nextLine())){
				System.out.println((index+1)+" "+(count));
			}
			count+=2;
			index++;
		}
		scan1.close();
		scan2.close();

	}

}
