package com.ll.mbooks.domain.order.entity;


import com.ll.mbooks.base.entity.BaseEntity;
import com.ll.mbooks.domain.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;


@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@Table(name = "product_order")
public class Order extends BaseEntity {
    private LocalDateTime refundDate;
    private LocalDateTime payDate;
    private LocalDateTime cancelDate;

    @ManyToOne(fetch = LAZY)
    private Member buyer;

    private String name;

    private boolean isPaid; // 결제여부
    private boolean isCanceled; // 취소여부
    private boolean isRefunded; // 환불여부

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem orderItem) {
        orderItem.setOrder(this);

        orderItems.add(orderItem);
    }

    public int calculatePayPrice() {
        return orderItems
                .stream()
                .mapToInt(orderItem -> orderItem.getSalePrice())
                .sum();
    }

    public void setCancelDone() {
        cancelDate = LocalDateTime.now();

        isCanceled = true;
    }

    public void setPaymentDone() {
        payDate = LocalDateTime.now();

        for (OrderItem orderItem : orderItems) {
            orderItem.setPaymentDone();
        }

        isPaid = true;
    }

    public void setRefundDone() {
        refundDate = LocalDateTime.now();

        for (OrderItem orderItem : orderItems) {
            orderItem.setRefundDone();
        }

        isRefunded = true;
    }

    public int getPayPrice() {
        return orderItems
                .stream()
                .mapToInt(orderItem -> orderItem.getPayPrice())
                .sum();
    }

    public void makeName() {
        String name = orderItems.get(0).getProduct().getSubject();

        if (orderItems.size() > 1) {
            name += " 외 %d권".formatted(orderItems.size() - 1);
        }

        this.name = name;
    }

    public boolean isPayable() {
        if (isPaid) return false;
        if (isCanceled) return false;

        return true;
    }
}
