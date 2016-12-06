package ml.exportReport;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;


/**
 * Created by MLangreau on 15/11/2016.
 */
public class ReportCreator {
    private String input, output;

    public ReportCreator(String input, String output) throws IOException {
        this.input = input;
        this.output = output;
        copyInternExcel();
        reportCSVtoXlsx();
    }
    private void copyInternExcel() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("testissuelog.xlsx").getFile());
        FileInputStream fileInputStream = new FileInputStream(file);
        final XSSFWorkbook wb = new XSSFWorkbook(fileInputStream);

        // For resulting Excel file
        FileOutputStream writeFile = new FileOutputStream(this.output);
        XSSFWorkbook wb_new = wb;
        wb_new.write(writeFile);
        writeFile.flush();
        writeFile.close();
        fileInputStream.close();

    }
    private void reportCSVtoXlsx() throws IOException {
        String csvFile = this.input;
        String line = "";
        String cvsSplitBy = ",";

        File excel = new File(this.output);
        FileInputStream fis = new FileInputStream(excel);
        XSSFWorkbook book = new XSSFWorkbook(fis);
        XSSFSheet sheet = book.getSheet("Issues Log");

        int i = 5;
        BufferedReader br = new BufferedReader(new FileReader(csvFile));

        try {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] yasca = line.split(cvsSplitBy);
                Row row = sheet.createRow(i++);

                for (int j = 0; j < 7; j++) {
                   row.createCell(j).setCellValue(yasca[j]);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        book.write(new FileOutputStream(excel));
        br.close();
        fis.close();
        book.close();


    }





}
