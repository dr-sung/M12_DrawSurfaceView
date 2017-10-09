package edu.uco.hsung.m12_drawsurfaceview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

    private CircleView circleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout relativeLayout = findViewById(R.id.draw_frame);
        circleView = new CircleView(getApplicationContext());

        relativeLayout.addView(circleView);
    }

    private class CircleView extends SurfaceView {

        private int x, y;
        private int RADIUS = 100;
        private DisplayMetrics display;
        private int displayWidth, displayHeight;
        private int canvasWidth, canvasHeight;
        private SurfaceHolder surfaceHolder;
        private final Paint paint = new Paint();
        private Thread drawingThread;

        private int moveStep = 2; // will be computed
        private int direction = 1; // +1 for down, -1 for up

        public CircleView(Context context) {
            super(context, null);

            display = new DisplayMetrics();
            MainActivity.this.getWindowManager().getDefaultDisplay().getMetrics(display);
            // screen size, not canvas size
            displayWidth = display.widthPixels;
            displayHeight = display.heightPixels;

            x = displayWidth / 2;
            y = displayHeight / 2;

            surfaceHolder = getHolder();
            surfaceHolder.addCallback(new SurfaceHolderListener());
        }

    }

    private class SurfaceHolderListener implements SurfaceHolder.Callback {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            // actual canvas size is avaliable at runtime rendering
            // same as the screen size if full screen with no title bar
            // style = @android:style/Theme.NoTitleBar.Fullscreen
            circleView.canvasWidth = width;
            circleView.canvasHeight = height;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            circleView.drawingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    long prevTime = System.currentTimeMillis();
                    Canvas canvas = null;

                    while (!Thread.currentThread().isInterrupted()) {
                        canvas = circleView.surfaceHolder.lockCanvas();
                        if (canvas != null) {
                            long currTime = System.currentTimeMillis();
                            double elapsedTime = currTime - prevTime;
                            circleView.moveStep = (int) (elapsedTime / 5 ) + 5;

                            if (circleView.y + circleView.RADIUS >= circleView.canvasHeight ||
                                    circleView.y - circleView.RADIUS <= 0) {
                                circleView.direction *= -1;
                            }
                            circleView.y += circleView.moveStep * circleView.direction;

                            canvas.drawColor(Color.LTGRAY);
                            circleView.paint.setColor(Color.RED);
                            canvas.drawCircle(circleView.x, circleView.y, circleView.RADIUS, circleView.paint);

                            prevTime = currTime;
                            circleView.surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
            });
            circleView.drawingThread.start();

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (circleView.drawingThread != null) {
                circleView.drawingThread.interrupt();
            }
        }
    }
}
