package by.mluvse.UserService.specification;

import by.mluvse.UserService.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    private UserSpecification(){

    }

    public static Specification<User> nameContains(String name){
        return (root, query, criteriaBuilder) -> name==null|| name.isBlank() ?
                criteriaBuilder.conjunction():
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),"%"+name.toLowerCase()+"%");
    }

    public static Specification<User> surnameContains(String surname){
        return (root, query, cb) -> surname==null|| surname.isBlank() ?
        cb.conjunction() :
        cb.like(cb.lower(root.get("name")),"%"+surname.toLowerCase()+"%")   ;
    }
}
