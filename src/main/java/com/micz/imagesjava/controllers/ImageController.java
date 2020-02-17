package com.micz.imagesjava.controllers;

import com.micz.imagesjava.models.Image;
import com.micz.imagesjava.models.User;
import com.micz.imagesjava.payload.response.ImageResponse;
import com.micz.imagesjava.payload.response.ImagesResponse;
import com.micz.imagesjava.payload.response.MessageResponse;
import com.micz.imagesjava.repository.ImageRepository;
import com.micz.imagesjava.repository.UserRepository;
import com.micz.imagesjava.security.services.UserDetailsImpl;
import com.micz.imagesjava.services.ImagesPaginationSrv;
import com.micz.imagesjava.services.TypeOfSorting;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
public class ImageController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    ImagesPaginationSrv imagesPaginationSrv;
    @Value("${michu.app.directory.images}")
    private String path;


    @CrossOrigin
    @DeleteMapping("/image/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long idUser = ((UserDetailsImpl) (authentication).getPrincipal()).getId();
        User user = userRepository.getOne(idUser);
        Optional<Image> imageByIdOpt = imageRepository.findById(id);
        if (imageByIdOpt.isPresent()) {
            File fileFolder = new File(path+"/"+idUser.toString());
            Image image = imageByIdOpt.get();
            for (final File file : fileFolder.listFiles()) {
                if (!file.isDirectory() && file.getName().equals(image.getName())) {
                    file.delete();
                    break;
                }
            }
            user.getImages().remove(image);
            userRepository.save(user);
            imageRepository.delete(image);
            return ResponseEntity.ok(new MessageResponse("Image was deleted successfully!"));
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: You can't remove this image. This image doesn't exists!"));
        }
    }


    @CrossOrigin
    @PostMapping(value = "/image")
    public ResponseEntity<?> postImage(@RequestParam("picture") MultipartFile picture) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long idUser = ((UserDetailsImpl) (authentication).getPrincipal()).getId();
//        if (!(authentication instanceof AnonymousAuthenticationToken)) {
//            System.out.println(authentication.getName());
//
//            System.out.println("idUsera:" + idUser);
//        }
        User user = userRepository.getOne(idUser);
        Optional<Image> presentImageOpt = user.getImages().stream().filter(i -> i.getName().equals(picture.getOriginalFilename())).findAny();
        if (presentImageOpt.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User already has image with this name!"));
        }
        File userDirectory = new File("/"+ path + "/" + idUser.toString());
        if (!userDirectory.exists()){
            userDirectory.mkdir();
        }
        Path filepath = Paths.get(userDirectory.getPath(), picture.getOriginalFilename());
        try (OutputStream os = Files.newOutputStream(filepath)) {
            os.write(picture.getBytes());
        }

        Image newImage = new Image();
        newImage.setName(picture.getOriginalFilename());
        imageRepository.save(newImage);
        user.getImages().add(newImage);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Image was added successfully!"));
//        image.transferTo(new File(path));
    }


    @GetMapping(value="/image", params = { "page", "sorting" })
    @CrossOrigin
    public ResponseEntity<ImagesResponse> getImages(Integer page, String sorting) {
        ImagesResponse imagesResponse = new ImagesResponse();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long idUser = ((UserDetailsImpl) (authentication).getPrincipal()).getId();
        List<ImageResponse> images = new LinkedList<>();
        File fileFolder = new File(path+"/"+idUser.toString());
        if (fileFolder != null) {
            User user = userRepository.getOne(idUser);
            TypeOfSorting type = TypeOfSorting.valueOf(sorting);
            Set<Image> allUserImages = user.getImages();
            imagesResponse.setCountOfAllImages(allUserImages.size());
            List<Image> userImages = imagesPaginationSrv.getSortedAndPaginatedUserImages(allUserImages, type, page);
            List<File> files = Arrays.asList(fileFolder.listFiles());
            for (Image image  : userImages) {
                File file = files.stream().filter(f -> f.getName().equals(image.getName())).findAny().get();
                if (!file.isDirectory()) {
                    String encodeBase64 = null;
                    try {
                        String extension = FilenameUtils.getExtension(file.getName());
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] bytes = new byte[(int)file.length()];
                        fileInputStream.read(bytes);
                        encodeBase64 = Base64.getEncoder().encodeToString(bytes);
                        String content = "data:image/" + extension +";base64," + encodeBase64;
                        String name = file.getName();
                        ImageResponse imageResponse = new ImageResponse();
                        imageResponse.setId(image.getId());
                        imageResponse.setContent(content);
                        imageResponse.setName(name);
                        images.add(imageResponse);
                        fileInputStream.close();
                    } catch (Exception e) {

                    }
                }
            }
            imagesResponse.setImages(images);
        }
        return new ResponseEntity<ImagesResponse>(imagesResponse, HttpStatus.OK);
    }

}
