package com.example.testingweb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.testingweb.webdriver.IndexPage;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class WebDriverTests {

    @Autowired
    MockMvc mockMvc;

    public static final String AMOUNT = "amount";
    @Test
    @WithMockUser
    void validateInputName() throws Exception {
        this.mockMvc.perform(get("/"))
            .andExpect(content().string(containsString(AMOUNT)));
    }

    @Test
    @WithMockUser
    void validateTransfer() throws Exception {
        MockHttpServletRequestBuilder request = post("/transfer")
                                                    .param(AMOUNT, "1")
                                                    .with(csrf());
        this.mockMvc.perform(request)
            .andExpect(status().is3xxRedirection());
    }

    @Autowired
    private WebDriver driver;

    @Test
    @DirtiesContext
    public void transfer() {
        IndexPage index = IndexPage.to(this.driver, IndexPage.class);
        index.assertAt();

        assertThat(index.balance()).isEqualTo(100);

        index = index.transfer(50);

        assertThat(index.balance()).isEqualTo(50);
    }
}
