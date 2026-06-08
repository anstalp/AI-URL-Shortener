package com.anastasis.minilink.service;

import com.anastasis.minilink.AiService;
import com.anastasis.minilink.entity.UrlMapping;
import com.anastasis.minilink.entity.UrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.List;

@Service
public class UrlService {

    private static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();
    private final UrlRepository urlRepository;
    private final AiService aiService;

    public UrlService(UrlRepository urlRepository, AiService aiService) {
        this.urlRepository = urlRepository;
        this.aiService = aiService;
    }

    public String generateShortCode(){

        String finalCode;

        do {
            StringBuilder shortCodeBuilder = new StringBuilder();

            for (int i = 0; i < 6; i++) {
                int val = random.nextInt(ALLOWED_CHARACTERS.length());
                shortCodeBuilder.append(ALLOWED_CHARACTERS.charAt(val));
            }
            finalCode = shortCodeBuilder.toString();

        } while (urlRepository.findByShortCode(finalCode).isPresent());

        return finalCode;
    }


    public UrlMapping shortenUrl(String originalUrl, String customAlias){

        //if (!StringUtils.hasText(customAlias)){
            UrlMapping url = new UrlMapping();
            url.setOriginalUrl(originalUrl);

            try {
                // ΣΧΕΔΙΟ Α: Ζητάμε λέξη από το AI
                url.setShortCode(aiService.generateAlias(originalUrl));
            } catch (Exception e) {
                // ΣΧΕΔΙΟ Β: Το AI απέτυχε, βάζουμε τυχαία γράμματα
                url.setShortCode(generateShortCode());
            }

            return urlRepository.save(url);

//        }
//      else { //περριτο αυτο(ο χρηστης δεν δινει ποτε customAlias)
//            if (urlRepository.findByShortCode(customAlias).isPresent()){
//                throw new RuntimeException("Alias already exist: " + customAlias);
//            }
//
//            UrlMapping url = new UrlMapping();
//            url.setOriginalUrl(originalUrl);
//            url.setShortCode(customAlias);
//
//            return urlRepository.save(url);
//        }
    }

    public UrlMapping getLink(String shortCode){
       UrlMapping mapping = urlRepository.findByShortCode(shortCode).orElseThrow(
                () -> new RuntimeException("No such shortCode: " + shortCode)
       );

       mapping.setClickCount(mapping.getClickCount() + 1);
       urlRepository.save(mapping);

       return mapping;
    }

    public List<UrlMapping> getAllLinks(){
        return urlRepository.findAll();
    }


}
