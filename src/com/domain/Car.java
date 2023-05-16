package com.domain;


import org.orm.annotations.Column;

public class Car {
    private Long cno ;
    //@Column("car_name")
    private String cname ;
    private String color ;
    private Double price ;

    public Long getCno() {
        return cno;
    }

    public void setCno(Long cno) {
        this.cno = cno;
    }

    public String getCname() {
        return cname;
    }

    //@Column("car_name")
    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Car(Long cno, String cname, String color, Double price) {
        this.cno = cno;
        this.cname = cname;
        this.color = color;
        this.price = price;
    }

    public Car() {
    }

    @Override
    public String toString() {
        return "Car{" +
                "cno=" + cno +
                ", cname='" + cname + '\'' +
                ", color='" + color + '\'' +
                ", price=" + price +
                '}';
    }
}
