package com.bkap.dto;

import java.time.format.DateTimeFormatter;

import com.bkap.entity.Review;

public class ReviewViewModel {

    private final Review review;

    public ReviewViewModel(Review review) {
        this.review = review;
    }

    public Long getId() {
        return review.getId();
    }

    public String getProductName() {
        return review.getProduct().getName();
    }

    public String getContent() {
        return review.getContent();
    }

    public int getStarValue() {
        return review.getStarValue();
    }

    public String getStarDisplay() {
        int stars = review.getStarValue();
        return "★".repeat(stars) + "☆".repeat(5 - stars);
    }

    public String getFormattedCreatedDate() {
        if (review.getCreated() == null) return "";
        return review.getCreated().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}

