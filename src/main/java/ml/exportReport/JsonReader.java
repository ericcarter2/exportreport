package ml.exportReport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.schemas.office.office.STInsetMode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

        this.copyInternExcel();
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
        Map<String, String> jmap = new HashMap<>();
        int i = 4;

        while (iterator.hasNext()) {
            JsonObject it = (JsonObject) iterator.next();

            jmap.put("key", it.get("key").getAsString());
            //todo resolution ?
            jmap.put("project", it.get("project").getAsString());
            jmap.put("status", it.get("status").getAsString());
            jmap.put("severity", it.get("severity").getAsString());
            jmap.put("message", it.get("message").getAsString());
            jmap.put("author", it.get("author").getAsString());
            jmap.put("type", it.get("type").isJsonNull() ? "" : it.get("type").getAsString());
            jmap.put("assignee", it.get("assignee").isJsonNull()  ? "" : it.get("assignee").getAsString()); //test
            jmap.put("effort", it.get("effort").getAsString());
            jmap.put("creationDate", it.get("creationDate").getAsString());
            jmap.put("updateDate", it.get("updateDate").getAsString());
            //todo comment ?
            //jmap.put("comments", it.get("comments").isJsonNull()  ? "" : it.get("comments").getAsString()); //test
            jmap.put("project", it.get("project").getAsString()); //test
            jmap.put("component", it.get("component").getAsString()); //test

            writeReport(jmap, i++);
        }

    }
    private void writeReport(Map<String, String> map, int i) throws IOException {

        File excel = new File(this.path);
        FileInputStream fis = new FileInputStream(excel);
        XSSFWorkbook book = new XSSFWorkbook(fis);
        XSSFSheet sheet = book.getSheet("Issues Log");

        Row row = sheet.createRow(i);

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();

        FormulaEvaluator evaluator = book.getCreationHelper().createFormulaEvaluator();
        row.createCell(0).setCellValue(map.get("key"));
        row.createCell(1).setCellValue(map.get("status"));
        row.createCell(2).setCellValue(map.get("author"));
        row.createCell(3).setCellValue(map.get("creationDate"));
        row.createCell(4).setCellValue(map.get("assignee"));
        row.createCell(5).setCellValue(map.get("severity"));
        row.createCell(6).setCellValue(map.get("type"));
        row.createCell(7).setCellValue(map.get("project") + " : " + map.get("component"));
        row.createCell(8).setCellValue(dateFormat.format(date));
        row.createCell(9).setCellValue(map.get("message"));
        row.createCell(10).setCellValue("1.0");
        row.createCell(11).setCellValue("JAVA");
                //row.createCell(12).setCellValue(map.get("resolution"));
                //row.createCell(14).setCellValue(map.get("comments"));

        // suppose your formula is in B3
        XSSFSheet sheet2 = book.getSheet("Status Report");

        for (int i1= 60; i1 < 64; i1++) {

            CellReference cellReference = new CellReference("B" + i1);
            Row row2 = sheet2.getRow(cellReference.getRow());
            Cell cell = row2.getCell(cellReference.getCol());

            CellValue cellValue = evaluator.evaluate(cell);

            CellReference cellReference2 = new CellReference("D" + i1);
            Row row3 = sheet2.getRow(cellReference.getRow());
            Cell cell3 = row3.getCell(cellReference.getCol());

            CellValue cellValue2 = evaluator.evaluate(cell);
        }


        // Close workbook, OutputStream and Excel file to prevent leak
        book.write(new FileOutputStream(excel));
        fis.close();
        book.close();
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
    private void copyInternExcel() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("testissuelog.xlsx").getFile());
        FileInputStream fileInputStream = new FileInputStream(file);
        final XSSFWorkbook wb = new XSSFWorkbook(fileInputStream);

        // For resulting Excel file
        FileOutputStream writeFile = new FileOutputStream(this.path);
        XSSFWorkbook wb_new = wb;
        wb_new.write(writeFile);
        writeFile.flush();
        writeFile.close();
        fileInputStream.close();

    }
}
