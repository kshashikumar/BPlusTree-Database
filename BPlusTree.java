import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.LinkedList;
import java.util.Queue;

/**
1. No duplicate keys inserted 2. Order D:D<=number of keys in a node <=2*D 3. All keys are non-negative
 */
public class BPlusTree<K extends Comparable<K>, T> {

	public Node<K,T> root;
	public static final int D = 4;
    public static int totSplits = 0;
    public static int parentSplits = 0;
    public static int totFusions = 0;
    public static int parentFusions = 0;

    public T searchTree(K key) {
        if (root == null || key == null) {
            return null;
        }

        LeafNode leafFound = searchLeaf(root, key);

        for (int i = 0; i < leafFound.keys.size(); i++) {
            if (key.compareTo((K) (leafFound.keys.get(i))) == 0) {
                return (T)leafFound.values.get(i);
            }
        }
        return null;
    }

     public int calculateDepth(Node root) {
        if (root == null) {
            return 0;
        }

        if (root instanceof LeafNode) {
            return 1;
        }

        int depth = 0;
        Queue<Node> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            int size = queue.size();
            depth++;

            for (int i = 0; i < size; i++) {
                Node current = queue.poll();

                if (current instanceof IndexNode) {
                    IndexNode indexNode = (IndexNode) current;
                    for (int j = 0; j < indexNode.keys.size() + 1; j++) {
                        if ((Node)indexNode.children.get(j) != null) {
                            queue.offer((Node)indexNode.children.get(j));
                        }
                    }
                }
            }
        }

        return depth;
    }

    private LeafNode searchLeaf(Node node, K key) {
        if (node.isLeafNode) {
            return (LeafNode) node;
        } else {
            IndexNode indexNode = (IndexNode) node;
            int i = 0;
            while (i < indexNode.keys.size() && key.compareTo((K)indexNode.keys.get(i)) >= 0) {
                i++;
            }
            return searchLeaf((Node)indexNode.children.get(i), key);
        }
    }

//Search the value for a specific key

	public T search(K key) {
        if (root == null || key == null)
            return null;
        LeafNode leafFound = (LeafNode)treeSearch(root, key); // down casting

        T val = null;
        for(int pos = 0; pos < leafFound.keys.size(); pos++){
            if (key.compareTo((K) (leafFound.keys.get(pos))) == 0)
                val = (T)leafFound.values.get(pos);
        }

        return val;
	}
    private Node treeSearch(Node<K, T> node, K searchKey) {
        if (node.isLeafNode){
            return node;
        } else {
            IndexNode tmp = (IndexNode)node;// Now it is a index node
            System.out.println("inside  "+node.keys.size());
            if (node.keys.size() >0 && searchKey.compareTo(node.keys.get(0)) < 0){
                System.out.println("inside < "+root);
                return treeSearch((Node)(tmp.children.get(0)),searchKey);
            } else if (node.keys.size()>0 && searchKey.compareTo(node.keys.get(node.keys.size()-1)) >= 0){
                System.out.println("inside > "+root);
                return treeSearch((Node)tmp.children.get(tmp.children.size()-1),searchKey);
            } else {
                System.out.println("inside = "+root);
                //binary search
                //right/key left
                int left = 0;
                int right = tmp.keys.size()-1;
                while (left <= right){
                    int mid = left+(right-left)/2;
                    K tmpKey = (K)tmp.keys.get(mid);
                    if (searchKey.compareTo(tmpKey) < 0){
                        right = mid - 1;
                    } else {
                        left = mid + 1;
                    }
                }
                return treeSearch((Node)(tmp.children.get(left)),searchKey);
            }
        }
    }

//update value of an element passing key
    public void update(K key, T newValue) {
        if (root == null || key == null) {
            return;
        }
        updateLeaf(root, key, newValue);
    }

    private void updateLeaf(Node node, K key, T newValue) {
        if (node.isLeafNode) {
            LeafNode leafNode = (LeafNode) node;
            int index = findKeyIndex(leafNode.keys, key);

            if (index != -1) {
                leafNode.values.set(index, newValue);
            }
        } else {
            IndexNode indexNode = (IndexNode) node;
            int i = 0;
            while (i < indexNode.keys.size() && key.compareTo((K)indexNode.keys.get(i)) >= 0) {
                i++;
            }
            updateLeaf((Node)indexNode.children.get(i), key, newValue);
        }
    }

    private int findKeyIndex(ArrayList<K> keys, K key) {
        for (int i = 0; i < keys.size(); i++) {
            if (key.compareTo(keys.get(i)) == 0) {
                return i;
            }
        }
        return -1;
    }


// Insert a key/value pair into the BPlusTree

	public void insert(K key, T value) {
        Entry<K,Node<K,T>> entry = new AbstractMap.SimpleEntry<K,Node<K,T>>(key, new LeafNode<K, T>(key,value));
        if(root == null)
            root = entry.getValue();

        Entry<K,Node<K,T>> newChildEntry = insertHelper(root,entry,null);

        if (newChildEntry == null)
            return;
        else {
            IndexNode newRoot = new IndexNode(newChildEntry.getKey(),root,newChildEntry.getValue());
            root = newRoot;
            return;
        }
	}
    private Entry<K, Node<K,T>> insertHelper(Node<K, T> node,Entry<K, Node<K,T>> entry, Entry<K, Node<K,T>> newChildEntry){
        if (!node.isLeafNode){
            IndexNode idx = (IndexNode)node;
            int i = 0;
            while (i < node.keys.size()){
                if (entry.getKey().compareTo(node.keys.get(i)) < 0)
                    break;
                i++;
            }
            newChildEntry = insertHelper((Node) idx.children.get(i), entry, newChildEntry);
            if (newChildEntry == null) {
                return newChildEntry;
            } else {
                int j = 0;
                while (j < idx.keys.size()){
                    if (newChildEntry.getKey().compareTo(node.keys.get(j)) < 0)
                        break;
                    j++;
                }

                idx.insertSorted(newChildEntry,j);

                if (!idx.isOverflowed()){
                    newChildEntry = null;
                    return newChildEntry;
                } else {
                    newChildEntry = splitIndexNode(idx);
                    if (idx == root){
                        IndexNode newRoot = new IndexNode(newChildEntry.getKey(),root,newChildEntry.getValue());
                        root = newRoot;
                        newChildEntry = null;
                        return newChildEntry;
                    }
                    return newChildEntry;
                }
            }
        } else {
            LeafNode lf = (LeafNode)node;
            LeafNode InsertLeaf = (LeafNode)entry.getValue();

            lf.insertSorted(entry.getKey(),InsertLeaf.values.get(0));

            if (!lf.isOverflowed()){
                newChildEntry = null;
                return newChildEntry;
            } else {
                newChildEntry = splitLeafNode(lf);
                if (lf == root){
                    IndexNode newRoot = new IndexNode(newChildEntry.getKey(),lf,newChildEntry.getValue());
                    root = newRoot;
                    newChildEntry = null;
                    return newChildEntry;
                }
                return newChildEntry;
            }
        }
    }

// Split a leaf node and return the new right node and the splitting key as an Entry<splittingKey, RightNode>

	public Entry<K, Node<K,T>> splitLeafNode(LeafNode<K,T> leaf) {
        totSplits=totSplits+1;
        ArrayList<K> rightKeys = new ArrayList<K>();
        ArrayList<T> rightValues = new ArrayList<T>();
        K splittingKey = leaf.keys.get(D);

        while (leaf.keys.size() > D){
            rightKeys.add(leaf.keys.get(D));
            leaf.keys.remove(D);
            rightValues.add(leaf.values.get(D));
            leaf.values.remove(D);
        }

        LeafNode rightNode = new LeafNode(rightKeys, rightValues);
        LeafNode Tmp = leaf.nextLeaf;
        leaf.nextLeaf = rightNode;
        leaf.nextLeaf.previousLeaf = rightNode;
        rightNode.previousLeaf = leaf;
        rightNode.nextLeaf = Tmp;


        Entry<K,Node<K,T>> entry = new AbstractMap.SimpleEntry<K,Node<K,T>>(splittingKey, rightNode);
		return entry;
	}

// split an indexNode and return the new right node and the splitting key as an Entry<splittingKey, RightNode>
	public Entry<K, Node<K,T>> splitIndexNode(IndexNode<K,T> index) {
        totSplits=totSplits+1;
        parentSplits=parentSplits+1;
        K splittingKey = index.keys.get(D);
        index.keys.remove(D);

        ArrayList<K> RightKey = new ArrayList<K>();
        ArrayList<Node<K,T>> RightChildren = new ArrayList<Node<K, T>>();

        RightChildren.add(index.children.get(D+1));
        index.children.remove(D+1);

        while (index.keys.size() > D){
            RightKey.add(index.keys.get(D));
            index.keys.remove(D);
            RightChildren.add(index.children.get(D + 1));
            index.children.remove(D + 1);
        }

        IndexNode Right = new IndexNode(RightKey, RightChildren);
        Entry<K,Node<K,T>> entry = new AbstractMap.SimpleEntry<K,Node<K,T>>(splittingKey, Right);
		return entry;
	}

// Delete a key/value pair from this B+Tree

	public void delete(K key) {
        if (root == null)
            return;
        LeafNode leafFound = (LeafNode)treeSearch(root, key);
        if (leafFound == null)
            return;
        Entry<K,Node<K,T>> entry = new AbstractMap.SimpleEntry<K,Node<K,T>>(key,leafFound);
        Entry<K,Node<K,T>> oldChildEntry = deleteHelper(root,root,entry,null);
        if (oldChildEntry == null) {
            if (root.keys.size() == 0){
                if (!root.isLeafNode)
                    root = (Node)((IndexNode)root).children.get(0);
            }
            return;
        } else {
            int i = 0;
            while (i < root.keys.size()){
                if (oldChildEntry.getKey().compareTo(root.keys.get(i)) == 0)
                    break;
                i++;
            }
            if (i == root.keys.size())
                return;

            root.keys.remove(i);
            ((IndexNode)root).children.remove(i+1);
            return;
        }
	}
    private Entry<K, Node<K,T>> deleteHelper(Node<K,T> parentNode,Node<K,T> node,Entry<K, Node<K,T>> entry,Entry<K, Node<K,T>> oldChildEntry) {
        if (!node.isLeafNode) {
            IndexNode idx = (IndexNode) node;
            int i = 0;
            while (i < idx.keys.size()) {
                if (entry.getKey().compareTo(node.keys.get(i)) < 0)
                    break;
                i++;
            }
            oldChildEntry = deleteHelper(idx, (Node) idx.children.get(i), entry, oldChildEntry);
            if (oldChildEntry == null) {
                return oldChildEntry;
            } else {
                int j = 0;
                while (j < idx.keys.size()){
                    if (oldChildEntry.getKey().compareTo((K)idx.keys.get(j)) == 0)
                        break;
                    j++;
                }
                idx.keys.remove(j);
                idx.children.remove(j+1);

                if (!idx.isUnderflowed() || idx.keys.size() == 0) {
                    oldChildEntry = null;
                    return oldChildEntry;
                } else {
                    if (idx == root)
                        return oldChildEntry;

                    K tmp =(K)idx.keys.get(0);
                    int k = 0;
                    while(k < parentNode.keys.size()){
                        if (tmp.compareTo(parentNode.keys.get(k)) < 0)
                            break;
                        k++;
                    }

                    int operation;
                    if (k > 0 && ((IndexNode)parentNode).children.get(k-1) != null){
                        operation = handleIndexNodeUnderflow((IndexNode)((IndexNode)parentNode).children.get(k-1),idx,(IndexNode) parentNode);
                    } else {
                        operation = handleIndexNodeUnderflow(idx,(IndexNode)((IndexNode)parentNode).children.get(k+1),(IndexNode) parentNode);
                    }

                    if (operation == -1) {
                        oldChildEntry = null;
                        return oldChildEntry;
                    } else {
                        oldChildEntry = new AbstractMap.SimpleEntry<K, Node<K, T>>(parentNode.keys.get(operation), parentNode);
                        return oldChildEntry;
                    }
                }
            }
        } else {
            LeafNode lf = (LeafNode) node;
            int i = 0;
            while (i < lf.keys.size()) {
                if (lf.keys.get(i).equals(entry.getKey()))
                    break;
                i++;
            }
            lf.keys.remove(i);
            lf.values.remove(i);

            if (!lf.isUnderflowed()) {
                oldChildEntry = null;
                return oldChildEntry;
            } else {
                if (lf == root)
                    return oldChildEntry;

                int operation;
                if (lf.previousLeaf != null && ((K)lf.keys.get(0)).compareTo(parentNode.keys.get(0)) >= 0) {
                    operation = handleLeafNodeUnderflow(lf.previousLeaf, lf, (IndexNode) parentNode);
                } else {
                    operation = handleLeafNodeUnderflow(lf, lf.nextLeaf, (IndexNode) parentNode);
                }

                if (operation == -1) {
                    oldChildEntry = null;
                    return oldChildEntry;
                } else {
                    oldChildEntry = new AbstractMap.SimpleEntry<K, Node<K, T>>(parentNode.keys.get(operation), parentNode);
                    return oldChildEntry;
                }
            }
        }
    }

	/**
	 * Handle LeafNode Underflow (merge or redistribution) left: the smaller node, right: the bigger node, parent: their parent index node return the splitkey position in parent if merged so that parent can
	 * delete the splitkey later on. -1 otherwise
	 */
	public int handleLeafNodeUnderflow(LeafNode<K,T> left, LeafNode<K,T> right,
			IndexNode<K,T> parent) {
        totFusions=totFusions+1;
        if (right.keys.size() + left.keys.size() > 2*D){
            int i = 0;
            while (i < parent.keys.size()){
                if (right.keys.get(0).compareTo(parent.keys.get(i)) < 0)
                    break;
                i++;
            }

            if (right.keys.size() > left.keys.size()){
                while (left.keys.size() < D){
                    left.keys.add(right.keys.get(0));
                    right.keys.remove(0);
                    left.values.add(right.values.get(0));
                    right.values.remove(0);
                }
            } else {
                while (left.keys.size() > D){
                    right.keys.add(0,left.keys.get(left.keys.size()-1));
                    left.keys.remove(left.keys.size()-1);
                    right.values.add(0,left.values.get(left.values.size()-1));
                    left.values.remove(left.values.size()-1);
                }
            }

            parent.keys.set(i-1,right.keys.get(0));

            return -1;

        } else {
            int i = 0;
            while(i < parent.keys.size()){
                if (right.keys.get(0).compareTo(parent.keys.get(i)) < 0)
                    break;
                i++;
            }

            while (right.keys.size()>0){
                left.keys.add(right.keys.get(0));
                right.keys.remove(0);
                left.values.add(right.values.get(0));
                right.values.remove(0);
            }
            if (right.nextLeaf != null)
                right.nextLeaf.previousLeaf = left;
            left.nextLeaf = right.nextLeaf;

            return i - 1;
        }
	}

	/**
	 * Handle IndexNode Underflow (merge or redistribution) leftIndex smaller node, rightIndex bigger node, parent index node, return the splitkey position in parent if merged so that parent can
	 * delete the splitkey later on. -1 otherwise
	 */
	public int handleIndexNodeUnderflow(IndexNode<K,T> leftIndex, IndexNode<K,T> rightIndex, IndexNode<K,T> parent) {
        parentFusions=parentFusions+1;
        totFusions=totFusions+1;
        if (rightIndex.keys.size() + leftIndex.keys.size() >= 2*D){
            int i = 0;
            while (i < parent.keys.size()) {
                if (rightIndex.keys.get(0).compareTo(parent.keys.get(i)) < 0)
                    break;
                i++;
            }

            if(leftIndex.keys.size() > rightIndex.keys.size()){
                while (leftIndex.keys.size() > D) {
                    rightIndex.keys.add(0, parent.keys.get(i - 1));
                    rightIndex.children.add(leftIndex.children.get(leftIndex.children.size()-1));
                    parent.keys.set(i-1,leftIndex.keys.get(leftIndex.keys.size()-1));
                    leftIndex.keys.remove(leftIndex.keys.size()-1);
                    leftIndex.children.remove(leftIndex.children.size()-1);
                }
            } else {
                while (leftIndex.keys.size() < D) {
                    leftIndex.keys.add(parent.keys.get(i-1));
                    leftIndex.children.add(rightIndex.children.get(0));
                    parent.keys.set(i-1,rightIndex.keys.get(0));
                    rightIndex.keys.remove(0);
                    rightIndex.children.remove(0);
                }
            }

            return -1;

        } else {
            int i = 0;
            while (i < parent.keys.size()){
                if (rightIndex.keys.get(0).compareTo(parent.keys.get(i)) < 0)
                    break;
                i++;
            }

            leftIndex.keys.add(parent.keys.get(i-1));

            while (rightIndex.keys.size() > 0){
                leftIndex.children.add(rightIndex.children.get(0));
                rightIndex.children.remove(0);
                leftIndex.keys.add(rightIndex.keys.get(0));
                rightIndex.keys.remove(0);
            }
            leftIndex.children.add(rightIndex.children.get(0));
            rightIndex.children.remove(0);

            return i - 1;
        }
	}
	
	

}