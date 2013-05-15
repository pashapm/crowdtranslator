package zalivka.translator;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import zalivka.translator.Translator.TransUnit;


import android.content.Context;

import com.google.gson.Gson;
import com.parse.ParseException;
import com.parse.ParseObject;

public class Store {

	private static Gson sGson = new Gson();
	
	public static String postToCloud(String fname, String params) throws IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("https://api.parse.com/1/functions/"+fname);
		httppost.addHeader("X-Parse-Application-Id", "U8YbqxIpgBFEe1Oxvuws91T1D7LrUv83wi8GX4fx");
		httppost.addHeader("X-Parse-REST-API-Key", "LVXMivzLvK7Yjo4slmZa9MMbqKKnV5U4vrGpdymM");
		httppost.addHeader("Content-Type", "application/json");
		httppost.setEntity(new StringEntity(params));
	    HttpResponse response = httpclient.execute(httppost);
	    return EntityUtils.toString(response.getEntity());
	}
	
	public static void save(Context ctx, TransUnit unit) throws Exception {
		ParseObject pObj = new ParseObject("Translation");
		pObj.put("name", unit.fieldName);
		pObj.put("lang", ctx.getResources().getConfiguration().locale.getLanguage());
		pObj.put("transl", unit.nat);
		pObj.save();
	}
	
}
