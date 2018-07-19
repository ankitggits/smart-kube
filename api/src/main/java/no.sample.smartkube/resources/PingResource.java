package no.sample.smartkube.resources;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import no.sample.smartkube.common.web.model.Representation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class PingResource {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    private final RestTemplate restTemplate;

    @Value("${server.port}")
    private int port;

    public PingResource() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping(path = "/ping", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> ping(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:"+port+"/pong");
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                Representation.class);
    }

    @GetMapping(path = "/pong", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Representation<Greeting>> pong(){
        return ResponseEntity
                .ok(Representation.of(new Greeting(counter.incrementAndGet(), String.format(template, "from ping-pong!! Good night AGAIN AGAIN y not!!")))
                );
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class Greeting {
        private final long id;
        private final String content;
    }


}
