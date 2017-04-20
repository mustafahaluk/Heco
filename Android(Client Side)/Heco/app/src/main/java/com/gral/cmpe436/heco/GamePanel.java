package com.gral.cmpe436.heco;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;



public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    public static final int WIDTH = 450;//width of the panel
    public static final int HEIGHT = 800;//height of the panel
    public static int EDGE;//distance between subsequent two point on the board
    public Socket socket;//for communicating with server
    public DataOutputStream dos;//for sending data to server
    public BufferedReader inn;//for reading data sent by server
    public static int turn;//for deciding who is going to play, bot not used
    boolean touching;//for understanding the first touch on the screen
    private MainThread thread;//thread for drawing images
    private Background bg;
    //there are 2 kinds of stones in the game, Kuro and Siro. Kusiro refers to the both
    private int[][] mKS;//2-d array for demonstrating where the kusiros are

    public GamePanel(Context context) {
        super(context);
        if(First.isKuro) turn=0;
        else turn=1;
        EDGE=46;//for 9x9 board, the distance is closer to 46
        touching=false;//initially, no touch
        mKS = new int[9][9];//array gor
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        while(retry)
        {
            try{ thread.setRunning(false);
                 thread.join();
            }catch(InterruptedException e){e.printStackTrace();}
            retry = false;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.goboard));
        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();

        AsyncTask aaaa = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    socket= new Socket("138.68.104.126", First.port);//connected to server's port
                    dos = new DataOutputStream(socket.getOutputStream());
                    inn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (IOException e) { e.printStackTrace(); }
                try {
                    mKS = strToKusiro(inn.readLine());//firstly read initial board from the server
                } catch (IOException e) { e.printStackTrace(); }
                return null; } };
        aaaa.execute();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction()==MotionEvent.ACTION_DOWN){//when touching screen
            if(!touching /*&& turn%2==0*/) {// enter when first touch
                touching = true;
                try { if(putKuSiro(event)) turn++; }//send server where you going to put the kusiro
                catch (IOException e) { e.printStackTrace(); }
            }
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP)//when releasing
        {
            if(touching) {
                touching = false;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean putKuSiro(MotionEvent event) throws IOException {
        //adjusting for all screen types
        int yy = (int)(event.getY()/getHeight()*HEIGHT)-15, xx = (int)(event.getX()/getWidth()*WIDTH)-15;
        final String strKuSiros= (xx/EDGE)+"vs"+(yy/EDGE);//prepare a string that contains x and y values
        if(xx<HEIGHT && xx>0 && yy<EDGE*9 && yy>0 ){//work when user touchs the board
            if(mKS[xx/EDGE][yy/EDGE]==8) {//if touched area does not contain a kusiro
                AsyncTask wwww = new AsyncTask(){
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        try {  dos.writeBytes(strKuSiros + '\n'); }//send the data
                        catch (IOException e) { e.printStackTrace(); }
                        return null; } };
                wwww.execute();

                AsyncTask rrrr = new AsyncTask(){
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        try { mKS = strToKusiro(inn.readLine()); }//gets current board
                        catch (IOException e) { e.printStackTrace(); }
                        return null; } };
                rrrr.execute();

            }else {//if it contains a kusiro
                AsyncTask wwww = new AsyncTask(){
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        try {  dos.writeBytes("" + '\n'); }//sends nothing
                        catch (IOException e) { e.printStackTrace(); }
                        return null; } };
                wwww.execute();

                AsyncTask rrrr = new AsyncTask(){
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        try { mKS = strToKusiro(inn.readLine()); }//gets current board
                        catch (IOException e) { e.printStackTrace(); }
                        return null; } };
                rrrr.execute();

                return false;//if not a valid move
            }
        }else {//work when user touchs outside of the board
            AsyncTask wwww = new AsyncTask(){
                @Override
                protected Object doInBackground(Object[] objects) {
                    try {  dos.writeBytes("" + '\n'); }//sends nothing
                    catch (IOException e) { e.printStackTrace(); }
                    return null; } };
            wwww.execute();

            AsyncTask rrrr = new AsyncTask(){
                @Override
                protected Object doInBackground(Object[] objects) {
                    try { mKS = strToKusiro(inn.readLine()); }//get current board
                    catch (IOException e) { e.printStackTrace(); }
                    return null; } };
            rrrr.execute();
            return false;//if not a valid move
        }
        return true;//if a valid move
    }
    @Override
    public void draw(Canvas canvas) {
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);

        if(canvas!=null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            for(int i=0;i<mKS.length;i++){
                for(int j=0;j<mKS[0].length;j++){
                    //puts kuro to the board
                    if(mKS[i][j]==1) new KuSiro(BitmapFactory.decodeResource(getResources(), R.drawable.rsz_kuro),
                            i * EDGE + EDGE/2, j * EDGE + EDGE/2).draw(canvas);
                    //puts siro to the board
                    else if(mKS[i][j]==2)new KuSiro(BitmapFactory.decodeResource(getResources(), R.drawable.rsz_siro),
                            i * EDGE + EDGE/2, j * EDGE + EDGE/2).draw(canvas);
                }
            }
            canvas.restoreToCount(savedState);
        }
    }
    //converts data sent by server to 2-d array representing the board
    private static int[][] strToKusiro(String s) {
        String[] splitted =s.split("!!");
        int l = splitted.length;
        String[][] onbyone= new String[l][];
        for(int i=0;i<l;i++){
            onbyone[i]=splitted[i].split("-");
        }
        int [][] val = new int[l][onbyone[0].length];
        for(int i=0;i<l;i++){
            for(int j=0;j<l;j++){
                val[i][j]=Integer.parseInt(onbyone[i][j]);
            }
        }
        return val;
    }
}