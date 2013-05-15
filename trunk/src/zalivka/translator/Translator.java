package zalivka.translator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

public class Translator extends ListActivity {

	private TranslateAdapter mAdapter;
	
	private List<TransUnit> mStrings = new LinkedList<Translator.TransUnit>();
	
	private List<Integer> mExcludeList = new LinkedList<Integer>() {{
		add(R.string.about);
	}};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setTitle("Click to add a better translation");
		mAdapter = new TranslateAdapter();
		
		obtainStringsMap();
		loadCachedTranslations();
		
		getListView().setAdapter(mAdapter);
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Editor.editTranslation(Translator.this, (TransUnit) 
						arg0.getItemAtPosition(arg2));
			}
		});
		
		
		// parse stuff 
		Parse.initialize(this, "",
		"");
		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
		ParseACL.setDefaultACL(defaultACL, true);
	}

	private void loadCachedTranslations() {
		Map<String, String> cache = Cache.load(this);
		for (TransUnit unit : mStrings) {
			if (cache.containsKey(unit.fieldName)) {
				unit.nat = cache.get(unit.fieldName);
				unit.modified = true;
			}
		}
	}

	private void obtainStringsMap() {
		Configuration conf = getResources().getConfiguration();
		Locale oldLocale = conf.locale;
		
		Field[] fields = R.string.class.getFields();
		try {
			for (int i=0; i<fields.length; ++i) {
				TransUnit unit = new TransUnit();
				unit.fieldName = fields[i].getName();
				unit.id = fields[i].getInt(null);
				if (! mExcludeList.contains(unit.id)) {
					mStrings.add(unit);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		// getting English strings
		conf.locale = new Locale("en");
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		Resources resources = new Resources(getAssets(), metrics, conf);
		
		Iterator<TransUnit> eiterator = mStrings.iterator();
		while (eiterator.hasNext()) {
			TransUnit unit = eiterator.next();
			try {
				unit.en = resources.getString(unit.id);
			} catch (android.content.res.Resources.NotFoundException e) {
				// no English string
				eiterator.remove();
			}
		}
		
		// getting the other lang strings
		conf.locale = oldLocale;
		resources = new Resources(getAssets(), metrics, conf);
		Iterator<TransUnit> niterator = mStrings.iterator();
		while (niterator.hasNext()) {
			TransUnit unit = niterator.next();
			try {
				unit.nat = resources.getString(unit.id);
			} catch (android.content.res.Resources.NotFoundException e) {
				// no translated string
				unit.nat = unit.en;
			}
		}
	}

	public void cache() {
		Map<String, String> map = new HashMap<String, String>();
		for (TransUnit unit : mStrings) {
			map.put(unit.fieldName, unit.nat);
		}
		Cache.cache(this, map);
	}
	
	public void save(TransUnit unit) {
		cache();
		mAdapter.notifyDataSetChanged();
		upload(this, unit);
	}
	
	private void upload(final Context ctx, TransUnit unit) {
		new AsyncTask<TransUnit, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(TransUnit... params) {
				try {
					Store.save(ctx, params[0]);
					Log.d("!!!!", "Translation saved");
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				} 
			}
			
			protected void onPostExecute(Boolean result) {
				String res = result ? "uploaded" : "fail";
				Toast.makeText(Translator.this, res, Toast.LENGTH_SHORT).show();
			};  
			
		}.execute(unit);
	}
	
	private class TranslateAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mStrings.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mStrings.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			TransUnit unit = (TransUnit) getItem(position);
			
			LinearLayout lay = null;
			LayoutInflater inf = LayoutInflater.from(Translator.this);
			if (convertView == null) {  
				lay = (LinearLayout) inf.inflate(R.layout.elem, null);
	        } else {
	        	lay = (LinearLayout) convertView;
	        }
			
			TextView enStr = (TextView) lay.findViewById(R.id.tr_string);
			enStr.setText(unit.en);
			
			TextView natStr = (TextView) lay.findViewById(R.id.tr_trans);
			natStr.setText(unit.nat);
			
			return lay;
		}
		
	}
	
	public static class TransUnit {
		transient boolean modified = false;
		transient int id;
		transient String en;
		
		String fieldName;
		String nat;
		
		@Override
		public int hashCode() {
			return id;
		}
		
		@Override
		public boolean equals(Object o) {
			return o != null && ((TransUnit)o).id == id;
		}
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("clear cache");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Cache.clean(this);
		obtainStringsMap();
		mAdapter.notifyDataSetInvalidated();
		return true;
	}
	
}
