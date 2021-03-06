package com.eskyray.im.ui.beans;


public class PeopleItem {

    private String name = "";
    private String area = "";
    private String sex = "male";
    private int age = 0;
    private String image;
    private String introduce = "";

    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getArea(){
        return this.area;
    }
    public void setArea(String area){
        this.area = area;
    }

    public String getSex(){
        return this.sex;
    }
    public void setSex(String sex){
        this.sex = sex;
    }

    public int getAge(){
        return this.age;
    }
    public void setAge(int age ){
        this.age = age;
    }

    public void setImage(String image){
        this.image = image;
    }
    public String getImage(){
        return this.image;
    }

    public String getIntroduce(){
        return this.introduce;
    }
    public void setIntroduce(String introduce){
        this.introduce = introduce;
    }

}
