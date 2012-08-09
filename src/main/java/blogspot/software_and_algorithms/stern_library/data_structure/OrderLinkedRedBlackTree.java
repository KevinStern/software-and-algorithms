package blogspot.software_and_algorithms.stern_library.data_structure;

import java.util.Comparator;

/* Copyright (c) 2012 Kevin L. Stern
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * A red black tree that has been augmented to support linear time partial
 * iteration by storing pointers to a node's predecessor and successor within
 * the node itself.
 * 
 * @author Kevin L. Stern
 */
public class OrderLinkedRedBlackTree<T> extends RedBlackTree<T> {
	private RedBlackTree.Node<T> head;

	/**
	 * Default constructor.
	 */
	public OrderLinkedRedBlackTree() {
		this(null);
	}

	/**
	 * Construct a new instance which uses the specified comparator.
	 * 
	 * @param comparator
	 *            the comparator to use when ordering elements.
	 */
	public OrderLinkedRedBlackTree(Comparator<T> comparator) {
		super(comparator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		super.clear();
		head = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected RedBlackTree.Node<T> createNewNode(T value) {
		return new Node<T>(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RedBlackTree.Node<T> delete(T value) {
		if (head != null && head.getValue().equals(value)) {
			head = getSuccessor(head);
		}
		RedBlackTree.Node<T> result = super.delete(value);
		if (result != null) {
			Node<T> linkedNode = (Node<T>) result;
			if (linkedNode.getPredecessor() != null)
				linkedNode.getPredecessor().setSuccessor(
						linkedNode.getSuccessor());
			if (linkedNode.getSuccessor() != null)
				linkedNode.getSuccessor().setPredecessor(
						linkedNode.getPredecessor());
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void exchangeValues(RedBlackTree.Node<T> node,
			RedBlackTree.Node<T> successor) {
		super.exchangeValues(node, successor);
		Node<T> linkedNode = (Node<T>) node;
		Node<T> linkedSuccessor = (Node<T>) successor;
		linkedNode.setSuccessor(linkedSuccessor.getSuccessor());
		if (linkedNode.getSuccessor() != null)
			linkedNode.getSuccessor().setPredecessor(linkedNode);
		linkedSuccessor.setPredecessor(null);
		linkedSuccessor.setSuccessor(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RedBlackTree.Node<T> getFirstNode() {
		return head;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RedBlackTree.Node<T> getPredecessor(RedBlackTree.Node<T> node) {
		return ((Node<T>) node).getPredecessor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RedBlackTree.Node<T> getSuccessor(RedBlackTree.Node<T> node) {
		return ((Node<T>) node).getSuccessor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RedBlackTree.Node<T> insert(T value) {
		RedBlackTree.Node<T> result = super.insert(value);

		if (result != null) {
			Node<T> linkedNode = (Node<T>) result;
			Node<T> pred = (Node<T>) super.getPredecessor(result);
			linkedNode.setPredecessor(pred);
			if (pred != null) {
				pred.setSuccessor(linkedNode);
			}
			Node<T> succ = (Node<T>) super.getSuccessor(result);
			linkedNode.setSuccessor(succ);
			if (succ != null) {
				succ.setPredecessor(linkedNode);
			}

			if (head == null) {
				head = getRoot();
			} else {
				RedBlackTree.Node<T> node = getPredecessor(head);
				if (node != null) {
					head = node;
				}
			}
		}

		return result;
	}

	/**
	 * A red-black tree node augmented to store pointers to its predecessor and
	 * successor.
	 * 
	 * @author Kevin L. Stern
	 */
	public static class Node<T> extends RedBlackTree.Node<T> {
		private Node<T> predecessor, successor;

		/**
		 * Construct a node with the specified value.
		 * 
		 * @param value
		 *            the value to associate with this node.
		 */
		public Node(T value) {
			super(value);
		}

		/**
		 * Get the predecessor node.
		 * 
		 * @return the predecessor of this node in the tree.
		 */
		public Node<T> getPredecessor() {
			return predecessor;
		}

		/**
		 * Get the successor node.
		 * 
		 * @return the successor of this node in the tree.
		 */
		public Node<T> getSuccessor() {
			return successor;
		}

		/**
		 * Set the predecessor node.
		 * 
		 * @param node
		 *            the predecessor of this node in the tree.
		 */
		protected void setPredecessor(Node<T> node) {
			predecessor = node;
		}

		/**
		 * Set the successor node.
		 * 
		 * @param node
		 *            the successor of this node in the tree.
		 */
		protected void setSuccessor(Node<T> node) {
			successor = node;
		}
	}
}