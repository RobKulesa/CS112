package lse;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class Test {
	
	public static void main(String args[]) throws FileNotFoundException {
		LittleSearchEngine lse = new LittleSearchEngine();
		lse.makeIndex("docs.txt", "noisewords.txt");
		
		System.out.println(lse.top5search("bring", "world"));
	}
}
