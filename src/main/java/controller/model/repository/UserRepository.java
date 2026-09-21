package controller.model.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByRoleIgnoreCaseOrderByLastNameAscFirstNameAsc(String role);

    @Query("""
            select u from User u
            where lower(u.role) = lower(:role)
              and (
                    lower(coalesce(u.firstName, '')) like lower(concat('%', :search, '%'))
                 or lower(coalesce(u.lastName, '')) like lower(concat('%', :search, '%'))
                 or lower(coalesce(u.username, '')) like lower(concat('%', :search, '%'))
                 or lower(coalesce(u.bloodType, '')) like lower(concat('%', :search, '%'))
                 or lower(coalesce(u.phoneNumber, '')) like lower(concat('%', :search, '%'))
              )
            order by u.lastName asc, u.firstName asc
            """)
    List<User> searchByRole(@Param("role") String role, @Param("search") String search);

}
