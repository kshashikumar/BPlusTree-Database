import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.*;
import java.io.*;

public class Utils<K extends Comparable<K>, T> extends BPlusTree<K,T> {

  public  Map<K,T> mp = new HashMap<K,T>();

	public String outputTree(BPlusTree<K,T> tree) {
  LinkedBlockingQueue<Node<K,T>> queue;
  queue = new LinkedBlockingQueue<Node<K,T>>();
  String result = "";
  
  int nodesInCurrentLevel = 1;
  int nodesInNextLevel = 0;
  ArrayList<Integer> childrenPerIndex = new ArrayList<Integer>();
  queue.add(tree.root);
  while (!queue.isEmpty()) {
    Node<K,T> target = queue.poll();
    nodesInCurrentLevel--;
    if (target.isLeafNode) {
      LeafNode<K,T> leaf = (LeafNode<K,T>) target;
      result += "[";
      for (int i = 0; i < leaf.keys.size(); i++) {
        result += "(" + leaf.keys.get(i) + "," + leaf.values.get(i)
            + ");";
        mp.put(leaf.keys.get(i),leaf.values.get(i));
      }
      if (childrenPerIndex.isEmpty()) {
        result += "]$";
      } else {
        childrenPerIndex.set(0, childrenPerIndex.get(0) - 1);
        if (childrenPerIndex.get(0) == 0) {
          result += "]$";
          childrenPerIndex.remove(0);
        } else {
          result += "]#";
        }
  
      }

    } else {
      IndexNode<K,T> index = ((IndexNode<K,T>) target);
      result += "@";
      for (int i = 0; i < index.keys.size(); i++) {
        result += "" + index.keys.get(i) + "/";
      }
      result += "@";
      queue.addAll(index.children);
      if (index.children.get(0).isLeafNode) {
        childrenPerIndex.add(index.children.size());
      }
      nodesInNextLevel += index.children.size();
    }
  
    if (nodesInCurrentLevel == 0) {
      result += "%%";
      nodesInCurrentLevel = nodesInNextLevel;
      nodesInNextLevel = 0;
    }
  
  }
  result = "";
  return result;

}
	
	
//print the current tree to console

	public void printTree(BPlusTree<K,T> tree){
    outputTree(tree);
    Vector<String> v= new Vector<String>();
    for (Map.Entry<K,T> entry : mp.entrySet()) {
        v.add("Key : "+entry.getKey()+" Value: "+entry.getValue());
    }
    int c=0,r=0;
    while(true){
      int flag=0;
      Scanner s = new Scanner(System.in);
      System.out.println("1. next 10 elements");
      System.out.println("2. exit");
      switch(s.nextInt()){
        case 1: for(int i=c;i<v.size();i++){
                  System.out.println(v.get(i));
                  c++;
                  r++;
                  if(r==10){
                    r=0;
                    break;
                  }
                }
                break;
        case 2: flag=1;
                break;
      }
      if(flag==1){
        break;
      }
    }
	}

}