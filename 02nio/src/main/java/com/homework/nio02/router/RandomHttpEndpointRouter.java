package com.homework.nio02.router;

import java.util.List;
import java.util.Random;

public class RandomHttpEndpointRouter implements HttpEndpointRouter{

  @Override
  public String route(List<String> endpoints) {
    int size = endpoints.size();
    Random random = new Random(System.currentTimeMillis());
    return endpoints.get(random.nextInt(size));
  }
}
