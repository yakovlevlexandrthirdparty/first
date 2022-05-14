package ru.skb.rentguy.first.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@Table(name = "order_dates")
@ToString(exclude = {"id", "price"})
public class OrderDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name="recipient_id")
    private Long recipientId;

    @Column(name="date")
    private Date date;

    @Column(name="state")
    private int state;

    @Column(name = "advert_id")
    private Long advertId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private Order order;
}
