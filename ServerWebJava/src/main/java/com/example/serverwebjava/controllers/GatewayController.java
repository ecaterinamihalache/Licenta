package com.example.serverwebjava.controllers;

import com.example.serverwebjava.interfaces.ServerWebServiceInterface;
import com.example.serverwebjava.models.*;
import com.example.serverwebjava.services.ServerWebService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;

@CrossOrigin
@RestController
public class GatewayController {
    private DatagramSocket datagramSocket=null;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServerWebServiceInterface serverWebServiceInterface = new ServerWebService();

    private final HashMap<Integer, TokenAndClaims> sessionsMap = new HashMap<>();

    //Accounts
    @GetMapping(path = "accounts")
    public ResponseEntity<Account[]> getAccounts(@RequestHeader(required = false) String authorization) {
        if(isValidSession(authorization))
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        String url = "http://localhost:8080/uac/uac/accounts";

        try {
            return restTemplate.getForEntity(url, Account[].class);
        } catch (HttpClientErrorException.NotFound e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (HttpClientErrorException.BadRequest e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (HttpClientErrorException.Unauthorized e) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping(path = "accounts")
    public ResponseEntity<Account> addAccount(@RequestBody Account account) {
        String url = "http://localhost:8080/uac/uac/accounts";

        try {
            return restTemplate.postForEntity(url, account, Account.class);
        } catch (HttpClientErrorException.NotFound e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (HttpClientErrorException.BadRequest e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (HttpClientErrorException.Unauthorized e) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    //functie de verificare daca sesiunea primita din front este in harta mea de seiuni
    //daca da face requestul daca nu -> nu este autentificat
    private boolean isValidSession(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer"))
            return true;

        String token = authorization.substring(7);
        Collection<TokenAndClaims> tokenAndClaims = sessionsMap.values();

        try {
            for(TokenAndClaims obj : tokenAndClaims) {
                if (Objects.equals(obj.token, token)) {
                    return false;
                }
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return true;
        }
        return true;
    }

    //UserAccessControl -- autentificarea
    @PostMapping("/authenticate")
    public ResponseEntity<TokenAndClaims> authenticateUser(@RequestBody Credentials credentials) {
        String url = "http://localhost:8080/uac/authenticate";

        try {
            //return restTemplate.postForEntity(url, credentials, TokenAndClaims.class);
            //imi intoarce un obiect ce contine token-ul, id-ul userului si rolul sau
            TokenAndClaims tokenAndClaims = restTemplate.postForObject(url, credentials, TokenAndClaims.class);

            //il salvez in hashMap-ul meu si dupa il trimit catre front
            if(tokenAndClaims != null){
                System.out.println("Inainte de a adauga o alta sesiune!" + sessionsMap);
                if(!sessionsMap.containsKey(tokenAndClaims.accountId)){
                    sessionsMap.put(tokenAndClaims.accountId, tokenAndClaims);
                }
                System.out.println("Dupa adaugarea unei alte sesiuni!" + sessionsMap);
            }
            return new ResponseEntity<>(tokenAndClaims, HttpStatus.OK);
        } catch (HttpClientErrorException.NotFound e) {
           return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (HttpClientErrorException.BadRequest | HttpServerErrorException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (HttpClientErrorException.Unauthorized e) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    //ManagerCentral
    @PostMapping(path = "writenodefree")
    public void uploadFile(@RequestParam MultipartFile file, @RequestParam Integer idUser, @RequestHeader(required = false) String authorization) throws IOException {
        if (authorization == null || !authorization.startsWith("Bearer"))
            return;

        String url = "http://localhost:8081/manager";

        //cere lista de noduri de la manager
        WebClient client = WebClient.create(url);
        String listNodes = client.get().uri("/files").retrieve().bodyToMono(String.class).block();

        //formatez stringul cu lista de porturi ale nodurilor intr-o lista de integeri
        List<Integer> integerList = serverWebServiceInterface.convertStringToListOfIntegers(listNodes);
        System.out.println("aici in server web" + integerList);

        //creaza socket-ul si trimite fisierul primit la noduri
        try {
            int port = 777;
            datagramSocket = new DatagramSocket(port);
            System.out.println("Nodul se executa pe portul: " + datagramSocket.getLocalPort() + "si la adresa: " + datagramSocket.getLocalAddress());

            InetAddress address = InetAddress.getByName("localhost");

            String gsonString = serverWebServiceInterface.convertFileForSend(file, idUser, integerList);//it fisierul cu lista porturilor la care sa mai trimita
            DatagramPacket packetSend = new DatagramPacket(gsonString.getBytes(), gsonString.length(), address, integerList.get(0));
            System.out.println("Trimit mesajul: " + packetSend);
            datagramSocket.send(packetSend);
        }
        finally {
            datagramSocket.close();
        }
    }

    @PostMapping(path = "downloadFile")
    public ResponseEntity<File> downloadFile(@RequestBody DownloadFile downloadFile, @RequestHeader(required = false) String authorization) throws IOException {
        if(isValidSession(authorization))
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        System.out.println("Id-ul userului este: " + downloadFile.getId());
        System.out.println("Numele fisierului de descarcat este: " + downloadFile.getFileName());

        String url = "http://localhost:8081/manager/downloadFile/" + downloadFile.getId() + "/" + downloadFile.getFileName();

        //cere nodul de la manager
        WebClient client = WebClient.create(url);
        int port = client.get().uri(url).retrieve().bodyToMono(Integer.class).block();
        System.out.println("aici in server web " + port);
        byte[] buf;
        DataSend data;
        File fisier;
        File fileNew;

        //creaza socket-ul si trimite cererea de descarcare catre nod si tin deschis pana primesc mesajul
        try {
            int portSocket = 777;
            InetAddress address = InetAddress.getByName("localhost");
            datagramSocket = new DatagramSocket(portSocket);
            System.out.println("Socketul este deschis pe portul: " + datagramSocket.getLocalPort() + "si la adresa: " + datagramSocket.getInetAddress());

            System.out.println("aici in socket web " + port);
            //creaza mesajul de trimitere la node
            //tot codul asta intr-o functie
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(downloadFile.getFileName());
            List<Integer> list = new ArrayList<>();

            //trimit numele fisierului catre nod impreuna cu id-ul sau ca sa imi intoarca intreg fisierul pentru descarcat
            //lista de porturi din parametrii obiectului este goala pentru ca are treaba doar cu nodul la care ii va trimite mesaj
            DataSend dataSend = new DataSend(downloadFile.getFileName(), downloadFile.getFileName().getBytes(), 4, downloadFile.getId(), list);
            String gsonString = objectMapper.writeValueAsString(dataSend);
            DatagramPacket packetSend = new DatagramPacket(gsonString.getBytes(), gsonString.length(), address, port);
            System.out.println("Trimit mesajul: " + packetSend);
            datagramSocket.send(packetSend);

            try{
                buf=new byte[65535];
                DatagramPacket packet=new DatagramPacket(buf, buf.length);
                datagramSocket.receive(packet);

                //se afiseaza continutul pachetului
                String s = new String(packet.getData(), packet.getOffset(), packet.getLength());
                System.out.println("Mesajul primit este: " + s);

                data = objectMapper.readValue(s, DataSend.class);
                System.out.println(data.fileName);
                //fisier = objectMapper.readValue(s, File.class);
                //System.out.println(fisier.getName());
                //fileNew = data.file;
            }catch (EOFException e){
                return new ResponseEntity(null, HttpStatus.NOT_FOUND);
            }
        }
        finally {
            datagramSocket.close();
        }
        return new ResponseEntity(data.fileContent, HttpStatus.OK);
    }

    @GetMapping(path = "usersFiles/{id}")
    public ResponseEntity<String[]> getUserFiles(@PathVariable Integer id, @RequestHeader(required = false) String authorization) {
        if(isValidSession(authorization))
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        String url = "http://localhost:8081/manager/userFiles/" + id.toString();

        try {
            return restTemplate.getForEntity(url, String[].class);
        } catch (HttpClientErrorException.NotFound e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (HttpClientErrorException.BadRequest e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (HttpClientErrorException.Unauthorized e) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(path = "users")
    public ResponseEntity<User[]> getUsers(@RequestHeader(required = false) String authorization) {
        if(isValidSession(authorization))
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        String url = "http://localhost:8081/manager/users";

        try {
            return restTemplate.getForEntity(url, User[].class);
        } catch (HttpClientErrorException.NotFound e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (HttpClientErrorException.BadRequest e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (HttpClientErrorException.Unauthorized e) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping(path = "users")
    public ResponseEntity<User> addUser(@RequestBody User user, @RequestHeader(required = false) String authorization) {
        String url = "http://localhost:8081/manager/users";

        try {
            return restTemplate.postForEntity(url, user, User.class);
        } catch (HttpClientErrorException.NotFound e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (HttpClientErrorException.BadRequest e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (HttpClientErrorException.Unauthorized e) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(path = "hashMapFiles")
    public ResponseEntity<FilesMap[]> getFilesMap(@RequestHeader(required = false) String authorization) {
        if(isValidSession(authorization))
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        String url = "http://localhost:8081/manager/hashMapFiles";

        try {
            return restTemplate.getForEntity(url, FilesMap[].class);
        } catch (HttpClientErrorException.NotFound e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (HttpClientErrorException.BadRequest e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (HttpClientErrorException.Unauthorized e) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }
}
