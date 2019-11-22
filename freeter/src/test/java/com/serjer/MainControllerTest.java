package com.serjer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.serjer.freeter.controller.MainController;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-user-before.sql", "/messages-list-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/messages-list-after.sql", "/create-user-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@WithUserDetails(value="dru")
class MainControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	MainController controller;

	
	@Test
	public void mainPageTest() throws Exception {
		this.mockMvc.perform(get("/main"))
        .andDo(print())
        .andExpect(authenticated())
        .andExpect(xpath("//*[@id='navbarSupportedContent']/div").string("dru"));;
	}
	
	@Test
    public void messageListTest() throws Exception {
        this.mockMvc.perform(get("/main"))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(xpath("//*[@id='message-list']/div").nodeCount(4));
	}
	
	@Test
    public void filterMessageTest() throws Exception {
        this.mockMvc.perform(get("/main").param("filter", "my-tag"))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(xpath("//*[@id='message-list']/div").nodeCount(2))
                .andExpect(xpath("//*[@id='message-list']/div[@data-id='1']").exists())
                .andExpect(xpath("//*[@id='message-list']/div[@data-id='3']").exists());
    }
	
	 @Test
	    public void addMessageToListTest() throws Exception {
	        MockHttpServletRequestBuilder multipart = MockMvcRequestBuilders.multipart("/main")
	                .file("file", "123".getBytes())
	                .param("text", "fifth")
	                .param("tag", "new one")
	                .with(csrf());

	        this.mockMvc.perform(multipart)
	                .andDo(print())
	                .andExpect(authenticated())
	                .andExpect(xpath("//*[@id='message-list']/div").nodeCount(5))
	                .andExpect(xpath("//*[@id='message-list']/div[@data-id='10']").exists())
	                .andExpect(xpath("//*[@id='message-list']/div[@data-id='10']/div/span").string("fifth"))
	                .andExpect(xpath("//*[@id='message-list']/div[@data-id='10']/div/i").string("#new one"));
	    }
}
