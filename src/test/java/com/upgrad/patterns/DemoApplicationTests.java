package com.upgrad.patterns;

import com.upgrad.patterns.Authentication.Authenticator;
import com.upgrad.patterns.Authentication.BasicAuthProvider;
import com.upgrad.patterns.Authentication.JwtAuthProvider;
import com.upgrad.patterns.Config.RestServiceGenerator;
import com.upgrad.patterns.Constants.SourceType;
import com.upgrad.patterns.Interfaces.IndianDiseaseStat;
import com.upgrad.patterns.Middleware.AuthenticationProcessor;
import com.upgrad.patterns.Service.DiseaseCountFacade;
import com.upgrad.patterns.Service.IndiaDiseaseStatFactory;
import com.upgrad.patterns.Strategies.DiseaseShStrategy;
import com.upgrad.patterns.Strategies.JohnHopkinsStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private DiseaseCountFacade diseaseCountFacade;

	@Autowired
	private IndiaDiseaseStatFactory indiaDiseaseStatFactory;

	@Autowired
	private DiseaseShStrategy diseaseShStrategy;

	@Autowired
	private JohnHopkinsStrategy johnHopkinsStrategy;

	@Test
	void contextLoads() {
		assertNotNull(diseaseCountFacade);
		assertNotNull(indiaDiseaseStatFactory);
		assertNotNull(diseaseShStrategy);
		assertNotNull(johnHopkinsStrategy);
	}

	@Test
	void testRestServiceGeneratorSingleton() {
		RestTemplate instance1 = RestServiceGenerator.GetInstance();
		RestTemplate instance2 = RestServiceGenerator.GetInstance();
		assertNotNull(instance1);
		assertSame(instance1, instance2, "RestServiceGenerator.GetInstance() must return the same singleton instance");
	}

	@Test
	void testAuthenticationChain() {
		AuthenticationProcessor processor = Authenticator.GetAuthProcessor();
		assertNotNull(processor);

		// Valid JWT
		assertTrue(processor.isAuthorized(new JwtAuthProvider("Bearer upgrad")));

		// Invalid JWT
		assertFalse(processor.isAuthorized(new JwtAuthProvider("Bearer invalid")));

		// Valid Basic Auth
		assertTrue(processor.isAuthorized(new BasicAuthProvider("India", "India")));

		// Invalid Basic Auth
		assertFalse(processor.isAuthorized(new BasicAuthProvider("India", "wrong")));
		assertFalse(processor.isAuthorized(new BasicAuthProvider("wrong", "India")));
		assertFalse(processor.isAuthorized(new BasicAuthProvider(null, null)));
	}

	@Test
	void testIndiaDiseaseStatFactory() {
		IndianDiseaseStat shStrategy = indiaDiseaseStatFactory.GetInstance(SourceType.DiseaseSh);
		assertTrue(shStrategy instanceof DiseaseShStrategy);

		IndianDiseaseStat jhStrategy = indiaDiseaseStatFactory.GetInstance(SourceType.JohnHopkins);
		assertTrue(jhStrategy instanceof JohnHopkinsStrategy);

		assertThrows(IllegalArgumentException.class, () -> indiaDiseaseStatFactory.GetInstance(null));
	}

	@Test
	void testDiseaseShStrategyCount() {
		String activeCount = diseaseShStrategy.GetActiveCount();
		assertNotNull(activeCount);
		assertEquals("44997780", activeCount);
	}

	@Test
	void testJohnHopkinsStrategyCount() {
		String activeCount = johnHopkinsStrategy.GetActiveCount();
		assertNotNull(activeCount);
		assertEquals("10001", activeCount);
	}

	@Test
	void testDiseaseCountFacade() {
		assertEquals("44997780", diseaseCountFacade.getDiseaseShCount());
		assertEquals("10001", diseaseCountFacade.getJohnHopkinCount());

		Object diseaseShRatio = diseaseCountFacade.getInfectedRatio("DiseaseSh");
		assertNotNull(diseaseShRatio);

		Object jhRatio = diseaseCountFacade.getInfectedRatio("JohnHopkins");
		assertNotNull(jhRatio);

		assertThrows(IllegalArgumentException.class, () -> diseaseCountFacade.getInfectedRatio("InvalidSource"));
	}

	@Test
	void testEndpointsWithAuth() throws Exception {
		// Unauthorized without headers
		mockMvc.perform(get("/v1/disease/disease-sh-io/count"))
				.andExpect(status().isUnauthorized());

		// Authorized with JWT
		mockMvc.perform(get("/v1/disease/disease-sh-io/count")
				.header("Authorization", "Bearer upgrad"))
				.andExpect(status().isOk())
				.andExpect(content().string("44997780"));

		// Authorized with Basic Auth
		mockMvc.perform(get("/v1/disease/john-hopkins/count")
				.header("Username", "India")
				.header("Password", "India"))
				.andExpect(status().isOk())
				.andExpect(content().string("10001"));

		// Infected ratio valid
		mockMvc.perform(get("/v1/disease/infected-ratio")
				.param("sourceType", "DiseaseSh")
				.header("Authorization", "Bearer upgrad"))
				.andExpect(status().isOk());

		// Infected ratio invalid source -> 502 Bad Gateway
		mockMvc.perform(get("/v1/disease/infected-ratio")
				.param("sourceType", "Unknown")
				.header("Authorization", "Bearer upgrad"))
				.andExpect(status().isBadGateway());
	}
}
