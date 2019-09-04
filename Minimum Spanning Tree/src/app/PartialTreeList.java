package app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import structures.*;


/**
 * Stores partial trees in a circular linked list
 * 
 */
public class PartialTreeList implements Iterable<PartialTree> {
    
	/**
	 * Inner class - to build the partial tree circular linked list 
	 * 
	 */
	public static class Node {
		/**
		 * Partial tree
		 */
		public PartialTree tree;
		
		/**
		 * Next node in linked list
		 */
		public Node next;
		
		/**
		 * Initializes this node by setting the tree part to the given tree,
		 * and setting next part to null
		 * 
		 * @param tree Partial tree
		 */
		public Node(PartialTree tree) {
			this.tree = tree;
			next = null;
		}
	}

	/**
	 * Pointer to last node of the circular linked list
	 */
	private Node rear;
	
	/**
	 * Number of nodes in the CLL
	 */
	private int size;
	
	/**
	 * Initializes this list to empty
	 */
    public PartialTreeList() {
    	rear = null;
    	size = 0;
    }

    /**
     * Adds a new tree to the end of the list
     * 
     * @param tree Tree to be added to the end of the list
     */
    public void append(PartialTree tree) {
    	Node ptr = new Node(tree);
    	if (rear == null) {
    		ptr.next = ptr;
    	} else {
    		ptr.next = rear.next;
    		rear.next = ptr;
    	}
    	rear = ptr;
    	size++;
    }

    /**
	 * Initializes the algorithm by building single-vertex partial trees
	 * 
	 * @param graph Graph for which the MST is to be found
	 * @return The initial partial tree list
	 */
	public static PartialTreeList initialize(Graph graph) {
		PartialTreeList ptl = new PartialTreeList(); //Empty list of trees
		for(Vertex v : graph.vertices) { //Separately for each vertex v in the graph
			PartialTree tree = new PartialTree(v); //Create partial tree T containing only v
			Vertex.Neighbor n = v.neighbors; 
			while(n != null) {
				Arc edge = new Arc(v, n.vertex, n.weight);
				tree.getArcs().insert(edge);
				n = n.next;
			}
			tree.getRoot().parent = tree.getRoot();
			ptl.append(tree);
		}
		
		return ptl;
	}
	
	/**
	 * Executes the algorithm on a graph, starting with the initial partial tree list
	 * for that graph
	 * 
	 * @param ptlist Initial partial tree list
	 * @return Array list of all arcs that are in the MST - sequence of arcs is irrelevant
	 */
	public static ArrayList<Arc> execute(PartialTreeList ptlist) {
		ArrayList<Arc> arcList = new ArrayList<Arc>();
		int counter = 1;
		while(ptlist.size() > 1) {
			System.out.println("Loop " + counter + ": ");
			System.out.println("PTList before: ");
			for(PartialTree pt : ptlist) {
	        	System.out.println(pt.toString());
	        }
			PartialTree PTX = ptlist.remove(); //Remove first partial tree PTX from L.
			MinHeap<Arc> PQX = PTX.getArcs(); //MinHeap PQX
			Arc edge = PQX.deleteMin(); //Get highest-priority arc from PQX
			while(edge != null) {
				Vertex v1 = edge.getv1(); Vertex v2 = edge.getv2();
				if(v1.parent.equals(v2.parent)) {
					edge = PQX.deleteMin();
					continue;
				}
				PartialTree v1Tree = ptlist.removeTreeContaining(v1);
				PartialTree v2Tree = ptlist.removeTreeContaining(v2);
				PartialTree PTY = null;
				if(v1Tree != null) PTY = v1Tree;
				else PTY = v2Tree;
				if(PTY != null) {
					PTX.merge(PTY);
					arcList.add(edge);
					ptlist.append(PTX);
					System.out.println("PTList after: ");
					for(PartialTree pt : ptlist) {
			        	System.out.println(pt.toString());
			        }
					break;
				}
				edge = PQX.deleteMin();
			}
			counter++;
		}

		return arcList;
	}
	
    /**
     * Removes the tree that is at the front of the list.
     * 
     * @return The tree that is removed from the front
     * @throws NoSuchElementException If the list is empty
     */
    public PartialTree remove() throws NoSuchElementException {
    			
    	if (rear == null) {
    		throw new NoSuchElementException("list is empty");
    	}
    	PartialTree ret = rear.next.tree;
    	if (rear.next == rear) {
    		rear = null;
    	} else {
    		rear.next = rear.next.next;
    	}
    	size--;
    	return ret;
    		
    }

    /**
     * Removes the tree in this list that contains a given vertex.
     * 
     * @param vertex Vertex whose tree is to be removed
     * @return The tree that is removed
     * @throws NoSuchElementException If there is no matching tree
     */
    public PartialTree removeTreeContaining(Vertex vertex) throws NoSuchElementException {
    	PartialTree ptRemoved = null;
    	if(this.rear == null) throw new NoSuchElementException("PartialTreeList is empty");
    	PartialTreeList.Node ptr = rear;
    	do {
    		while(vertex.parent != vertex) {
    			vertex = vertex.parent;
    		}
    		PartialTree curr = ptr.tree;
    		if(vertex == curr.getRoot()) {
    			ptRemoved = curr;
    			PartialTreeList.Node parent = ptr;
    			while(parent.next != ptr) {
    				parent = parent.next;
    			}
    			if(ptr.next == ptr && parent == ptr) this.rear = null; //1
    			else if (ptr.next == parent) { //2
    				if(ptr == this.rear) this.rear = this.rear.next;
    				ptr.next.next = ptr.next;
    			} else { //3+
    				if(ptr == this.rear) this.rear = parent;
    				parent.next = ptr.next;
    			}
    			this.size--;
    			ptr.next = null;
    			break;
    		}
    		ptr = ptr.next;
    	} while(ptr != rear);
    	return ptRemoved;
    }
    
    /**
     * Gives the number of trees in this list
     * 
     * @return Number of trees
     */
    public int size() {
    	return size;
    }
    
    /**
     * Returns an Iterator that can be used to step through the trees in this list.
     * The iterator does NOT support remove.
     * 
     * @return Iterator for this list
     */
    public Iterator<PartialTree> iterator() {
    	return new PartialTreeListIterator(this);
    }
    
    private class PartialTreeListIterator implements Iterator<PartialTree> {
    	
    	private PartialTreeList.Node ptr;
    	private int rest;
    	
    	public PartialTreeListIterator(PartialTreeList target) {
    		rest = target.size;
    		ptr = rest > 0 ? target.rear.next : null;
    	}
    	
    	public PartialTree next() 
    	throws NoSuchElementException {
    		if (rest <= 0) {
    			throw new NoSuchElementException();
    		}
    		PartialTree ret = ptr.tree;
    		ptr = ptr.next;
    		rest--;
    		return ret;
    	}
    	
    	public boolean hasNext() {
    		return rest != 0;
    	}
    	
    	public void remove() 
    	throws UnsupportedOperationException {
    		throw new UnsupportedOperationException();
    	}
    	
    }
}


