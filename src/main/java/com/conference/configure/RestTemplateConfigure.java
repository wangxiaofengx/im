package com.conference.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestTemplateConfigure {

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }

//    @Bean
//    public RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
//        SSLContextBuilder builder = new SSLContextBuilder();
//        builder.loadTrustMaterial(null, (certificate, authType) -> true);
//        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
//                builder.build(), NoopHostnameVerifier.INSTANCE);
//        HttpClient httpClient = HttpClients.custom()
//                .setSSLSocketFactory(sslConnectionSocketFactory)
//                .build();
//        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
//    }
}
