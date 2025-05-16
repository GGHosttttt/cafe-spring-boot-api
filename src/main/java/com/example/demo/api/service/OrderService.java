package com.example.demo.api.service;
import com.example.demo.api.exception.EntityNotFoundException;

import com.example.demo.api.model.Order;
import com.example.demo.api.model.OrderDetail;
import com.example.demo.api.model.Product;
import com.example.demo.api.mapper.OrderDetailMapper;
import com.example.demo.api.mapper.OrderMapper;
import com.example.demo.api.repository.OrderDetailRepository;
import com.example.demo.api.repository.OrderRepository;
import com.example.demo.api.repository.ProductRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.api.dto.OrderDTO;
import com.example.demo.api.dto.OrderDetailDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;              
import org.slf4j.LoggerFactory;
@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final boolean DEFAULT_STATUS = true; // Default to paid (true)

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> {
                    OrderDTO dto = orderMapper.toDTO(order);
                    List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId());
                    dto.setOrderDetails(details != null && !details.isEmpty()
                            ? details.stream().map(orderDetailMapper::toDTO).collect(Collectors.toList())
                            : Collections.emptyList());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Optional<OrderDTO> getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    OrderDTO dto = orderMapper.toDTO(order);
                    List<OrderDetail> details = orderDetailRepository.findByOrderId(id);
                    dto.setOrderDetails(details != null && !details.isEmpty()
                            ? details.stream().map(orderDetailMapper::toDTO).collect(Collectors.toList())
                            : Collections.emptyList());
                    return dto;
                });
    }

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        logger.debug("Creating order with DTO: {}", orderDTO);
        // Validate order details
        if (orderDTO.getOrderDetails() == null || orderDTO.getOrderDetails().isEmpty()) {
            throw new IllegalArgumentException("Order details cannot be empty");
        }

        // Calculate total amount and populate order details
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (OrderDetailDTO detailDTO : orderDTO.getOrderDetails()) {
            logger.debug("Processing order detail: {}", detailDTO);
            Product product = productRepository.findById(detailDTO.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + detailDTO.getProductId()));
            if (!product.isAvailable()) {
                throw new IllegalArgumentException("Product with id " + detailDTO.getProductId() + " is not available");
            }
            if (product.getStock() != null && product.getStock() < detailDTO.getQty()) {
                throw new IllegalArgumentException("Insufficient stock for product id: " + detailDTO.getProductId());
            }
            BigDecimal subTotal = product.getPrice().multiply(new BigDecimal(detailDTO.getQty()));
            detailDTO.setUnitPrice(product.getPrice());
            detailDTO.setSubTotal(subTotal);
            calculatedTotal = calculatedTotal.add(subTotal);
            // Update stock
            product.setStock(product.getStock() != null ? product.getStock() - detailDTO.getQty() : null);
            productRepository.save(product);
        }

        // Set defaults
        if (orderDTO.getOrderDateTime() == null) {
            orderDTO.setOrderDateTime(LocalDateTime.now());
        }
        if (orderDTO.getTotalAmount() == null) {
            orderDTO.setTotalAmount(calculatedTotal);
        } else if (orderDTO.getTotalAmount().compareTo(calculatedTotal) != 0) {
            throw new IllegalArgumentException("Provided total amount " + orderDTO.getTotalAmount() + 
                                              " does not match calculated total " + calculatedTotal);
        }
        // Explicitly set default status to true if not provided
        Boolean status = orderDTO.isStatus();
        orderDTO.setStatus(status != null ? status : DEFAULT_STATUS);

        // Create order
        Order order = orderMapper.toEntity(orderDTO);
        order.setCreatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        logger.debug("Saved order with id: {}", savedOrder.getId());

        // Create order details
        List<OrderDetail> orderDetails = orderDTO.getOrderDetails().stream()
                .map(dto -> {
                    OrderDetail detail = orderDetailMapper.toEntity(dto);
                    detail.setOrder(savedOrder);
                    logger.debug("Mapped order detail with product_id: {}", detail.getProduct() != null ? detail.getProduct().getId() : null);
                    return detail;
                })
                .collect(Collectors.toList());
        orderDetailRepository.saveAll(orderDetails);
        logger.debug("Saved order details: {}", orderDetails);

        // Fetch saved order with details
        OrderDTO result = orderMapper.toDTO(savedOrder);
        List<OrderDetail> savedDetails = orderDetailRepository.findByOrderId(savedOrder.getId());
        result.setOrderDetails(savedDetails != null && !savedDetails.isEmpty()
                ? savedDetails.stream().map(orderDetailMapper::toDTO).collect(Collectors.toList())
                : Collections.emptyList());

        logger.info("Order created with id: {}, total amount: {}, status: {}", 
                    savedOrder.getId(), result.getTotalAmount(), result.isStatus());
        return result;
    }

    @Transactional
    public OrderDTO updateOrder(Long id, OrderDTO orderDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        order.setOrderDateTime(orderDTO.getOrderDateTime() != null ? orderDTO.getOrderDateTime() : LocalDateTime.now());
        order.setTotalAmount(orderDTO.getTotalAmount());
        Boolean status = orderDTO.isStatus();
        order.setStatus(status != null ? status : DEFAULT_STATUS);
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toDTO(updatedOrder);
    }

    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
        logger.info("Order deleted with id: {}", id);
    }
}