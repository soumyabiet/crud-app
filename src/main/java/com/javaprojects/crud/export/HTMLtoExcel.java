package com.javaprojects.crud.export;

import net.htmlparser.jericho.Config;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class HTMLtoExcel {

    private static final Pattern HEAVY_REGEX = Pattern.compile("(<br/>)|(</br>)|(<br />)|(< /br>)");
    private static final int START_TAG = 0;
    private static final int END_TAG = 1;
    private static final String NEW_LINE = System.getProperty("line.separator");

    @Cacheable("tocellvalue")
   public RichTextString fromHtmlToCellValue(String html, Workbook workBook){
       Config.IsHTMLEmptyElementTagRecognised = true;
       
       Matcher m = HEAVY_REGEX.matcher(html);
       String replacedhtml =  m.replaceAll("");
       StringBuilder sb = new StringBuilder();
       sb.insert(0, "<div>");
       sb.append(replacedhtml);
       sb.append("</div>");
       String newhtml = sb.toString();
       Source source = new Source(newhtml);
       List<RichTextDetails> cellValues = new ArrayList<RichTextDetails>();
       for(Element el : source.getAllElements("div")){
           cellValues.add(createCellValue(el.toString(), workBook));
       }
       RichTextString cellValue = mergeTextDetails(cellValues);

       
       return cellValue;
   }
   
   // 


    // this returns a rich text string
    private static RichTextString mergeTextDetails(List<RichTextDetails> cellValues) {
        Config.IsHTMLEmptyElementTagRecognised = true;
        StringBuilder textBuffer = new StringBuilder();
        Map<Integer, Font> mergedMap = new LinkedHashMap<Integer, Font>(550, .95f);
        int currentIndex = 0;
        for (RichTextDetails richTextDetail : cellValues) {
            //textBuffer.append(BULLET_CHARACTER + " ");
            currentIndex = textBuffer.length();
            for (Entry<Integer, Font> entry : richTextDetail.getFontMap()
                .entrySet()) {
                mergedMap.put(entry.getKey() + currentIndex, entry.getValue());
            }
            textBuffer.append(richTextDetail.getRichText())
                .append(NEW_LINE);
        }

        RichTextString richText = new XSSFRichTextString(textBuffer.toString());
        for (int i = 0; i < textBuffer.length(); i++) {
            Font currentFont = mergedMap.get(i);
            if (currentFont != null) {
                richText.applyFont(i, i + 1, currentFont);
            }
        }
        return richText;
    }

    static RichTextDetails createCellValue(String html, Workbook workBook) {
        Config.IsHTMLEmptyElementTagRecognised  = true;
        Source source = new Source(html);
        Map<String, TagInfo> tagMap = new LinkedHashMap<String, TagInfo>(550, .95f);
        for (Element e : source.getChildElements()) {
            getInfo(e, tagMap);
        }

        StringBuilder sbPatt = new StringBuilder();
        sbPatt.append("(").append(StringUtils.join(tagMap.keySet(), "|")).append(")");
        String patternString = sbPatt.toString();
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(html);

        StringBuilder textBuffer = new StringBuilder();
        List<RichTextInfo> textInfos = new ArrayList<RichTextInfo>();
        ArrayDeque<RichTextInfo> richTextBuffer = new ArrayDeque<RichTextInfo>();
        while (matcher.find()) {
            matcher.appendReplacement(textBuffer, "");
            TagInfo currentTag = tagMap.get(matcher.group(1));
            if (START_TAG == currentTag.getTagType()) {
                richTextBuffer.push(getRichTextInfo(currentTag, textBuffer.length(), workBook));
            } else {
                if (!richTextBuffer.isEmpty()) {
                    RichTextInfo info = richTextBuffer.pop();
                    if (info != null) {
                        info.setEndIndex(textBuffer.length());
                        textInfos.add(info);
                    }
                }
            }
        }
        matcher.appendTail(textBuffer);
        Map<Integer, Font> fontMap = buildFontMap(textInfos, workBook);

        return new RichTextDetails(textBuffer.toString(), fontMap);
    }

    private static Map<Integer, Font> buildFontMap(List<RichTextInfo> textInfos, Workbook workBook) {
        Map<Integer, Font> fontMap = new LinkedHashMap<Integer, Font>(550, .95f);

        for (RichTextInfo richTextInfo : textInfos) {
            if (richTextInfo.isValid()) {
                for (int i = richTextInfo.getStartIndex(); i < richTextInfo.getEndIndex(); i++) {
                    fontMap.put(i, mergeFont(fontMap.get(i), richTextInfo.getFontStyle(), richTextInfo.getFontValue(), workBook));
                }
            }
        }

        return fontMap;
    }

    @SuppressWarnings("deprecation")
    private static Font mergeFont(Font font, STYLES fontStyle, String fontValue, Workbook workBook) {
        if (font == null) {
            font = workBook.createFont();
        }

        switch (fontStyle) {
        case BOLD:
        case EM:
        case STRONG:
            font.setBold(Boolean.TRUE);
            break;
        case UNDERLINE:
            font.setUnderline(Font.U_SINGLE);
            break;
        case ITALLICS:
            font.setItalic(true);
            break;
        case PRE:
            font.setFontName("Courier New");
        case COLOR:
            if (!isEmpty(fontValue)) {
                font.setColor(IndexedColors.BLACK.getIndex());
            }
            break;
        default:
            break;
        }

        return font;
    }

    private static RichTextInfo getRichTextInfo(TagInfo currentTag, int startIndex, Workbook workBook) {
        RichTextInfo info = null;
        switch (STYLES.fromValue(currentTag.getTagName())) {
        case SPAN:
            if (!isEmpty(currentTag.getStyle())) {
                for (String style : currentTag.getStyle()
                    .split(";")) {
                    String[] styleDetails = style.split(":");
                    if (styleDetails != null && styleDetails.length > 1) {
                        if ("COLOR".equalsIgnoreCase(styleDetails[0].trim())) {
                            info = new RichTextInfo(startIndex, -1, STYLES.COLOR, styleDetails[1]);
                        }
                    }
                }
            }
            break;
        default:
            info = new RichTextInfo(startIndex, -1, STYLES.fromValue(currentTag.getTagName()));
            break;
        }
        return info;
    }

    private static boolean isEmpty(String str) {
        return (str == null || str.trim().isEmpty());
    }

    private static void getInfo(Element e, Map<String, TagInfo> tagMap) {
        tagMap.put(e.getStartTag()
            .toString(),
            new TagInfo(e.getStartTag()
                .getName(), e.getAttributeValue("style"), START_TAG));
        if (e.getChildElements()
            .size() > 0) {
            List<Element> children = e.getChildElements();
            for (Element child : children) {
                getInfo(child, tagMap);
            }
        }
        if (e.getEndTag() != null) {
            tagMap.put(e.getEndTag()
                .toString(),
                new TagInfo(e.getEndTag()
                    .getName(), END_TAG));
        } else {
            // Handling self closing tags
            tagMap.put(e.getStartTag()
                .toString(),
                new TagInfo(e.getStartTag()
                    .getName(), END_TAG));
        }
    }

}
