package com.example.serverwebjava.interfaces;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface ServerWebServiceInterface {
    List<Integer> convertStringToListOfIntegers(String string);
    String convertFileForSend(MultipartFile file, Integer idUser, List<Integer> integerPorts) throws IOException;
}
