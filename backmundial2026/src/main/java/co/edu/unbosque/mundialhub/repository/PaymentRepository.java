package co.edu.unbosque.mundialhub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.mundialhub.model.Payments;
import co.edu.unbosque.mundialhub.model.User;


public interface PaymentRepository extends JpaRepository<Payments, Long> {

	Optional<Payments> findByMpPreferenceId(String mpPreferenceId);

	Optional<Payments> findByMpPaymentId(Long mpPaymentId);

	boolean existsByUserId(User user);
}
