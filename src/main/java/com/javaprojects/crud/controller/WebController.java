package com.javaprojects.crud.controller;

import com.javaprojects.crud.export.CreateWorksheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/excel")
public class WebController {

    private String currentContent;

    @Autowired
    CreateWorksheet createExcel;

    @GetMapping("/generate")
    public void generate() {
        String content = "<b>Soumya Biswas</b><b>Test Editor</b><b>Test Underline</b>";

        Workbook wb = new XSSFWorkbook();
        String tabName = "test";
        createExcel.createWorkSheet(wb, content, tabName);

    }

}
