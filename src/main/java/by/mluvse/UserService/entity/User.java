package by.mluvse.UserService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String email;
    private boolean active;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentCard> cards= new ArrayList<>();

    public void addCard(PaymentCard card){
        if(!cards.contains(card)) {
            cards.add(card);
            card.setUser(this);
        }
    }

    public void removeCard(PaymentCard card){
        cards.remove(card);
        card.setUser(null);
    }
}
