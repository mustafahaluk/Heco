package com.gral.cmpe436.heco;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class KuSiro {// Kuro and Siro  in general
    Bitmap res;//for drawing
    int x;//x axis on the panel
    int y;//y axis on the panel
    public KuSiro(Bitmap res, int x, int y ){
        this.x=x;
        this.y=y;
        this.res=res;
    }
    public void draw(Canvas canvas)
    {
        canvas.drawBitmap(this.res,x,y,null);
    }


}
