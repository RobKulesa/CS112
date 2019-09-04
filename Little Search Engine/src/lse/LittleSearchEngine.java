package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) throws FileNotFoundException {
		if(docFile == null || docFile == "") {
			throw new FileNotFoundException();
		}
		
		HashMap<String, Occurrence> map = new HashMap<String, Occurrence>();
		Scanner sc = new Scanner(new File(docFile));
		while(sc.hasNext()) {
			String keyWord = getKeyword(sc.next());
			if(keyWord != null) {
				boolean contains = false;
				for(String key : map.keySet()) {
					if(key.equalsIgnoreCase(keyWord)) {
						contains = true;
						map.get(key).frequency++;
					}
				}
				if(!contains) map.put(keyWord, new Occurrence(docFile, 1));
			}
		}
		sc.close();
		return map;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 * @throws InterruptedException 
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) {
		for(String key : kws.keySet()) {
			key = key.toLowerCase();
			ArrayList<Occurrence> occs = new ArrayList<Occurrence>();
			if(keywordsIndex.containsKey(key)) occs = keywordsIndex.get(key);
			occs.add(kws.get(key));
			/*if(occs.size() > 1) {
				System.out.println(key + ": ");
				System.out.println("old list: " + occs);
			}*/
			ArrayList<Integer> midpoints = insertLastOccurrence(occs);
			if(midpoints != null && midpoints.size() > 0) { 
				int mid = midpoints.get(midpoints.size() - 1);
				//System.out.println(mid);
				if(mid >= 0) {
					if(occs.get(mid).frequency > occs.get(occs.size() - 1).frequency) {
						occs.add(mid + 1, occs.remove(occs.size() - 1));
					} else {
						occs.add(mid, occs.remove(occs.size() - 1));
					}
				}
			}
			if(occs.size() >= 2 && (occs.get(0).frequency < occs.get(1).frequency)) {
				occs.add(occs.remove(0));
			}
	
			/*if(occs.size() > 1) System.out.println("new list: " + occs);
			//testing
			int[] frequencies = new int[occs.size()];
			for(int i = 0; i < frequencies.length; i++) {
				frequencies[i] = occs.get(i).frequency;
			}
			for(int i = 0; i < frequencies.length - 1; i++) {
				if(frequencies[i] < frequencies[i + 1]) {
					System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t\t\t not sorted! index: " + i);
					break;
				}
			}*/
			
			keywordsIndex.put(key, occs);
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation(s), consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * NO OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
	 * 
	 * If a word has multiple trailing punctuation characters, they must all be stripped
	 * So "word!!" will become "word", and "word?!?!" will also become "word"
	 * 
	 * See assignment description for examples
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) {
		if(word == null) return null;
		word = word.toLowerCase();
		
		//remove trailing punctuation, reject word if punctuation in middle of letters
		for(int i = 0; i < word.length(); i++) {
			char curr = word.charAt(i);
			if(curr == '.' || curr == ',' || curr == '?' || curr == ':' || curr == ';' || curr == '!') {
				if(i == word.length() - 1) {
					word = word.substring(0, i);
				} else {
					if(word.charAt(i + 1) == 'a' || word.charAt(i + 1) == 'b' || word.charAt(i + 1) == 'c' || word.charAt(i + 1) == 'd' || word.charAt(i + 1) == 'e' || word.charAt(i + 1) == 'f' || word.charAt(i + 1) == 'g' || word.charAt(i + 1) == 'h' || word.charAt(i + 1) == 'i' || word.charAt(i + 1) == 'j' || word.charAt(i + 1) == 'k' || word.charAt(i + 1) == 'l' || word.charAt(i + 1) == 'm' || word.charAt(i + 1) == 'n' || word.charAt(i + 1) == 'o' || word.charAt(i + 1) == 'p' || word.charAt(i + 1) == 'q' || word.charAt(i + 1) == 'r' || word.charAt(i + 1) == 's' || word.charAt(i + 1) == 't' || word.charAt(i + 1) == 'u' || word.charAt(i + 1) == 'v' || word.charAt(i + 1) == 'w' || word.charAt(i + 1) == 'x' || word.charAt(i + 1) == 'y' || word.charAt(i + 1) == 'z') return null;
					word = word.substring(0, i) + word.substring(i + 1);
				}
				i--;
			}
		}
		
		//return null if word contains anything but letters
		StringTokenizer tokenizer = new StringTokenizer(word, "abcdefghijklmnopqrstuvwxyz.,?:;!", false);
		if(tokenizer.hasMoreTokens()) return null;
		
		//check if its a noise word
		for(String key : noiseWords) {
			if(key.equalsIgnoreCase(word)) return null;
		}
		
		//none of the above returned null, so we return word
		return word;
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		ArrayList<Integer> midpoints = new ArrayList<Integer>();
		if(occs.size() < 2) return null;
		Occurrence last = occs.get(occs.size() - 1);
		int left = 0; int right = occs.size() - 2;
		int mid = 0;
		while(left <= right) {
			mid = (left + right) / 2;
			midpoints.add(mid);
			//System.out.println("\n" + "left: " + left + " right: " + right + " mid: " + mid + " last: " + last.frequency);
			if(occs.get(mid).frequency == last.frequency) {
				break;
			}
			if(occs.get(mid).frequency > last.frequency) left = mid + 1;
			else right = mid - 1;
		}
		return midpoints;
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 * @throws InterruptedException 
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while(sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. 
	 * 
	 * Note that a matching document will only appear once in the result. 
	 * 
	 * Ties in frequency values are broken in favor of the first keyword. 
	 * That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same 
	 * frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * See assignment description for examples
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, 
	 *         returns null or empty array list.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<Occurrence> kw1OccList = new ArrayList<Occurrence>();
		ArrayList<Occurrence> kw2OccList = new ArrayList<Occurrence>();
		ArrayList<Occurrence> combinedList = new ArrayList<Occurrence>();
		
		for(String key : keywordsIndex.keySet()) {
			if(key.equalsIgnoreCase(kw1)) kw1OccList = keywordsIndex.get(key);
			if(key.equalsIgnoreCase(kw2)) kw2OccList = keywordsIndex.get(key);
		}
		
		/*System.out.println(kw1OccList);
		System.out.println(kw2OccList);*/
		while(!(kw1OccList.isEmpty() || kw2OccList.isEmpty())) {
			if(kw1OccList.get(0).frequency > kw2OccList.get(0).frequency) {
				combinedList.add(kw1OccList.remove(0));
			} else if(kw1OccList.get(0).frequency < kw2OccList.get(0).frequency) {
				combinedList.add(kw2OccList.remove(0));
			} else {
				combinedList.add(kw1OccList.remove(0));
				kw2OccList.remove(0);
			}
		}
		combinedList.addAll(kw1OccList);
		combinedList.addAll(kw2OccList);
		//System.out.println("\n" + combinedList);
		for(int i = 0; i < combinedList.size(); i++) {
			for(int j = i + 1; j < combinedList.size(); j++) {
				if(combinedList.get(i).document.equalsIgnoreCase(combinedList.get(j).document)) combinedList.remove(j);
			}
		}
		
		while(combinedList.size() > 5) combinedList.remove(combinedList.size() - 1);
		//System.out.println(combinedList);
		for(Occurrence o : combinedList) results.add(o.document);
		//System.out.println(results);
		return results;
	}
}
