package se.alexanderblom.delicious.fragments;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import se.alexanderblom.delicious.DeliciousAccount;
import se.alexanderblom.delicious.adapter.PostsAdapter;
import se.alexanderblom.delicious.model.Post;
import se.alexanderblom.delicious.model.PostsParser;
import se.alexanderblom.delicious.util.AsyncLoader;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class PostListFragment extends ListFragment implements LoaderCallbacks<List<Post>> {
	private static final String RECENTS_URL = "https://api.del.icio.us/v1/json/posts/recent";
	
	private DeliciousAccount deliciousAccount;
	
	private PostsAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		deliciousAccount = new DeliciousAccount(getActivity());
		adapter = new PostsAdapter(getActivity());
		
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	}

	@Override
	public Loader<List<Post>> onCreateLoader(int id, Bundle args) {
		return new PostsLoader(getActivity(), deliciousAccount, RECENTS_URL);
	}

	@Override
	public void onLoadFinished(Loader<List<Post>> loader, List<Post> posts) {
		adapter.addAll(posts);
		setListAdapter(adapter);
	}

	@Override
	public void onLoaderReset(Loader<List<Post>> loader) {
		adapter.clear();
	}
	
	private static class PostsLoader extends AsyncLoader<List<Post>> {
		private static final String TAG = "PostsLoader";
		
		private DeliciousAccount account;
		private String url;
		
		public PostsLoader(Context context, DeliciousAccount account, String url) {
			super(context);
			
			this.account = account;
			this.url = url;
		}

		@Override
		public List<Post> loadInBackground() {
			try {
				HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
				account.addAuth(request);
				
				try {
					InputStream is = request.getInputStream();
					
					return new PostsParser(new BufferedInputStream(is)).getPosts();
				} finally {
					request.disconnect();
				}
			} catch (IOException e) {
				Log.e(TAG, "Failed to fetch posts", e);
				
				return null;
			}
		}
	}
}