import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by MLangreau on 02/11/2016.
 */
public class JsonReader {
    private String sURL, path, conLogin, conPass ;
    private String project;

    public ArrayList<String> getStrList() {
        return strList;
    }

    private ArrayList<String> strList;

    public JsonReader(String sURL, String path, String conLogin, String conPass) throws IOException {
        this.sURL = sURL;
        this.path = path;
        this.conLogin = conLogin;
        this.conPass = conPass;
        this.getProjects();
    }

    public JsonReader(String sURL, String path, String conLogin, String conPass, String project) throws IOException {
        this.sURL = sURL;
        this.path = path;
        this.conLogin = conLogin;
        this.conPass = conPass;
        this.project = project;
        this.PrintIssues();

    }

    private void PrintIssues() throws IOException {

        String userPassc = this.conLogin + ":" + this.conPass;
        String encoding = new sun.misc.BASE64Encoder().encode(userPassc.getBytes());
        String url =  this.sURL +"/api/issues/search?statuses=OPEN,REOPENED,CONFIRMED&types=VULNERABILITY"
                + "&projectKeys=" + this.project;
        URL myURL2 = new URL(url);
        HttpURLConnection myURLConnection2 = (HttpURLConnection)myURL2.openConnection();
        myURLConnection2.setRequestMethod("GET");
        myURLConnection2.setRequestProperty("Authorization", "Basic " + encoding);
        myURLConnection2.setDoOutput(true);


        int responseCode = myURLConnection2.getResponseCode();
        System.out.println("Sending 'POST' request to URL : " + url
                + "\n Response Code : " + responseCode + "\n\n");


        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) myURLConnection2.getContent())); //Convert the input stream to a json element

        JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.
        JsonArray issues = (JsonArray) rootobj.get("issues");
        Iterator iterator = issues.iterator();

        FileWriter fw = new FileWriter(this.path);

        while (iterator.hasNext()) {
            JsonObject it = (JsonObject) iterator.next();
            //System.out.println(iterator.next());
           /* System.out.println(it.get("key").getAsString() + ", "
                    + it.get("status").getAsString() + ", "
//                    + it.get("resolution").toString() + ", "
                    + it.get("severity").getAsString() + ", "
                    + it.get("message").getAsString() + ", "
                    + it.get("author").getAsString() + ", "
                    //+ it.get("assignee").toString() + ", "
                    + it.get("effort").getAsString() + ", "
                  //  + it.get("tag").getAsString() + ", "
                    + it.get("creationDate").getAsString() + ", "
                    + it.get("updateDate").getAsString() + ", ");
                    //+ it.get("comments").toString());*/
            CSVUtils.writeLine(fw, Arrays.asList(it.get("key").getAsString(),
                    it.get("project").getAsString(),
                    it.get("status").getAsString(),
                    it.get("severity").getAsString(),
                    it.get("message").getAsString(),
                    it.get("author").getAsString(),
                    it.get("effort").getAsString(),
                    it.get("creationDate").getAsString(),
                    it.get("updateDate").getAsString()));
        }
        fw.flush();
        fw.close();
    }

    private void getProjects() throws IOException {
        String userPassc = this.conLogin + ":" + this.conPass;
        String encoding = new sun.misc.BASE64Encoder().encode(userPassc.getBytes());
        String url =  this.sURL + "/api/projects/index?format=json";
        URL myURL2 = new URL(url);
        HttpURLConnection myURLConnection2 = (HttpURLConnection)myURL2.openConnection();
        myURLConnection2.setRequestMethod("GET");
        myURLConnection2.setRequestProperty("Authorization", "Basic " + encoding);
        myURLConnection2.setDoOutput(true);


        int responseCode = myURLConnection2.getResponseCode();
        System.out.println("Sending 'POST' request to URL : " + url
                + "\n Response Code : " + responseCode + "\n\n");


        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) myURLConnection2.getContent())); //Convert the input stream to a json element

        //JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.
        JsonArray issues = root.getAsJsonArray();
        Iterator iterator = issues.iterator();
        this.strList = new ArrayList<>();

        while (iterator.hasNext()) {
            JsonObject it = (JsonObject) iterator.next();
            this.strList.add(it.get("k").getAsString());
            //it.get("nm").getAsString();
        }
    }

}
