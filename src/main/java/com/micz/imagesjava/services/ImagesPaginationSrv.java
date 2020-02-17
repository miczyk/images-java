package com.micz.imagesjava.services;

import com.micz.imagesjava.models.Image;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ImagesPaginationSrv {
    public List<Image> getSortedAndPaginatedUserImages (Set<Image> images, TypeOfSorting type, int pageNumber) {
        int countOfImagesOnPage = 8;
        Stream<Image> result = null;
        if (type == TypeOfSorting.BY_DATE_ASC) {
            result = images.stream().sorted(Comparator.comparing(Image::getId));
        } else if (type == TypeOfSorting.BY_DATE_DESC) {
            result = images.stream().sorted(Comparator.comparing(Image::getId).reversed());
        } else if (type == TypeOfSorting.BY_NAME_ASC) {
            result = images.stream().sorted(Comparator.comparing(Image::getName));
        } else if (type == TypeOfSorting.BY_NAME_DESC) {
            result = images.stream().sorted(Comparator.comparing(Image::getName).reversed());
        }
        return result.skip((pageNumber - 1)*countOfImagesOnPage).limit(countOfImagesOnPage).collect(Collectors.toList());
    }
}
