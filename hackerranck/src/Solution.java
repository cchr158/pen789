import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;
    public class Solution {
        public static void main(String[] args) throws IOException {
        	String regex = "/* Write a RegEx matching repeated words here. */";
            Pattern p = Pattern.compile(regex, /* Insert the correct Pattern flag here.*/);

            Scanner in = new Scanner(System.in);
            int numSentences = Integer.parseInt(in.nextLine());
            
            while (numSentences-- > 0) {
                String input = in.nextLine();
                
                Matcher m = p.matcher(input);
                
                // Check for subsequences of input that match the compiled pattern
                while (m.find()) {
                    input = input.replaceAll(/* The regex to replace */, /* The replacement. */);
                }
                
                // Prints the modified sentence.
                System.out.println(input);
            }
            
            in.close();
    }
