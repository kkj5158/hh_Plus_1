package io.hhplus.tdd.point;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.point.controller.PointController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PointController.class)
public class PointControllerValidationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void id의값은_1이상의양수() throws Exception{
    mockMvc.perform(get("/point/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Id값은 1이상의 값이여야합니다."));

  }

  @Test
  void amount의값은_1이상의양수() throws Exception{
    mockMvc.perform(get("/point/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Id값은 1이상의 값이여야합니다."));

  }




}
