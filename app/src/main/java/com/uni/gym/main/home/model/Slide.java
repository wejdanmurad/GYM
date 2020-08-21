package com.uni.gym.main.home.model;

public class Slide {
    private String Image;
    private String Title;
    private String docId;

    public Slide(String Image, String Title, String docId) {
        this.Image = Image;
        this.Title = Title;
        this.docId = docId;
    }

    public String getImage() {
        return Image;
    }

    public String getTitle() {
        return Title;
    }

    public void setImage(String image) {
        Image = image;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}
