/*
 * The MIT License
 *
 * Copyright 2020 Maxim Eltratov <Maxim.Eltratov@yandex.ru>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ru.maxeltr.rstpldr.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.maxeltr.rstpldr.Config.AppAnnotationConfig;
import ru.maxeltr.rstpldr.Config.Config;

public class RestUploadService {

    private static final Logger logger = Logger.getLogger(RestUploadService.class.getName());

    private final CryptService cryptService;

    private final Config config;

    private String accessToken = "";
    private String expiresIn = "";
    private String tokenType = "";
    private String scope = "";

    public RestUploadService(Config config, CryptService cryptService) {
        this.config = config;
        this.cryptService = cryptService;

    }

    public boolean uploadFile(String filename, byte[] file, String description) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + this.getToken());

        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename(filename)
                .build();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        HttpEntity<byte[]> fileEntity = new HttpEntity(file, fileMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap();
        body.add("file", fileEntity);
        body.add("description", description);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.exchange(
//                    this.config.getProperty("UrlUploadFile", ""),
                    new String(cryptService.decrypt(config.getProperty("UrlUploadFile", ""))),
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
        } catch (RestClientResponseException ex) {
            logger.log(Level.SEVERE, String.format("Cannot upload file: %s.%n", filename), ex);

            return false;
        }

        return true;
    }

    public String getToken() {
        return this.accessToken;
    }

    public String authenticate() {
        this.accessToken = "";
        this.expiresIn = "";
        this.tokenType = "";
        this.scope = "";

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("client_secret", new String(this.cryptService.decrypt(this.config.getProperty("ClientSecret", ""))));
        body.put("client_id", new String(this.cryptService.decrypt(this.config.getProperty("ClientId", ""))));

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(
//                    this.config.getProperty("UrlGetToken", ""),
                    new String(cryptService.decrypt(config.getProperty("UrlGetToken", ""))),
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
        } catch (HttpClientErrorException ex) {
            this.logger.log(Level.SEVERE, "Cannot authenticate. HTTP status code 4xx.", ex);

            return this.accessToken;
        } catch (HttpStatusCodeException ex) {
            this.logger.log(Level.SEVERE, "Cannot authenticate. HTTP status code 5xx", ex);

            return this.accessToken;
        } catch (UnknownHttpStatusCodeException ex) {
            this.logger.log(Level.SEVERE, "Cannot authenticate. Unknown HTTP status", ex);

            return this.accessToken;
        }

        Map responseBody = response.getBody();

        this.accessToken = responseBody.get("access_token").toString();
        this.expiresIn = responseBody.get("expires_in").toString();
        this.tokenType = responseBody.get("token_type").toString();
        this.scope = responseBody.get("scope").toString();

        return this.accessToken;
    }

}
