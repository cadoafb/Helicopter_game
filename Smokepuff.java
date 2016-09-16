package com.example.android.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Smokepuff extends GameObject{
    public int r;
    public Smokepuff(int x, int y)
    {
        r = 5;
        super.x = x;
        super.y = y;
    }
    public void update()
    {
        x-=10;
    }

    public void draw(Canvas canvas, int  _score)
    {
        Paint paint = new Paint();
        //Cor da fumaca muda de acordo c a distancia/dificuldade atingida
        if(_score <= 100)
            paint.setColor(Color.GREEN);
        else if(_score > 100 && _score <= 300)
            paint.setColor(Color.BLUE);
        else if(_score > 300 && _score <= 600)
            paint.setColor(Color.RED);
        else if(_score > 600)
            paint.setColor(Color.BLACK);

        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x-r, y-r, r, paint);
        canvas.drawCircle(x-r+2, y-r-2,r,paint);
        canvas.drawCircle(x-r+4, y-r+1, r, paint);
    }

}
