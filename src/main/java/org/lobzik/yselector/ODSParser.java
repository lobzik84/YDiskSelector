package org.lobzik.yselector;

import com.github.miachm.sods.Range;
import com.github.miachm.sods.Sheet;
import com.github.miachm.sods.SpreadSheet;

import java.io.File;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.List;

public class ODSParser {
    public static TreeMap parse(File odsFile) throws Exception {
        TreeMap result = new TreeMap();
        SpreadSheet spread = new SpreadSheet(odsFile);
        //System.out.println("Number of sheets: " + spread.getNumSheets());

        List<Sheet> sheets = spread.getSheets();

        for (Sheet sheet : sheets) {
            //System.out.println("In sheet " + sheet.getName());

            Range range = sheet.getDataRange();
            for (int rowNum = range.getRow(); rowNum <= range.getLastRow(); rowNum++) {
                Range lastNameRange = range.getCell(rowNum, 2);
                if (lastNameRange == null || lastNameRange.getValue() == null) {
                    continue;
                }
                String lastName = lastNameRange.getValue().toString();
                lastName = lastName.trim();
                if (lastName.isEmpty()) {
                    continue;
                }

                Range portraitRange = range.getCell(rowNum, 4);
                if (portraitRange != null && portraitRange.getValue() != null) {
                    String portrait = portraitRange.getValue().toString();
                    if (portrait.contains(".")) {
                        portrait = portrait.substring(0,portrait.indexOf("."));
                    }
                    String trimmed = portrait.trim();
                    trimmed = trimmed.replaceAll("\\D+","");
                    trimmed = removeLeadingZeroes(trimmed);

                    if (!trimmed.isEmpty()) {
                        HashSet<String> set = new HashSet();
                        set.add(trimmed);
                        result.put("Portrait_" + lastName, set);
                    }
                }

                Range filesRange = range.getCell(rowNum, 5);
                if (filesRange != null && filesRange.getValue() != null ) {
                    String filesStr = filesRange.getValue().toString();
                    result.put(lastName, stringListToSet(filesStr));
                }

            }

        }
        return result;
    }

    public static HashSet<String> stringListToSet(String source) {
        HashSet<String> set = new HashSet();
        source = source.replaceAll(";", ",");
        source = source.replaceAll("\\.", ",");
        source = source.replaceAll("\n", ",");
        source = source.replaceAll(" ", ",");
        for (String str: source.split(",")) {
            String trimmed = str.trim();
            trimmed = trimmed.replaceAll("\\D+","");
            trimmed = removeLeadingZeroes(trimmed);
            if (!trimmed.isEmpty()) set.add(trimmed);
        }
        return set;
    }

    public static  String removeLeadingZeroes(String s) {
        while (s.startsWith("0")) {
            s = s.substring(1);
        }
        return s;
    }

}
