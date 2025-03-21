package com.trinity.ctc.global.util.parser;

import com.opencsv.CSVReader;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.ParserErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CsvParser {

    public List<Map<String, String>> parse(MultipartFile file) {
        List<Map<String, String>> resultList = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    row.put(headers[i].trim(), i < line.length ? line[i].trim() : "");
                }
                resultList.add(row);
            }
        } catch (Exception e) {
            throw new CustomException(ParserErrorCode.FAILED_PARSING);
        }

        return resultList;
    }
}
