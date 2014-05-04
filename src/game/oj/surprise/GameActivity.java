package game.oj.surprise;

import game.oj.surprise.GameView.OjThread;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.os.Build;

public class GameActivity extends ActionBarActivity implements OnClickListener
{
	private static final int MENU_PAUSE = 1;
	private static final int MENU_RESUME = 2;
	private static final int MENU_START = 3;
	private static final int MENU_STOP = 4;

	private OjThread thread;
	private GameView gView;

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_PAUSE, 0, "PAUSE");
		menu.add(0, MENU_RESUME, 0, "RESUME");
		menu.add(0, MENU_START, 0, "START");
		menu.add(0, MENU_STOP, 0, "STOP");

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id)
		{
			case MENU_PAUSE:
				thread.pause();
				return true;
			case MENU_RESUME:
				thread.unpause();
				return true;
			case MENU_START:
				thread.doStart();
				return true;
			case MENU_STOP:
				thread.setState(OjThread.LOSE, "STOPPED");
				return true;
		}

		return false;

	}

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oj_layout);

		gView = (GameView) findViewById(R.id.oj);
		thread = gView.getThread();

		if (savedInstanceState == null)
		{
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		else
		{

			thread.restoreState(savedInstanceState);

		}
	}

	protected void onPause()
	{

		super.onPause();
		gView.getThread().pause();

	}

	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		thread.saveState(outState);

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment
	{

		public PlaceholderFragment()
		{
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.fragment_game, container,
					false);
			return rootView;
		}
	}

	@Override
	public void onClick(View v)
	{

	}

}
