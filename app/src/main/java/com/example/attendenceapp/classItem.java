package com.example.attendenceapp;

public class classItem {

    public classItem(long cid, String classname, String subjectName) {
        this.cid = cid;
        this.classname = classname;
        this.subjectName = subjectName;
    }

    private long cid;
    private String classname;

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    private String subjectName;
    public classItem(String classname, String subjectName){
        this.classname = classname;
        this.subjectName = subjectName;
    }

    public long getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }
}
