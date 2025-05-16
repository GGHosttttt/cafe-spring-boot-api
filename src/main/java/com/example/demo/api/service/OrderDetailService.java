package com.example.demo.api.service;
import com.example.demo.api.exception.EntityNotFoundException;

import com.example.demo.api.model.OrderDetail;
import com.example.demo.api.mapper.OrderDetailMapper;
import com.example.demo.api.dto.OrderDetailDTO;

import com.example.demo.api.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderDetailService {
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    public List<OrderDetailDTO> getAllOrderDetails() {
        return orderDetailRepository.findAll().stream()
                .map(orderDetailMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<OrderDetailDTO> getOrderDetailById(Long id) {
        return orderDetailRepository.findById(id)
                .map(orderDetailMapper::toDTO);
    }

    public OrderDetailDTO createOrderDetail(OrderDetailDTO orderDetailDTO) {
        OrderDetail orderDetail = orderDetailMapper.toEntity(orderDetailDTO);
        OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);
        return orderDetailMapper.toDTO(savedOrderDetail);
    }

    public OrderDetailDTO updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) {
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OrderDetail not found with id: " + id));
        orderDetail.setOrder(orderDetailMapper.toEntity(orderDetailDTO).getOrder());
        orderDetail.setProduct(orderDetailMapper.toEntity(orderDetailDTO).getProduct());
        orderDetail.setUnitPrice(orderDetailDTO.getUnitPrice());
        orderDetail.setSubTotal(orderDetailDTO.getSubTotal());
        OrderDetail updatedOrderDetail = orderDetailRepository.save(orderDetail);
        return orderDetailMapper.toDTO(updatedOrderDetail);
    }

    public void deleteOrderDetail(Long id) {
        if (!orderDetailRepository.existsById(id)) {
            throw new EntityNotFoundException("OrderDetail not found with id: " + id);
        }
        orderDetailRepository.deleteById(id);
    }
}