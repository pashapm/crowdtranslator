package zalivka.translator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

public class Cache {

	private static Gson sGson = new Gson();
	
	public static void cacheValue(Context ctx, String key, String value) {
		
	}
	
	public static void cache(Context ctx, Map<String, String> trs) {
		List<String[]> ltrs	= new ArrayList<String[]>();
		for (String s : trs.keySet()) {
			ltrs.add(new String[] {s, trs.get(s)});
		}
		try {
			File cache = new File(ctx.getFilesDir(), "translation_cache.txt");
			cache.delete();
			cache.createNewFile();
			PrintWriter pw = new PrintWriter(new FileWriter(cache));
			pw.print(sGson.toJson(ltrs));
	        pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Map<String, String> load(Context ctx) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			File cache = new File(ctx.getFilesDir(), "translation_cache.txt");
			if (!cache.exists()) {
				return map;
			}
	        BufferedReader reader = new BufferedReader(new FileReader(cache));
	        String text = reader.readLine(); //TODO
	        String[][] tt = sGson.fromJson(text, String[][].class);
	        for (String[] s : tt) {
	        	map.put(s[0], s[1]);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public static void clean(Context ctx) {
		File cache = new File(ctx.getFilesDir(), "translation_cache.txt");
		cache.delete();
	}
	
}
