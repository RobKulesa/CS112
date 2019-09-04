package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	String customDelims1 = " \t*+-/()]012345679";
    	String customDelims2 = "[";
    	StringTokenizer tokenizer1 = new StringTokenizer(expr, customDelims1, false);
    	String current1 = null; 
    	while(tokenizer1.hasMoreTokens()) {
    		current1 = tokenizer1.nextToken();
    		if(current1.contains("[")) {
    			StringTokenizer tokenizer2 = new StringTokenizer(current1, customDelims2, true);
    			String current2 = null; String prev2 = null;
    			while(tokenizer2.hasMoreTokens()) {
    				current2 = tokenizer2.nextToken();
    				if(current2.equals("[")) {
    					boolean contains = false;
    					for(Array array : arrays) {
    						if(array.name.equals(prev2)) contains = true;
    					}
    					if(!contains) arrays.add(new Array(prev2));
    				}
    				prev2 = current2;
    			}
    		} else {
    			boolean contains = false;
    			for(Variable var : vars) {
    				if(var.name.equals(current1)) contains = true;
    			}
				if(!contains) vars.add(new Variable(current1));
    		}
    	}
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	Stack<String> ops = new Stack<String>(); Stack<Float> nums = new Stack<Float>();
    	StringTokenizer tokenizer = new StringTokenizer(expr, delims, true);
    	String curr = null; String newExpression = ""; String prev = null; String lastArray = null;
    	while(tokenizer.hasMoreTokens()) {
    		curr = tokenizer.nextToken();
    		//add numbers to stack nums
    		if((curr.indexOf("0") >= 0 || curr.indexOf("1") >= 0 || curr.indexOf("2") >= 0 || curr.indexOf("3") >= 0 || curr.indexOf("4") >= 0 || curr.indexOf("5") >= 0 || curr.indexOf("6") >= 0 || curr.indexOf("7") >= 0 || curr.indexOf("8") >= 0 || curr.indexOf("9") >= 0)) {
    			nums.push(Float.parseFloat(curr));
    		} 
    		//add values of vars to stack nums
    		for(Variable var : vars) {
    			if(curr.equals(var.name)) nums.push((float) var.value);
    		}
    		
    		//begin parsing symbols
    		//next two if statements take care of parentheticals, nested or un-nested
    		if(curr.equals("(")) {
    			ops.push(curr);
    		} else if(curr.equals(")")) {
    			while(!ops.peek().equals("(")) {
    				nums.push(execute(ops.pop(), nums.pop(), nums.pop()));
    			}
    			ops.pop();
    		} else if(curr.equals("+") || curr.equals("-") || curr.equals("*") || curr.equals("/")) {
    			while(!ops.isEmpty() && doFirst(curr, ops.peek())) {
    				nums.push(execute(ops.pop(), nums.pop(), nums.pop()));
    			}
    			ops.push(curr);
    		} else if(curr.equals("[")) {
    			lastArray = prev;
    			int openBracket = expr.indexOf("["); int closeBracket = 0; int counter = 1;
    			if(openBracket != -1) {
    				for(int i = openBracket + 1; i < expr.length(); i++) {
    					if(counter != 0) {
    						if((((Character) expr.charAt(i)).toString()).equals("[")) {
    							counter++;
    						}
    						else if(expr.charAt(i) == ']' && counter != 0) {
    							counter--;
    						}
    						if(counter == 0) {
    							closeBracket = i;
    						}
    					}
    				} 
    			} else {
    				openBracket = 0;
    			}
    			newExpression = expr.substring(openBracket + 1, closeBracket);
    			expr = expr.substring(0, openBracket) + expr.substring(closeBracket + 1, expr.length());
    			int index = ((Float) evaluate(newExpression, vars, arrays)).intValue();
    			float val = 0;
    			for(Array array : arrays) {
    				if(lastArray.equals(array.name)) val = array.values[index];
    			}
				nums.push(val);
				StringTokenizer st = new StringTokenizer(newExpression, delims, true);
				for(int i = 0; i < st.countTokens(); i++) {
					if(tokenizer.hasMoreTokens()) { 
						tokenizer.nextToken();
					}
				}
    		} 
    		prev = curr;
    	}
    	//execute remaining operations
		while(!ops.isEmpty()) {
			nums.push(execute(ops.pop(), nums.pop(), nums.pop()));
		}
    	return nums.pop();
    }
    
    private static float execute(String operation, float b, float a) {
    	switch(operation) {
    	case "+":
    		return a + b;
    	case "-":
    		return a - b;
    	case "*":
    		return a * b;
    	case "/":
    		if(b != 0) {
    			return a / b;
    		}
    		return 0;
    	}
    	return 0;
    }
    
    private static boolean doFirst(String str1, String str2) {
    	if(str2.equals("(") || str2.equals(")")) {
    		return false;
    	}
    	if((str1.equals("*") || str1.equals("/")) && (str2.equals("+") || str2.equals("-"))) {
    		return false;
    	}
    	return true;
    }
}