package com.edxavier.cerberus_sms.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Eder Xavier Rojas on 06/09/2016.
 */
public class CSVreader {
    InputStream inputStream;

    public CSVreader(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    public  ArrayList<String[]> read(){
        //List resultList = new ArrayList();
        ArrayList<String[]> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                list.add(row);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: "+ex);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: "+e);
            }
        }
        return list;
    }
}
