package by.mluvse.innowiseMicroservice.specification;

import by.mluvse.innowiseMicroservice.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> nameContains(String name){
        //TODO
        return null;
    }

    public static Specification<User> surnameContains(String surname){
        //TODO
        return null;
    }
}
