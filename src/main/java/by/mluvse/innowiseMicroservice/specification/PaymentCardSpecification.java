package by.mluvse.innowiseMicroservice.specification;

import by.mluvse.innowiseMicroservice.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {

    public static Specification<PaymentCard> containsName(String name){
        return (root, query, criteriaBuilder) -> name==null|name.isBlank() ?
                criteriaBuilder.conjunction() :
                criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("name")), "%"+ name.toLowerCase()+"%");

    }

    public static Specification<PaymentCard> containsSurname(String surname){
        return (root, query, criteriaBuilder) -> surname==null||surname.isBlank() ?
                criteriaBuilder.conjunction() :
                criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("surname")), "%"+ surname.toLowerCase()+"%");
    }
}
