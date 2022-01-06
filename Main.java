import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.sql.*;

class Main{

    // Global variable for generating unique invoice number
    private static final long LIMIT = 10000000000L;
    private static long last = 0;

    // Function to generate Invoice ID
    public static long getID() {
    long id = System.currentTimeMillis() % LIMIT;
    if ( id <= last ) {
        id = (last + 1) % LIMIT;
    }
    return last = id;
    }

    // Function to signup user
    public static void signup(String email,String name,StringBuffer password,String mobile){
        Connection c = null;
        Statement stmt = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:zoho.db");
            stmt = c.createStatement();

            String sql="INSERT INTO Customer (Email,Password,Name,Mobile) VALUES('"+email+"','"+password+"','"+name+"','"+mobile+"');"; 
            stmt.executeUpdate(sql);
            c.close();
            stmt.close();
        }
        catch(Exception e){
            System.out.println("Email already exists!");
        }
    }   
    

    // Function to encrypt password
    public static StringBuffer encrypt(String text, int s)
    {
        StringBuffer result= new StringBuffer();
        for (int i=0; i<text.length(); i++)
        {
            if (Character.isUpperCase(text.charAt(i)))
            {
                char ch = (char)(((int)text.charAt(i) +
                                  s - 65) % 26 + 65);
                result.append(ch);
            }
            else
            {
                char ch = (char)(((int)text.charAt(i) +
                                  s - 97) % 26 + 97);
                result.append(ch);
            }
        }
        return result;
    }

    // Function to login user
    public static int login(String uname,String passw){
        StringBuffer pwd=encrypt(passw, 4);
        Connection c = null;
        Statement stmt = null;
        int r=0;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:zoho.db");
            stmt = c.createStatement();
            String sql="SELECT * FROM Customer WHERE Email LIKE '"+uname+"' AND"+" Password LIKE '"+pwd+"';";
            ResultSet rs=stmt.executeQuery(sql);
            
            if(!rs.next()){
                sql="SELECT * FROM Customer WHERE Email LIKE '"+uname+"';";
                rs=stmt.executeQuery(sql);
                if(rs.next()){
                    System.out.println("\nWrong Password ! ");
                    return 3;
                }
                stmt.close();
                c.close();
                r=2;
            }
            else{
                stmt.close();
                c.close();
                r=1;
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
        return r;
    }


    // Function to carry out shopping
    public static void shopping(String email){
        Connection c = null;
        Statement stmt = null;

        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:zoho.db");
            stmt = c.createStatement();
        }
        catch(Exception e){
            System.out.println(e);
        }

        Scanner s=new Scanner(System.in);
        char choice='n';
        HashMap<String,Integer> items=new HashMap<>();
        ArrayList<String[]> cart=new ArrayList<>();
        String sql;
        do{
        ArrayList<String []> inventory =new ArrayList<>();
        System.out.println("\n\nChoose What you want ");
        System.out.println("\n\nMobile - 1\nLaptop - 2\nTablet - 3\nExit - 4\n\n");
        int ch=s.nextInt();
        if(ch==4){
            return;
        }
        ResultSet rs =null;
        try{
            switch(ch){
                case 1:
                    sql="SELECT * FROM Inventory WHERE Category LIKE 'Mobile' ORDER BY TimesBought DESC;";
                    rs=stmt.executeQuery(sql);
                    break;
                case 2 :
                    sql="SELECT * FROM Inventory WHERE Category LIKE 'Laptop' ORDER BY TimesBought DESC;";
                    rs=stmt.executeQuery(sql);
                    break;
                case 3:
                    sql="SELECT * FROM Inventory WHERE Category LIKE 'Tablet' ORDER BY TimesBought DESC;";
                    rs=stmt.executeQuery(sql);
                    break;
            }
            while ( rs.next() ) {
                String item[]=new String[6];
                item[0]=rs.getString("Brand");
                item[1]=rs.getString("Model");
                item[2]=rs.getString("Price");
                item[3]=rs.getString("Stock");
                item[4]=rs.getString("Category");
                item[5]=rs.getString("TimesBought");
                inventory.add(item);
            }

            System.out.println("\n--------------------------------\n!!!Best Selling Products!!!\n");
            for(int i=0;i<3;i++){
                if(i>=inventory.size()){
                    break;
                }
                for(int j=0;j<3;j++){
                    System.out.print(inventory.get(i)[j]+" ");
                }
                System.out.println();
            }
            System.out.println("--------------------------------\n");
            for(int i=0;i<inventory.size();i++){
                System.out.print(i+" - ");
                for(int j=0;j<3;j++){
                    System.out.print(inventory.get(i)[j]+" ");
                }
                System.out.println();
            }

            System.out.println("\nChoose an model : ");
            ch=s.nextInt();

            cart.add(inventory.get(ch));
            String key=inventory.get(ch)[0]+inventory.get(ch)[1];
            if(items.get(key)==null){
                items.put(key,0);
            }
            else{
                items.put(key,items.get(key)+1);
            }

            System.out.println("\nPress 'n' to continue shopping and 'y' to checkout. Press 'q' to quit. (y/n/q) : ");
            choice=s.next().charAt(0);
            if(choice=='q'){
                return;
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }while (choice=='n'); 

    String key="";
    System.out.println();
    for(int i=0;i<cart.size();i++){
        key=cart.get(i)[0]+cart.get(i)[1];
        if(Integer.parseInt(cart.get(i)[3])<items.get(key)){
            System.out.println(cart.get(i)[0]+" "+cart.get(i)[1]+" out of stock.");
            return;
        }
    }

    int updatedStock=0;
    for(int i=0;i<cart.size();i++){
        updatedStock=Integer.parseInt(cart.get(i)[3])-1;
        sql="UPDATE Inventory set Stock = "+updatedStock+" where Brand LIKE '"+cart.get(i)[0]+"' AND Model LIKE '"+cart.get(i)[1]+"';";
       
        try{    
            stmt.executeUpdate(sql);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    sql="CREATE TABLE IF NOT EXISTS OrderDetails(ID TEXT NOT NULL,Email TEXT NOT NULL,Date TEXT NOT NULL, Category TEXT  NOT NULL, Brand TEXT NOT NULL, Model TEXT NOT NULL, Price INT NOT NULL)";
    try{    
        stmt.executeUpdate(sql);
    }catch(Exception e){
        System.out.println(e);
    }

    long transactionId=getID();
    long millis=System.currentTimeMillis();  
    java.sql.Date date=new java.sql.Date(millis);  

    String temp[];
    int total=0,newTimesBought;
    System.out.println("\n\n----------------------ORDER DETAILS---------------------------\n\n");
    System.out.println("Invoice Number : "+transactionId);
    System.out.println("Date : "+date);
    for(int i=0;i<cart.size();i++){
        temp=cart.get(i);
        sql="INSERT INTO OrderDetails(ID,Email,Date,Category,Brand,Model,Price) VALUES('"+transactionId+"','"+email+"','"+date+"','"+temp[4]+"','"+temp[0]+"','"+temp[1]+"','"+temp[2]+"');"; 
        try{    
            stmt.executeUpdate(sql);
        }catch(Exception e){
            System.out.println(e);
        }
        newTimesBought=Integer.parseInt(temp[5])+1;
        sql="UPDATE Inventory set TimesBought = "+newTimesBought+" where Brand LIKE '"+temp[0]+"' AND Model LIKE '"+temp[1]+"';";
        try{    
            stmt.executeUpdate(sql);
        }catch(Exception e){
            System.out.println(e);
        }

        total+=Integer.parseInt(temp[2]);

        System.out.println("\n\nCategory\tBrand\tModel\tPrice");
        System.out.println(temp[4]+"\t"+temp[0]+"\t"+temp[1]+"\t"+temp[2]);
    }
    System.out.println("\nTotal : "+total);
    System.out.println("\n\n-----------------------------------------------------------------");
    shopping(email);
    }

    // Function to reorder items
    public static void restock(){
        int threshold=10;
        Connection c = null;
        Statement stmt = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:zoho.db");
            stmt = c.createStatement();
            String sql="SELECT * FROM Inventory WHERE Stock<="+threshold+";";
            ResultSet rs=stmt.executeQuery(sql);
            ArrayList<String []> reorderItems =new ArrayList<>();
            while(rs.next()){
                String temp[]=new String[4];
                temp[0]=rs.getString("Category");
                temp[1]=rs.getString("Brand");
                temp[2]=rs.getString("Model");
                temp[3]=rs.getString("Stock");
                reorderItems.add(temp);
            }

            String temp[];
            int idx,amount;
            Scanner s=new Scanner(System.in);
            do{
                System.out.println("\n\n------Items to be re-ordered------\n");
                for(int i=0;i<reorderItems.size();i++){
                    temp=reorderItems.get(i);
                    System.out.println(i+" - "+temp[0]+" "+temp[1]+" "+temp[2]+" "+temp[3]);
                }
                
                System.out.println("\nEnter the index of item to be restocked. Enter -1 to quit : ");
                idx=s.nextInt();

                if(idx==-1){
                    break;
                }
                else{
                    System.out.println("Enter the amount of the item to be Restocked : ");
                    amount=s.nextInt();
                    temp=reorderItems.get(idx);
                    sql="UPDATE Inventory set Stock = "+(Integer.parseInt(temp[3])+amount)+" where Brand LIKE '"+temp[1]+"' AND Model LIKE '"+temp[2]+"';";
                    try{    
                        stmt.executeUpdate(sql);
                    }catch(Exception e){
                        System.out.println(e);
                    }
                    reorderItems.remove(idx);
                }
            }while(true);

        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    // Function to list the orders of a user
    public static void viewOrder(String uname){
        Connection c = null;
        Statement stmt = null;
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:zoho.db");
            stmt = c.createStatement();
            String sql;
            sql="CREATE TABLE IF NOT EXISTS OrderDetails(ID TEXT NOT NULL,Email TEXT NOT NULL,Date TEXT NOT NULL, Category TEXT  NOT NULL, Brand TEXT NOT NULL, Model TEXT NOT NULL, Price INT NOT NULL)";
            try{    
                stmt.executeUpdate(sql);
            }catch(Exception e){
                System.out.println(e);
            }

            sql="SELECT * FROM OrderDetails WHERE Email=='"+uname+"';";
            ResultSet rs=stmt.executeQuery(sql);
            if(!rs.next()){
                System.out.println("No Orders Found");
                return;
            }
            String invoiceNo;
            HashMap<String,ArrayList<ArrayList<String>>>map=new HashMap<>();

            while(rs.next()){
                ArrayList<String> temp=new ArrayList<>();
                ArrayList<ArrayList<String>> a=new ArrayList<>();
                invoiceNo=rs.getString("ID");
                temp.add(rs.getString("Date"));
                temp.add(rs.getString("Category"));
                temp.add(rs.getString("Brand"));
                temp.add(rs.getString("Model"));
                temp.add(rs.getString("Price"));

                if(map.get(invoiceNo)==null){
                    map.put(invoiceNo,new ArrayList<ArrayList<String>>());
                }
                a=map.get(invoiceNo);
                a.add(temp);
                map.put(invoiceNo,a);
            }

            int total=0;
            System.out.println("\n\n--------------Orders---------------\n");
            for(Map.Entry t: map.entrySet()){
                ArrayList<String> temp=new ArrayList<>();
                invoiceNo=(String)t.getKey();
                System.out.println("\n\nInvoice Number : "+invoiceNo);

                @SuppressWarnings("unchecked")
                ArrayList<ArrayList<String>> orders=(ArrayList<ArrayList<String>>)t.getValue();
               
                System.out.println("Date : "+orders.get(0).get(0));

                System.out.println("\nCategory\tBrand\tModel\tPrice");
                for(int i=0;i<orders.size();i++){
                    temp=orders.get(i);
                    System.out.println(temp.get(1)+"\t"+temp.get(2)+"\t"+temp.get(3)+"\t"+temp.get(4));
                }
            }
            System.out.println("---------------------------------------------");


        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    public static void main(String args[]){
        Connection c = null;
        String sql="";
        ResultSet rs=null;
        Statement stmt = null;
        

        try{
            File fp=new File("zusers_db.txt");
            Scanner sc=new Scanner(fp);
            String header[]=sc.nextLine().split("\\s+");

            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:zoho.db");
            stmt = c.createStatement();

            sql = "CREATE TABLE IF NOT EXISTS Customer (Email TEXT PRIMARY KEY  NOT NULL,Password TEXT NOT NULL, Name TEXT  NOT NULL, Mobile TEXT NOT NULL );";
            stmt.executeUpdate(sql);

            sql = "SELECT * FROM Customer;";
            rs=stmt.executeQuery(sql);
            if(!rs.next()){
                while (sc.hasNextLine()){
                    header=sc.nextLine().split("\\s+");
                    sql="INSERT INTO Customer (Email,Password,Name,Mobile) VALUES('"+header[0]+"','"+header[1]+"','"+header[2]+"','"+header[3]+"');"; 
                    stmt.executeUpdate(sql);
                }
                System.out.println("Initialized customers database");
            }
            sc.close();
            stmt.close();
            c.close();
        }
        catch(Exception e){
            System.out.println(e);
        }


        try{
            File fp=new File("z-kart_db.txt");
            Scanner sc=new Scanner(fp);
            String header[]=sc.nextLine().split("\\s+");

            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:zoho.db");
            stmt = c.createStatement();

            sql = "CREATE TABLE IF NOT EXISTS Inventory (Category TEXT NOT NULL,Brand TEXT NOT NULL, Model TEXT  NOT NULL, Price INT NOT NULL,Stock INT NOT NULL,TimesBought INT NOT NULL );";
            stmt.executeUpdate(sql);

            sql = "SELECT * FROM Inventory;";
            rs=stmt.executeQuery(sql);
            if(!rs.next()){
                while (sc.hasNextLine()){
                    header=sc.nextLine().split("\\s+");
                    sql="INSERT INTO Inventory (Category,Brand,Model,Price,Stock,TimesBought) VALUES('"+header[0]+"','"+header[1]+"','"+header[2]+"','"+header[3]+"','"+header[4]+"','"+0+"');"; 
                    stmt.executeUpdate(sql);
                }
            System.out.println("Initialized inventory database");
            }
            sc.close();
            stmt.close();
            c.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
        

        while(true){
            System.out.println("\n\nZ-Kart App");
            System.out.println("\nEnter\n1. Login\n2. Signup\n3. quit\n");
            Scanner s=new Scanner(System.in);
            int ip=s.nextInt();
            if(ip==3){
                break;
            }
            else if(ip==2){
                System.out.print("\nEnter Email : ");
                s.nextLine();
                String email=s.nextLine();
                System.out.print("Enter Name : ");
                String name=s.nextLine();
                System.out.print("Enter Password : ");
                StringBuffer password=encrypt(s.nextLine(),4);
                System.out.print("Enter Mobile : ");
                String mobile=s.nextLine();
                signup(email,name,password,mobile);
            }
            else if(ip==1){
                System.out.print("Enter the Username/Email : ");
                s.nextLine();
                String uname=s.nextLine();
                System.out.print("Enter the Password : ");
                String pwd=s.nextLine();

                if(uname.equals("admin@zoho.com") && pwd.equals("fuzzy")){
                    restock();
                }
                else{   
                    int n=login(uname, pwd);
                    if(n==1){
                        int option=0;
                        do{
                            System.out.println("\nEnter option\n\n1. View Orders\n2. Shop\n3. Logout\n");
                            option=s.nextInt();
                            if(option==2){
                                shopping(uname);
                            }
                            else if(option==1){
                                viewOrder(uname);
                            }
                        }while(option!=3);
                    }               
                    else if(n==2){
                        System.out.println("\nUser not found. Please Sign up!");
                        System.out.println("\nEnter Email : ");
                        String email=s.nextLine();
                        System.out.println("Enter Name : ");
                        String name=s.nextLine();
                        System.out.println("Enter Password : ");
                        StringBuffer password=encrypt(s.nextLine(),4);
                        System.out.println("Enter Mobile : ");
                        String mobile=s.nextLine();
                        signup(email,name,password,mobile);
                    }
                }
            }
        }

    }
}