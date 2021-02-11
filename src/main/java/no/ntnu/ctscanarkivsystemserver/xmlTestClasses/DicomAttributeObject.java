package no.ntnu.ctscanarkivsystemserver.xmlTestClasses;

import java.util.ArrayList;

public class DicomAttributeObject {

    String keyword;
    String tag;
    String vr;
    ArrayList<ValueXmlObject> valueList;

    /**
     * Constructor
     */
    DicomAttributeObject() {
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getVr() {
        return vr;
    }

    public void setVr(String vr) {
        this.vr = vr;
    }

    public ArrayList<ValueXmlObject> getValueList() {
        return valueList;
    }

    public void setValueList(ArrayList<ValueXmlObject> valueList) {
        this.valueList = valueList;
    }

}
