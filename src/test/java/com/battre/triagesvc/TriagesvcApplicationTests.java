package com.battre.triagesvc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "grpc.server.port=9000")
class TriagesvcApplicationTests {
  @Test
  void contextLoads() {}
}
