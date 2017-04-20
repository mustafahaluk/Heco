//Mustafa Haluk AYDIN // 2011400327
//CmpE436 Term Project

import java.io.*;
import java.net.*;

class TCPServer
{
    private static int turn=0;//shows whose turn to play. Even for host, odd for client
    private static int[][] val= new int[9][9];
    private static int[][] table = new int[11][11];

    private static DataOutputStream outToClient1 = null;
    private static DataOutputStream outToClient2 = null;
    private static BufferedReader inFromClient1 = null;
    private static BufferedReader inFromClient2 = null;



    public static void main(String argv[]) throws IOException {
        System.out.println("Server started");
        for(int i=0; i<9;i++) {for(int j=0; j<9;j++) {val[i][j]=8;}}//creating an empty board
        //Firstly, the host must be connected ------------------------------------------------
        System.out.println("This is the Host thread");
        System.out.println("Waiting connection from host");
        ServerSocket hostSocket1 = null;
        try {
            hostSocket1 = new ServerSocket(44444);//connecting 44444 port of the server
        } catch (IOException e) {
            e.printStackTrace();
        }
        Socket connectionSocket1 = null;
        try {
            connectionSocket1 = hostSocket1.accept();//waiting host player to connect server
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Connected by host");

        try {
            outToClient1 = new DataOutputStream(connectionSocket1.getOutputStream());
            outToClient1.writeBytes(arrToStr(val) + '\n');//send initial board data
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inFromClient1 = new BufferedReader(new InputStreamReader(connectionSocket1.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //-------------------------------------------------------------------------------

        //After host connected, client must be connected --------------------------------
        System.out.println("Client threadi bu");
        System.out.println("Waiting connection from client");
        ServerSocket hostSocket2 = null;
        try {
            hostSocket2 = new ServerSocket(55555);//connecting 55555 port of the server
        } catch (IOException e) {
            e.printStackTrace();
        }
        Socket connectionSocket2 = null;
        try {
            connectionSocket2 = hostSocket2.accept();//waiting client player to connect server
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Connected by client");

        try {
            outToClient2 = new DataOutputStream(connectionSocket2.getOutputStream());
            outToClient2.writeBytes(arrToStr(val) + '\n');//send initial board data
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inFromClient2 = new BufferedReader(new InputStreamReader(connectionSocket2.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //--------------------------------------------------------------------------
        Thread host = new Thread(){
            @Override
            public void run() {

                while(true)
                {
                    if (turn % 2 == 0) {//if turn is even, host allowed to play
                        System.out.println("Host should play");
                        String ss="";
                        boolean canPlay=false;
                        try {
                            ss= inFromClient1.readLine();//reads host input
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(!ss.equals("")){
                            System.out.println("Valid value is read from server");
                            String[] sp = ss.split("vs");
                            int xval=Integer.parseInt(sp[0]);
                            int yval=Integer.parseInt(sp[1]);
                            canPlay=calculation(xval,yval,1);//change board after the play from host
                        }else System.out.println("Value read from server is not valid for a play ");
                        try { outToClient1.writeBytes(arrToStr(val) + '\n'); } //send current board
                        catch (IOException e) { e.printStackTrace(); }
                        System.out.println("Board sent to server");
                        System.out.println("In host, the turn is " + turn + " .");
                        if(!ss.equals("") && canPlay) turn += 1;//if the play is valid and change the board, then turn++
                    }else{
                        try { Thread.sleep(100); }
                        catch (InterruptedException e) { e.printStackTrace(); }
                    }
                }
            }
        };
        Thread client = new Thread(){
            @Override
            public void run() {
                //Almost same for client thread
                while(true)
                {
                    if (turn % 2 == 1) {//if turn is odd, client allowed to play
                        System.out.println("Client should play");
                        String ss= "";
                        boolean canPlay=false;
                        try {
                            ss = inFromClient2.readLine();//reads client input
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(!ss.equals("")){
                            System.out.println("Valid value is read from server");
                            String[] sp = ss.split("vs");
                            int xval=Integer.parseInt(sp[0]);
                            int yval=Integer.parseInt(sp[1]);
                            canPlay=calculation(xval,yval,2);//change board after the play from client
                        }else System.out.println("Value read from server is not valid for a play ");
                        try { outToClient2.writeBytes(arrToStr(val) + '\n'); } //send current board
                        catch (IOException e) { e.printStackTrace(); }
                        System.out.println("Board sent to server");
                        System.out.println("In client, the turn is " + turn + " .");
                        if(!ss.equals("") && canPlay) turn += 1;
                    }else{
                        try { Thread.sleep(100); }
                        catch (InterruptedException e) { e.printStackTrace(); }
                    }
                }
            }
        };
        host.start();//running host thread to send and get data
        client.start();//running client thread to send and get data
    }

    private static boolean calculation(int xval, int yval, int kusiro) {
        int x=xval+1, y=yval+1;
        if(val[xval][yval]==1 || val[xval][yval]==2){  System.out.print("The place already taken"); return false;}
        else {
            table = new int[11][11];//extended board for calculation of stones in all edges of the initial board
            //kuros are 1, siros are 2, blank zones are 0, edges are 3
            /*'val' array             'table' array
             *                          3 3 3 3 3
             *  8 2 1                   3 0 2 1 3
             *  8 1 8      ==>          3 0 1 0 3
             *  2 1 8                   3 2 1 0 3
             *                          3 3 3 3 3
             */
            for(int i=0;i<11;i++) for(int j=0;j<11;j++) if((i==0 || i==10) || (j==0 || j==10)) table[i][j]=3;
            for(int i=0;i<9;i++) {
                for(int j=0;j<9;j++){
                    if(val[i][j]==1) table[i+1][j+1]=1;
                    else if(val[i][j]==2) table[i+1][j+1]=2;
                }
            }

            if(x>0 && y>0 && x<10 && y<10 && table[x][y]==0){
                table[x][y] = kusiro;//put the stone
                //if the neighbour is not one of its kind, check the neighbour is surrounded by enemy
                if(table[x][y+1]==2/table[x][y] && isSurrounded(x,y+1,2/table[x][y]))  remove(x,y+1,2/table[x][y]);
                if(table[x+1][y]==2/table[x][y] && isSurrounded(x+1,y,2/table[x][y]))  remove(x+1,y,2/table[x][y]);
                if(table[x][y-1]==2/table[x][y] && isSurrounded(x,y-1,2/table[x][y]))  remove(x,y-1,2/table[x][y]);
                if(table[x-1][y]==2/table[x][y] && isSurrounded(x-1,y,2/table[x][y]))  remove(x-1,y,2/table[x][y]);
                //if the player makes a suicidal move
                if(isSurrounded(x,y,table[x][y])) remove(x,y,table[x][y]);
            }
        }
        /*'val' array             'table' array
         *                          3 3 3 3 3
         *  1 8 1                   3 1 0 1 3
         *  8 1 8      <==          3 0 1 0 3
         *  2 1 2                   3 2 1 2 3
         *                          3 3 3 3 3
         */
        for(int i=0;i<9;i++) {
            for(int j=0;j<9;j++) {
                if(table[i+1][j+1]==0) val[i][j]=8;
                else val[i][j]=table[i+1][j+1];
            }
        }
        return true;
    }

    public static boolean isSurrounded(int x, int y, int kusiro){
        boolean[][] isChecked = new boolean[11][11];//table representing where is checked
        isChecked[x][y]=true;
        //check all neigbours for finding a way to live
        if(table[x][y]==kusiro) return( check(x+1,y,kusiro,isChecked) && check(x,y+1,kusiro,isChecked) &&
                                        check(x-1,y,kusiro,isChecked) && check(x,y-1,kusiro,isChecked));
        else if(table[x][y]==0) return false;
        else return true;
    }
    public static boolean check(int x, int y, int kusiro, boolean[][] isChecked){

        if(isChecked[x][y]) return true;
        else if(table[x][y]==kusiro){
            isChecked[x][y]=true;
            return( check(x+1,y,kusiro,isChecked) && check(x,y+1,kusiro,isChecked) &&
                    check(x-1,y,kusiro,isChecked) && check(x,y-1,kusiro,isChecked));
        }
        else if(table[x][y]==0) return false;
        else{
            isChecked[x][y]=true;
            return true;
        }
    }
    //if check method finds a group surrounded by enemy, run remove method to remove from board
    public static void remove(int x, int y, int kusiro){
        if(table[x][y]==kusiro){
            table[x][y]=0;
            if(table[x][y+1]!=kusiro && table[x+1][y]!=kusiro && table[x][y-1]!=kusiro && table[x-1][y]!=kusiro) return;
            else{ remove(x-1,y,kusiro); remove(x+1,y,kusiro); remove(x,y-1,kusiro); remove(x,y+1,kusiro); }
        }
    }
    //for sending the board as a string
    private static String arrToStr(int[][] arr) {
        String s="";
        for(int i=0; i<9;i++) {
            for(int j=0; j<9;j++) {
                s=s + arr[i][j]+ "-";
            }
            s = s + "!!";
        }
        return s;
    }
}