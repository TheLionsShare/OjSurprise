package game.oj.surprise;

import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback
{

	// will be used to randomly generate fruits and bombs
	private static final Random RNG = new Random();

	class OjThread extends Thread
	{

		// Keys used for saving the state of the game
		private static final String JAR_X = "jarX";
		private static final String JAR_Y = "jarY";
		private static final String SCORE = "score";

		private int gameMode = READY;
		public static final int PAUSE = 0;
		public static final int READY = 1;
		public static final int RUNNING = 2;
		public static final int LOSE = 3;

		private int movement = STILL;

		// only jar can move left/right
		private static final int STILL = 0;
		private static final int LEFT = -1;
		private static final int RIGHT = 1;

		// Only fruit/explosives can move down.
		private static final int DOWN = 2;

		// Drawables
		private static final int JAR = 1;

		private long score;
		private long movementDelay = 600;

		private TextView statusText;

		// Draw background as bitmap: more efficient
		private Bitmap bgrnd;

		private int canvasWidth = 1;
		private int canvasHeight = 1;

		// Drawables:
		private Drawable jar;
		private Drawable monkey;
		private Drawable orange;

		// Movement speeds for fruit and jar respectively
		private double fallSpeed;
		private double moveSpeed;

		private Handler handler;

		private Context context;

		private int jarHeight;
		private int jarWidth;

		private long lastTime;

		private Paint juiceMeter;

		private boolean runnable;

		private SurfaceHolder surfaceHolder;

		private double jarX;
		private double jarY;

		public OjThread(SurfaceHolder sfh, Context context, Handler handler)
		{
			surfaceHolder = sfh;
			this.context = context;
			this.handler = handler;

			Resources res = context.getResources();

			jar = res.getDrawable(R.drawable.ceramic);
			monkey = res.getDrawable(R.drawable.monkey);
			orange = res.getDrawable(R.drawable.orange);

			bgrnd = BitmapFactory.decodeResource(res, R.drawable.jungle1);

			jarWidth = jar.getIntrinsicWidth();
			jarHeight = jar.getIntrinsicHeight();

			juiceMeter = new Paint();
			juiceMeter.setAntiAlias(true);
			juiceMeter.setARGB(255, 120, 180, 0);

			score = 0;

		}

		public void doStart()
		{
			synchronized (surfaceHolder)
			{

				jarX = canvasWidth / 2;
				jarY = canvasHeight - jarHeight / 2;

				lastTime = System.currentTimeMillis() + 100;
				setState(RUNNING);

			}

		}

		/**
		 * @return void
		 * 
		 *         Pauses the game, defined below
		 * 
		 */
		public void pause()
		{
			synchronized (surfaceHolder)
			{
				if (gameMode == RUNNING)
				{
					setState(PAUSE);
				}

			}

		}

		/**
		 * @param savedState
		 * @return void
		 * 
		 *         Restores previous state if surfaceview is destroyed
		 * 
		 */
		public synchronized void restoreState(Bundle savedState)
		{
			setState(PAUSE);
			movement = 0;

			jarX = savedState.getDouble(JAR_X);
			jarY = savedState.getDouble(JAR_Y);

			score = savedState.getInt(SCORE);

		}

		public void run()
		{

			while (runnable)
			{
				Canvas c = null;
				try
				{
					c = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder)
					{

						if (gameMode == RUNNING)
						{
							update();
						}
						;
						doDraw(c);

					}

				}
				finally
				{
					if (c != null)
					{
						surfaceHolder.unlockCanvasAndPost(c);

					}

				}

			}

		}

		/**
		 * @param map
		 * @return
		 * @return Bundle
		 * 
		 *         Gets the position of the monkey, bombs, and fruits and the
		 *         jar
		 * 
		 */
		public Bundle saveState(Bundle map)
		{
			if (map != null)
			{
				map.putLong(SCORE, Long.valueOf(score));
				map.putDouble(JAR_X, Double.valueOf(jarX));
				map.putDouble(JAR_Y, Double.valueOf(jarY));

			}

			return map;

		}

		public void setRunning(boolean b)
		{
			runnable = b;

		}

		public void setState(int mode)
		{
			synchronized (surfaceHolder)
			{

				setState(mode, null);

			}

		}

		public void setState(int mode, CharSequence message)
		{
			gameMode = mode;

			if (gameMode == RUNNING)
			{
				Message msg = handler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("text", "");
				b.putInt("visible", View.INVISIBLE);
				msg.setData(b);
				handler.sendMessage(msg);

			}
			else
			{
				Resources res = context.getResources();
				CharSequence str = "";

				if (gameMode == READY)
				{
					str = "READY";

				}

				else if (gameMode == PAUSE)
				{
					str = "PAUSED";

				}

				else if (gameMode == LOSE)
				{
					str = "GAME OVER";

				}

				if (message != null)
				{
					str = message + "\n" + str;

				}

				if (gameMode == LOSE)
				{
					score = 0;

				}

				Message msg = handler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("text", str.toString());
				b.putInt("viz", View.VISIBLE);
				msg.setData(b);
				handler.sendMessage(msg);

			}
		}

		public void unpause()
		{
			synchronized (surfaceHolder)
			{
				lastTime = System.currentTimeMillis() + 100;

			}
			setState(RUNNING);

		}

		boolean doKeyDown(int keyCode, KeyEvent msg)
		{

			synchronized (surfaceHolder)
			{
				boolean okStart = false;

				// Can only go left or right

				if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
					okStart = true;
				if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
					okStart = true;

				if (okStart && gameMode == READY || gameMode == LOSE)
				{

					doStart();
					return true;

				}

				else if (okStart && gameMode == PAUSE)
				{
					unpause();
					return true;

				}
				else if (gameMode == RUNNING)
				{

					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
					{
						jarX -= 1;
						return true;

					}
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
					{
						jarX += 1;
						return true;

					}

				}

			}

			return false;

		}

		public void doDraw(Canvas canvas)
		{

			canvas.drawBitmap(bgrnd, 0, 0, null);

			canvas.save();

			jar.setBounds(0, 0, jarHeight, jarWidth);

			canvas.restore();

		}

		public void update()
		{
			long now = System.currentTimeMillis();

			if (lastTime > now)
				return;

		}
	}

	private Context context;

	private TextView statusText;

	private OjThread thread;

	public GameView(Context context, AttributeSet attr)
	{
		super(context, attr);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		thread = new OjThread(holder, context, new Handler()
		{
			public void handleMessage(Message m)
			{
				statusText.setVisibility(m.getData().getInt("visible"));
				statusText.setText(m.getData().getString("text"));

			}

		});

		setFocusable(true);

	}

	public OjThread getThread()
	{

		return thread;

	}

	public boolean onKeyDown(int keyCode, KeyEvent msg)
	{
		return thread.doKeyDown(keyCode, msg);

	}

	public void onWindowFocusChanged(boolean hasWindowFocus)
	{

		if (!hasWindowFocus)
		{
			thread.pause();
		}

	}

	public void setTextview(TextView text)
	{

		statusText = text;

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height)
	{
		// Surface does not change;

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0)
	{
		thread.setRunning(true);
		thread.start();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0)
	{
		boolean retry = true;
		thread.setRunning(false);
		while (retry)
		{

			try
			{
				thread.join();
				retry = false;
			}
			catch (InterruptedException e)
			{

			}

		}

	}

}