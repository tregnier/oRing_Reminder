package com.lubenard.oring_reminder.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CsvWriter {

    Writer fileWriter;

    public CsvWriter(OutputStream outputFileStream) {
        fileWriter = new OutputStreamWriter(outputFileStream, StandardCharsets.UTF_8);
    }

    public void writeColumnsName(String[] names) throws IOException {
        if (fileWriter != null) {
            for (int i = 0; i != names.length; i++) {
                fileWriter.append(names[i]);
                fileWriter.append(',');
            }
            fileWriter.append('\n');
        }
    }

    public void writeColumnsDatas(ArrayList<String> datas) throws IOException {
        if (fileWriter != null) {
            for (int i = 0; i != datas.size(); i++) {
                fileWriter.append(datas.get(i));
                fileWriter.append(',');
            }
            fileWriter.append('\n');
        }
    }

    public void close() throws IOException{
        fileWriter.close();
    }
}
