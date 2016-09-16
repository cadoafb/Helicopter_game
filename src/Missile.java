package com.example.android.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

public class Missile extends GameObject
{
    private int score;
    private int speed;
    private Random rand = new Random();
    private Animation animation = new Animation();
    private Bitmap spritesheet;

    public Missile(Bitmap res, int x, int y, int w, int h, int s, int numFrames)
    {
        super.x = x;
        super.y = y;
        width = w;
        height = h;
        score = s;

        //velocidade dos misseis dependendo do score
        if(s <= 100)
            speed = 10 + (int) (rand.nextInt(10));
        else if(s > 100 && s <= 300)
            speed = 15 + (int) (rand.nextInt(10));
        else if(s > 300 && s <= 600)
            speed = 15 + (int) (rand.nextInt(15));
        else if(s > 600)
            speed = 15 + (int) (rand.nextInt(18));


        //speed = 40 + (int) (rand.nextDouble()*score/15);

        //cap missile speed
        //if(speed>40)speed = 40;

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        for(int i = 0; i<image.length;i++)
        {
            image[i] = Bitmap.createBitmap(spritesheet, 0, i*height, width, height);
        }

        animation.setFrames(image);
        animation.setDelay(90-speed);

    }
    public void update()
    {
        x -= speed;
        animation.update();
    }
    public void draw(Canvas canvas)
    {
        try
        {
            canvas.drawBitmap(animation.getImage(),x,y,null);
        }

        catch(Exception e){}
    }

    @Override
    public int getWidth()
    {
        //offset slightly for more realistic collision detection
        return width-10;
    }

}
