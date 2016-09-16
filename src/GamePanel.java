package com.example.android.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Random;


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;
    private long smokeStartTime;
    private long missileStartTime;
    private long coinStartTime;
    private long powerStartTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<Coin> coins;
    private ArrayList<Power_balls> power_balls;
    private ArrayList<TopBorder> topborder;
    private ArrayList<BotBorder> botborder;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;

    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best = 0;
    private int coins_earned = 0;



    public GamePanel(Context context)
    {

        super(context);


        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);



        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000)
        {
            counter++;
            try{thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;

            }catch(InterruptedException e){e.printStackTrace();}

        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.fundof));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        smoke = new ArrayList<Smokepuff>();
        missiles = new ArrayList<Missile>();
        coins = new ArrayList<Coin>();
        topborder = new ArrayList<TopBorder>();
        botborder = new ArrayList<BotBorder>();
        smokeStartTime=  System.nanoTime();
        missileStartTime = System.nanoTime();
        coinStartTime = System.nanoTime();

        thread = new MainThread(getHolder(), this);
        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();

    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction()==MotionEvent.ACTION_DOWN)
        {
            if(!player.getPlaying() && newGameCreated && reset)
            {
                player.setPlaying(true);
                player.setUp(true);
            }
            if(player.getPlaying())
            {

                if(!started)started = true;
                reset = false;
                player.setUp(true);
            }
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update()
    {

        if(player.getPlaying()) {

            if(botborder.isEmpty())
            {
                player.setPlaying(false);
                return;
            }
            if(topborder.isEmpty())
            {
                player.setPlaying(false);
                return;
            }

            bg.update();
            player.update();

            //calculate the threshold of height the border can have based on the score
            //max and min border heart are updated, and the border switched direction when either max or
            //min is met

            //Deixa o Border cum uma grossura fixa
            maxBorderHeight = player.getScore()*3/progressDenom;
            //cap max border height so that borders can only take up a total of 1/2 the screen
            //if(maxBorderHeight > HEIGHT/3)maxBorderHeight = HEIGHT/3;

            //Deixa o Border cum uma grossura fixa
            minBorderHeight = player.getScore()*3/progressDenom;

            //check bottom border collision
            for(int i = 0; i<botborder.size(); i++)
            {
                if(collision(botborder.get(i), player))
                    player.setPlaying(false);
            }

            //check top border collision
            for(int i = 0; i <topborder.size(); i++)
            {
                if(collision(topborder.get(i),player))
                    player.setPlaying(false);
            }

            //update top border
            this.updateTopBorder();

            //udpate bottom border
            this.updateBottomBorder();

            //add missiles on timer
            long missileElapsed = (System.nanoTime()-missileStartTime)/400000;

            if(missileElapsed >(2000 - player.getScore()/40))
            {
                //first missile always goes down the middle
                if(missiles.size()==0)
                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.
                            missile),WIDTH + rand.nextInt(), HEIGHT/rand.nextInt(3), 45, 15, player.getScore(), 13));

                }
                else
                {

                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+10, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight),45,15, player.getScore(),13));

                    //if the score its bigger than 300 (Blue) it starts to come 2 missels at once
                    if(player.getScore() > 300 && rand.nextInt(5) > 3)
                        missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                                WIDTH+12, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight),45,15, player.getScore(),13));

                }

                //reset timer
                missileStartTime = System.nanoTime();


            }

            //loop through every missile and check collision and remove
            for(int i = 0; i<missiles.size();i++)
            {
                //update missile
                missiles.get(i).update();

                if(collision(missiles.get(i),player))
                {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //remove missile if it is way off the screen
                if(missiles.get(i).getX()<-100)
                {
                    missiles.remove(i);
                    break;
                }
            }

            //add coins on time
            long coinElapsed = (System.nanoTime()-coinStartTime)/1000000;

           if(coinElapsed >(2000 - player.getScore()/40))
            {
                //first coin always goes down the middle
                if(coins.size()==0)
                {
                    //DESENHO AAQUIII
                    coins.add(new Coin(BitmapFactory.decodeResource(getResources(),R.drawable.coins),
                    WIDTH + rand.nextInt(), HEIGHT/rand.nextInt(3), 40, 43, player.getScore(), 4));

                }
                else
                {

                    coins.add(new Coin(BitmapFactory.decodeResource(getResources(),R.drawable.coins),
                    WIDTH + 10, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight),40,43, player.getScore(),4));

                }

                //reset timer
                coinStartTime = System.nanoTime();

            }




            for(int i = 0; i<coins.size();i++)
            {
                //update coins
                coins.get(i).update();

                if(collision(coins.get(i),player))
                {
                    coins.remove(i);
                    if(player.getScore() >300)
                        coins_earned += 1;

                    coins_earned += 1;
                    break;
                }
                //remove coin if it is way off the screen
                if(coins.get(i).getX()<-100)
                {
                    coins.remove(i);
                    break;
                }
            }

            //add power on time
            long PowersElapsed = (System.nanoTime()-powerStartTime)/1000000;

            if(coinElapsed >(2000 - player.getScore()/40))
            {
                //first coin always goes down the middle
                if(power_balls.size()==0)
                {
                    //DESENHO AAQUIII
                    power_balls.add(new Power_balls(BitmapFactory.decodeResource(getResources(),R.drawable.powers),
                            WIDTH + rand.nextInt(), HEIGHT/rand.nextInt(3), 55, 55, player.getScore(), 1));

                }
                else
                {

                    power_balls.add(new Power_balls(BitmapFactory.decodeResource(getResources(),R.drawable.powers),
                            WIDTH + 10, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight),45, 45, player.getScore(),1));

                }

                //reset timer
                powerStartTime = System.nanoTime();

            }




            for(int i = 0; i<power_balls.size();i++)
            {
                //update coins
                power_balls.get(i).update();

                if(collision(coins.get(i),player))
                {
                    power_balls.remove(i);
                    break;
                }
                //remove coin if it is way off the screen
                if(power_balls.get(i).getX()<-100)
                {
                    power_balls.remove(i);
                    break;
                }
            }



            //add smoke puffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime)/1000000;
            if(elapsed > 120){
                smoke.add(new Smokepuff(player.getX(), player.getY()+10));
                smokeStartTime = System.nanoTime();
            }

            for(int i = 0; i<smoke.size();i++)
            {
                smoke.get(i).update();
                if(smoke.get(i).getX()<-10)
                {
                    smoke.remove(i);
                }
            }
        }
        else
        {
                player.resetDY();
                if(!reset)
                {
                    newGameCreated = false;
                    startReset = System.nanoTime();
                    reset = true;
                    dissapear = true;
                    explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion),player.getX(),
                            player.getY()-30, 100, 100, 25);
                }

                explosion.update();
                long resetElapsed = (System.nanoTime()-startReset)/1000000;

                if(resetElapsed > 2500 && !newGameCreated)
                {
                    newGame();
                }


        }

    }
    public boolean collision(GameObject a, GameObject b)
    {
        if(Rect.intersects(a.getRectangle(), b.getRectangle()))
        {
            return true;
        }
        return false;
    }

    /*
    public boolean catching(GameObject a, GameObject b)
    {
        coins_earned += 1;
        return true;
    }
    */


    @Override
    public void draw(Canvas canvas)
    {
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);

        if(canvas!=null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if(!dissapear) {
                player.draw(canvas);
            }
            //draw smokepuffs
            for(Smokepuff sp: smoke)
            {
                sp.draw(canvas,  player.getScore());
            }
            //draw missiles
            for(Missile m: missiles)
            {
                m.draw(canvas);
            }


            //draw coins
            for(Coin c: coins)
            {
                c.draw(canvas);
            }


            //draw topborder
            for(TopBorder tb: topborder)
            {
                tb.draw(canvas);
            }

            //draw botborder
            for(BotBorder bb: botborder)
            {
                bb.draw(canvas);
            }
            //draw explosion
            if(started)
            {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);

        }
    }

    public void updateTopBorder()
    {
        //every 900 points, insert randomly placed top blocks that break the pattern
        if(player.getScore()%900 ==0)
        {
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
            topborder.get(topborder.size()-1).getX()+20,0,(int)((rand.nextDouble()*(maxBorderHeight))+1)));
        }
        for(int i = 0; i<topborder.size(); i++)
        {
            topborder.get(i).update();
            if(topborder.get(i).getX()<-20)
            {
                topborder.remove(i);
                //remove element of arraylist, replace it by adding a new one

                //calculate topdown which determines the direction the border is moving (up or down)
                if(topborder.get(topborder.size()-1).getHeight()>=maxBorderHeight)
                {
                    topDown = false;
                }
                if(topborder.get(topborder.size()-1).getHeight()<=minBorderHeight)
                {
                    topDown = true;
                }
                //new border added will have larger height
                if(topDown)
                {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick),topborder.get(topborder.size()-1).getX()+20,
                            0, topborder.get(topborder.size()-1).getHeight()+2));
                }
                //new border added wil have smaller height
                else
                {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick),topborder.get(topborder.size()-1).getX()+20,
                            0, topborder.get(topborder.size()-1).getHeight()));
                }

            }
        }

    }
    public void updateBottomBorder()
    {
        //every 900 points, insert randomly placed bottom blocks that break pattern

   //    RESOLVER AINDA!!! EM CIMA TBM

        if(player.getScore()%900 == 0)
        {
            botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botborder.get(botborder.size()-1).getX()+20,(int)((maxBorderHeight)
                    +(HEIGHT-maxBorderHeight))));
        }


        //update bottom border
        for(int i = 0; i<botborder.size(); i++)
        {
            botborder.get(i).update();

            //if border is moving off screen, remove it and add a corresponding new one
            if(botborder.get(i).getX()<-20) {
                botborder.remove(i);


                //determine if border will be moving up or down
                if (botborder.get(botborder.size() - 1).getY() <= HEIGHT-maxBorderHeight) {
                    botDown = true;
                }
                if (botborder.get(botborder.size() - 1).getY() >= HEIGHT - minBorderHeight) {
                    botDown = false;
                }

                if (botDown)
                {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1)
                    .getY()));
                } else
                {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1)
                    .getY()-2));
                }

            }
        }
    }

    public void saves()
    {
        Save salva = new Save(best, coins_earned);
        salva.load();
        best = salva.getBest();
        coins_earned = salva.getCoins();
    }
    public void newGame()
    {

        //Save salva = new Save(best, coins_earned);
        /*
        best = salva.getBest();
        coins_earned = salva.getCoins();*/
        dissapear = false;

        botborder.clear();
        topborder.clear();

        missiles.clear();
        coins.clear();
        smoke.clear();

        minBorderHeight = 15;
        maxBorderHeight = 20;

        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT/2);



        //create initial borders

        //initial top border
        for(int i = 0; i*20<WIDTH+40;i++)
        {
            //first top border create
            if(i==0)
            {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
                ),i*20,0, 10));
            }
            else
            {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
                ),i*20,0, topborder.get(i-1).getHeight()));
            }
        }
        //initial bottom border
        for(int i = 0; i*20<WIDTH+40; i++)
        {
            //first border ever created
            if(i==0)
            {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick)
                        ,i*20,HEIGHT - minBorderHeight));
            }
            //adding borders until the initial screen is filed
            else
            {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, botborder.get(i - 1).getY()));
            }
        }

        newGameCreated = true;


    }


    public void drawText(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: " + (player.getScore()), 10, HEIGHT - 10, paint);

        /*Save salva = new Save(best, coins_earned);
        salva.load();
        best = salva.getBest();
        coins_earned = salva.getCoins();*/

        best = (player.getScore() > best) ? player.getScore() : best;

        canvas.drawText("BEST: " + best, WIDTH - 195, HEIGHT - 10, paint);
        canvas.drawText("COINS: " + coins_earned, 10, HEIGHT - 438, paint);

        if(!player.getPlaying()&&newGameCreated&&reset)
        {
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH/2-50, HEIGHT/2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH/2-50, HEIGHT/2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH/2-50, HEIGHT/2 + 40, paint1);
        }
    }



}