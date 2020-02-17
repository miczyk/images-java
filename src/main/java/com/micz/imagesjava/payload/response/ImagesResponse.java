package com.micz.imagesjava.payload.response;

import java.util.List;

public class ImagesResponse {

    private Integer countOfAllImages;
    private List<ImageResponse> images;

    public Integer getCountOfAllImages() {
        return countOfAllImages;
    }

    public void setCountOfAllImages(Integer countOfAllImages) {
        this.countOfAllImages = countOfAllImages;
    }

    public List<ImageResponse> getImages() {
        return images;
    }

    public void setImages(List<ImageResponse> images) {
        this.images = images;
    }
}
