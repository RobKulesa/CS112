package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode, with fields for
 * tag/text, first child and sibling.
 * 
 */
public class Tree {
	
	/**
	 * Root node
	 */
	TagNode root=null;
	
	/**
	 * Scanner used to read input HTML file when building the tree
	 */
	Scanner sc;
	
	/**
	 * Initializes this tree object with scanner for input HTML file
	 * 
	 * @param sc Scanner for input HTML file
	 */
	public Tree(Scanner sc) {
		this.sc = sc;
		root = null;
	}
	
	/**
	 * Builds the DOM tree from input HTML file, through scanner passed
	 * in to the constructor and stored in the sc field of this object. 
	 * 
	 * The root of the tree that is built is referenced by the root field of this object.
	 */
	public void build() {
		if(!sc.hasNextLine()) return;
		
		Stack<TagNode> tags = new Stack<TagNode>();
		root = new TagNode(removeBrackets(sc.nextLine()), null, null);
		tags.push(root);
		
		while(sc.hasNextLine()) {
			String curr = sc.nextLine();
			boolean currIsTag = (curr.indexOf("<") == 0) ? true : false;
			boolean currIsCloseTag = (curr.indexOf("/") == 1) ? true : false;
			if(currIsTag && currIsCloseTag) tags.pop();
			else {
				if(currIsTag) {
					curr = removeBrackets(curr);
				}
				TagNode node = new TagNode(curr, null, null);
				if(tags.peek().firstChild == null) {
					tags.peek().firstChild = node;
				} else {
					TagNode tmp = tags.peek().firstChild;
					while(tmp.sibling != null) {
						tmp = tmp.sibling;
					}
					tmp.sibling = node;
				}
				if(currIsTag) tags.push(node);
			}
		}
	}
	
	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag) {
		recursionReplaceTag(root, oldTag, newTag);
	}
	private void recursionReplaceTag(TagNode node, String oldTag, String newTag) {
		if(node == null) return;
		if(node.tag.equals(oldTag)) node.tag = newTag;
		if(node.firstChild != null) recursionReplaceTag(node.firstChild, oldTag, newTag);
		if(node.sibling != null) recursionReplaceTag(node.sibling, oldTag, newTag);
	}
	
	/**
	 * Boldfaces every column of the given row of the table in the DOM tree. The boldface (b)
	 * tag appears directly under the td tag of every column of this row.
	 * 
	 * @param row Row to bold, first row is numbered 1 (not 0).
	 */
	public void boldRow(int row) {
		recursionBoldRow(root, row);
	}
	private void recursionBoldRow(TagNode node, int row) {
		if(node == null) return;
		if(node.tag.equals("table")) {
			TagNode nodeRow = node.firstChild;
			for(int i = 2; i <= row; i++) {
				nodeRow = nodeRow.sibling;
			}
			TagNode nodeCol = nodeRow.firstChild;
			while(nodeCol != null) {
				nodeCol.firstChild = new TagNode("b", nodeCol.firstChild, nodeCol.sibling);
				nodeCol.firstChild.sibling = null;
				nodeCol = nodeCol.sibling;
			}
		}
		if(node.firstChild != null) recursionBoldRow(node.firstChild, row);
		if(node.sibling != null) recursionBoldRow(node.sibling, row);
	}
	
	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b, all occurrences of the tag
	 * are removed. If the tag is ol or ul, then All occurrences of such a tag are removed from the tree, and, 
	 * in addition, all the li tags immediately under the removed tag are converted to p tags. 
	 * 
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag) {
		recursionRemoveTag(root, tag);
	}
	private void recursionRemoveTag(TagNode node, String tag) {
		if(node == null) return;
		if(node.tag.equals(tag) && node.firstChild == null) {
			node = null;
			return;
		}
		if(node.firstChild == null) return;
		TagNode ptr1 = node.firstChild;
		while(ptr1 != null) {
			recursionRemoveTag(ptr1, tag);
			if(ptr1.tag.equals(tag)) {
				node.firstChild = buildSubTree(node.firstChild, tag);
			}
			ptr1 = ptr1.sibling;
		}
		
		if(node.sibling != null) recursionRemoveTag(node.sibling, tag);
		recursionRemoveTag(node.firstChild, tag);
	}

	private TagNode buildSubTree(TagNode problem, String tag) {
		TagNode subtree = null;
		TagNode last = null;
		while(problem != null) {
			if(problem.tag.equals(tag)) {
				TagNode ptr1 = problem.firstChild;
				while(ptr1 != null) {
					if(subtree == null) {
						if(tag.equals("ol") || tag.equals("ul")) subtree = new TagNode("p", ptr1.firstChild, null);
						else subtree = new TagNode(ptr1.tag, ptr1.firstChild, null);
						last = subtree;
					} else {
						if(tag.equals("ol") || tag.equals("ul")) last.sibling = new TagNode("p", ptr1.firstChild, null);
						else last.sibling = new TagNode(ptr1.tag, ptr1.firstChild, null);
						last = last.sibling;
					}
					ptr1 = ptr1.sibling;
				}
			} else {
				if(subtree == null) {
					subtree = new TagNode(problem.tag, problem.firstChild, null);
					last = subtree;
				} else {
					last.sibling = new TagNode(problem.tag, problem.firstChild, null);
					last = last.sibling;
				}
			}
			problem = problem.sibling;
		}
		return subtree;
	}
	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag Tag to be added
	 */
	public void addTag(String word, String tag) {
		if(tag.equalsIgnoreCase("em") || tag.equalsIgnoreCase("b")) {
			recursiveAddTag(root, word, tag);
			cleanTree(root);
		}	
	}
	private void recursiveAddTag(TagNode node, String word, String tag) {
		if(node == null) return;
		if(node.firstChild == null) return;
		else {
			TagNode ptr = node.firstChild;
			while(ptr != null) {
				recursiveAddTag(ptr, word, tag);
				int idx = ptr.tag.toLowerCase().indexOf(word.toLowerCase());
				if(idx >= 0) {
					if(ptr.tag.equalsIgnoreCase(word) || ptr.tag.substring(0, ptr.tag.length() - 1).equalsIgnoreCase(word)) {
						if(ptr.tag.equalsIgnoreCase(word)) node.firstChild = buildSubTree2(node.firstChild, word, tag);
						else node.firstChild = buildSubTree2(node.firstChild, word + ptr.tag.charAt(ptr.tag.length() - 1), tag);
					} else {
						if((word.length() != 1) && idx == 0 && ptr.tag.toLowerCase().charAt(idx + word.length() - 2) != word.charAt(word.length() - 1)) {
							
						} else if((idx == 0 && (ptr.tag.charAt(idx + word.length()) == ' ' || ptr.tag.charAt(idx + word.length()) == '.' || ptr.tag.charAt(idx + word.length()) == ',' || ptr.tag.charAt(idx + word.length()) == '?' || ptr.tag.charAt(idx + word.length()) == '!' || ptr.tag.charAt(idx + word.length()) == ':' || ptr.tag.charAt(idx + word.length()) == ';'))
								|| (ptr.tag.charAt(idx - 1) == ' ' && (ptr.tag.substring(idx).equalsIgnoreCase(word)))
								|| (ptr.tag.charAt(idx - 1) == ' ' && (ptr.tag.charAt(idx + word.length()) == ' ' || ptr.tag.charAt(idx + word.length()) == '.' || ptr.tag.charAt(idx + word.length()) == ',' || ptr.tag.charAt(idx + word.length()) == '?' || ptr.tag.charAt(idx + word.length()) == '!' || ptr.tag.charAt(idx + word.length()) == ':' || ptr.tag.charAt(idx + word.length()) == ';'))) {
							ArrayList<String> stringList = new ArrayList<String>();
							stringList.add(ptr.tag.substring(0, idx));
							if(ptr.tag.charAt(ptr.tag.length() - 1) == '.' || ptr.tag.charAt(ptr.tag.length() - 1) == ',' || ptr.tag.charAt(ptr.tag.length() - 1) == '?' || ptr.tag.charAt(ptr.tag.length() - 1) == '!'|| ptr.tag.charAt(ptr.tag.length() - 1) == ':' || ptr.tag.charAt(ptr.tag.length() - 1) == ';') {
								System.out.println("idx is " + idx);
								try {
									if(ptr.tag.substring(idx , idx + word.length() + 1).contains(".") || ptr.tag.substring(idx , idx + word.length() + 1).contains(",") || ptr.tag.substring(idx , idx + word.length() + 1).contains("?") || ptr.tag.substring(idx , idx + word.length() + 1).contains("!") || ptr.tag.substring(idx , idx + word.length() + 1).contains(":") || ptr.tag.substring(idx , idx + word.length() + 1).contains(";")) {
										stringList.add(ptr.tag.substring(idx, idx + word.length() + 1));
										stringList.add(ptr.tag.substring(idx + word.length() + 1));	
									} else {
										stringList.add(ptr.tag.substring(idx, idx + word.length()));
										stringList.add(ptr.tag.substring(idx + word.length()));
									}
								} catch (NullPointerException e) {
									stringList.add(ptr.tag.substring(idx, idx + word.length()));
									stringList.add(ptr.tag.substring(idx + word.length()));
								}
							} else {
								stringList.add(ptr.tag.substring(idx, idx + word.length()));
								stringList.add(ptr.tag.substring(idx + word.length()));
							}
							System.out.println("<" + stringList.get(0) + ", " + stringList.get(1) + ", " + stringList.get(2) + ">");
							if(stringList.get(0).equals("")) {
								ptr.tag = tag;
								ptr.firstChild = new TagNode(stringList.get(1), null, null);
								ptr.sibling =  new TagNode(stringList.get(2), null, ptr.sibling);
							} else if(stringList.get(2).equals("")) {
								ptr.tag = stringList.get(0);
								if(ptr.sibling == null) {
									ptr.sibling = new TagNode(null, null, null);
								}
								ptr.sibling.tag = tag;
								ptr.sibling.firstChild = new TagNode(stringList.get(1), null, ptr.sibling.sibling);
							} else {
								ptr.tag = stringList.get(0);
								if(ptr.sibling == null) {
									ptr.sibling = new TagNode(null, null, null);
								}
								ptr.sibling.tag = tag;
								ptr.sibling.firstChild = new TagNode(stringList.get(1), null, null);
								ptr.sibling.sibling = new TagNode(stringList.get(2), null, ptr.sibling.sibling);
							}
						}
					}	
				}
				ptr = ptr.sibling;
			}
		}
	}
	private void cleanTree(TagNode node) {
		if(node == null) return;
		if(node.firstChild != null) cleanTree(node.firstChild);
		if(node.sibling != null) cleanTree(node.sibling);
		try {
			if(node.tag.equals(node.firstChild.tag)) {
				node.firstChild = new TagNode(node.firstChild.firstChild.tag, null, null);
			}	
		} catch(NullPointerException e) {
			return;
		}
	}
	
	private TagNode buildSubTree2(TagNode node, String word, String tag) {
		TagNode subtree = null;
		TagNode lastCreated = null;
		while(node != null) {
			if(node.tag.equalsIgnoreCase(word)) {
				if(subtree == null) {
					subtree = new TagNode(tag, new TagNode(node.tag, node.firstChild, null), null);
					lastCreated = subtree;
				} else {
					lastCreated.sibling = new TagNode(tag, new TagNode(node.tag, node.firstChild, null), null);
					lastCreated = lastCreated.sibling;
				}
			} else {
				if(subtree == null) {
					subtree = new TagNode(node.tag, node.firstChild, null);
					lastCreated = subtree;
				} else {
					lastCreated.sibling = new TagNode(node.tag, node.firstChild, null);
					lastCreated = lastCreated.sibling;
				}
			}
			node = node.sibling;
		}
		print(subtree, 3);
		System.out.println("\n \n \n");
		return subtree;
	}
	
	private String removeBrackets(String str) {
		return str.substring(1, str.length() - 1);
	}
	
	/**
	 * Gets the HTML represented by this DOM tree. The returned string includes
	 * new lines, so that when it is printed, it will be identical to the
	 * input file from which the DOM tree was built.
	 * 
	 * @return HTML string, including new lines. 
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		getHTML(root, sb);
		return sb.toString();
	}
	
	private void getHTML(TagNode root, StringBuilder sb) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild == null) {
				sb.append(ptr.tag);
				sb.append("\n");
			} else {
				sb.append("<");
				sb.append(ptr.tag);
				sb.append(">\n");
				getHTML(ptr.firstChild, sb);
				sb.append("</");
				sb.append(ptr.tag);
				sb.append(">\n");	
			}
		}
	}
	
	/**
	 * Prints the DOM tree. 
	 *
	 */
	public void print() {
		print(root, 1);
	}
	
	private void print(TagNode root, int level) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			for (int i=0; i < level-1; i++) {
				System.out.print("      ");
			};
			if (root != this.root) {
				System.out.print("|----");
			} else {
				System.out.print("     ");
			}
			System.out.println(ptr.tag);
			if (ptr.firstChild != null) {
				print(ptr.firstChild, level+1);
			}
		}
	}
}