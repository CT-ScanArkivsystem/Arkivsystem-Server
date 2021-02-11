package no.ntnu.ctscanarkivsystemserver.xmlTestClasses;

import java.util.ArrayList;

public class DicomAttributeObject {

    String keyword;
    String tag;
    String vr;
    ArrayList<ValueXmlObject> valueList = new ArrayList<>();

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

    public String getAllValues() {
        StringBuilder allValues = new StringBuilder("");
        for (int i = 0; i < valueList.size(); i++) {
            allValues.append(valueList.get(i).elementContent);
            if (i != (valueList.size() - 1)) {
                allValues.append(", ");
            }
        }

        return allValues.toString();
    }

    public void setValueList(ArrayList<ValueXmlObject> valueList) {
        this.valueList = valueList;
    }

}
