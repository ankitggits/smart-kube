package no.sample.smartkube.resources;

import no.sample.smartkube.common.web.model.Representation;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

public class PingResourceTest {

    private PingResource pingResource = new PingResource();

    @Test
    public void pong() {
        ResponseEntity<Representation<PingResource.Greeting>> pong = pingResource.pong();
        Assert.assertNotNull(pong.getBody().getPayload().getContent());
    }
}