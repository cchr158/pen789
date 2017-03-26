import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;
    public class Solution {
        public static void main(String[] args) throws IOException {
        	Scanner scan = new Scanner(System.in);
            int n =scan.nextInt();
            ArrayList<int[]> list = new ArrayList<int[]>();
            for(int i=0; i<n; i++){
                int d = scan.nextInt();
                int[] temp = new int[d];
                for(int j=0; j<d; j++){
                    temp[j] = scan.nextInt();
                }
                list.add(temp);
            }
            int q = scan.nextInt();
            for(int i=0; i<q; i++){
                int line = scan.nextInt();
                int j = scan.nextInt();
                int[] temp = null;
                if(list.size()>line-1){
                	temp = list.get(line-1);
                	if(temp.length>j-1){
                		System.out.println(temp[j-1]);
                	}else{
                		System.out.println("ERROR!");
                	}
                }else{
                    System.out.println("ERROR!");
                }
            }
            scan.close();
        }
    }
