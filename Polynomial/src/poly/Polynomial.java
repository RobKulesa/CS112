package poly;

import java.io.IOException;
import java.util.Scanner;

/**
 * This class implements evaluate, add and multiply for polynomials.
 * 
 * @author runb-cs112
 *
 */
public class Polynomial {
	
	/**
	 * Reads a polynomial from an input stream (file or keyboard). The storage format
	 * of the polynomial is:
	 * <pre>
	 *     <coeff> <degree>
	 *     <coeff> <degree>
	 *     ...
	 *     <coeff> <degree>
	 * </pre>
	 * with the guarantee that degrees will be in descending order. For example:
	 * <pre>
	 *      4 5
	 *     -2 3
	 *      2 1
	 *      3 0
	 * </pre>
	 * which represents the polynomial:
	 * <pre>
	 *      4*x^5 - 2*x^3 + 2*x + 3 
	 * </pre>
	 * 
	 * @param sc Scanner from which a polynomial is to be read
	 * @throws IOException If there is any input error in reading the polynomial
	 * @return The polynomial linked list (front node) constructed from coefficients and
	 *         degrees read from scanner
	 */
	public static Node read(Scanner sc) 
	throws IOException {
		Node poly = null;
		while (sc.hasNextLine()) {
			Scanner scLine = new Scanner(sc.nextLine());
			poly = new Node(scLine.nextFloat(), scLine.nextInt(), poly);
			scLine.close();
		}
		return poly;
	}
	
	/**
	 * Returns the sum of two polynomials - DOES NOT change either of the input polynomials.
	 * The returned polynomial MUST have all new nodes. In other words, none of the nodes
	 * of the input polynomials can be in the result.
	 * 
	 * @param poly1 First input polynomial (front of polynomial linked list)
	 * @param poly2 Second input polynomial (front of polynomial linked list
	 * @return A new polynomial which is the sum of the input polynomials - the returned node
	 *         is the front of the result polynomial
	 */
	public static Node add(Node poly1, Node poly2) {
		if(poly1 == null) {
			return poly2;
		} else if (poly2 == null) {
			return poly1;
		}
		
		int firstHighest = 0; int secondHighest = 0; int highestDeg = 0;
		for(Node first = poly1; first != null; first = first.next) {
			firstHighest = first.term.degree;
		}
		for(Node second = poly2; second != null; second = second.next) {
			secondHighest = second.term.degree;
		}
		highestDeg = Math.max(firstHighest, secondHighest);
		
		Node front = null;
		for(int deg = highestDeg; deg >= 0; deg--) {
			float coeffSum = 0;
			for(Node first = poly1; first != null; first = first.next) {
				if(first.term.degree == deg) {
					coeffSum += first.term.coeff;
				}
			}
			for(Node second = poly2; second != null; second = second.next) {
				if(second.term.degree == deg) {
					coeffSum += second.term.coeff;
				}
			}
			if(coeffSum != 0) {
				front = new Node(coeffSum, deg, front);
			}
		}
		return front;
	}
	
	/**
	 * Returns the product of two polynomials - DOES NOT change either of the input polynomials.
	 * The returned polynomial MUST have all new nodes. In other words, none of the nodes
	 * of the input polynomials can be in the result.
	 * 
	 * @param poly1 First input polynomial (front of polynomial linked list)
	 * @param poly2 Second input polynomial (front of polynomial linked list)
	 * @return A new polynomial which is the product of the input polynomials - the returned node
	 *         is the front of the result polynomial
	 */
	public static Node multiply(Node poly1, Node poly2) {
		Node oldFront = null;
		for(Node first = poly1; first != null; first = first.next) {
			for(Node second = poly2; second != null; second = second.next) {
				oldFront = new Node(first.term.coeff * second.term.coeff, first.term.degree + second.term.degree, oldFront);
			}
		}
		
		//reverse
		Node front = null;
		for(Node ptr = oldFront; ptr != null; ptr = ptr.next) {
			front = new Node(ptr.term.coeff, ptr.term.degree, front);
		}
		return simplify(front);
	}
		
	/**
	 * Evaluates a polynomial at a given value.
	 * 
	 * @param poly Polynomial (front of linked list) to be evaluated
	 * @param x Value at which evaluation is to be done
	 * @return Value of polynomial p at x
	 */
	public static float evaluate(Node poly, float x) {
		float total = 0;
		for(Node node = poly; node != null; node = node.next) {
			total += node.term.coeff*(Math.pow(x, node.term.degree));
		}
		return total;
	}
	
	/**
	 * Returns string representation of a polynomial
	 * 
	 * @param poly Polynomial (front of linked list)
	 * @return String representation, in descending order of degrees
	 */
	public static String toString(Node poly) {
		if (poly == null) {
			return "0";
		} 
		
		String retval = poly.term.toString();
		for (Node current = poly.next ; current != null ;
		current = current.next) {
			retval = current.term.toString() + " + " + retval;
		}
		return retval;
	}	
	
	private static Node simplify(Node poly) {
		Node added = add(poly, poly);
		for(Node ptr = added; ptr != null; ptr = ptr.next) {
			ptr.term.coeff /= 2;
		}
		
		return added;
	}	
}