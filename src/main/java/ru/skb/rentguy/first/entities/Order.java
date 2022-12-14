package ru.skb.rentguy.first.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name="recipient_id")
    private Long recipientId;

    @Column(name="advert_id")
    private Long advertId;

    @Column(name="state")
    private int state;

    @OneToMany(
            fetch = FetchType.EAGER,
            mappedBy = "order")
    private Collection<OrderDate> orderDates;
}
