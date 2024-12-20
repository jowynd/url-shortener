package com.xyz.urlshortener.controllers;

import com.xyz.urlshortener.controllers.dto.UrlRequestDto;
import com.xyz.urlshortener.controllers.dto.UrlResponseDTO;
import com.xyz.urlshortener.entities.Url;
import com.xyz.urlshortener.repositories.UrlRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
public class UrlController {

    @Autowired
    private UrlRepository urlRepository;

    //O encurtamento das urls é feito aqui
    @PostMapping(value = "/shorten-url")
    public ResponseEntity<UrlResponseDTO> shortenUrl(@RequestBody UrlRequestDto urlRequest, HttpServletRequest httpRequest) {

        //cria um id aleatório caso ele não exista no banco de dados
        String id;
        do {
            id = RandomStringUtils.randomAlphanumeric(5);
        } while(urlRepository.existsById(id));

        urlRepository.save(new Url(id, urlRequest.url(), LocalDateTime.now().plusMinutes(3)));

        //Substitui o nome original da rota pelo id gerado aleatoriamente
        var redirectUrl = httpRequest.getRequestURL().toString().replace("shorten-url", id);

        return ResponseEntity.ok(new UrlResponseDTO(redirectUrl));
    }

    //Aqui é feito o redirecionamento das urls
    @GetMapping("{id}")
    public ResponseEntity redirect(@PathVariable("id") String id) {

        var url = urlRepository.findById(id);

        //Caso a url não exista ou esteja expirada, retorna um erro 404
        if (url.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        //Redireciona para a url original
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url.get().getFullUrl()));

        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    //Deleta urls expiradas
    @Scheduled(fixedRate = 3000)
    public void cleanupExpiredUrls() {
        LocalDateTime now = LocalDateTime.now();
        urlRepository.deleteExpiredUrls(now);
    }
}
