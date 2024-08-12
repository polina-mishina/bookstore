package ru.evolenta.order.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.evolenta.order.dto.*;
import ru.evolenta.order.model.Order;
import ru.evolenta.order.model.OrderItem;
import ru.evolenta.order.model.Status;
import ru.evolenta.order.repository.OrderItemRepository;
import ru.evolenta.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.key}")
    private String apiKey;

    @Value("${url.book-service}")
    private String BOOK_SERVICE_URL;

    public Iterable<OrderDTO> getAllOrders(String authHeader) {
        HttpHeaders headers = createAuthHeader(authHeader);
        List<Order> orders = (List<Order>) orderRepository.findAll();
        return orders.stream().map((order) -> convertToOrderDTO(order, headers)).collect(Collectors.toList());
    }

    public Iterable<OrderDTO> getAllOrdersByUserId(long id, String authHeader) {
        HttpHeaders headers = createAuthHeader(authHeader);
        List<Order> orders = (List<Order>) orderRepository.findAllByUserId(id);
        return orders.stream().map((order) -> convertToOrderDTO(order, headers)).collect(Collectors.toList());
    }

    public Iterable<OrderDTO> getAllOrdersCurrentUser(String authHeader) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return getAllOrdersByUserId(userId, authHeader);
    }

    public Order createOrder(CreateOrderDTO createOrderDTO, String authHeader) {
        // Создание заголовка авторизации
        HttpHeaders headers = createAuthHeader(authHeader);

        // Проверка наличия книг и запись их в карту
        Map<Integer, BookDTO> books = new HashMap<>();
        for (OrderItem orderItem : createOrderDTO.getOrderItems()) {
            BookDTO bookDTO = getBookDTO(headers, orderItem.getBookId());
            if (bookDTO == null) {
                throw new RuntimeException("Book does not exist");
            }
            if (bookDTO.getQuantity() < orderItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for book: " + bookDTO.getTitle());
            }
            books.put(bookDTO.getId(), bookDTO);
        }

        // Установка id текущего пользователя, даты создания и начального статуса
        Order order = new Order();
        order.setUserId((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        order.setCreationDate(LocalDateTime.now());
        order.setStatus(Status.NEW);

        // Установка связи между заказом и элементами заказа
        for (OrderItem item : createOrderDTO.getOrderItems()) {
            item.setOrder(order);
        }
        order.setOrderItems(createOrderDTO.getOrderItems());

        // Сохранение заказа
        Order savedOrder = orderRepository.save(order);

        // Обновление количества книг в карте
        for (OrderItem orderItem : order.getOrderItems()) {
            BookDTO bookDTO = books.get(orderItem.getBookId());
            bookDTO.setQuantity(bookDTO.getQuantity() - orderItem.getQuantity());
            books.put(orderItem.getId(), bookDTO);
        }

        // Обновление книг
        updateBooksQuantity(headers, books.values().stream().toList());

        return savedOrder;
    }

    public Order updateOrder(int id, UpdateOrderDTO updateOrderDTO, String authHeader) {
        // Создание заголовка авторизации
        HttpHeaders headers = createAuthHeader(authHeader);

        // Поиск существующего заказа
        Order existingOrder = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

        // Проверка пользователя
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!existingOrder.getUserId().equals(userId)) {
            throw new RuntimeException("This is not your order");
        }

        // Проверка статуса
        if(existingOrder.getStatus() == Status.DONE) {
            throw new RuntimeException("Order is done");
        }

        // Проверка наличия книг и запись их в карту
        Map<Integer,BookDTO> books = new HashMap<>();
        for (OrderItem orderItem : existingOrder.getOrderItems()) {
            BookDTO bookDTO = getBookDTO(headers, orderItem.getBookId());
            if (bookDTO == null) {
                throw new RuntimeException("Book does not exist");
            }
            bookDTO.setQuantity(bookDTO.getQuantity() + orderItem.getQuantity());
            books.put(bookDTO.getId(), bookDTO);
        }

        // Проверка доступности книг для новых элементов заказа
        for (OrderItem orderItem : updateOrderDTO.getOrderItems()) {
            if (books.containsKey(orderItem.getBookId())) {
                if(books.get(orderItem.getBookId()).getQuantity() < orderItem.getQuantity()) {
                    throw new RuntimeException("Not enough stock for book: " + books.get(orderItem.getBookId()).getTitle());
                }
            }
            else {
                BookDTO bookDTO = getBookDTO(headers, orderItem.getBookId());
                if (bookDTO == null) {
                    throw new RuntimeException("Book does not exist");
                }
                if (bookDTO.getQuantity() < orderItem.getQuantity()) {
                    throw new RuntimeException("Not enough stock for book: " + bookDTO.getTitle());
                }
                books.put(orderItem.getBookId(), bookDTO);
            }
        }

        // Удаление старых элементов заказа
        orderItemRepository.deleteAll(existingOrder.getOrderItems());
        existingOrder.getOrderItems().clear();

        // Обновление количества книг в карте
        for (OrderItem orderItem : updateOrderDTO.getOrderItems()) {
            BookDTO bookDTO = books.get(orderItem.getBookId());
            bookDTO.setQuantity(bookDTO.getQuantity() - orderItem.getQuantity());
            books.put(orderItem.getBookId(), bookDTO);
            orderItem.setOrder(existingOrder);
            existingOrder.getOrderItems().add(orderItem);
        }

        // Обновление книг
        updateBooksQuantity(headers, books.values().stream().toList());

        // Сохранение обновленного заказа
        return orderRepository.save(existingOrder);
    }

    public Order updateStatusOrder(int id, UpdateOrderStatusDTO updateOrderStatusDTO) {
        Order existingOrder = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        existingOrder.setStatus(updateOrderStatusDTO.getStatus());
        return orderRepository.save(existingOrder);
    }

    public void deleteOrder(int id, String authHeader) {
        // Создание заголовка авторизации
        HttpHeaders headers = createAuthHeader(authHeader);

        // Поиск существующего заказа
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

        // Проверка статуса
        if(order.getStatus() == Status.DONE) {
            throw new RuntimeException("Order is done");
        }

        // Формирование списка книг с восстановленным количеством
        List<BookDTO> books = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            BookDTO book = getBookDTO(headers, orderItem.getBookId());
            book.setQuantity(book.getQuantity() + orderItem.getQuantity());
            books.add(book);
        }

        // Обновление книг
        updateBooksQuantity(headers, books);

        // Удаление заказа
        orderRepository.deleteById(id);
    }

    private HttpHeaders createAuthHeader(String authHeader) {
        String token = authHeader.substring(7);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    private OrderDTO convertToOrderDTO(Order order, HttpHeaders headers) {
        List<OrderItemDTO> orderItemsDTO = orderItemRepository.findAllByOrderId(order.getId()).stream().map(orderItem -> {
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<BookDTO> response = restTemplate.exchange(
                    BOOK_SERVICE_URL + "/" + orderItem.getBookId(),
                    HttpMethod.GET,
                    entity,
                    BookDTO.class
            );
            BookDTO book = response.getBody();
            if (book == null) {
                throw new RuntimeException("Book not found for ID: " + orderItem.getBookId());
            }

            BigDecimal price = BigDecimal.valueOf(book.getPrice());
            BigDecimal quantity = BigDecimal.valueOf(orderItem.getQuantity());
            BigDecimal totalPrice = price.multiply(quantity);
            return new OrderItemDTO(
                    orderItem.getBookId(),
                    book.getTitle(),
                    orderItem.getQuantity(),
                    book.getPrice(),
                    totalPrice.setScale(2, RoundingMode.HALF_UP).doubleValue()
            );
        }).collect(Collectors.toList());

        BigDecimal totalPrice = BigDecimal.valueOf(orderItemsDTO.stream().mapToDouble(OrderItemDTO::getTotalPrice).sum());

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setUserId(order.getUserId());
        orderDTO.setStatus(order.getStatus());
        orderDTO.setOrderItems(orderItemsDTO);
        orderDTO.setTotalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP).doubleValue());

        return orderDTO;
    }

    private BookDTO getBookDTO(HttpHeaders headers, Integer bookId) {
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<BookDTO> response = restTemplate.exchange(
                BOOK_SERVICE_URL + "/" + bookId,
                HttpMethod.GET,
                entity,
                BookDTO.class
        );
        return response.getBody();
    }

    private void updateBooksQuantity(HttpHeaders headers, List<BookDTO> books) {
        // Создание заголовков и добавление API Key
        headers.set("X-Internal-Api-Key", apiKey);
        HttpEntity<Collection<BookDTO>> requestEntity = new HttpEntity<>(books, headers);
        // Обновление книг в микросервисе Book
        restTemplate.exchange(
                BOOK_SERVICE_URL,
                HttpMethod.PUT,
                requestEntity,
                List.class
        );
    }
}
