package org.onesun.textmining.uclassify;

public class DefaultResult {
    
    private String keywords;
    
    private Double percent;

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }
    
    public String toString()
    {
        return percent+":"+keywords;
    }
    
}
