package ru.evolenta.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.evolenta.order.config.SecurityConfiguration;
import ru.evolenta.order.controller.OrderController;
import ru.evolenta.order.dto.*;
import ru.evolenta.order.model.Order;
import ru.evolenta.order.model.OrderItem;
import ru.evolenta.order.model.Status;
import ru.evolenta.order.repository.OrderRepository;
import ru.evolenta.order.service.JwtService;
import ru.evolenta.order.service.OrderService;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfiguration.class)
class OrderControllerTests {

	private final String jwtSecret = "TGpsK0tTaG5JWHcvZldrdGRtd3RjbXN6ZldsVVMzZDU";

	private final long jwtExpirationTime = 3600000L;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private OrderService orderService;

	@MockBean
	private JwtService jwtService;

	@MockBean
	private OrderRepository orderRepository;

	@Test
	@WithMockUser
	void testCreateOrderSuccess() throws Exception {
		Order order = new Order();
		order.setId(1);
		order.setUserId(1L);
		order.setCreationDate(LocalDateTime.now());
		order.setStatus(Status.NEW);
		order.setOrderItems(getOrderItems(order));

		CreateOrderDTO orderDTO = new CreateOrderDTO();
		orderDTO.setOrderItems(getOrderItems(null));

		String authHeader = "Bearer " + generateToken(1L, "user", "ROLE_USER");
		Mockito.when(this.orderService.createOrder(orderDTO, authHeader))
				.thenReturn(order);

		mockMvc.perform(post("/orders")
						.with(csrf())
						.header("Authorization", authHeader)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(orderDTO)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(order.getId()))
				.andExpect(jsonPath("$.userId").value(order.getUserId()))
				.andExpect(jsonPath("$.creationDate").value(order.getCreationDate().format(DateTimeFormatter.ISO_DATE_TIME)))
				.andExpect(jsonPath("$.status").value(order.getStatus().toString()))
				.andExpect(jsonPath("$.orderItems.size()").value(order.getOrderItems().size()));
	}

	@Test
	@WithAnonymousUser
	void testCreateOrderForbidden() throws Exception {
		Order order = new Order();
		order.setOrderItems(getOrderItems(order));

		CreateOrderDTO orderDTO = new CreateOrderDTO();
		orderDTO.setOrderItems(order.getOrderItems());
		mockMvc.perform(post("/orders")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(orderDTO)))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testGetAllOrdersSuccess() throws Exception {
		OrderDTO orderDTO1 = new OrderDTO();
		orderDTO1.setId(1);
		orderDTO1.setUserId(1L);
		orderDTO1.setTotalPrice(15000.99);
		orderDTO1.setStatus(Status.NEW);
		orderDTO1.setOrderItems(getOrderItemsDTO());

		OrderDTO orderDTO2 = new OrderDTO();
		orderDTO2.setId(2);
		orderDTO2.setUserId(1L);
		orderDTO2.setTotalPrice(15000.99);
		orderDTO2.setStatus(Status.NEW);
		orderDTO2.setOrderItems(getOrderItemsDTO());

		List<OrderDTO> orderDTOList = List.of(orderDTO1, orderDTO2);
		String authHeader = "Bearer " + generateToken(1L, "user", "ROLE_USER");
		Mockito.when(this.orderService.getAllOrders(authHeader))
				.thenReturn(orderDTOList);

		mockMvc.perform(get("/orders")
						.header("Authorization", authHeader))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(orderDTOList.size()))
				.andExpect(jsonPath("$[0].id").value(orderDTOList.get(0).getId()))
				.andExpect(jsonPath("$[1].id").value(orderDTOList.get(1).getId()));
	}

	@Test
	@WithMockUser
	void testGetAllOrdersForbidden() throws Exception {
		String authHeader = "Bearer " + generateToken(1L, "user", "ROLE_USER");
		mockMvc.perform(get("/orders")
						.header("Authorization", authHeader))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testGetAllOrdersByUserIdSuccess() throws Exception {
		OrderDTO orderDTO1 = new OrderDTO();
		orderDTO1.setId(1);
		orderDTO1.setUserId(1L);
		orderDTO1.setTotalPrice(15000.99);
		orderDTO1.setStatus(Status.NEW);
		orderDTO1.setOrderItems(getOrderItemsDTO());

		OrderDTO orderDTO2 = new OrderDTO();
		orderDTO2.setId(2);
		orderDTO2.setUserId(1L);
		orderDTO2.setTotalPrice(15000.99);
		orderDTO2.setStatus(Status.NEW);
		orderDTO2.setOrderItems(getOrderItemsDTO());

		List<OrderDTO> orderDTOList = List.of(orderDTO1, orderDTO2);
		Long targetUserId = 1L;
		String authHeader = "Bearer " + generateToken(1L, "user", "ROLE_USER");
		Mockito.when(this.orderService.getAllOrdersByUserId(targetUserId, authHeader))
				.thenReturn(orderDTOList);

		mockMvc.perform(get("/orders/user/" + targetUserId)
						.header("Authorization", authHeader))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(orderDTOList.size()))
				.andExpect(jsonPath("$[0].id").value(orderDTOList.get(0).getId()))
				.andExpect(jsonPath("$[0].userId").value(targetUserId))
				.andExpect(jsonPath("$[1].id").value(orderDTOList.get(1).getId()))
				.andExpect(jsonPath("$[1].userId").value(targetUserId));
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testGetAllOrdersByUserIdNotFound() throws Exception {
		List<OrderDTO> orderDTOList = new ArrayList<>();
		long targetUserId = 5L;
		String authHeader = "Bearer " + generateToken(1L, "user", "ROLE_USER");
		Mockito.when(this.orderService.getAllOrdersByUserId(targetUserId, authHeader))
				.thenReturn(orderDTOList);

		mockMvc.perform(get("/orders/user/" + targetUserId)
						.header("Authorization", authHeader))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(orderDTOList.size()));
	}

	@Test
	@WithAnonymousUser
	void testGetAllOrdersByUserIdForbidden() throws Exception {
		long targetUserId = 1L;
		mockMvc.perform(get("/orders/" + targetUserId))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser
	void testGetAllOrdersMeSuccess() throws Exception {
		OrderDTO orderDTO1 = new OrderDTO();
		orderDTO1.setId(1);
		orderDTO1.setUserId(1L);
		orderDTO1.setTotalPrice(15000.99);
		orderDTO1.setStatus(Status.NEW);
		orderDTO1.setOrderItems(getOrderItemsDTO());

		OrderDTO orderDTO2 = new OrderDTO();
		orderDTO2.setId(2);
		orderDTO2.setUserId(1L);
		orderDTO2.setTotalPrice(15000.99);
		orderDTO2.setStatus(Status.NEW);
		orderDTO2.setOrderItems(getOrderItemsDTO());

		List<OrderDTO> orderDTOList = List.of(orderDTO1, orderDTO2);
		long targetUserId = 1L;
		String authHeader = "Bearer " + generateToken(1L, "user", "ROLE_USER");
		Mockito.when(this.orderService.getAllOrdersCurrentUser(authHeader))
				.thenReturn(orderDTOList);

		mockMvc.perform(get("/orders/me")
						.header("Authorization", authHeader))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(orderDTOList.size()))
				.andExpect(jsonPath("$[0].id").value(orderDTOList.get(0).getId()))
				.andExpect(jsonPath("$[0].userId").value(targetUserId))
				.andExpect(jsonPath("$[1].id").value(orderDTOList.get(1).getId()))
				.andExpect(jsonPath("$[1].userId").value(targetUserId));
	}

	@Test
	@WithAnonymousUser
	void testGetAllOrdersMeForbidden() throws Exception {
		mockMvc.perform(get("/orders/me"))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser
	void testUpdateOrderSuccess() throws Exception {
		Order order = new Order();
		order.setId(1);
		order.setUserId(1L);
		order.setCreationDate(LocalDateTime.now());
		order.setStatus(Status.NEW);
		order.setOrderItems(getOrderItems(order));

		UpdateOrderDTO updateOrderDTO = new UpdateOrderDTO();
		updateOrderDTO.setOrderItems(getOrderItems(null));

		int targetOrderId = 1;
		String authHeader = "Bearer " + generateToken(1L, "user", "ROLE_USER");
		Mockito.when(this.orderService.updateOrder(targetOrderId, updateOrderDTO, authHeader))
				.thenReturn(order);

		mockMvc.perform(put("/orders/" + targetOrderId)
						.with(csrf())
						.header("Authorization", authHeader)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsBytes(updateOrderDTO)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(order.getId()))
				.andExpect(jsonPath("$.userId").value(order.getUserId()))
				.andExpect(jsonPath("$.creationDate").value(order.getCreationDate().format(DateTimeFormatter.ISO_DATE_TIME)))
				.andExpect(jsonPath("$.status").value(order.getStatus().toString()))
				.andExpect(jsonPath("$.orderItems.size()").value(order.getOrderItems().size()));
	}

	@Test
	@WithAnonymousUser
	void testUpdateOrderForbidden() throws Exception {
		Order order = new Order();
		order.setId(1);
		order.setUserId(1L);
		order.setCreationDate(LocalDateTime.now());
		order.setStatus(Status.NEW);
		order.setOrderItems(getOrderItems(order));

		UpdateOrderDTO updateOrderDTO = new UpdateOrderDTO();
		updateOrderDTO.setOrderItems(getOrderItems(order));

		int targetOrderId = 1;
		mockMvc.perform(put("/orders/" + targetOrderId)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsBytes(updateOrderDTO)))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testUpdateStatusOrderSuccess() throws Exception {
		Order order = new Order();
		order.setId(1);
		order.setUserId(1L);
		order.setCreationDate(LocalDateTime.now());
		order.setOrderItems(getOrderItems(order));

		UpdateOrderStatusDTO orderDTO = new UpdateOrderStatusDTO();
		orderDTO.setStatus(Status.IN_PROCESSING);
		order.setStatus(orderDTO.getStatus());

		int targetOrderId = 1;
		String authHeader = "Bearer " + generateToken(1L, "user", "ROLE_USER");
		Mockito.when(this.orderService.updateStatusOrder(targetOrderId, orderDTO))
				.thenReturn(order);

		mockMvc.perform(put("/orders/" + targetOrderId + "/status")
						.with(csrf())
						.header("Authorization", authHeader)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsBytes(orderDTO)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(order.getId()))
				.andExpect(jsonPath("$.userId").value(order.getUserId()))
				.andExpect(jsonPath("$.creationDate").value(order.getCreationDate().format(DateTimeFormatter.ISO_DATE_TIME)))
				.andExpect(jsonPath("$.status").value(order.getStatus().toString()))
				.andExpect(jsonPath("$.orderItems.size()").value(order.getOrderItems().size()));
	}

	@Test
	@WithMockUser
	void testUpdateStatusOrderForbidden() throws Exception {
		Order order = new Order();
		order.setId(1);
		order.setUserId(1L);
		order.setCreationDate(LocalDateTime.now());
		order.setOrderItems(getOrderItems(order));

		UpdateOrderStatusDTO orderDTO = new UpdateOrderStatusDTO();
		orderDTO.setStatus(Status.IN_PROCESSING);
		order.setStatus(orderDTO.getStatus());

		int targetOrderId = 1;
		String authHeader = "Bearer " + generateToken(1L, "user", "ROLE_USER");
		mockMvc.perform(put("/orders/" + targetOrderId + "/status")
						.with(csrf())
						.header("Authorization", authHeader)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsBytes(orderDTO)))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testDeleteOrderSuccess() throws Exception {
		int targetOrderId = 1;
		String authHeader = "Bearer " + generateToken(1L, "user", "ROLE_USER");
		mockMvc.perform(delete("/orders/" + targetOrderId)
						.with(csrf())
						.header("Authorization", authHeader))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	void testDeleteOrderForbidden() throws Exception {
		int targetOrderId = 1;
		String authHeader = "Bearer " + generateToken(1L, "user", "ROLE_USER");
		mockMvc.perform(delete("/orders/" + targetOrderId)
						.with(csrf())
						.header("Authorization", authHeader))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	private List<OrderItemDTO> getOrderItemsDTO() {
		List<OrderItemDTO> orderItems = new ArrayList<>();
		orderItems.add(new OrderItemDTO(1, "Title 1", 100, 550.99, 100*550.99));
		orderItems.add(new OrderItemDTO(2, "Title 2", 200, 650.99, 200*650.99));
		orderItems.add(new OrderItemDTO(3, "Title 3", 300, 750.99, 300*750.99));
		return orderItems;
	}

	private List<OrderItem> getOrderItems(Order order) {
		List<OrderItem> orderItems = new ArrayList<>();
		for(int i = 1; i <= 3; i++) {
			OrderItem orderItem = new OrderItem();
			orderItem.setBookId(i);
			orderItem.setOrder(order);
			orderItem.setQuantity(i*100);
			orderItems.add(orderItem);
		}
		return orderItems;
	}

	private String generateToken(long userId, String username, String role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("id", userId);
		claims.put("username", username);
		claims.put("role", role);
		return createToken(claims, username);
	}

	private String createToken(Map<String, Object> extraClaims, String username) {
		return Jwts
				.builder()
				.setClaims(extraClaims)
				.setSubject(username)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationTime))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	private Key getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}

}
