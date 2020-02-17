package com.micz.imagesjava.controllers;

import com.micz.imagesjava.models.User;
import com.micz.imagesjava.payload.response.MessageResponse;
import com.micz.imagesjava.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
public class EditController {

    public static Object lock = new Object();

    @Autowired
    UserRepository userRepository;


    @CrossOrigin
    @PutMapping("/email/{email}")
    public ResponseEntity<?> deleteImage(@PathVariable String email, @RequestParam String newEmail) {
//		String newEmail = "newEmail";
        if (newEmail == null || newEmail.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Empty email!"));
        }
        String regex = "^(.+)@(.+)$";
        if (!newEmail.matches(regex)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: invalid email!"));
        }
        synchronized (lock) {
            Boolean existsEmail = userRepository.existsByEmail(newEmail);
            if (existsEmail) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Someone has got this email!"));
            }
            Optional<User> editedUserOpt = userRepository.findByEmail(email);
            if (!editedUserOpt.isPresent()) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: no user!"));
            }
            User user = editedUserOpt.get();
            user.setEmail(newEmail);
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("Email was changed succesfully!"));
        }

    }

}

