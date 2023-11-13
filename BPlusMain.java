import java.util.*;
import java.io.*;

public class BPlusMain{
    public static void main(String args[]){
         BPlusTree<String,String> bp = new BPlusTree<String,String>();
         Map<String,String> data = new HashMap<String,String>();
        int ch=0;
        System.out.println(" BPlus Database");
        while(true){
            System.out.println(" BPlus Database");
            System.out.println(" 1. Load BPlus File");
            System.out.println(" 2. Search an Element");
            System.out.println(" 3. Print BPlus Tree");
            System.out.println(" 4. Insert an element");
            System.out.println(" 5. Delete an element");
            System.out.println(" 6. exit");
            
            Scanner s = new Scanner(System.in);
            int  num = Integer.parseInt(s.nextLine());

            switch(num){
                case 1: System.out.println(" Input file path ");
                        String filePath=s.nextLine();
                        try {
                        BufferedReader reader = new BufferedReader(new FileReader(filePath));
                        String line;
                        while((line=reader.readLine())!=null)  {
                            System.out.println(line.substring(15,line.length()));
                            data.put(line.substring(0,7),line.substring(15,line.length()));
                            bp.insert(line.substring(0,7),line.substring(15,line.length()));
                        }
                        reader.close();
                        //(new Utils()).printTree(bp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                case 2: System.out.println(" Enter Key Element ");
                        String key=s.nextLine();
                        System.out.println("Value for Key"+key+" is : " + bp.searchTree((String)key));
                        break;
                case 3: (new Utils()).printTree(bp);
                        break;
                case 4: System.out.println(" Enter Key ");
                        String key1=s.nextLine();
                        System.out.println(" Enter Value ");
                        String value1=s.nextLine();
                        bp.insert(key1,value1);
                        data.put(key1,value1);
                        break;
                case 5: System.out.println(" Enter Key ");
                        String key2=s.nextLine();
                        bp.delete(key2);
                        data.remove(key2);
                        break;
                case 6: System.out.println(" Existing the Console Prompt.... ");
                        ch=1;
                        break;
                default:
                        System.out.println("wrong input");
                        break;

            }
            if(ch==1){
                break;
            }
        }
        System.out.println("Total Splits : "+ bp.totSplits);
        System.out.println("Parent Splits : "+ bp.parentSplits);
        System.out.println("Total Fusions : "+ bp.totFusions);
        System.out.println("Parent Fusions : "+ bp.parentFusions);
    }
}